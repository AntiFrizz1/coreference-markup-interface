package client;

import document.Converter;

import java.io.*;
import java.net.Socket;

/**
 * This class provides some extra fields for {@code Client}.
 *
 * @author Vadim Baydyuk
 * @see Client
 */
public abstract class AbstractClient implements Client {

    protected Converter converter;
 
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
  
    public AbstractClient() {
        try {
            converter = new Converter();
            socket = new Socket(serviceAddress, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
        } catch (IOException e) {
            System.err.println("Can't connect to server");
        }
    }

    /**
     * Send connection information to server and waiting for positive response
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

    @Override
    public void sendInfo(Document document) {
        writer.println(document.pack());
        writer.flush();
    }

    @Override
    public Document getInfo() {
        try {
            return new DocumentImpl(reader.readLine());
        } catch (IOException e) {
            System.err.println("Can't get information from server");
        }
        return null;
    }

    @Override
    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Can't close socket");
        }
    }

}
