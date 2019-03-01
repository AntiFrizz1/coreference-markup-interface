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
        int textNum;

        List<Action> teamOneApproved;
        List<Action> teamTwoApproved;
        List<Integer> decisions;

        PrintWriter writer;

        Game(int teamOneId, int teamTwoId, int textNum) {
            this.teamOneId = teamOneId;
            this.teamTwoId = teamTwoId;

            teamOneApproved = new ArrayList<>(0);
            teamTwoApproved = new ArrayList<>(0);
            decisions = new ArrayList<>(0);

            this.textNum = textNum;

            try {
                writer = new PrintWriter(teamOneId + "vs" + teamTwoId + "text=" + textNum);
            } catch (FileNotFoundException e) {
                System.err.println("Can't find file: " + teamOneId + "vs" + teamTwoId + "text=" + textNum);
            }
        }
    }

    List<Game> games;
    AtomicIntegerArray atomicIntegerArray;

    JudgeStore() {
        games = new ArrayList<>(0);
        atomicIntegerArray = new AtomicIntegerArray(100);
    }

    public void putOneAction(Action teamOne, Action teamTwo, int textNum, int decision) {
        System.out.println(textNum + " " + decision);
        games.get(textNum).teamOneApproved.add(teamOne);
        games.get(textNum).teamTwoApproved.add(teamTwo);
        games.get(textNum).writer.println(teamOne);
        games.get(textNum).decisions.add(decision);

        PrintWriter writer = games.get(textNum).writer;
        writer.println(teamOne.pack() + "$" + teamTwo.pack() + "$" + decision);
        writer.flush();
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
        Game tmp = new Game(teamOneId, teamTwoId, games.size());
        games.add(tmp);
    }
}
