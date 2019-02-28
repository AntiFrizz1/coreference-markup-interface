package server;

import chain.Action;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class JudgeStore {
    class Game {
        List<Action> teamOneApproved;
        List<Action> teamTwoApproved;

        Game() {
            teamOneApproved = new ArrayList<>(0);
            teamTwoApproved = new ArrayList<>(0);
        }
    }

    List<Game> games;
    AtomicIntegerArray atomicIntegerArray;

    JudgeStore() {
        games = new ArrayList<>(0);
        atomicIntegerArray = new AtomicIntegerArray(100);
    }

    public boolean putActions(List<Action> teamOne, List<Action> teamTwo, int textNum) {
        if (atomicIntegerArray.compareAndSet(textNum, 0, 1)) {
            games.get(textNum).teamOneApproved.addAll(teamOne);
            games.get(textNum).teamTwoApproved.addAll(teamTwo);
            atomicIntegerArray.compareAndSet(textNum, 1, 0);
            return true;
        } else {
            return false;
        }
    }

    public boolean putOneAction(Action teamOne, Action teamTwo, int textNum) {
        // взять chainId из actiona
        // пройтись по листу найти несколько с таким же chainId
        // сделать новый лист
        if (atomicIntegerArray.compareAndSet(textNum, 0, 1)) {
            games.get(textNum).teamOneApproved.add(teamOne);
            games.get(textNum).teamTwoApproved.add(teamTwo);
            atomicIntegerArray.compareAndSet(textNum, 1, 0);
            return true;
        } else {
            return false;
        }
    }

    public List<Action> getTeamList(int textNum, int teamNum) {
        if (teamNum == 1) {
            return games.get(textNum).teamOneApproved;
        } else {
            return games.get(textNum).teamTwoApproved;
        }
    }

    public void addNewGame() {
        Game tmp = new Game();
        games.add(tmp);
    }
}
