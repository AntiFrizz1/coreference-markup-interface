package server;

import chain.Action;
import chain.Chain;
import client.Conflict;
import document.ConflictData;
import document.ConflictInfo;
import document.Converter;
import document.UpdateDocument;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;

/**
 * This class describes interaction protocol of Server
 *
 * @author Vadim Baydyuk
 * @see Server
 */
public class ServerImpl implements Server {
    /**
     * Identifier of server.
     */
    private int port;

    /**
     * An internal endpoint for sending or receiving data.
     */
    private ServerSocket socket;

    /**
     * Texts.
     */
    private List<String> texts;

    /**
     * Pool of clients.
     */
    private Queue<Socket> clients;

    /**
     * Pool of users.
     */
    private Queue<Socket> users;

    /**
     * Pool of users.
     */
    private Queue<Socket> onlineUsers;

    /**
     * Pool of users.
     */
    private Queue<Socket> offlineUsers;

    /**
     * Set of judges id.
     */
    private Set<Integer> judgesId;

    /**
     * Pool of judges.
     */
    private List<JudgeInfo> judges;

    /**
     * List of workers
     */
    private List<Thread> workers;

    /**
     * List of conflicts
     */
    static Queue<ConflictInfo> conflicts;

    private Queue<AddTask> tasks;

    private ServerStore serverStore;
    private JudgeStore judgeStore;

    private Converter converter;

    Map<Integer, Socket> idToSocket;

    Map<Socket, Integer> socketToId;

