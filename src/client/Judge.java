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

    public List<String> texts;

    public Judge(String id, int port, String serviceAddress) {
        super(id, port, serviceAddress);
        texts = new ArrayList<>();
    }
    //private Listener listener;

    @Override
    public int joinOnline() {
        if (sendConnectionInfo() == 0) {
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
            while (!reader.ready()) {
                Thread.sleep(1000);
            }
            String first = reader.readLine();

            while (!reader.ready()) {
                Thread.sleep(1000);
            }
            String second = reader.readLine();

            while (!reader.ready()) {
                Thread.sleep(1000);
            }
            String third = reader.readLine();

            if (first == null || second == null || third == null) {
                connect();
                sendConnectionInfo();
            } else {
                int id = Integer.parseInt(third);
                return new ConflictImpl(first, second, texts.get(id));
            }
        } catch (IOException e) {
            System.err.println("Can't get information from server");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void readAllTextes() {
        try {
            while (!reader.ready()) {
                Thread.sleep(1000);
            }
            int size = Integer.parseInt(reader.readLine());
            for (int i = 0; i < size; i++) {
                while (!reader.ready()) {
                    Thread.sleep(1000);
                }
                texts.add(reader.readLine());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}
