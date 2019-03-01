package client;

import chain.Action;

import java.io.IOException;
import java.net.Socket;
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
    public int joinOnline() {
        if (sendConnectionInfo(String.valueOf(id)) == 0) {
            System.out.println("Successful connect to server as judge with id = " + id);
            return 0;
        } else {
            System.err.println("Can't connect to server as judge with id = " + id);
            return -1;
        }
    }

    public void sendDecision(int decision) {
        writer.println(decision);
        writer.flush();
    }

    public Conflict getInfo() {
        try {
            String first = reader.readLine();
            String second = reader.readLine();
            String third = reader.readLine();
            return new ConflictImpl(first, second, third);
        } catch (IOException e) {
            System.err.println("Can't get information from server");
        }
        return null;
    }
}
