package client;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

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
    protected String id;

    /**
     * Client reader
     */
    protected BufferedReader reader;

    /**
     * Client writer
     */
    protected PrintWriter writer;

    protected Queue<String> dataToSend;

    protected Thread senderThread;

    protected Thread receiverThread;

    public boolean isServerWork = false;

    public AbstractClient(String id, int port, String serviceAddress) {
        try {
            this.id = id;
            this.port = port;
            this.serviceAddress = serviceAddress;
            dataToSend = new ConcurrentLinkedQueue<>();
            connect();
        } catch (IOException e) {
            System.err.println("Can't connect to server");
        }
    }

    /**
     * Send connection information to server and waiting for positive response
     */
    protected int sendConnectionInfo() {
        while (true) {
            try {
                writer.println(id);
                writer.flush();
                while (!reader.ready()) {
                    Thread.sleep(1000);
                }
                int request = reader.read();
                if (request == -1) {
                    break;
                    //continue;
                }
                return request;
            } catch (Exception e) {
                System.err.println("Can't read answer from server: " + e.getMessage());
                break;
            }
        }
        return 3;
    }

    protected void connect() throws IOException {
        socket = new Socket(serviceAddress, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)));
    }

    public void close() {
        try {
            senderThread.interrupt();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected Runnable sender = () -> {
        while (isServerWork) {
            if (!dataToSend.isEmpty()) {
                writer.write(0);
                writer.println(dataToSend.poll());
                writer.flush();
            } else {
                writer.write(1);
                writer.flush();
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                break;
            }
        }
    };


    protected Runnable receiver = () -> {
        while (isServerWork) {
            try {
                int count = 0;
                while (!reader.ready() && count < 3) {
                    Thread.sleep(5000);
                    count++;
                }
                if (count == 3) {
                    isServerWork = false;
                    break;
                }
                int out = reader.read();
                if (out != 0) {
                    isServerWork = false;
                    break;
                }
            } catch (IOException e) {

            } catch (InterruptedException e) {
                break;
            }
        }
    };

}
