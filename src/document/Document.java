package document;

import chain.Chain;

import java.util.List;

public interface Document {
    String pack();
    int getId();
    List<Chain> getChains();
}
