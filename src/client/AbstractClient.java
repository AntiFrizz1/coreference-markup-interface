package client;

import chain.Action;
import chain.Chain;
import document.UpdateDocument;

import java.io.*;
import java.net.Socket;
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
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
        } catch (IOException e) {
            System.err.println("Can't connect to server");
        }
    }

    /**
     * Send connection information to server and waiting for positive response
     *
     * @param info information for sending
     */
    protected void sendConnectionInfo(String info) {
        try {
            writer.println(info);
            writer.flush();
            String request = reader.readLine();
            if (!request.equals("OK")) {
                System.err.println("Can't connect to server");
                return;
            }
        } catch (IOException e) {
            System.err.println("Can't read answer from server");
        }
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
