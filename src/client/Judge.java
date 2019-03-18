package client;

import chain.Action;
import userInterface.JudgeController;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


/**
 * This class describes interaction protocol of Judge
 *
 * @author Vadim Baydyuk
 * @see Client
 */
public class Judge extends AbstractClient {

    List<String> texts;

    public Judge(int id, int port, String serviceAddress) {
        super(id, port, serviceAddress);
        texts = new ArrayList<>();
    }
    //private Listener listener;

    @Override
    public int joinOnline() {
        if (sendConnectionInfo(String.valueOf(id)) == 0) {
            System.out.println("Successful connect to server as judge with id = " + id);
            readAllTextes();
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
            int id = Integer.parseInt(third);
            return new ConflictImpl(first, second, texts.get(id));
        } catch (IOException e) {
            System.err.println("Can't get information from server");
        }
        return null;
    }

    private void readAllTextes() {
        try {
            int size = Integer.parseInt(reader.readLine());
            for (int i = 0; i < size; i++) {
                texts.add(reader.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
