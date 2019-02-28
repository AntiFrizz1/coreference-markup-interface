package server;

import chain.Action;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class JudgeStore {
    class Game {
        int teamOneId;
        int teamTwoId;

        List<Action> teamOneApproved;
        List<Action> teamTwoApproved;
        List<Integer> decisions;

        PrintWriter writer;

        Game(int teamOneId, int teamTwoId) {
            this.teamOneId = teamOneId;
            this.teamTwoId = teamTwoId;

            teamOneApproved = new ArrayList<>(0);
            teamTwoApproved = new ArrayList<>(0);
            decisions = new ArrayList<>(0);

           /* try {
                writer = new PrintWriter("data/" + teamOneId + "vs" + teamTwoId);
            } catch (FileNotFoundException e) {
                System.err.println("Can't find file: " + "data/" + teamOneId + "vs" + teamTwoId);
            }*/
        }
    }

    List<Game> games;
    AtomicIntegerArray atomicIntegerArray;

    JudgeStore() {
        games = new ArrayList<>(0);
        atomicIntegerArray = new AtomicIntegerArray(100);
    }

    public boolean putActions(List<Action> teamOne, List<Action> teamTwo, int textNum, List<Integer> decisions) {
        if (atomicIntegerArray.compareAndSet(textNum, 0, 1)) {
            games.get(textNum).teamOneApproved.addAll(teamOne);
            games.get(textNum).teamTwoApproved.addAll(teamTwo);
            games.get(textNum).decisions.addAll(decisions);

            PrintWriter writer = games.get(textNum).writer;
            for (int i = 0; i < teamOne.size(); i++) {
                writer.println(teamOne.get(i).pack() + "$" + teamTwo.get(i).pack() + "$" + decisions.get(i));
                writer.flush();
            }

            atomicIntegerArray.compareAndSet(textNum, 1, 0);
            return true;
        } else {
            return false;
        }
    }

    public boolean putOneAction(Action teamOne, Action teamTwo, int textNum, int decision) {
        if (atomicIntegerArray.compareAndSet(textNum, 0, 1)) {
            games.get(textNum).teamOneApproved.add(teamOne);
            games.get(textNum).teamTwoApproved.add(teamTwo);
            games.get(textNum).writer.println(teamOne);
            games.get(textNum).decisions.add(decision);

            PrintWriter writer = games.get(textNum).writer;
            writer.println(teamOne.pack() + "$" + teamTwo.pack() + "$" + decision);
            writer.flush();

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

    public List<Integer> getDecisionList(int textNum) {
        return games.get(textNum).decisions;
    }

    public void addNewGame(int teamOneId, int teamTwoId) {
        Game tmp = new Game(teamOneId, teamTwoId);
        games.add(tmp);
    }
}
