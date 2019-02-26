package userInterface;

import chain.Chain;

import java.util.List;

public interface Controller {
    void showUI();
    void onlineMode();
    void offlineMode();
    void nextSentence();
    void prevSentence();
    List<Chain> addToChain();
    List<Chain> addNewChain();
    void deleteChain(Chain chain);
    void saeStateOffline();
    List<Chain> cancel();
    void showConflict();
    void resolveConflict(int decision);
    void disableOption(int option);
    void enableOption(int option);
}
