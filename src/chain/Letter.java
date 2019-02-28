package chain;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Letter {
    private boolean empty;
    private int position;
    private int chainId;

    public Letter(int position, int chainId) {
        this.position = position;
        this.chainId = chainId;
        empty = false;
    }

    public Letter(String string) {
        if (string.length() > 1) {
            String[] strings = string.split("|");
            position = Integer.parseInt(strings[0]);
            chainId = Integer.parseInt(strings[1]);
            empty = false;
        } else {
            empty = true;
        }
    }

    public Letter() {
        empty = true;
    }

    public boolean isEmpty() {
        return false;
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
        letterList.sort(Letter::compareTo);
        return letterList;
    }

    @Override
    public String toString() {
        if (!empty) {
            return position + "|" + chainId;
        } else {
            return "|";
        }
    }

    public int compareTo(Letter anotherLetter) {
        if (this.position == anotherLetter.position) {
            return 0;
        } else if (this.position < anotherLetter.position) {
            return -1;
        } else {
            return 1;
        }
    }

}
