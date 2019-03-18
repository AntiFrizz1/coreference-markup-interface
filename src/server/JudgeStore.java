package server;

import chain.Action;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

import static server.ServerImpl.log;

public class JudgeStore {
    class Game {
        ReentrantLock mutex = new ReentrantLock();
        List<Integer> teamIdList = new CopyOnWriteArrayList<>();

        int textNum;

        Map<Integer, List<Action>> idToTeamApprovedList = new ConcurrentHashMap<>();

        List<Integer> decisions;

        String prefix;

        PrintWriter writer;

        Game(int textNum, String prefix) {
            decisions = new CopyOnWriteArrayList<>();
            this.textNum = textNum;
            this.prefix = prefix;
        }

        Game(int teamOneId, int teamTwoId, int textNum, List<Action> teamOneApproved, List<Action> teamTwoApproved, List<Integer> decisions, PrintWriter writer, String prefix) {
            teamIdList.add(teamOneId);
            teamIdList.add(teamTwoId);
            this.textNum = textNum;
            idToTeamApprovedList.put(teamOneId, teamOneApproved);
            idToTeamApprovedList.put(teamTwoId, teamTwoApproved);
            this.decisions = decisions;
            this.writer = writer;
            this.prefix = prefix;
        }

        void addTeam(int teamId) {
            teamIdList.add(teamId);
            idToTeamApprovedList.put(teamId, new CopyOnWriteArrayList<>());
        }

        void makeWriter() {
            try {
                writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(prefix + ServerImpl.DELIMITER +
                        teamIdList.get(0) + "vs" + teamIdList.get(1) + "text=" + textNum),
                        StandardCharsets.UTF_8)));
                dumpWriter.println(teamIdList.get(0) + "vs" + teamIdList.get(1) + "text=" + textNum);
                dumpWriter.flush();
            } catch (FileNotFoundException e) {
                log("JudgeStore.Game.makeWriter", e.getMessage());
            }
        }
    }

    List<Game> games;
    PrintWriter dumpWriter;
    Map<Integer, Game> gamesMap;

    JudgeStore() {
        games = new CopyOnWriteArrayList<>();
        gamesMap = new ConcurrentHashMap<>();
    }

    public void setJudgeWriter(String prefix) {
        try {
            dumpWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    prefix + ServerImpl.DELIMITER + "judgeStoreGames"), StandardCharsets.UTF_8)));
        } catch (FileNotFoundException e) {
            log("JudgeStore.setJudgeWriter", e.getMessage());
        }
    }

    public void putOneAction(int teamOneId, Action teamOneAction, int teamTwoId, Action teamTwoAction, int textNum, int decision) {
        games.get(textNum).idToTeamApprovedList.get(teamOneId).add(teamOneAction);
        games.get(textNum).idToTeamApprovedList.get(teamTwoId).add(teamTwoAction);
        games.get(textNum).decisions.add(decision);

        PrintWriter writer = games.get(textNum).writer;
        writer.println(teamOneAction.pack() + "@" + teamTwoAction.pack() + "@" + decision);
        writer.flush();
    }

    public List<Action> getTeamList(int textNum, int teamId) {
        return games.get(textNum).idToTeamApprovedList.get(teamId);
    }

    public List<Integer> getDecisionList(int textNum) {
        return games.get(textNum).decisions;
    }

    public void addNewTeam(int teamId, int textNum, String prefix) {
        if (!gamesMap.containsKey(textNum)) {
            Game tmp = new Game(textNum, prefix);
            tmp.mutex.lock();
            games.add(tmp);
            gamesMap.put(textNum, tmp);
            tmp.addTeam(teamId);
            tmp.mutex.unlock();
        } else {
            games.get(textNum).mutex.lock();
            games.get(textNum).addTeam(teamId);
            games.get(textNum).mutex.unlock();
            games.get(textNum).makeWriter();
        }
    }

    public void addNewRecoverGame(int teamOneId, int teamTwoId, int textNum, List<Action> teamOneApproved, List<Action> teamTwoApproved, List<Integer> decisions, PrintWriter writer, String prefix) {
        Game tmp = new Game(teamOneId, teamTwoId, textNum, teamOneApproved, teamTwoApproved, decisions, writer, prefix);
        games.add(tmp);
    }
}