package client;

import java.net.Socket;

public abstract class AbstractClient implements Client {
    protected Socket socket;
    protected int port;
    protected String serviceAddress;
}
