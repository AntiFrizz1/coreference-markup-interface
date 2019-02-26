package server;

import chain.Chain;
import chain.ChainImpl;
import document.Document;

import java.util.*;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class StoreImpl implements Store {
    class Game {
        Document text;
        Map<String, String> names;
        List<Chain> approved;
        List<Chain> teamOne;
        List<Chain> teamTwo;
    }

    List<Game> texts;
    AtomicIntegerArray statusOfElement;

    public void put(Chain chain, int textNum, int teamNum) {
    }

    public boolean compareChains(Map<String, String> names, List<Chain> approved, List<Chain> teamOne, List<Chain> teamTwo, Chain chain1, Chain chain2) {
        return false;
    }

    private void doApproved(Game tmp) {
    }

    public void get() {
    }

    public boolean update(List<Chain> chains) {
        return true;
    }
}
