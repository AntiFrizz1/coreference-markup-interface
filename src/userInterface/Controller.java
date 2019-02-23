package userInterface;

import chain.Chain;

public interface Controller {
    void showUI();
    void onlineMode();
    void offlineMode();
    void nextSentence();
    void prevSentence();
    void addChain(Chain chain);
    void deleteChain(Chain chain);
    void saeStateOffline();
    void cancel();
    void showConflict();
    void resolveConflict(int decision);
    void disableOption(int option);
    void enableOption(int option);
}
