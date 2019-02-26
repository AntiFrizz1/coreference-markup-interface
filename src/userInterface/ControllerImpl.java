package userInterface;

import chain.Action;
import chain.Blank;
import chain.Chain;
import chain.ChainImpl;
import chain.Location;
import chain.Phrase;
import javafx.util.Pair;

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
    private final int DELWORD = 3;

    private List<Chain> chains;
    private List<Pair<Action, Integer>> prevStates;
    private int textId;
    private int curSentence;
    private Chain curChain;
    private Map<Integer, String> selected;
    private int selectedBlank = -1;
    private String newChainName;
    private List<Action> actions;  // TODO: send this to the server and then empty it after each send

    ControllerImpl(String text) {
        chains = new ArrayList<>();
        prevStates = new ArrayList<>();
        selected = new HashMap<>();
        actions = new ArrayList<>();
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

    public List<Chain> getChains() {
        return chains;
    }

    public Set<Integer> getSelected() {
        return new HashSet<>(selected.keySet());
    }

    public int getSelectedBlank() {
        return selectedBlank;
    }

    public boolean isSelectedAlreadyBound() {
        return chains.stream().map(Chain::getLocations)
                .anyMatch(list -> list.contains(new Phrase("", new HashSet<>(selected.keySet()))));
    }

    public boolean isSelectedBlankAlreadyBound() {
        return chains.stream().map(Chain::getLocations)
                .anyMatch(list -> list.contains(new Blank(selectedBlank)));
    }

    public void clearSelected() {
        selected.clear();
    }

    public Integer getSelectedChain() {
        return (curChain == null) ? -1 : chains.indexOf(curChain);
    }

    @Override
    public Action addToChain() {
        if (selected.isEmpty() || curChain == null) return null;
        int prevIndex = chains.indexOf(curChain);
        chains.remove(curChain);
        String result = selected.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey))
                .map(Map.Entry::getValue).collect(Collectors.joining(" "));
        Location phrase = new Phrase(result, new HashSet<>(selected.keySet()));
        curChain.addPart(phrase);
        Action ac = new Action(ADDWORD, curChain.getId(), phrase);
        saveState(ac, prevIndex);
        chains.add(0, curChain);
        selected.clear();
        curChain = null;
        return ac;
    }

    @Override
    public Action addAnaphoraToChain() {
        if (selectedBlank == -1 || curChain == null) return null;
        int prevIndex = chains.indexOf(curChain);
        chains.remove(curChain);
        Location blank = new Blank(selectedBlank);
        curChain.addPart(blank);
        Action ac = new Action(ADDWORD, curChain.getId(), blank);
        saveState(ac, prevIndex);
        chains.add(0, curChain);
        selectedBlank = -1;
        curChain = null;
        return ac;
    }

    public void setNewChainName(String name) {
        newChainName = name;
    }

    @Override
    public Action addNewChain() {
        if (selected.isEmpty()) return null;
        String result = selected.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey))
                .map(Map.Entry::getValue).collect(Collectors.joining(" "));
        // TODO: maybe Utils.generateRandomColor()?
        Phrase phrase = new Phrase(result, new HashSet<>(selected.keySet()));
        ChainImpl newChain = new ChainImpl(newChainName, generateRandomColor(),
                chains.size(), phrase);
        Action ac = new Action(ADDCHAIN, newChain.getId(), phrase);
        saveState(ac, -1);
        newChainName = "";
        chains.add(0, newChain);
        curChain = null;
        selected.clear();
        return ac;
    }

    /**
     *
     * @param btn - the text on the pressed button
     * @param position - the position of this button in the text
     * @return if the press was recorded or not
     */
    public boolean pressedButton(String btn, int position) {
        if (!btn.trim().isEmpty() && selectedBlank == -1) {
            if (!selected.containsKey(position)) selected.put(position, btn);
            else selected.remove(position);
            return true;
        }
        else if (selected.isEmpty() && (selectedBlank == -1 || selectedBlank == position)) {
            if (selectedBlank == -1) selectedBlank = position;
            else selectedBlank = -1;
            return true;
        }
        return false;
    }

    public int selectChain(int num) {
        int prev = chains.indexOf(curChain);
        curChain = chains.get(num);
        return prev;
    }

    @Override
    public void deleteChain(Chain chain) {

    }

    @Override
    public void saveStateOffline() {

    }

    @Override
    public Action cancel() {
        Pair<Action, Integer> ac = prevStates.get(prevStates.size() - 1);
        if (actions.size() != 0) {
            actions.remove(actions.size() - 1);
        } else {
            if (ac.getKey().getAction() == ADDCHAIN) {
                actions.add(new Action(DELCHAIN, ac.getKey().getChainId(), ac.getKey().getLocation()));
            } else {
                actions.add(new Action(DELWORD, ac.getKey().getChainId(), ac.getKey().getLocation()));
            }
        }
        prevStates.remove(prevStates.size() - 1);
        if (ac.getKey().getAction() == ADDCHAIN) {
            chains.remove(0);
        }
        if (ac.getKey().getAction() == ADDWORD) {
            Chain c = chains.remove(0);
            c.getLocations().remove(ac.getKey().getLocation());
            chains.add(ac.getValue(), c);
        }
        return ac.getKey();
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

    private void saveState(Action a, Integer prevIndex) {
        if (prevStates.size() >= 20) prevStates.remove(0);
        actions.add(a);
        prevStates.add(new Pair<>(a, prevIndex));
    }

}
