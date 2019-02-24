package chain;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
     * Location of the chain.
     */
    private List<Location> locations;


    public ChainImpl(String name, Color color, Location... locations) {
        this.color = color;
        this.name = name;
        this.locations = new ArrayList<>();
        this.locations.addAll(Arrays.asList(locations));
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
    public void addPart(Location location) {
        locations.add(location);
    }

    @Override
    public void addAll(List<List<String>> phrases, List<Location> locations) {
        this.phrases.addAll(phrases);
        this.locations.addAll(locations);
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

//    @Override
//    public List<List<String>> getParts() {
//        return phrases;
//    }
//
//    @Override
//    public void mergeWith(Chain chain) {
//        phrases.addAll(chain.getParts());
//        locations.addAll(chain.getLocations());
//    }

    @Override
    public List<Location> getLocations() {
        return locations;
    }


    @Override
    public void pack(StringBuilder sb) {
        sb.append(name).append(' ').append(color.getRed())
                .append(' ').append(color.getGreen())
                .append(' ').append(color.getBlue())
                .append(' ').append(locations.size()).append('\n')
                .append(this.toString());
        locations.forEach(location -> location.pack(sb));
    }

    @Override
    public String toString() {
        return locations.stream().map(Object::toString).collect(Collectors.joining(" -- "));
    }
}
