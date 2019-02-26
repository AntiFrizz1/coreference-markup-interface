package userInterface;

import chain.Chain;
import chain.ChainImpl;
import chain.Phrase;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class ControllerImpl implements Controller {
    private final int ADDWORD = 0;
    private final int ADDCHAIN = 1;
    private final int DELCHAIN = 2;

    private List<Chain> chains;  // TODO: id for chains
    private List<List<Chain>> prevStates;
    private int textId;
    private int curSentence;
    private Chain curChain;
    int mode;
    private Map<Integer, String> selected;
    private int selectedSpace;
    private String newChainName;

    ControllerImpl(String text) {
        chains = new ArrayList<>();
        prevStates = new ArrayList<>();
        selected = new HashMap<>();
    }

    @Override
    public void showUI() {

    }

    @Override
    public void onlineMode() {

    }

    @Override
    public void offlineMode() {

    }

    @Override
    public void nextSentence() {

    }

    @Override
    public void prevSentence() {

    }

    public Set<Integer> getSelected() {
        return new HashSet<>(selected.keySet());
    }

    public Integer getSelectedChain() {
        return (curChain == null) ? -1 : chains.indexOf(curChain);
    }

    @Override
    public List<Chain> addToChain() {
        if (selected.isEmpty() || curChain == null) return null;
        saveState();
        chains.remove(curChain);
        String result = selected.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey))
                .map(Map.Entry::getValue).collect(Collectors.joining(" "));
        curChain.addPart(new Phrase(result, new HashSet<>(selected.keySet())));
        chains.add(0, curChain);
        selected.clear();
        curChain = null;
        return chains;
    }

    public void setNewChainName(String name) {
        newChainName = name;
    }

    @Override
    public List<Chain> addNewChain() {
        if (selected.isEmpty()) return null;
        saveState();
        String result = selected.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey))
                .map(Map.Entry::getValue).collect(Collectors.joining(" "));
        // TODO: maybe Utils.generateRandomColor()?
        ChainImpl newChain = new ChainImpl(newChainName, generateRandomColor(),
                chains.size(), new Phrase(result, new HashSet<>(selected.keySet())));
        newChainName = "";
        chains.add(0, newChain);
        selected.clear();
        return chains;
    }

    public void pressedButton(String word, int position) {
        if (!word.trim().isEmpty()) {
            if (!selected.containsKey(position)) selected.put(position, word);
            else selected.remove(position);
        }
        else selectedSpace = position;
    }

    public void selectChain(int num) {
        curChain = chains.get(num);
    }

    @Override
    public void deleteChain(Chain chain) {

    }

    @Override
    public void saeStateOffline() {

    }

    @Override
    public List<Chain> cancel() {  // TODO: it might be better to save changes instead of full chains and redo them
        chains = prevStates.get(prevStates.size() - 1);
        prevStates.remove(prevStates.size() - 1);
        return chains;
    }

    public int getPrevStatesSize() {
        return prevStates.size();
    }

    @Override
    public void showConflict() {

    }

    @Override
    public void resolveConflict(int decision) {

    }

    @Override
    public void disableOption(int option) {

    }

    @Override
    public void enableOption(int option) {

    }


    private Color generateRandomColor() {
        Random R = new Random();
        int r = R.nextInt(256);
        int g = R.nextInt(256);
        int b = R.nextInt(256);
        if (r + g + b < 120) return generateRandomColor();
        if (r + g + b > 600) return generateRandomColor();
        else return new Color(r, g, b);
    }

    private void saveState() {
        if (prevStates.size() >= 20) prevStates.remove(0);
        List<Chain> copy = new ArrayList<>();
        for (Chain c: chains) copy.add(new ChainImpl(c));
        prevStates.add(copy);
    }

}
