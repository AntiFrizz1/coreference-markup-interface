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

    @Override
    public int getTextId() {
        return textId;
    }

    @Override
    public int getSentenceNumber() {
        return sentenceNumber;
    }
}
