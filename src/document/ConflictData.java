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

    }

    /*@Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(action1.pack()).append("|")
        return action1.pack() + "|"
    }*/

}
