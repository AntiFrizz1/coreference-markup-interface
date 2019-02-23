package chain;

import java.awt.*;
import java.util.ArrayList;
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


    ChainImpl(String name, Color color) {
        this.color = color;
        this.name = name;
        phrases = new ArrayList<>();
        locations = new ArrayList<>();
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void addPart(List<String> phrase, Location location) {
        phrases.add(phrase);
        locations.add(location);
    }

    @Override
    public void deletePart(Location location) {
        locations.remove(location);
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public List<List<String>> getParts() {
        return phrases;
    }

    @Override
    public void mergeWith(Chain chain) {
        phrases.addAll(chain.getParts());
        locations.addAll(chain.getLocations());
    }

    @Override
    public List<Location> getLocations() {
        return locations;
    }


}
