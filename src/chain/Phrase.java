package chain;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * This class describes phrase of text.
 *
 * @author Vadim Baydyuk
 */
public class Phrase implements Location {

    /**
     * Positions of each word of phrase in the text.
     */
    private Set<Integer> positions;

    private String stringRepresentation;

    public Phrase(String stringRepresentation, Integer... positions) {
        this.positions = new HashSet<>(Arrays.asList(positions));
        this.stringRepresentation = stringRepresentation;
    }

    public Phrase(String stringRepresentation, Set<Integer> positions) {
        this.positions = positions;
        this.stringRepresentation = stringRepresentation;
    }

    /**
     * Check if phrase which described by {@code obj} and phrase
     * which described by this class have the same positions in
     * the text.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if both anaphora have the same positions
     * in th text, {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Phrase)) {
            return false;
        } else {
            return positions.equals(((Phrase) obj).positions);
        }
    }

    public Set<Integer> getPositions() {
        return positions;
    }

    @Override
    public String toString() {
        return stringRepresentation;
    }

    @Override
    public Set<String> getWords() {
        return new HashSet<>(Arrays.asList(stringRepresentation.split(" ")));
    }

    @Override
    public void pack(StringBuilder sb) {
        sb.append("Phrase: ");
        positions.forEach(position -> sb.append(position).append(' '));
        sb.append('\n');
    }
}