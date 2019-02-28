package client;

import chain.Action;

import java.io.IOException;
import java.util.List;

/**
 * This class describes interaction protocol of Judge
 *
 * @author Vadim Baydyuk
 * @see Client
 */
public class Judge extends AbstractClient {

    public Judge(int id, int port, String serviceAddress) {
        super(id, port, serviceAddress);
    }
    //private Listener listener;

    @Override
    public void joinOnline() {
        sendConnectionInfo(String.valueOf(id));
        System.out.println("Successful connect to server as judge with id = " + id);
    }

    public void sendDecision(int decision) {
        writer.println(decision);
        writer.flush();
    }

    public Conflict getInfo() {
        try {
            String first = reader.readLine();
            String second = reader.readLine();
            return new ConflictImpl(first, second);
        } catch (IOException e) {
            System.err.println("Can't get information from server");
        }
        return null;
    }
}
