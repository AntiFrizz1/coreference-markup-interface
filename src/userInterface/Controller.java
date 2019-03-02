package userInterface;

import chain.Action;
import chain.Chain;

public interface Controller {
    void showUI();
    void onlineMode();
    void offlineMode();
    Action addToChain();
    Action addAnaphoraToChain();
    Action addNewChain();
    void deleteChain(Chain chain);
    void saveStateOffline();
    Action cancel();
    void disableOption(int option);
    void enableOption(int option);
}
