package client;

import chain.Chain;
import document.Converter;

import java.util.List;

/**
 * This class describes interaction protocol of User
 *
 * @author Vadim Baydyuk
 * @see Client
 */
public class User extends AbstractClient {
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
