package chain;

import document.Packable;

import java.util.Arrays;
import java.util.List;

public class Action implements Packable {

    private String name;

    private int action;

    private int chainId;

    private Location location;

    private boolean empty = false;

    public Action(int ac, int id, Location loc, String name) {
        this.name = name;
        action = ac;
        chainId = id;
        location = loc;
    }

    public Action(String info) {
        List<String> list = Arrays.asList(info.split("\t"));
        action = Integer.valueOf(list.get(0).split(" ")[0]);
        chainId = Integer.valueOf(list.get(0).split(" ")[1]);
        name = list.get(0).split(" ", 3)[2];
        if (list.get(1).contains("Blank")) {
            location = new Blank(list.get(1));
        } else {
            location = new Phrase(list.get(1));
        }
    }

    public Action() {
        empty = true;
    }

    public int getAction() {
        return action;
    }

    public String getName() {
        return name;
    }

    public int getChainId() {
        return chainId;
    }

    public Location getLocation() {
        return location;
    }

    public boolean isEmpty() {
        return action == -1;
    }

    @Override
    public String pack() {
        StringBuilder sb = new StringBuilder();
        sb.append(action).append(' ').append(chainId).append(' ').append(name).append('\t');
        sb.append(location.pack());
        return sb.toString();
    }

    @Override
    public String packSB(StringBuilder sb) {
        return null;
    }
}