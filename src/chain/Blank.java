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

    public Blank(int textId, int sentenceNumber, int position) {
        super(textId, sentenceNumber);
        this.position = position;
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

    @Override
    public void getPositions(StringBuilder sb) {
        sb.append(position).append('\n');
    }
}
