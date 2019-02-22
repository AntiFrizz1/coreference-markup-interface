package server;

import client.Judge;
import client.User;

import java.net.Socket;
import java.util.List;

public class ServerImpl implements Server {
    private int port;
    private Socket socket;
    private List<String> texts;
    private List<User> users;
    private List<Judge> judges;
}
