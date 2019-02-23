package chain;

import java.awt.*;
import java.util.List;

/**
 * This class describes dedicated coreference
 *
 * @author Vadim Bydyuk
 * @see Location
 */
public class ChainImpl implements Chain {
    /**
     * Color of the chain.
     */
    private Color color;

    /**
     * Name of the chain.
     */
    private String name;

    /**
     * Part of the chain
     */
    private List<List<String>> phrases;

    /**
     * Location of the chain.
     */
    private List<Location> locations;

    @Override
    public void setName(String name) {

    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void addPart(List<String> phrase, Location location) {

    }

    @Override
    public void deleteWord(Location location) {

    }

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public void setColor(Color color) {

    }

    @Override
    public List<List<String>> getWords() {
        return null;
    }


    @Override
    public void mergeWith(Chain chain) {

    }
}
