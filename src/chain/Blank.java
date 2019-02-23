package chain;

/**
 * This class describes zero anaphora.
 *
 * @author Vadim Baydyuk
 */
public class Blank extends AbstractLocation {

    /**
     * Position of zero anaphora in sentence.
     */
    private int position;

    @Override
    public int getTextId() {
        return 0;
    }

    @Override
    public int getSentenceNumber() {
        return 0;
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
}
