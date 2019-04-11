package client;

import chain.Action;
import userInterface.JudgeController;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * This class describes interaction protocol of Judge
 *
 * @author Vadim Baydyuk
 * @see Client
 */
public class Judge extends AbstractClient {

    private List<String> texts;

    public Judge(String id, int port, String serviceAddress) {
        super(id, port, serviceAddress);
        texts = new ArrayList<>();
    }

    public Queue<ConflictImpl> conflictQueue = new ConcurrentLinkedQueue<>();

    private Runnable judgeReceiver = () -> {
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
                System.out.println(out);
                if (out == 0) {
                } else if (out == 1) {
                    while (!reader.ready()) {
                        Thread.sleep(200);
                    }
                    String first = reader.readLine();

                    while (!reader.ready()) {
                        Thread.sleep(200);
                    }
                    String second = reader.readLine();

                    while (!reader.ready()) {
                        Thread.sleep(200);
                    }
                    String third = reader.readLine();

                    if (first == null || second == null || third == null) {
                        isServerWork = false;
                        break;
                    }
                    int id = Integer.parseInt(third);
                    conflictQueue.add(new ConflictImpl(first, second, texts.get(id)));
                }
            } catch (InterruptedException | IOException e) {
                isServerWork = false;
                break;
            }
        }
    };

    @Override
    public int joinOnline() {
        if (sendConnectionInfo() == 0) {
            System.out.println("Successful connect to server as judge with id = " + id);
            readAllTextes();
            receiverThread = new Thread(judgeReceiver);
            senderThread = new Thread(sender);
            isServerWork = true;
            receiverThread.start();
            senderThread.start();
            return 0;
        } else {
            System.err.println("Can't connect to server as judge with id = " + id);
            return -1;
        }
    }

    public void sendDecision(int decision) {
        dataToSend.add(String.valueOf(decision));
    }

    public Conflict getInfo() {

        while (isServerWork) {
            try {
                while (conflictQueue.isEmpty() && isServerWork) {
                    Thread.sleep(1000);
                }
                return conflictQueue.poll();
            } catch (InterruptedException e) {
                break;
            }
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

    public void kill() {
        isServerWork = false;
        receiverThread.interrupt();
        senderThread.interrupt();
    }
}
