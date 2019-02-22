package userInterface;

import chain.Chain;

import java.util.Collection;
import java.util.List;

public class ControllerImpl implements Controller {
    private final int ADDWORD = 0;
    private final int ADDCHAIN = 1;
    private final int DELCHAIN = 2;

    private Collection<Chain> chains;
    private List<Collection<Chain>> prevStates;
    private List<String> text;
    private int textId;
    private int curSentence;
    private Chain curChain;
    int mode;

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

    @Override
    public void addChain(Chain chain) {

    }

    @Override
    public void deleteChain(Chain chain) {

    }

    @Override
    public void saeStateOffline() {

    }

    @Override
    public void cancel() {

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
}
