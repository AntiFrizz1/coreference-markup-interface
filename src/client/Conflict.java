package client;

import chain.Chain;
import chain.Location;

/**
 * Interface {@code Conflict} is used for describing conflict
 */
public interface Conflict {

    Location getCollision();

    Chain getFirstSolution();

    Chain getSecondSolution();
}
