package chain;


import document.Packable;

import java.util.Set;

/**
 * Interface {@code Location} uses for description position
 * of part of {@code Chain} in the text.
 *
 * @author Vadim Baydyuk
 * @see Chain
 */
public interface Location extends Packable {
    Set<String> getWords();
}
