package userInterface;

import chain.Action;
import chain.Chain;

public interface Controller {
    void showUI();
    void onlineMode();
    void offlineMode();
    void nextSentence();
    void prevSentence();
    Action addToChain();
    Action addAnaphoraToChain();
    Action addNewChain();
    void deleteChain(Chain chain);
    void saveStateOffline();
    Action cancel();
    void showConflict();
    void resolveConflict(int decision);
    void disableOption(int option);
    void enableOption(int option);
}
