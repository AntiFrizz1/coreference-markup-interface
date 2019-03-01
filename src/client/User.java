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

    public void sendUpdates(List<Action> actions) {
        UpdateDocument document = new UpdateDocument(actions);
        writer.println(document.pack());
        writer.flush();
    }

    @Override
    public boolean joinOnline() {
        if (sendConnectionInfo(String.valueOf(id))) {
            if (sendConnectionInfo("0")) {
                System.out.println("Successful connect to server in online mode with id = " + id);
                return true;
            } else {
                System.err.println("Can't connect to server in online mode with id = " + id);
                return false;
            }
        } else {
            System.err.println("Can't connect to server in online mode with id = " + id);
            return false;
        }
    }

    public boolean joinOffline() {
        if (sendConnectionInfo(String.valueOf(id))) {
            if (sendConnectionInfo("0")) {
                System.out.println("Successful connect to server in offline mode with id = " + id);
                return true;
            } else {
                System.err.println("Can't connect to server in offline mode with id = " + id);
                return false;
            }
        } else {
            System.err.println("Can't connect to server in offline mode with id = " + id);
            return false;
        }
    }

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
