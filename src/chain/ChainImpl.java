package chain;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    /**
     * Unique identifier of this chain.
     */
    private int id;

    public ChainImpl(String name, Color color, int id, Location... locations) {
        this.color = color;
        this.name = name;
        this.id = id;
        this.locations = new ArrayList<>();
        this.locations.addAll(Arrays.asList(locations));
    }

    public ChainImpl(String name, Color color, int id, List<Location> locations) {
        this.color = color;
        this.name = name;
        this.id = id;
        this.locations = new ArrayList<>();
        this.locations.addAll(locations);
    }

    public ChainImpl(Action action) {
        id = action.getChainId();
        name = action.getName();
        locations = new ArrayList<>();
        locations.add(action.getLocation());
//        locations = Collections.singletonList(action.getLocation());
    }

    public ChainImpl(List<Action> actions) {
        if (!actions.isEmpty()) {
            id = actions.get(0).getChainId();
            name = actions.get(0).getName();
        }
        locations = actions.stream().map(Action::getLocation).collect(Collectors.toList());
    }

    public ChainImpl(Chain another) {
        this.color = another.getColor();
        this.name = another.getName();
        this.id = another.getId();
        this.locations = new ArrayList<>();
        this.locations.addAll(another.getLocations());
    }

    public ChainImpl(String info) {
        List<String> list = Arrays.asList(info.split("\t"));
        List<String> nameColor = Arrays.asList(list.get(0).split(" "));
        name = nameColor.get(0);
        id = Integer.valueOf(nameColor.get(1));
        color = new Color(Integer.valueOf(nameColor.get(2)), Integer.valueOf(nameColor.get(3)), Integer.valueOf(nameColor.get(4)));
        locations = new ArrayList<>();
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i).contains("Blank: ")) {
                locations.add(new Blank(list.get(i)));
            } else {
                locations.add(new Phrase(list.get(i)));
            }
        }
    }

    public ChainImpl() {
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    @Override
    public void addPart(Location location) {
        locations.add(location);
    }

    @Override
    public void addAll(List<Location> locations) {
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
    public String pack() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(' ').append(id)
                .append(' ').append(color.getRed())
                .append(' ').append(color.getGreen())
                .append(' ').append(color.getBlue()).append('\t');
        sb.append(locations.stream().map(Location::pack).collect(Collectors.joining("\t")));
        return sb.toString();
    }

    @Override
    public String packSB(StringBuilder sb) {
        return null;
    }

    @Override
    public String toString() {
        return locations.stream().map(Object::toString).collect(Collectors.joining(" -- "));
    }

    public List<Set<String>> listOfParts() {
        return locations.stream().map(Location::getWords).collect(Collectors.toList());
    }
}