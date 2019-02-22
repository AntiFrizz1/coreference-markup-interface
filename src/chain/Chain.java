package chain;


import java.awt.Color;
import java.util.List;

public interface Chain {
    void setName(String name);
    String getName();
    void addWord(String word, Location location);
    void deleteWord(Location location);
    Color getColor();
    void setColor(Color color);
    List<String> getWords();
    void mergeWith(Chain chain);
}
