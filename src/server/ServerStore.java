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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicIntegerArray;

import static server.ServerImpl.conflicts;

public class ServerStore {
    public static class Game {
        int teamOne;
        int teamTwo;
        int textNum;
        List<Action> teamOneList;
        List<Action> teamTwoList;

        PrintWriter writerOne;
        PrintWriter writerTwo;

        Game(int teamOne, int teamTwo, int textNum, String prefixOld, PrintWriter writer) {
            this.teamOne = teamOne;
            this.teamTwo = teamTwo;
            this.textNum = textNum;
            writer.println(teamOne + "text=" + textNum);
            writer.flush();
            writer.println(teamTwo + "text=" + textNum);
            writer.flush();

            try {
                writerOne = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(prefixOld + ServerImpl.DELIMETER + teamOne + "text=" + textNum), StandardCharsets.UTF_8)));
            } catch (FileNotFoundException e) {
                System.err.println("Can't find file : " + teamOne + "text=" + textNum);
            }
            try {
                writerTwo = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(prefixOld + ServerImpl.DELIMETER + teamTwo + "text=" + textNum), StandardCharsets.UTF_8)));
            } catch (FileNotFoundException e) {
                System.err.println("Can't find file : " + teamTwo + "text=" + textNum);
            }
            teamOneList = new CopyOnWriteArrayList<>();
            teamTwoList = new CopyOnWriteArrayList<>();
        }

        public Game(int teamOne, int teamTwo, int textNum, List<Action> teamOneList, List<Action> teamTwoList, PrintWriter writerOne, PrintWriter writerTwo) {
            this.teamOne = teamOne;
            this.teamTwo = teamTwo;
            this.textNum = textNum;

            this.teamOneList = new CopyOnWriteArrayList<>(teamOneList);
            this.teamTwoList = new CopyOnWriteArrayList<>(teamTwoList);
            this.writerOne = writerOne;
            this.writerTwo = writerTwo;
        }
    }

    List<Game> games;
    AtomicIntegerArray mutexArray;
    PrintWriter writer;

    ServerStore() {
        games = new CopyOnWriteArrayList<>();
        mutexArray = new AtomicIntegerArray(100);
    }

    public void setServerWriter(String prefix) {
        try {
            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(prefix + ServerImpl.DELIMETER + "gamesServer"), StandardCharsets.UTF_8)));
        } catch (FileNotFoundException e) {
            System.err.println("Can't find file " + prefix + ServerImpl.DELIMETER + "gamesServer");
        }
    }

    boolean putActions(List<Action> actions, int textNum, int teamNum) {
        Game curGame = games.get(textNum);
        actions.sort(this::compareActions);
        if (teamNum == 1) {
            curGame.teamOneList.addAll(actions);
            PrintWriter writer = curGame.writerOne;
            for (Action action : actions) {
                writer.println(action.pack());
                writer.flush();
            }
        } else {
            curGame.teamTwoList.addAll(actions);
            PrintWriter writer = curGame.writerTwo;
            for (Action action : actions) {
                writer.println(action.pack());
                writer.flush();
            }
        }
        mutexArray.compareAndSet(textNum, 1, 0);
        return true;
    }

    Runnable worker = () -> {
        while (true) {
            for (int i = 0; i < games.size(); i++) {
                    Game curGame = games.get(i);
                    if (!curGame.teamOneList.isEmpty() && !curGame.teamTwoList.isEmpty()) {
                        Action actionFromTeamOne = curGame.teamOneList.get(0);
                        Action actionFromTeamTwo = curGame.teamTwoList.get(0);
                        if (compare(actionFromTeamOne.getLocation(), actionFromTeamTwo.getLocation()) < 0) {
                            conflicts.get(i).add(new ConflictInfo(new ConflictData(actionFromTeamOne, new Action(-1, -1, new Blank(1), "qq"), i, curGame.teamOne, curGame.teamTwo)));
                            curGame.teamOneList.remove(0);
                        } else  if (compare(actionFromTeamOne.getLocation(), actionFromTeamTwo.getLocation()) > 0) {
                            conflicts.get(i).add(new ConflictInfo(new ConflictData(new Action(-1, -1, new Blank(1), "qq"), actionFromTeamTwo, i, curGame.teamOne, curGame.teamTwo)));
                            curGame.teamTwoList.remove(0);
                        } else {
                            conflicts.get(i).add(new ConflictInfo(new ConflictData(actionFromTeamOne, actionFromTeamTwo, i, curGame.teamOne, curGame.teamTwo)));
                            curGame.teamOneList.remove(0);
                            curGame.teamTwoList.remove(0);
                        }
                    }
                }
            }
    };

    synchronized void addNewGame(int teamOne, int teamTwo, int textNum, List<Action> teamOneList, List<Action> teamTwoList, PrintWriter writerOne, PrintWriter writerTwo) {
        Game newGame = new Game(teamOne, teamTwo, textNum, teamOneList, teamTwoList, writerOne, writerTwo);
        games.add(newGame);
    }

    synchronized void addNewGame(int teamOne, int teamTwo, int textNum, String prefixOld) {
        Game newGame = new Game(teamOne, teamTwo, textNum, prefixOld, writer);
        games.add(newGame);
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
                int result;
                for (int i = 0; i < ar1.length; i++) {
                    for (int j = 0; j < ar2.length; j++) {
                        if (ar1[i] == ar2[j]) {
                            return 0;
                        }
                    }
                }
                int res1 = phrase1.getPositions().stream().min(Integer::compareTo).get();
                int res2 = phrase2.getPositions().stream().min(Integer::compareTo).get();
                if (res1 > res2) {
                    return 1;
                } else {
                    return -1;
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