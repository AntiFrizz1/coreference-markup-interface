package client;

import chain.Action;
import chain.Chain;
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

    public User(int id, int port, String serviceAddress) {
        super(id, port, serviceAddress);
    }

    public Data getData() {
        String input = null;
        try {
            input = reader.readLine();
            return new Data(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getText() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int sendUpdates(List<Action> actions) {
        try {
            UpdateDocument document = new UpdateDocument(actions);
            writer.println(document.pack());
            writer.flush();
            return 0;
        } catch (Exception e) {
            return 1;
        }
    }

    @Override
    public int joinOnline() {
        int out = sendConnectionInfo(String.valueOf(id));
        if (out == 0) {
            out = sendConnectionInfo("0");
            if (out == 0) {
                System.out.println("Successful connect to server in online mode with id = " + id);
                return 0;
            } else {
                System.err.println("Can't connect to server in online mode with id = " + id);
                return -1;
            }
        } else if (out == 1) {
            out = sendConnectionInfo("0");
            if (out == 0) {
                System.out.println("Successful reconnect to server in online mode with id = " + id);
                return 1;
            } else {
                System.err.println("Can't reconnect to server in online mode with id = " + id);
                return -1;
            }
        } else if (out == 2) {
            return 2;
        } else {
            return -1;
        }
    }

    public int joinOffline() {
        int out = sendConnectionInfo(String.valueOf(id));
        if (out == 0) {
            out = sendConnectionInfo("1");
            if (out == 0) {
                System.out.println("Successful connect to server in offline mode with id = " + id);
                return 0;
            } else {
                System.err.println("Can't connect to server in offline mode with id = " + id);
                return -1;
            }
        } else {
            return -1;
        }
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
