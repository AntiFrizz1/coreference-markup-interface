package client;

import chain.Action;

import java.util.List;

/**
 * This class describes interaction protocol of Judge
 *
 * @author Vadim Baydyuk
 * @see Client
 */
public class Judge extends AbstractClient {
    //private Listener listener;

    @Override
    public void sendUpdates(List<Action> actions) {

    }

    @Override
    public void joinOnline() {
        sendConnectionInfo(String.valueOf(id));
        System.out.println("Successful connect to server as judge with id = " + id);
    }

    @Override
    public void joinOffline() {

    }
}
