package document;

import chain.Action;

import java.util.concurrent.atomic.AtomicInteger;

public class ConflictData {
    public int textId;
    public int teamOneId;
    public int teamTwoId;

    public Action action1;
    public Action action2;

    public ConflictData(Action action1, Action action2, int textId, int teamOneId, int teamTwoId) {
        this.action1 = action1;
        this.action2 = action2;
        this.teamOneId = teamOneId;
        this.teamTwoId = teamTwoId;
        this.textId = textId;
    }

    public ConflictData(String string) {
        String[] strings = string.split("|");
        this.action1 = new Action(strings[0]);
        this.action2 = new Action(strings[1]);
        this.textId = Integer.parseInt(strings[2]);
        this.teamOneId = Integer.parseInt(strings[3]);
        this.teamTwoId = Integer.parseInt(strings[4]);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(action1.pack()).append("|").append(action2.pack()).append("|").append(textId).append("|")
                .append(teamOneId).append("|").append(teamTwoId);
        return sb.toString();
    }

}
