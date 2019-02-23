package client;

import document.Document;

/**
 * This class describes interaction protocol of Judge
 *
 * @author Vadim Baydyuk
 * @see Client
 */
public class Judge extends AbstractClient {
    //private Listener listener;

    @Override
    public void sendUpdates(Document document) {

    }

    @Override
    public void sendInfo(Document document) {

    }

    @Override
    public Document getInfo() {
        return null;
    }


    @Override
    public void join() {

    }


    @Override
    public void close() {

    }
}
