package client;

import chain.Chain;

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
    public void sendUpdates(List<Chain> document) {
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
