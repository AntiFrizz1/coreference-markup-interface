package chain;

/**
 * This class provides some extra fields for {@code Location}.
 *
 * @author Vadim Baydyuk
 * @see Location
 * @see Chain
 */

public abstract class AbstractLocation implements Location {

    /**
     * Number of text that contains this part of chain.
     */
    private int textId;

    /**
     * Number of sentence that contains this part of chain.
     */
    private int sentenceNumber;

    AbstractLocation(int textId, int sentenceNumber) {
        this.textId = textId;
        this.sentenceNumber = sentenceNumber;
    }
    @Override
    public int getTextId() {
        return textId;
    }

    @Override
    public int getSentenceNumber() {
        return sentenceNumber;
    }

    abstract public void getPositions(StringBuilder sb);

    @Override
    public void pack(StringBuilder sb) {
        sb.append(textId).append(' ').append(sentenceNumber).append('\n');
        getPositions(sb);
    }
}
