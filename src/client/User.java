package client;

import chain.Action;
import chain.Chain;
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

    public String getText() {
        try {
            StringBuilder text = new StringBuilder(reader.readLine());
            while (reader.ready()) {
                text.append(reader.readLine() + "\n");
            }
            return text.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void sendUpdates(List<Action> actions) {
        // probably need to cut last 15-20 actions
        //while (true) {
            try {
                UpdateDocument document = new UpdateDocument(actions);
                writer.println(document.pack());
                writer.flush();
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        //}
    }

    @Override
    public void joinOnline() {
        sendConnectionInfo(String.valueOf(id));
        sendConnectionInfo("0");
        System.out.println("Successful connect to server in online mode with id = " + id);
    }

    public void joinOffline() {
        sendConnectionInfo(String.valueOf(id));
        sendConnectionInfo("1");
        System.out.println("Successful connect to server in offline mode with id = " + id);
    }

        /*@Override
    public void sendInfo(List<Action> actions) {
        UpdateDocument document = new UpdateDocument(actions);
        writer.println(document.pack());
        writer.flush();
    }*/

    /*public void sendInfo(List<Chain> document) {
        writer.println(converter.pack(document));
        writer.flush();
    }*/

    /*public List<Chain> getInfo() {
        try {
            return converter.unpack(reader.readLine());
        } catch (IOException e) {
            System.err.println("Can't get information from server");
        }
        return null;
    }*/

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
