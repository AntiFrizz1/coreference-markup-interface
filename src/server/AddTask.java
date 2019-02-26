package server;

import chain.Chain;

import java.util.List;

public class AddTask {
    int taskType;
    Chain chain;
    List<Chain> chainList;
    int textNum;
    int teamNum;

    public AddTask(Chain chain, int textNum, int teamNum) {
        taskType = 0;
        this.chain = chain;
        this.textNum = textNum;
        this.teamNum = teamNum;
    }

    public AddTask(List<Chain> chainList, int textNum, int teamNum) {
        taskType = 1;
        this.chainList = chainList;
        this.textNum = textNum;
        this.teamNum = teamNum;
    }
}
