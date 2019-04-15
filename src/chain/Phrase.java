package chain;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

    public Phrase(String stringRepresentation, int first, Integer... positions) {
        this.positions = new HashSet<>();
        this.positions.add(first);
        this.positions.addAll(Arrays.asList(positions));
        this.stringRepresentation = stringRepresentation;
    }

    public Phrase(String stringRepresentation, Set<Integer> positions) {
        this.positions = positions;
        this.stringRepresentation = stringRepresentation;
    }

    public Phrase(String info) {
        String suff = info.substring(8);
        stringRepresentation = suff.split(" :: ")[0];
        positions = Arrays.stream(suff.split(" :: ")[1].split(" ")).
                map(Integer::valueOf).collect(Collectors.toSet());
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
    public String pack() {
        StringBuilder sb = new StringBuilder();
        sb.append("Phrase: ").append(stringRepresentation).append(" :: ");
        sb.append(positions.stream().map(Objects::toString).collect(Collectors.joining(" ")));
        return sb.toString();
    }

    @Override
    public String packSB(StringBuilder sb) {
        return null;
    }
}