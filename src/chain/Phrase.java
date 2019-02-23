package chain;

import java.util.Set;

/**
 * This class describes phrase of text.
 *
 * @author Vadim Baydyuk
 */
public class Phrase extends AbstractLocation {

    /**
     * Positions of each word of phrase in the sentence.
     */
    private Set<Integer> positions;

    Phrase(Set<Integer> positions) {
        this.positions = positions;
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
}
