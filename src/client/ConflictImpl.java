package client;

import chain.*;
import document.UpdateDocument;
import org.omg.CORBA.MARSHAL;

import java.lang.ref.PhantomReference;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ConflictImpl implements Conflict {
    public List<Action> first, second;
    public int textId;
    public String text;

    public List<String> wordList;

    public Set<Integer> firstWordsLocation;
    public Set<Integer> firstBlanksLocation;

    public Set<Integer> secondWordsLocation;
    public Set<Integer> secondBlanksLocation;

    public Set<Integer> firstLast;
    public Set<Integer> secondLast;


    public ConflictImpl(String list1, String list2, String text) {
        UpdateDocument doc1 = new UpdateDocument(list1);
        UpdateDocument doc2 = new UpdateDocument(list2);
        first = doc1.getActions();
        second = doc2.getActions();

        this.text = text;

        firstWordsLocation = new HashSet<>();
        firstBlanksLocation = new HashSet<>();

        secondWordsLocation = new HashSet<>();
        secondBlanksLocation = new HashSet<>();

        makeSets();

        makeText();
    }

    private void makeSets() {
        for (Action action: first) {
            Location loc = action.getLocation();
            if (loc instanceof Phrase) {
                firstWordsLocation.addAll(((Phrase) loc).getPositions());
            } else if (loc instanceof Blank) {
                firstBlanksLocation.add(((Blank) loc).getPosition());
            }
        }

        for (Action action: second) {
            Location loc = action.getLocation();
            if (loc instanceof Phrase) {
                secondWordsLocation.addAll(((Phrase) loc).getPositions());
            }if (loc instanceof Blank) {
                secondBlanksLocation.add(((Blank) loc).getPosition());
            }
        }
    }

    private void makeText() {
        List<String> words = Arrays.asList(text.split("\\s+"));
        int left = 0;
        int right = 0;
        boolean f = false;
        for (int i = 0; i < words.size(); i++) {
            if (firstWordsLocation.contains(i) || firstBlanksLocation.contains(i) || secondWordsLocation.contains(i)
                    || secondBlanksLocation.contains(i)) {
                if (!f) {
                    left = i;
                }
                f = true;
                right = i;
            }
        }
        left = Math.max(0, left - 20);
        right = Math.min(words.size() - 1, right + 20);

        wordList = words.subList(left, right);

        int finalLeft = left;
        firstWordsLocation = firstWordsLocation.stream().map(e -> e - finalLeft).collect(Collectors.toSet());
        secondWordsLocation = secondWordsLocation.stream().map(e -> e - finalLeft).collect(Collectors.toSet());
        firstBlanksLocation = firstBlanksLocation.stream().map(e -> e - finalLeft).collect(Collectors.toSet());
        secondBlanksLocation = secondBlanksLocation.stream().map(e -> e - finalLeft).collect(Collectors.toSet());

        Location firstTmp = first.get(first.size() - 1).getLocation();
        Location secondTmp = second.get(second.size() - 1).getLocation();

        if (firstTmp instanceof Phrase) {
            firstLast = ((Phrase) firstTmp).getPositions();
        } else if (firstTmp instanceof Blank) {
            firstLast = new HashSet<>();
            firstLast.add(((Blank) firstTmp).getPosition());
        }

        if (secondTmp instanceof Phrase) {
            secondLast = ((Phrase) secondTmp).getPositions();
        } else if (firstTmp instanceof Blank) {
            secondLast = new HashSet<>();
            secondLast.add(((Blank) secondTmp).getPosition());
        }
    }

    @Override
    public List<Action> getFirst() {
        return first;
    }

    @Override
    public List<Action> getSecond() {
        return second;
    }

    @Override
    public int getTextId() {
        return textId;
    }

    @Override
    public String pack() {
        return null;
    }

    @Override
    public String packSB(StringBuilder sb) {
        return null;
    }
}
