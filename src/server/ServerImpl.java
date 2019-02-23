package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Set;

/**
 * This class describes interaction protocol of Server
 *
 * @author Vadim Baydyuk
 * @see Server
 */
public class ServerImpl implements Server {
    /**
     * Identifier of server.
     */
    private int port;

    /**
     * An internal endpoint for sending or receiving data.
     */
    private ServerSocket socket;

    /**
     * Texts.
     */
    private List<List<String>> texts;

    /**
     * Pool of users.
     */
    private List<Socket> users;


    /**
     * Set of judges id.
     */
    private Set<Integer> judgesId;

    /**
     * Pool of judges.
     */
    private List<Socket> judges;
}
