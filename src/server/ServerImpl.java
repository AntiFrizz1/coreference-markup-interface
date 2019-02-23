package server;

import client.Judge;
import client.User;

import java.net.Socket;
import java.util.List;

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
    private Socket socket;

    /**
     * Texts.
     */
    private List<List<String>> texts;

    /**
     * Pool of users.
     */
    private List<User> users;

    /**
     * Pool of judges.
     */
    private List<Judge> judges;
}
