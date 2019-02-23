package client;

import document.Document;

/**
 * This interface describes client.
 *
 * @author Vadim Baydyuk
 * @see Document
 * @TODO Listener with endless loop that waiting for tasks from server
 * @TODO class Task which describes conflict for Judge or User
 */
public interface Client {

    /**
     * Send updated information about coreferences to server.
     * It happens every 30 seconds.
     *
     * @param document the information that should be sent.
     */
    void sendUpdates(Document document);

    /**
     * Send information about conflict resolution to server.
     *
     * @param document the information that should be sent.
     */
    void sendInfo(Document document);

    /**
     * Get information about conflict from server.
     *
     * @return the document with information about conflict.
     */
    Document getInfo();

    /**
     * Connect to server as User of as Judge.
     */
    void join();

    /**
     * Complete work.
     */
    void close();
}
