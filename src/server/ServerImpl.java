package server;

import chain.Chain;
import document.Document;
import document.DocumentImpl;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicReference;

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
    private List<List<String>> texts;

    /**
     * Pool of clients.
     */
    private List<Socket> clients;

    /**
     * Pool of users.
     */
    private List<Socket> users;

    /**
     * Pool of users.
     */
    private List<Socket> onlineUsers;

    /**
     * Pool of users.
     */
    private List<Socket> offlineUsers;

    /**
     * Set of judges id.
     */
    private Set<Integer> judgesId;

    /**
     * Pool of judges.
     */
    private List<JudgeInfo> judges;

    /**
     * Set of selected texts
     */
    private Set<Integer> selectedTexts;

    /**
     * List of workers
     */
    private List<Thread> workers;

    /**
     * List of conflicts
     */
    private List<ConflictInfo> conflicts;


    public ServerImpl() {
        try {
            socket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        users = new CopyOnWriteArrayList<>();
        judges = new CopyOnWriteArrayList<>();
        clients = new CopyOnWriteArrayList<>();
        offlineUsers = new CopyOnWriteArrayList<>();
        onlineUsers = new CopyOnWriteArrayList<>();
        workers = new CopyOnWriteArrayList<>();

        judgesId = new ConcurrentSkipListSet<>();
        selectedTexts = new ConcurrentSkipListSet<>();
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

        listenerThread.start();
        schedulerThread.start();
        userSchedulerThread.start();
        onlineUsersSchedulerThread.start();
        offlineUsersSchedulerThread.start();

        listenerThread.join();
        schedulerThread.join();
        userSchedulerThread.join();
        onlineUsersSchedulerThread.join();
        offlineUsersSchedulerThread.join();
    }

    /**
     * Connect clients to server
     */
    private Runnable listener = () -> {
        while (true) {
            try {
                Socket client = socket.accept();
                System.out.println("listener :=: connected: " + client.toString());
                clients.add(client);
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
                    Socket client = clients.remove(0);
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                        PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())));

                        String request = reader.readLine();
                        System.out.println("scheduler :=: request from " + client.toString() + " = " + request);

                        int id = Integer.parseInt(request);
                        if (judgesId.contains(id)) {
                            judges.add(new JudgeInfo(client));
                        } else {
                            users.add(client);
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
                    Socket client = users.remove(0);
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                        PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())));

                        String request = reader.readLine();
                        System.out.println("userScheduler :=: request from " + client.toString() + " = " + request);

                        int id = Integer.parseInt(request);
                        if (id == 0) {
                            onlineUsers.add(client);
                        } else {
                            offlineUsers.add(client);
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
        int textNumber = 0;
        try {
            while (true) {
                if (onlineUsers.size() >= 2) {
                    Socket client1 = onlineUsers.remove(0);
                    Socket client2 = onlineUsers.remove(0);
                    Thread worker = new Thread(() -> {
                        try {
                            DocumentImpl document1;
                            DocumentImpl document2;

                            BufferedReader reader1 = new BufferedReader(new InputStreamReader(client1.getInputStream()));
                            BufferedReader reader2 = new BufferedReader(new InputStreamReader(client2.getInputStream()));

                            PrintWriter writer1 = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client1.getOutputStream())));
                            PrintWriter writer2 = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client2.getOutputStream())));

                            writer1.println(/*document.pack()*/);
                            writer2.println(/*document.pack()*/);

                            writer1.flush();
                            writer2.flush();

                            Thread thread1 = new Thread(() -> {
                                while (true) {
                                    try {
                                        String request = reader1.readLine();
                                        //process data
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                            Thread thread2 = new Thread(() -> {
                                while (true) {
                                    try {
                                        String request = reader2.readLine();
                                        //process data
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
        try {
            while (true) {
                if (!offlineUsers.isEmpty()) {
                    Socket client = offlineUsers.remove(0);
                    System.out.println("offlineUsersScheduler :=: " + client.toString());
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                        PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())));

                        writer.println("doc");
                        writer.flush();
                        Thread worker = new Thread(() -> {
                            try {
                                String doc = reader.readLine();
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

    /*Runnable judgeScheduler = () -> {
        try {
            while (true) {

            }
        }
    };*/

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
                        task.getReference().apply();
                        //process
                    } else {
                        Thread.sleep(1000);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

    }

    /**
     * Consist data about conflict
     */
    class ConflictInfo {
        Chain chain1;
        Chain chain2;
        AtomicInteger status;
        Thread counter;

        ConflictInfo(Chain chain1, Chain chain2) {
            this.chain1 = chain1;
            this.chain2 = chain2;
            this.status = new AtomicInteger(0);
        }

        boolean complete() {
            return status.compareAndSet(1, 2);
        }

        boolean apply() {
            if (status.compareAndSet(0, 1)) {
                counter = new Thread(() -> {
                    while(status.get() != 2) {
                        try {
                            Thread.sleep(1000000);
                            int localStatus = status.get();
                            if(localStatus == 1) {
                                status.compareAndSet(localStatus, 0);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
                return true;
            } else {
                return false;
            }
        }
    }

}
