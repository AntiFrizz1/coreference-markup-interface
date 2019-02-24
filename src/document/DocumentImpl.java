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
     * Create document from list with chains.
     *
     * @param chains the list of chains.
     */
    public DocumentImpl(List<Chain> chains) {
        this.chains = chains;
    }

    @Override
    public void pack(StringBuilder sb) {
        sb.append(chains.size()).append('\n');
        chains.forEach(chain -> {
            chain.pack(sb);
        });
    }


    @Override
    public List<Chain> getChains() {
        return chains;
    }
}
