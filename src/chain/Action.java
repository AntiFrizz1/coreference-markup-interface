package chain;

public class Action {

    private int action;

    private int chainId;

    private Location location;

    public Action(int ac, int id, Location loc) {
        action = ac;
        chainId = id;
        location = loc;
    }

    public int getAction() {
        return action;
    }

    public int getChainId() {
        return chainId;
    }

    public Location getLocation() {
        return location;
    }

}
