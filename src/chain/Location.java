package chain;


import document.Packable;

/**
 * Interface {@code Location} uses for description position
 * of part of {@code Chain} in the text.
 *
 * @author Vadim Baydyuk
 * @see Chain
 */
public interface Location extends Packable {

    /**
     * @return Number of text that contains this part of chain.
     */
    int getTextId();

    /**
     * @return Number of sentence that contains this part of chain.
     */
    int getSentenceNumber();
}
