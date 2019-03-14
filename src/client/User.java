package client;

import chain.Action;
import document.Data;
import document.UpdateDocument;

import java.io.IOException;
import java.util.List;

/**
 * This class describes interaction protocol of User
 *
 * @author Vadim Baydyuk
 * @see Client
 */
public class User extends AbstractClient {
    //private Listener listener;

    public User(String id, int port, String serviceAddress) {
        super(id, port, serviceAddress);
    }

    public Data getData() {
        while (true) {
            try {
                while (!reader.ready()) {
                    Thread.sleep(1000);
                }
                String input = reader.readLine();
                if (input == null) {
                    /*connect();
                    sendConnectionInfo();*/
                    return null;
                } else {
                    receiverThread = new Thread(receiver);
                    receiverThread.start();
                    return new Data(input);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public String getText() {
        while (true) {
            try {
                while (!reader.ready()) {
                    Thread.sleep(1000);
                }
                String text = reader.readLine();
                if (text == null) {
                    /*connect();
                    sendConnectionInfo();*/
                    return null;
                } else {
                    receiverThread = new Thread(receiver);
                    receiverThread.start();
                    return text;
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public int sendUpdates(List<Action> actions) {
        try {
            UpdateDocument document = new UpdateDocument(actions);
            dataToSend.add(document.pack());
            /*writer.println(document.pack());
            writer.flush();*/
            return 0;
        } catch (Exception e) {
            return 1;
        }
    }

    @Override
    public int joinOnline() {
        int out = sendConnectionInfo();
        if (out == 1 || out == 0) {
            isServerWork = true;
            senderThread = new Thread(sender);
            senderThread.start();
        }
        return out;
    }

    public void close(List<Action> actions) {
        UpdateDocument document = new UpdateDocument(actions);
        writer.println(document.pack());
        writer.flush();
        try {
            writer.println("EXIT");
            writer.flush();
            socket.close();
        } catch (IOException e) {
            System.err.println("Can't close socket");
        }
    }

}
