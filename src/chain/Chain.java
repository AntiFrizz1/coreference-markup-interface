package chain;


import java.awt.Color;
import java.util.List;

/**
 * Interface {@code Chain} describes dedicated coreference.
 *
 * @author Vadim Baydyuk
 * @see Location
 */

public interface Chain {
    /**
     * Set name of chain.
     *
     * @param name the name which should be sated
     */
    void setName(String name);

    /**
     * Get name of the chain.
     *
     * @return name of the chain.
     */
    String getName();

    /**
     * Add selected part to chain.
     *
     * @param phrase   the list of words which should be added as part of chain.
     * @param location the location of this part.
     */
    void addPart(List<String> phrase, Location location);

    /**
     * Delete part of the chain.
     *
     * @param location the location op part that should be deleted.
     */
    void deletePart(Location location);

    /**
     * Get color of the chain.
     *
     * @return the color of chain.
     */
    Color getColor();

    /**
     * Set color of the chain
     *
     * @param color the color that should be set.
     */
    void setColor(Color color);

    /**
     * Get all parts of the chain.
     *
     * @return the list of parts of the chain.
     */
    List<List<String>> getParts();

    /**
     * Merge with {@code chain}.
     *
     * @param chain the chain to merge with.
     */
    void mergeWith(Chain chain);

    List<Location> getLocations();
}
