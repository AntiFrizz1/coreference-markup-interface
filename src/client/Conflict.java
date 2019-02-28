package client;

import chain.Action;
import chain.Chain;
import document.Packable;

import java.util.List;

/**
 * Interface {@code Conflict} is used for describing conflict
 */
public interface Conflict extends Packable {

    List<Action> getFirst();

    List<Action> getSecond();
}
