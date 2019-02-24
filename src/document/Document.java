package document;

import chain.Chain;

import java.util.List;

/**
 * This interface describes information about changes.
 *
 * @author Vadim Baydyuk
 * @see Chain
 */
public interface Document extends Packable {
    /**
     * Get chains.
     *
     * @return the list of chains.
     */
    List<Chain> getChains();
}
