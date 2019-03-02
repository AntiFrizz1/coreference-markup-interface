package client;

import chain.Action;
import chain.Chain;
import document.UpdateDocument;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

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
    protected String serviceAddress = "localhost";

    /**
     * Client id.
     */
    protected int id;

    /**
     * Client reader
     */
    protected BufferedReader reader;

    /**
     * Client writer
     */
    protected PrintWriter writer;

    public AbstractClient(int id, int port, String serviceAddress) {
        try {
            this.id = id;
            this.port = port;
            this.serviceAddress = serviceAddress;
            socket = new Socket(serviceAddress, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)));
        } catch (IOException e) {
            System.err.println("Can't connect to server");
        }
    }

    /**
     * Send connection information to server and waiting for positive response
     *
     * @param info information for sending
     */
    protected int sendConnectionInfo(String info) {
        try {
            writer.println(info);
            writer.flush();
            String request = reader.readLine();
            if (request.equals("OK")) {
                return 0;
            } else if (request.equals("R")) {
                return 1;
            } else if (request.equals("E")) {
                return 2;
            } else {
                return 3;
            }
        } catch (Exception e) {
            System.err.println("Can't read answer from server: " + e.getMessage());
        }
        return 3;
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
