package document;

import chain.Chain;

import java.util.List;

/**
 * This class describes information about changes.
 *
 * @author Vadim Baydyuk
 * @see Document
 * @see Chain
 */
public class DocumentImpl implements Document {

    /**
     * List of all selected coreferences.
     */
    List<Chain> chains;

    /**
     * Create document from string with information.
     * @param content the information with selected chains.
     */
    DocumentImpl(String content) {

    }

    @Override
    public String pack() {
        return null;
    }


    @Override
    public List<Chain> getChains() {
        return null;
    }
}
