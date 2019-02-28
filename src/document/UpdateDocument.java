package document;

import chain.Action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class UpdateDocument implements Document {

    List<Action> actions;

    public UpdateDocument(List<Action> actions) {
        this.actions = actions;
    }

    public UpdateDocument(Action... actions) {
        this.actions = Arrays.asList(actions);
    }

    public UpdateDocument(String info) {
        actions = new ArrayList<>();
        List<String> list = Arrays.asList(info.split("\n"));
        for (int i = 0; i < list.size(); i += 2) {
            actions.add(new Action(list.get(i).concat("\n").concat(list.get(i + 1))));
        }
    }

    public List<Action> getActions() {
        return actions;
    }

    @Override
    public String pack() {
        return actions.stream().map(Action::pack).collect(Collectors.joining("\n"));
    }

    @Override
    public String packSB(StringBuilder sb) {
        return null;
    }
}
