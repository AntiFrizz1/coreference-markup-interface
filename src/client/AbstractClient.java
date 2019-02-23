package client;

import java.net.Socket;

/**
 * This class provides some extra fields for {@code Client}.
 *
 * @author Vadim Baydyuk
 * @see Client
 */
public abstract class AbstractClient implements Client {

    /**
     * An internal endpoint for sending or receiving data.
     */
    protected Socket socket;

    /**
     * Identifier of client.
     */
    protected int port;

    /**
     * Client address.
     */
    protected String serviceAddress;
}
