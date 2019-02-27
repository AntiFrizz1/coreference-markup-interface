package client;

import chain.Chain;

/**
 * Interface {@code Conflict} is used for describing conflict
 */
public interface Conflict {

    Chain getFirstSolution();

    Chain getSecondSolution();
}
