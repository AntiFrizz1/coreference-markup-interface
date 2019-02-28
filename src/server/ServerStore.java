package server;

import chain.Action;
import document.ConflictData;
import document.ConflictInfo;

import java.util.*;
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

            teamOneList = new ArrayList<>(0);
            teamTwoList = new ArrayList<>(0);
        }
    }

    List<Game> games;
    AtomicIntegerArray mutexArray;

    ServerStore() {
        games = new ArrayList<>(0);
        mutexArray = new AtomicIntegerArray(100);
    }

    boolean putActions(List<Action> actions, int textNum, int teamNum) {
        if (mutexArray.compareAndSet(textNum, 0, 1)) {
            Game curGame = games.get(textNum);
            if (teamNum == 1) {
                curGame.teamOneList.addAll(actions);
            } else {
                curGame.teamTwoList.addAll(actions);
            }
            return true;
        } else {
            return false;
        }
    }

    int length = 50;

    Runnable worker = () -> {
        while (true) {
            for (int i = 0; i < games.size(); i++) {
                if (mutexArray.compareAndSet(i, 0, 1)) {
                    Game curGame = games.get(i);
                    if (!curGame.teamOneList.isEmpty() && !curGame.teamTwoList.isEmpty()) {
                        Action actionFromTeamOne = curGame.teamOneList.get(0);
                        Action actionFromTeamTwo = curGame.teamTwoList.get(0);
                        //compare actions
                    }
                    mutexArray.compareAndSet(i, 1, 0);
                }
            }
        }
    };

    synchronized void addNewGame(int teamOne, int teamTwo) {
        Game newGame = new Game(teamOne, teamTwo);
        games.add(newGame);
    }
}
