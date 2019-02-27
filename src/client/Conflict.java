package client;

import chain.Chain;
import document.Packable;

/**
 * Interface {@code Conflict} is used for describing conflict
 */
public interface Conflict extends Packable {

    Chain getFirstSolution();

    Chain getSecondSolution();
}
