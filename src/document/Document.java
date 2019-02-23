package document;

import chain.Chain;

import java.util.List;

/**
 * This interface describes information about changes.
 *
 * @author Vadim Baydyuk
 * @see Chain
 */
public interface Document {

    /**
     * Pack all information in one string.
     * It simplifies sending of message.
     *
     * @return the string with all information.
     */
    String pack();


    /**
     * Get chains.
     *
     * @return the list of chains.
     */
    List<Chain> getChains();
}
