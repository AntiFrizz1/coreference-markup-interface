package client;

import chain.Action;
import document.UpdateDocument;

import java.util.List;

/**
 * This class describes interaction protocol of User
 *
 * @author Vadim Baydyuk
 * @see Client
 */
public class User extends AbstractClient {
    //private Listener listener;
  
    public User() {
        super();
    }

    @Override
    public void sendUpdates(List<Action> actions) {
        // probably need to cut last 15-20 actions
        while (true) {
            try {
                UpdateDocument document = new UpdateDocument(actions);
                writer.println(document.pack());
                writer.flush();
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    @Override
    public void joinOnline() {
        sendConnectionInfo(String.valueOf(id));
        sendConnectionInfo("0");
        System.out.println("Successful connect to server in online mode with id = " + id);
    }

    @Override
    public void joinOffline() {
        sendConnectionInfo(String.valueOf(id));
        sendConnectionInfo("1");
        System.out.println("Successful connect to server in offline mode with id = " + id);
    }
}
