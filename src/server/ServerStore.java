package server;

import chain.Action;
import chain.Blank;
import chain.Location;
import chain.Phrase;
import document.ConflictData;
import document.ConflictInfo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static server.ServerImpl.conflicts;
import static server.ServerImpl.log;

public class ServerStore {
    class Game {
        List<Integer> teamIdList = new CopyOnWriteArrayList<>();
        int textNum;
        Map<Integer, List<Action>> idToActionList = new ConcurrentHashMap<>();

        String prefix;

        Map<Integer, PrintWriter> idToWriter = new ConcurrentHashMap<>();

        Game(int textNum, String prefix) {
            this.textNum = textNum;
            this.prefix = prefix;
        }

        Game(int teamOneId, int teamTwoId, int textNum, List<Action> teamOneActions, List<Action> teamTwoActions, PrintWriter writer1, PrintWriter writer2, String prefix) {
            teamIdList.add(teamOneId);
            teamIdList.add(teamTwoId);
            this.textNum = textNum;
            idToActionList.put(teamOneId, teamOneActions);
            idToActionList.put(teamTwoId, teamTwoActions);
            idToWriter.put(teamOneId, writer1);
            idToWriter.put(teamTwoId, writer2);
            this.prefix = prefix;
        }

        Game(int teamId, int textNum, List<Action> teamActions, PrintWriter writer, String prefix) {
            teamIdList.add(teamId);
            this.textNum = textNum;
            idToActionList.put(teamId, teamActions);
            idToWriter.put(teamId, writer);
            this.prefix = prefix;
        }

        void addTeam(int teamId) {
            teamIdList.add(teamId);
            idToActionList.put(teamId, new CopyOnWriteArrayList<>());
            try {
                idToWriter.put(teamId, new PrintWriter(prefix + ServerImpl.DELIMITER + teamId + "text=" + textNum));
                writer.println(teamId + "text=" + textNum);
                writer.flush();
            } catch (FileNotFoundException e) {
                log("ServerStore.Game.addTeam", e.getMessage());
            }
        }
    }

    List<Game> games;
    HashMap<Integer, Game> gamesMap;
    PrintWriter writer;
    AtomicIntegerArray mutexes = new AtomicIntegerArray(1000);

    int mode;

    ServerStore(int mode) {
        games = new CopyOnWriteArrayList<>();
        gamesMap = new HashMap<>();
        this.mode = mode;
    }

    ServerStore() {
        games = new CopyOnWriteArrayList<>();
        gamesMap = new HashMap<>();
        this.mode = 0;
    }

