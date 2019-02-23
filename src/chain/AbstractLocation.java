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
    protected int textId;

    /**
     * Number of sentence that contains this part of chain.
     */
    protected int sentenceNumber;
}
