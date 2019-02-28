package client;

import chain.Action;
import chain.Chain;
import document.Converter;
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

    /*@Override
    public void sendInfo(List<Action> actions) {
        UpdateDocument document = new UpdateDocument(actions);
        writer.println(document.pack());
        writer.flush();
    }*/

    @Override
    public void sendInfo(List<Chain> document) {
        writer.println(converter.pack(document));
        writer.flush();
    }

    @Override
    public List<Chain> getInfo() {
        try {
            return converter.unpack(reader.readLine());
        } catch (IOException e) {
            System.err.println("Can't get information from server");
        }
        return null;
    }

    @Override
    public void close(List<Action> actions) {
        UpdateDocument document = new UpdateDocument(actions);
        writer.println(document.pack());
        writer.flush();
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Can't close socket");
        }
    }

}