    public ServerImpl() {
        try {
            socket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        users = new ConcurrentLinkedQueue<>();
        judges = new CopyOnWriteArrayList<>();
        clients = new ConcurrentLinkedQueue<>();
        offlineUsers = new ConcurrentLinkedQueue<>();
        onlineUsers = new ConcurrentLinkedQueue<>();
        tasks = new ConcurrentLinkedQueue<>();
        workers = new CopyOnWriteArrayList<>();

        judgesId = new ConcurrentSkipListSet<>();

        serverStore = new ServerStore();
        judgeStore = new JudgeStore();

        converter = new Converter();

        idToSocket = new HashMap<>();

        conflicts = new ConcurrentLinkedQueue<>();
    }

    /**
     * Start server
     * @throws InterruptedException
     */
    public void run() throws InterruptedException {
        Thread listenerThread = new Thread(listener);
        Thread schedulerThread = new Thread(scheduler);
        Thread userSchedulerThread = new Thread(userScheduler);
        Thread onlineUsersSchedulerThread = new Thread(onlineUsersScheduler);
        Thread offlineUsersSchedulerThread = new Thread(offlineUsersScheduler);
        Thread serverStoreWorkerThred = new Thread(serverStore.worker);

        listenerThread.start();
        schedulerThread.start();
        userSchedulerThread.start();
        onlineUsersSchedulerThread.start();
        offlineUsersSchedulerThread.start();
        serverStoreWorkerThred.start();

        listenerThread.join();
        schedulerThread.join();
        userSchedulerThread.join();
        onlineUsersSchedulerThread.join();
        offlineUsersSchedulerThread.join();
        serverStoreWorkerThred.join();
    }

    /**
     * Connect clients to server
     */
    private Runnable listener = () -> {
        while (true) {
            try {
                Socket client = socket.accept();
                System.out.println("listener :=: connected: " + client.toString());
                clients.offer(client);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Split clients on clients or judges
     */
    private Runnable scheduler = () -> {
        try {
            while (true) {
                if (!clients.isEmpty()) {
                    Socket client = clients.poll();
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                        PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())));

                        String request = reader.readLine();
                        System.out.println("scheduler :=: request from " + client.toString() + " = " + request);

                        int id = Integer.parseInt(request);

                        idToSocket.put(id, client);
                        socketToId.put(client, id);

                        if (judgesId.contains(id)) {
                            judges.add(new JudgeInfo(client));
                        } else {
                            users.offer(client);
                        }

                        writer.println("OK");
                        writer.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Thread.sleep(1000);
                }
            }
        } catch (InterruptedException e) {
            System.err.println("Scheduler interrupted: " + e.getMessage());
        }
    };

    /**
     * Split users on online or offline users
     */
    private Runnable userScheduler = () -> {
        try {
            while (true) {
                if (!users.isEmpty()) {
                    Socket client = users.poll();
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                        PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())));

                        String request = reader.readLine();
                        System.out.println("userScheduler :=: request from " + client.toString() + " = " + request);

                        int id = Integer.parseInt(request);
                        if (id == 0) {
                            onlineUsers.offer(client);
                        } else {
                            offlineUsers.offer(client);
                        }

                        writer.println("OK");
                        writer.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Thread.sleep(1000);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    };

    /**
     * Give task for online users
     */
    private Runnable onlineUsersScheduler = () -> {
        AtomicInteger textNumber = new AtomicInteger();
        try {
            while (true) {
                if (onlineUsers.size() >= 2) {
                    Socket client1 = onlineUsers.poll();
                    Socket client2 = onlineUsers.poll();
                    Thread worker = new Thread(() -> {
                        try {

                            int text = textNumber.get();

                            textNumber.getAndIncrement();

                            BufferedReader reader1 = new BufferedReader(new InputStreamReader(client1.getInputStream()));
                            BufferedReader reader2 = new BufferedReader(new InputStreamReader(client2.getInputStream()));

                            PrintWriter writer1 = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client1.getOutputStream())));
                            PrintWriter writer2 = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client2.getOutputStream())));

                            writer1.println(texts.get(text));
                            writer2.println(texts.get(text));

                            writer1.flush();
                            writer2.flush();

                            serverStore.addNewGame(socketToId.get(client1), socketToId.get(client2));
                            judgeStore.addNewGame(socketToId.get(client1), socketToId.get(client2));

                            Thread thread1 = new Thread(() -> {
                                while (true) {
                                    try {
                                        String request = reader1.readLine();
                                        UpdateDocument document = new UpdateDocument(request);
                                        serverStore.putActions(document.getActions(), text, 1);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });

                            Thread thread2 = new Thread(() -> {
                                while (true) {
                                    try {
                                        String request = reader2.readLine();
                                        UpdateDocument document = new UpdateDocument(request);
                                        serverStore.putActions(document.getActions(), text, 2);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });

                            thread1.start();
                            thread2.start();
                            thread1.join();
                            thread2.join();

                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                    worker.start();
                    workers.add(worker);
                } else {
                    Thread.sleep(1000);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    };

    /**
     * Give task for offline users
     */
    private Runnable offlineUsersScheduler = () -> {
        int texNumber = 0;
        try {
            while (true) {
                if (!offlineUsers.isEmpty()) {
                    Socket client = offlineUsers.poll();
                    System.out.println("offlineUsersScheduler :=: " + client.toString());
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                        PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())));
                        int text = texNumber;

                        texNumber++;

                        writer.println(texts.get(text));
                        writer.flush();
                        Thread worker = new Thread(() -> {
                            try {
                                String doc = reader.readLine();
                                List<Chain> answer = converter.unpack(doc);
                                //save data
                                System.out.println("offlineUsersScheduler :=: " + client.toString() + " doc = " + doc);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                        worker.run();
                        workers.add(worker);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Thread.sleep(1000);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    };

    Runnable conflictInfoScheduler = () -> {
        while (true) {
            try {
                if (!conflicts.isEmpty()) {
                    ConflictInfo conflict = conflicts.poll();
                    if (conflict.status.get() == 0) {
                        boolean f = false;
                        for (int j = 0; j < judges.size(); j++) {
                            if (!judges.get(j).task.isMarked()) {
                                judges.get(j).setTask(conflict);
                                f = true;
                                break;
                            }
                        }
                        if (!f) {
                            conflicts.add(conflict);
                        }
                    } else if (conflict.status.get() == 1) {
                        conflicts.add(conflict);
                    }
                } else {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    class JudgeInfo {
        Socket socket;
        /**
         * reference has mark set false if judge is free otherwise true
         */
        AtomicMarkableReference<ConflictInfo> task;
        Thread worker;

        JudgeInfo(Socket socket) {
            this.socket = socket;
            task = new AtomicMarkableReference<>(null, false);
            worker = new Thread(judgeWorker);
            worker.start();
        }

        boolean setTask(ConflictInfo task) {
            return this.task.compareAndSet(null, task, false, true);
        }

        Runnable judgeWorker = () -> {
            try {
                while (true) {
                    if (task.getReference() != null) {
                        if (task.getReference().apply()) {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));

                            ConflictInfo conflict = task.getReference();

                            /*List<Action> teamOneActions = judgeStore.getTeamList(conflict.textId, 1);
                            List<Action> teamTwoActions = judgeStore.getTeamList(conflict.textId, 2);
                            List<Integer> decisions = judgeStore.getDecisionList(conflict.textId);*/

                        }
                    } else {
                        Thread.sleep(1000);
                    }
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        };

    }



}
