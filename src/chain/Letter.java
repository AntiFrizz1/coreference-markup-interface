package chain;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Letter {
    private int position;
    private int chainId;

    public Letter(int position, int chainId) {
        this.position = position;
        this.chainId = chainId;
    }

    public Letter(String string) {
        String[] strings = string.split("|");
        position = Integer.parseInt(strings[0]);
        chainId = Integer.parseInt(strings[1]);

    }

    public int getChainId() {
        return chainId;
    }

    public int getPosition() {
        return position;
    }

    public static List<Letter> listFromActionList(List<Action> actions) {
        List<Letter> letterList = new ArrayList<>();
        for (Action action: actions) {
            Location location = action.getLocation();
            if (location instanceof Phrase) {
                Set<Integer> positions = ((Phrase) location).getPositions();
                int chainId = action.getChainId();
                for (int position: positions) {
                    letterList.add(new Letter(position, chainId));
                }
            }
        }
        return letterList;
    }

    @Override
    public String toString() {
        return position + "|" + chainId;
    }
}
