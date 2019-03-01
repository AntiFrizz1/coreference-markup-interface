package document;

import chain.Action;

import java.util.List;

public class Data {
    String text;
    List<Action> actions;
    public Data(String text, List<Action> actions) {
        this.text = text;
        this.actions = actions;
    }
    public Data(String info) {
        String[] array = info.split("@");
        text = array[0];
        if(array[1].isEmpty()) {
            actions = null;
        } else {
            UpdateDocument upd = new UpdateDocument(array[1]);
            actions = upd.getActions();
        }
    }

    public List<Action> getActions() {
        return actions;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        UpdateDocument upd = new UpdateDocument(actions);
        return text + "@" + upd.pack();
    }
}