    public void setServerWriter(String prefix) {
        try {
            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(prefix + ServerImpl.DELIMITER + "gamesServer"), StandardCharsets.UTF_8)));
        } catch (FileNotFoundException e) {
            log("ServerStore.setServerWriter", e.getMessage());
        }
    }

    boolean putActions(List<Action> actions, int textNum, int teamId) {
        synchronized (games) {
            Game curGame = games.get(textNum);
            curGame.idToActionList.get(teamId).addAll(actions.stream().sorted(this::compareActions).collect(Collectors.toList()));
            PrintWriter writer = curGame.idToWriter.get(teamId);
            for (Action action : actions) {
                writer.println(action.pack());
                writer.flush();
            }
        }
        return true;
    }

    Runnable worker = () -> {
        while (true) {
            for (int i = 0; i < games.size(); i++) {
                Game curGame = games.get(i);
                if (!curGame.teamIdList.isEmpty()) {
                    Action actionFromTeamOne = null;
                    if (curGame.idToActionList.get(curGame.teamIdList.get(0)) != null && !curGame.idToActionList.get(curGame.teamIdList.get(0)).isEmpty()) {
                        actionFromTeamOne = curGame.idToActionList.get(curGame.teamIdList.get(0)).get(0);
                    }
                    Action actionFromTeamTwo = null;
                    if (curGame.teamIdList.size() == 2 && curGame.idToActionList.size() == 2 && curGame.idToActionList.get(curGame.teamIdList.get(1)) != null && !curGame.idToActionList.get(curGame.teamIdList.get(1)).isEmpty()) {
                        actionFromTeamTwo = curGame.idToActionList.get(curGame.teamIdList.get(1)).get(0);
                    }

                    if ((actionFromTeamOne == null || actionFromTeamTwo == null) && mode == 0) {
                        continue;
                    }

                    if (actionFromTeamOne != null && (mode == 1 ||
                            (mode == 0 && compare(actionFromTeamOne.getLocation(), actionFromTeamTwo.getLocation()) < 0))) {
                        conflicts.get(i).add(new ConflictInfo(new ConflictData(actionFromTeamOne,
                                new Action(-1, -1, new Blank(1), "qq"), i, curGame.teamIdList.get(0),
                                curGame.teamIdList.get(1))));
                        curGame.idToActionList.get(curGame.teamIdList.get(0)).remove(0);
                    } else if (actionFromTeamTwo != null && (mode == 1 || mode == 0 &&
                            compare(actionFromTeamOne.getLocation(), actionFromTeamTwo.getLocation()) > 0)) {
                        conflicts.get(i).add(new ConflictInfo(new ConflictData(
                                new Action(-1, -1, new Blank(1), "qq"), actionFromTeamTwo, i,
                                curGame.teamIdList.get(0), curGame.teamIdList.get(1))));
                        curGame.idToActionList.get(curGame.teamIdList.get(1)).remove(0);
                    } else if (actionFromTeamOne != null && actionFromTeamTwo != null) {
                        conflicts.get(i).add(new ConflictInfo(new ConflictData(actionFromTeamOne, actionFromTeamTwo, i,
                                curGame.teamIdList.get(0), curGame.teamIdList.get(1))));
                        curGame.idToActionList.get(curGame.teamIdList.get(0)).remove(0);
                        curGame.idToActionList.get(curGame.teamIdList.get(1)).remove(0);
                    }
                }
            }
        }
    };

    public void addFullRecoverGame(int teamOneId, int teamTwoId, int textNum, List<Action> teamOneActions,
                                   List<Action> teamTwoActions, PrintWriter writer1, PrintWriter writer2, String prefix) {
        Game tmp = new Game(teamOneId, teamTwoId, textNum, teamOneActions, teamTwoActions, writer1, writer2, prefix);
        games.add(tmp);
    }

    public void addHalfRecoverGame(int teamId, int textNum, List<Action> teamActions, PrintWriter writer, String prefix) {
        Game tmp = new Game(teamId, textNum, teamActions, writer, prefix);
        games.add(tmp);
    }

    void addSample(int teamNumber, int textNum, String prefix) {
        synchronized (games) {
            if (!gamesMap.containsKey(textNum)) {
                Game newGame = new Game(textNum, prefix);
                games.add(newGame);
                gamesMap.put(textNum, newGame);
                newGame.addTeam(teamNumber);
                conflicts.add(new ConcurrentLinkedQueue<>());
            } else {
                gamesMap.get(textNum).addTeam(teamNumber);
            }
        }
    }

    public int compareActions(Action action1, Action action2) {
        return compare(action1.getLocation(), action2.getLocation());
    }

    public static int compare(Location o1, Location o2) {
        if (o1 instanceof Blank) {
            if (o2 instanceof Phrase) {
                Blank blank = (Blank) o1;
                Phrase phrase = (Phrase) o2;
                int ar = phrase.getPositions().stream().min(Comparator.naturalOrder()).get();
                if (ar > blank.getPosition()) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                int id1 = ((Blank) o1).getPosition();
                int id2 = ((Blank) o2).getPosition();
                if (id1 < id2) {
                    return -1;
                } else if (id1 == id2) {
                    return 0;
                } else {
                    return 1;
                }
            }
        } else {
            if (o2 instanceof Phrase) {
                Phrase phrase1 = (Phrase) o1;
                Phrase phrase2 = (Phrase) o2;
                int[] ar1 = phrase1.getPositions().stream().mapToInt(Integer::valueOf).sorted().toArray();
                int[] ar2 = phrase2.getPositions().stream().mapToInt(Integer::valueOf).sorted().toArray();
                /*int result;
                for (int i = 0; i < ar1.length; i++) {
                    for (int j = 0; j < ar2.length; j++) {
                        if (ar1[i] == ar2[j]) {
                            return 0;
                        }
                    }
                }*/
                int res1 = phrase1.getPositions().stream().min(Integer::compareTo).get();
                int res2 = phrase2.getPositions().stream().min(Integer::compareTo).get();

                int res11 = phrase1.getPositions().stream().max(Integer::compareTo).get();
                int res22 = phrase2.getPositions().stream().max(Integer::compareTo).get();
                if (res1 > res2) {
                    return 1;
                } else if (res1 < res2) {
                    return -1;
                } else {
                    if (res11 < res22) {
                        return -1;
                    } else if (res11 > res22) {
                        return 1;
                    } else {
                        if (phrase1.getPositions().size() < phrase2.getPositions().size()) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                }
            } else {
                Blank blank = (Blank) o2;
                Phrase phrase = (Phrase) o1;
                int ar = phrase.getPositions().stream().min(Comparator.naturalOrder()).get();
                if (ar > blank.getPosition()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        }
    }
}