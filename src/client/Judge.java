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
    public void sendInfo(List<Chain> document) {

    }

    @Override
    public List<Chain> getInfo() {
        return null;
    }

    @Override
    public void join() {

    }

    @Override
    public void close() {

    }
}
