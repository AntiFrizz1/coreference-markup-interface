package client;

import chain.*;
import document.UpdateDocument;

import java.util.List;

public class ConflictImpl implements Conflict {
    List<Action> first, second;

    public ConflictImpl(String list1, String list2) {
        UpdateDocument doc1 = new UpdateDocument(list1);
        UpdateDocument doc2 = new UpdateDocument(list2);

        first = doc1.getActions();
        second = doc2.getActions();
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
    public String pack() {
        return null;
    }

    @Override
    public String packSB(StringBuilder sb) {
        return null;
    }
}
