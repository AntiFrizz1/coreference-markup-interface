package chain;

import java.util.HashSet;
import java.util.Set;

/**
 * This class describes zero anaphora.
 *
 * @author Vadim Baydyuk
 */
public class Blank implements Location {

    /**
     * Position of zero anaphora in the text.
     */
    private int position;

    public Blank(int position) {
        this.position = position;
    }

    public Blank(String info) {
        position = Integer.valueOf(info.substring(7));
    }


    /**
     * Check if anaphora which described by {@code obj} and anaphora
     * which described by {@code this} class have the same position in
     * the text.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if both anaphora have the same positions
     * in th text, {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Blank)) {
            return false;
        } else {
            return position == ((Blank) obj).position;
        }
    }

    public int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "@";
    }

    @Override
    public Set<String> getWords() {
        return new HashSet<>();
    }

    @Override
    public String pack() {
        StringBuilder sb = new StringBuilder();
        sb.append("Blank: ").append(position);
        return sb.toString();
    }
}
