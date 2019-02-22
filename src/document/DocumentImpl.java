package document;

import chain.Chain;

import java.util.List;

public class DocumentImpl implements Document {
    int id;
    List<Chain> chains;

    DocumentImpl(String content) {

    }

    @Override
    public String pack() {
        return null;
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public List<Chain> getChains() {
        return null;
    }
}
