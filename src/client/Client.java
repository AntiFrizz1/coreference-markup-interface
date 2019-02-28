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
     * Send updated information about coreferences to server.
     * It happens every 30 seconds.
     *
     * @param actions the information that should be sent.
     */
    void sendUpdates(List<Action> actions);

    /**
     * Send information about conflict resolution to server.
     *
     * @param document the information that should be sent.
     */
    void sendInfo(List<Chain> document);

    /**
     * Get information about conflict from server.
     *
     * @return the document with information about conflict.
     */
    List<Chain> getInfo();

    /**
     * Connect to server as User or as Judge for work online.
     */
    void joinOnline();

    /**
     * Connect to server as User or as Judge for work offline.
     */
    void joinOffline();

    /**
     * Complete work and send last actions to server
     */
    void close(List<Action> actions);
}
