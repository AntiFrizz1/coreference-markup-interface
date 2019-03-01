package client;

import chain.Action;
import chain.Chain;

import java.util.List;

/**
 * This interface describes client.
 *
 * @author Vadim Baydyuk
 * @TODO Listener with endless loop that waiting for tasks from server
 * @TODO class Task which describes conflict for Judge or User
 */
public interface Client {

    /**
     * Connect to server as User or as Judge for work online.
     */
    boolean joinOnline();

    void close();
}
