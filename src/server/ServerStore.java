package server;

import chain.Action;
import chain.Blank;
import chain.Location;
import chain.Phrase;
import document.ConflictData;
import document.ConflictInfo;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicIntegerArray;

import static server.ServerImpl.conflicts;

public class ServerStore {
    class Game {
        int teamOne;
        int teamTwo;
        List<Action> teamOneList;
        List<Action> teamTwoList;

        Game(int teamOne, int teamTwo) {
            this.teamOne = teamOne;
            this.teamTwo = teamTwo;

            teamOneList = new CopyOnWriteArrayList<>();
            teamTwoList = new CopyOnWriteArrayList<>();
        }
    }

    List<Game> games;
    AtomicIntegerArray mutexArray;

    ServerStore() {
        games = new CopyOnWriteArrayList<>();
        mutexArray = new AtomicIntegerArray(100);
    }

    boolean putActions(List<Action> actions, int textNum, int teamNum) {
            Game curGame = games.get(textNum);
            if (teamNum == 1) {
                curGame.teamOneList.addAll(actions);
            } else {
                curGame.teamTwoList.addAll(actions);
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
                            conflicts.add(new ConflictInfo(new ConflictData(actionFromTeamOne, new Action(), i, curGame.teamOne, curGame.teamTwo)));
                            curGame.teamOneList.remove(0);
                        } else  if (compare(actionFromTeamOne.getLocation(), actionFromTeamTwo.getLocation()) > 0) {
                            conflicts.add(new ConflictInfo(new ConflictData(new Action(), actionFromTeamTwo, i, curGame.teamOne, curGame.teamTwo)));
                            curGame.teamTwoList.remove(0);
                        } else {
                            conflicts.add(new ConflictInfo(new ConflictData(actionFromTeamOne, actionFromTeamTwo, i, curGame.teamOne, curGame.teamTwo)));
                            curGame.teamOneList.remove(0);
                            curGame.teamTwoList.remove(0);
                        }
                    }
            }
        }
    };

    synchronized void addNewGame(int teamOne, int teamTwo) {
        Game newGame = new Game(teamOne, teamTwo);
        games.add(newGame);
    }

    public int compare(Location o1, Location o2) {
        if (o1 instanceof Blank) {
            if (o2 instanceof Phrase) {
                Blank blank = (Blank) o1;
                Phrase phrase = (Phrase) o2;
                int ar = phrase.getPositions().stream().min(Comparator.naturalOrder()).get();
                if (ar < blank.getPosition()) {
                    return 1;
                } else {
                    return -1;
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
                for (int i = 0; i < Math.min(ar1.length, ar2.length); i++) {
                    if (ar1[i] < ar2[i]) {
                        return -1;
                    } else if (ar1[i] > ar2[i]) {
                        return 1;
                    }
                }
                if (ar1.length == ar2.length) {
                    return 0;
                } else if (ar1.length < ar2.length) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                Blank blank = (Blank) o2;
                Phrase phrase = (Phrase) o1;
                int ar = phrase.getPositions().stream().min(Comparator.naturalOrder()).get();
                if (ar < blank.getPosition()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }
    }
}
