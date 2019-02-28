package server;

import chain.Action;

import java.util.List;

public class AddTask {
    private List<Action> actionList;
    private int textNum;
    private int teamNum;

    public AddTask(List<Action> actionList, int textNum, int teamNum) {
        this.actionList = actionList;
        this.textNum = textNum;
        this.teamNum = teamNum;
    }

    public int getTeamNum() {
        return teamNum;
    }

    public int getTextNum() {
        return textNum;
    }

    public List<Action> getActionList() {
        return actionList;
    }
}
