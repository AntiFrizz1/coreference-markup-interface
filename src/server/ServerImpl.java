package server;

import chain.Action;
import document.ConflictInfo;
import document.Data;
import document.UpdateDocument;
import javafx.util.Pair;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.stream.Collectors;

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
    private Set<JudgeInfo> judges;

    /**
     * List of workers
     */
    private List<Thread> workers;

    /**
     * List of conflicts
     */
    static List<Queue<ConflictInfo>> conflicts;

    static final char DELIMETER = '/';

    private volatile ServerStore serverStore;
    private volatile JudgeStore judgeStore;

    Map<Integer, Socket> idToSocket;

    Map<Socket, Integer> socketToId;

    Map<Socket, Integer> reconnectMap;

    Map<Integer, Integer> idToTextId;

    private Queue<Socket> reconnectQueue;

    AtomicBoolean work;

    volatile PrintWriter logWriter;

    Socket nullSocket = new Socket();

    String backupName;

    AtomicInteger textNumber = new AtomicInteger(0);
    AtomicBoolean needBackUp = new AtomicBoolean(false);

    public ServerImpl(int port, String prefixOld, String prefixNew) {
        this.port = port;
        try {
            socket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        work = new AtomicBoolean(true);
        texts = new CopyOnWriteArrayList<>();

        users = new ConcurrentLinkedQueue<>();
        judges = new CopyOnWriteArraySet<>();
        clients = new ConcurrentLinkedQueue<>();
        offlineUsers = new ConcurrentLinkedQueue<>();
        onlineUsers = new ConcurrentLinkedQueue<>();
        workers = new CopyOnWriteArrayList<>();

        judgesId = new ConcurrentSkipListSet<>();

        serverStore = new ServerStore();
        judgeStore = new JudgeStore();


        idToSocket = new ConcurrentHashMap<>();
        socketToId = new ConcurrentHashMap<>();

        conflicts = new CopyOnWriteArrayList<>();

        reconnectMap = new ConcurrentHashMap<>();

        reconnectQueue = new ConcurrentLinkedQueue<>();

        idToTextId = new ConcurrentHashMap<>();


        File path = new File(prefixNew);
        path.mkdir();
        backupName = prefixNew;
        try {
            logWriter = new PrintWriter(prefixNew + DELIMETER + "server.log");
        } catch (FileNotFoundException e) {
            System.err.println("file " + prefixNew + DELIMETER + "server.log not found");
        }
        if (judgeRecover(prefixOld, prefixNew)) {
            if (serverRecover(prefixOld, prefixNew)) {
                if (finalRecover(prefixOld)) {
                    System.out.println("Successful reconnect server");
                } else {
                    System.err.println("Can't reconnect server: finalRecover error");
                }
            } else {
                System.err.println("Can't reconnect server: serverRecover error");
            }
        } else {
            System.err.println("Can't reconnect server: judgeRecover error");
        }
    }

    private Map<Integer, Integer> leaderBoard;
    private Map<Integer, String> idToUsername;

    public ServerImpl(int port) {
        this.port = port;
        try {
            socket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        work = new AtomicBoolean(true);
        texts = new CopyOnWriteArrayList<>();

        users = new ConcurrentLinkedQueue<>();
        judges = new CopyOnWriteArraySet<>();
        clients = new ConcurrentLinkedQueue<>();
        offlineUsers = new ConcurrentLinkedQueue<>();
        onlineUsers = new ConcurrentLinkedQueue<>();
        workers = new CopyOnWriteArrayList<>();

        judgesId = new ConcurrentSkipListSet<>();

        serverStore = new ServerStore();
        judgeStore = new JudgeStore();


        idToSocket = new ConcurrentHashMap<>();
        socketToId = new ConcurrentHashMap<>();

        conflicts = new CopyOnWriteArrayList<>();

        reconnectMap = new ConcurrentHashMap<>();

        reconnectQueue = new ConcurrentLinkedQueue<>();

        idToTextId = new ConcurrentHashMap<>();

        nullSocket = new Socket();

        leaderBoard = new ConcurrentHashMap<>();
        idToUsername = new ConcurrentHashMap<>();

        File path = new File("prefix");
        path.mkdir();
        backupName = "prefix";

        try {
            logWriter = new PrintWriter("prefix" + DELIMETER + "server.log");
        } catch (FileNotFoundException e) {
            System.err.println("file prefix" + DELIMETER + "server.log not found");
        }
        judgeStore.setJudgeWriter("prefix");
        serverStore.setServerWriter("prefix");
        backupInfo();
        System.out.println("Successful start server");
    }

    public void loadTexts(List<String> filenames) {
        for (String filename : filenames) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filename))));

                StringBuilder text = new StringBuilder();
                String help = "";

                while ((help = reader.readLine()) != null) {
                    text.append(help + " ");
                }

                texts.add(text.toString());
            } catch (FileNotFoundException e) {
                System.err.println("Can't find file : " + filename);
            } catch (IOException e) {
                System.err.println("Can't read file : " + filename + ". Error: " + e.getMessage());
            }
        }
    }

    Thread listenerThread;
    Thread schedulerThread;
    Thread userSchedulerThread;
    Thread onlineUsersSchedulerThread;
    Thread offlineUsersSchedulerThread;
    Thread serverStoreWorkerThread;
    Thread conflictInfoSchedulerThread;
    Thread reconnectWorkerThread;
    Thread backupThread;

    /**
     * Start server
     */
    public void run() {
        listenerThread = new Thread(listener);
        schedulerThread = new Thread(scheduler);
        userSchedulerThread = new Thread(userScheduler);
        onlineUsersSchedulerThread = new Thread(onlineUsersScheduler);
        offlineUsersSchedulerThread = new Thread(offlineUsersScheduler);
        serverStoreWorkerThread = new Thread(serverStore.worker);
        conflictInfoSchedulerThread = new Thread(conflictInfoScheduler);
        reconnectWorkerThread = new Thread(reconnectWorker);
        backupThread = new Thread(backupWorker);

        listenerThread.start();
        schedulerThread.start();
        userSchedulerThread.start();
        onlineUsersSchedulerThread.start();
        offlineUsersSchedulerThread.start();
        serverStoreWorkerThread.start();
        conflictInfoSchedulerThread.start();
        reconnectWorkerThread.start();
        backupThread.start();
        try {
            listenerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        work.compareAndSet(true, false);

        try {
            listenerThread.join();
            schedulerThread.join();
            userSchedulerThread.join();
            onlineUsersSchedulerThread.join();
            offlineUsersSchedulerThread.join();
            serverStoreWorkerThread.join();
            conflictInfoSchedulerThread.join();
        } catch (InterruptedException e) {
            System.err.println("close :=: Error: " + e.getMessage());
        }
    }

    /**
     * Connect clients to server
     */
    private Runnable listener = () -> {
        while (work.get()) {
            try {
                Socket client = socket.accept();
                System.out.println("listener :=: connected: " + client.toString());
                clients.add(client);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("listener :=: ServerSocket closed with error: " + e.getMessage());
        }
    };

    /**
     * Split clients on clients or judges
     */
    private Runnable scheduler = () -> {
        while (work.get()) {
            try {

                if (!clients.isEmpty()) {
                    Socket client = clients.poll();
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                        PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())));

                        String request = reader.readLine();
                        System.out.println("scheduler :=: Request from " + client.toString() + " = " + request);

                        int id = Integer.parseInt(request);

                        if (id > 100) {
                            writer.println("OK");
                            writer.flush();
                            judges.add(new JudgeInfo(client, judges.size()));

                        } else {
                            if (idToSocket.containsKey(id)) {
                                if (idToSocket.get(id) == nullSocket) {
                                    /*idToSocket.put(id, client);
                                    socketToId.put(client, id);*/
                                    writer.println("R");
                                    writer.flush();
                                    reconnectMap.put(client, id);
                                    users.add(client);
                                } else {
                                    writer.println("E");
                                    writer.flush();
                                }
                            } else {
                                writer.println("OK");
                                writer.flush();
                                idToSocket.put(id, client);
                                needBackUp.compareAndSet(false, true);
                                socketToId.put(client, id);
                                users.add(client);
                            }
                        }

                    } catch (Exception e) {
                        System.err.println("scheduler :=: Error: " + e.getMessage());
                    }
                } else {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                System.err.println("scheduler :=: scheduler interrupted: " + e.getMessage());
            }

        }
    };

    /**
     * Split users on online or offline users
     */
    private Runnable userScheduler = () -> {
        while (work.get()) {
            try {

                if (!users.isEmpty()) {
                    Socket client = users.poll();
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                        PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())));

                        String request = reader.readLine();
                        System.out.println("userScheduler :=: request from " + client.toString() + " = " + request);

                        int id = Integer.parseInt(request);

                        if (id == 0) {
                            if (reconnectMap.containsKey(client)) {
                                writer.println("OK");
                                writer.flush();
                                reconnectQueue.add(client);
                            } else {
                                writer.println("OK");
                                writer.flush();
                                onlineUsers.add(client);
                            }
                        } else {
                            writer.println("OK");
                            writer.flush();
                            offlineUsers.add(client);
                        }


                    } catch (Exception e) {
                        System.err.println("userScheduler :=: Error: " + e.getMessage());
                    }
                } else {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                System.err.println("userScheduler :=: userScheduler interrupted: " + e.getMessage());
            }
        }
    };

    private Runnable reconnectWorker = () -> {
        while (work.get()) {
            try {
                if (!reconnectQueue.isEmpty()) {
                    Socket client = reconnectQueue.poll();
                    int id = reconnectMap.get(client);
                    reconnectMap.remove(client);
                    int textId = idToTextId.get(id);

                    BufferedReader localReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    PrintWriter localWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())));

                    String fileName = backupName + DELIMETER + id + "text=" + textId;

                    BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), StandardCharsets.UTF_8));

                    List<Action> actionList = new ArrayList<>();

                    while (fileReader.ready()) {
                        actionList.add(new Action(fileReader.readLine()));
                    }

                    Data data = new Data(texts.get(textId), actionList);

                    localWriter.println(data.toString());
                    localWriter.flush();

                    idToSocket.put(id, client);
                    needBackUp.compareAndSet(false, true);
                    socketToId.put(client, id);

                } else {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                System.err.println("reconnectWorker :=: reconnectWorker interrupted: " + e.getMessage());
            } catch (IOException e) {
                System.err.println("reconnectWorker :=: Error: " + e.getMessage());
            }
        }
    };

    /**
     * Give task for online users
     */

    private Runnable onlineUsersScheduler = () -> {
        while (work.get()) {
            try {

                if (onlineUsers.size() >= 2) {
                    Socket client1 = onlineUsers.poll();
                    Socket client2 = onlineUsers.poll();
                    try {

                        int text = textNumber.get();

                        textNumber.getAndIncrement();

                        idToTextId.put(socketToId.get(client1), text);
                        idToTextId.put(socketToId.get(client2), text);

                        needBackUp.compareAndSet(false, true);

                        BufferedReader reader1 = new BufferedReader(new InputStreamReader(client1.getInputStream()));
                        BufferedReader reader2 = new BufferedReader(new InputStreamReader(client2.getInputStream()));

                        PrintWriter writer1 = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client1.getOutputStream())));
                        PrintWriter writer2 = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client2.getOutputStream())));
                        writer1.println(texts.get(text));
                        writer2.println(texts.get(text));

                        writer1.flush();
                        writer2.flush();


                        serverStore.addNewGame(socketToId.get(client1), socketToId.get(client2), text, backupName);
                        judgeStore.addNewGame(socketToId.get(client1), socketToId.get(client2), text, backupName);

                        conflicts.add(new ConcurrentLinkedQueue<>());

                        Thread thread1 = new Thread(() -> {
                            int localText = text;
                            int id = socketToId.get(client1);
                            while (true) {
                                try {
                                    if (idToSocket.get(id) != nullSocket) {
                                        BufferedReader localReader = new BufferedReader(new InputStreamReader(idToSocket.get(id).getInputStream()));
                                        PrintWriter localWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(idToSocket.get(id).getOutputStream())));
                                        String request = localReader.readLine();
                                        if (request == null) {
                                            idToSocket.put(id, nullSocket);
                                            needBackUp.compareAndSet(false, true);
                                            socketToId.remove(client1);
                                            continue;
                                        } else if (request.equals("EXIT")) {
                                            idToTextId.remove(id);
                                            socketToId.remove(client1);
                                            idToSocket.remove(id);
                                            break;
                                        }
                                        UpdateDocument document = new UpdateDocument(request);
                                        /*serverStore.putActions(document.getActions(), text, 1);*/
                                        serverStore.putActions(document.getActions(), localText, 1);
                                    }
                                } catch (IOException e) {
                                    idToSocket.put(id, nullSocket);
                                    needBackUp.compareAndSet(false, true);
                                    socketToId.remove(client1);
                                    System.err.println("onlineUsersScheduler[client1, id = " + socketToId.get(client1) + "] :=: Error: " + e.getMessage());
                                }
                            }
                        });

                        Thread thread2 = new Thread(() -> {
                            int localText = text;
                            int id = socketToId.get(client2);
                            while (true) {
                                try {
                                    if (idToSocket.get(id) != nullSocket) {
                                        BufferedReader localReader = new BufferedReader(new InputStreamReader(idToSocket.get(id).getInputStream()));
                                        PrintWriter localWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(idToSocket.get(id).getOutputStream())));
                                        String request = localReader.readLine();
                                        if (request == null) {
                                            idToSocket.put(id, nullSocket);
                                            needBackUp.compareAndSet(false, true);
                                            socketToId.remove(client2);
                                            continue;
                                        } else if (request.equals("EXIT")) {
                                            idToTextId.remove(id);
                                            socketToId.remove(client2);
                                            idToSocket.remove(id);
                                            break;
                                        }
                                        UpdateDocument document = new UpdateDocument(request);
                                        /*serverStore.putActions(document.getActions(), text, 1);*/
                                        serverStore.putActions(document.getActions(), localText, 2);
                                    }
                                } catch (IOException e) {
                                    idToSocket.put(id, nullSocket);
                                    needBackUp.compareAndSet(false, true);
                                    socketToId.remove(client2);
                                    System.err.println("onlineUsersScheduler[client2, id = " + id + "] :=: Error: " + e.getMessage());

                                }
                            }
                        });

                        thread1.start();
                        thread2.start();
                        workers.add(thread1);
                        workers.add(thread2);

                    } catch (IOException e) {
                        System.err.println("onlineUsersScheduler :=: Error: " + e.getMessage());
                    }
                } else {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                System.out.println("onlineUsersScheduler :=: onlineUsersScheduler interrupted : " + e.getMessage());
            }
        }
    };

    /**
     * Give task for offline users
     */
    private Runnable offlineUsersScheduler = () -> {
        while (work.get()) {
            try {

                if (!offlineUsers.isEmpty()) {
                    Socket client = offlineUsers.poll();
                    System.out.println("offlineUsersScheduler :=: " + client.toString());
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                        PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())));
                        int text = textNumber.get();

                        textNumber.getAndIncrement();

                        writer.println(texts.get(text));
                        writer.flush();
                        Thread worker = new Thread(() -> {
                            try {
                                String doc = reader.readLine();
                                UpdateDocument document = new UpdateDocument(doc);
                                List<Action> answer = document.getActions();
                                //save data

                            } catch (IOException e) {
                                System.out.println("offlineUsersScheduler[worker] :=: Error: " + e.getMessage());
                            }
                        });
                        worker.run();
                        workers.add(worker);
                    } catch (IOException e) {
                        System.out.println("offlineUsersScheduler :=: Error: " + e.getMessage());
                    }
                } else {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                System.out.println("offlineUsersScheduler :=: offlineUsersScheduler interrupted : " + e.getMessage());
            }
        }
    };

    /*private Runnable addTaskWorker = () -> {
        while (work.get() || !tasks.isEmpty()) {
            try {
                if (!tasks.isEmpty()) {
                    AddTask addTask = tasks.poll();
                    if (!serverStore.putActions(addTask.getActionList(), addTask.getTextNum(), addTask.getTeamNum())) {
                        tasks.add(addTask);
                    }
                } else {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                System.out.println("addTaskWorker :=: addTaskWorker interrupted : " + e.getMessage());
            }
        }
    };*/

    private Runnable conflictInfoScheduler = () -> {
        while (!conflicts.isEmpty() || work.get()) {
            try {
                if (!conflicts.isEmpty()) {
                    for (int i = 0; i < conflicts.size(); i++) {
                        Queue<ConflictInfo> conflictInfoQueue = conflicts.get(i);
                        if (!conflictInfoQueue.isEmpty()) {
                            ConflictInfo conflict = conflictInfoQueue.peek();
                            if (conflict.status.get() == 0) {
                                boolean f = false;
                                List<JudgeInfo> judgeInfoList;
                                synchronized (judges) {
                                    judgeInfoList = new ArrayList<>(judges);
                                }
                        /*logWriter.println(judgeInfoList.stream().map(judgeInfo -> socket.toString()).collect(Collectors.joining("----")));
                        logWriter.flush();*/
                                for (JudgeInfo judgeInfo : judgeInfoList) {
                                    if (!judgeInfo.task.isMarked()) {
                                        if (judgeInfo.setTask(conflict)) {

                                    /*logWriter.println("get task" + judges.get(j).socket + " " + conflict.textId + " " + conflict.teamOneId + " " + conflict.teamTwoId);
                                    logWriter.flush();*/
                                            f = true;
                                            break;
                                        }
                                    }
                                }
                                //System.out.println("not get " + conflict.textId + " " + conflict.teamOneId + " " + conflict.teamTwoId);
                            } else if (conflict.status.get() == 1) {
                                //System.out.println("in process " + conflict.textId + " " + conflict.teamOneId + " " + conflict.teamTwoId);
                            } else {
                                conflictInfoQueue.poll();
                            }
                        }
                    }
                } else {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                System.out.println("conflictInfoScheduler :=: conflictInfoScheduler interrupted : " + e.getMessage());
            }
        }
    };

    private Runnable leaderBoardRunnable = () -> {
        while (true) {
            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<!DOCTYPE html>\n" +
                    "<html lang=\"ru\">\n" +
                    "<head>\n" +
                    "   <meta charset=\"UTF-8\">\n" +
                    "   <title>Таблица Лидеров</title>\n" +
                    "   <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                    "   <link rel=\"stylesheet\" href=\"style.css\">" +
                    "    " +
                    "</head>\n" +
                    "<body>" +
                    "<div class=\"div-table\">\n" +
                    "   <h1>Таблица лидеров</h1>" +
                    "   <table class=\"table\">\n" +
                    "       <thead>\n" +
                    "           <tr>\n" +
                    "               <th>Название команды</th>\n" +
                    "               <th>Количество очков</th>\n" +
                    "           </tr>\n" +
                    "       </thead>\n" +
                    "       <tbody id=\"table-body\">\n");
            List<Pair<Integer, Integer>> local = leaderBoard.entrySet().stream()
                    .map(k -> new Pair<>(k.getKey(), k.getValue())).sorted(this::comparePairs).collect(Collectors.toList());
            Collections.reverse(local);
            for (int i = 0; i < local.size(); i++) {
                htmlBuilder.append(
                        "       <tr>\n" +
                                "           <td>").append(idToUsername.get(local.get(i).getKey())).append("</td>\n").append(
                        "           <td>").append(local.get(i).getValue()).append("</td>\n").append(
                        "       </tr>\n");
            }

            htmlBuilder.append(
                    "       </tbody>\n" +
                            "   </table>\n" +
                            "</div>\n" +
                            "</body>\n" +
                            "</html>\n");
            try {
                PrintWriter writer = new PrintWriter("leaderboard.html");
                writer.println(htmlBuilder.toString());
                writer.flush();
                writer.close();
                Thread.sleep(2000);
            } catch (InterruptedException | FileNotFoundException e) {
                System.err.println("leaderBoardRunnable :=: Error :" + e.getMessage());
                break;
            }
        }
    };
    private int comparePairs(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
        if(o1.getValue() > o2.getValue()) {
            return 1;
        } else if(o1.getValue() == o2.getValue()) {
            return  0;
        } else {
            return -1;
        }
    }

    AtomicBoolean down = new AtomicBoolean(false);
    Random random = new Random();

    class JudgeInfo {
        Socket socket;
        int index;
        /**
         * reference has mark set false if judge is free otherwise true
         */
        AtomicMarkableReference<ConflictInfo> task;
        Thread worker;

        JudgeInfo(Socket socket, int index) throws IOException {
            this.index = index;
            this.socket = socket;
            task = new AtomicMarkableReference<>(null, false);
            worker = new Thread(judgeWorker);
            try {
                PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
                writer.println(texts.size());
                writer.flush();
                for (String string: texts) {
                    writer.println(string);
                    writer.flush();
                }
            } catch (IOException e) {
                System.out.println("Can't send texts to judge" + socket.toString());
                throw e;
            }
            worker.start();
        }

        boolean setTask(ConflictInfo task) {
            return this.task.compareAndSet(null, task, false, true);
        }

        Runnable judgeWorker = () -> {
            try {
                while (true) {
                    try {
                        if (task.isMarked()) {
                            if (task.getReference().apply()) {
                                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));

                                ConflictInfo conflict = task.getReference();

                                List<Action> teamOneActions = judgeStore.getTeamList(conflict.textId, 1);
                                List<Action> teamTwoActions = judgeStore.getTeamList(conflict.textId, 2);
                                List<Integer> decisions = judgeStore.getDecisionList(conflict.textId);

                                Action action1 = conflict.action1;
                                Action action2 = conflict.action2;

                                List<Action> toJudgeAboutTeamOne = new CopyOnWriteArrayList<>();
                                List<Action> toJudgeAboutTeamTwo = new CopyOnWriteArrayList<>();

                                toJudgeAboutTeamOne.add(action1);
                                toJudgeAboutTeamTwo.add(action2);

                                if (!action1.isEmpty()) {
                                    for (int i = teamOneActions.size() - 1; i >= 0 && i >= teamOneActions.size() - 100 &&
                                            toJudgeAboutTeamOne.size() < 8; i--) {
                                        if (!teamOneActions.get(i).isEmpty() && (decisions.get(i) == 1 || decisions.get(i) == 3)) {
                                            toJudgeAboutTeamOne.add(0, teamOneActions.get(i));
                                        }
                                    }
                                }

                                if (!action2.isEmpty()) {
                                    for (int i = teamTwoActions.size() - 1; i >= 0 && i >= teamTwoActions.size() - 100 &&
                                            toJudgeAboutTeamTwo.size() < 8; i--) {
                                        if (!teamTwoActions.get(i).isEmpty() && (decisions.get(i) == 2 || decisions.get(i) == 3)) {
                                            toJudgeAboutTeamTwo.add(0, teamTwoActions.get(i));
                                        }
                                    }
                                }

                                /*if ((random.nextInt() % 3 == 0) && down.compareAndSet(false, true)) {
                                    System.out.println(socket.toString() + "down");
                                    socket.close();
                                }*/

                                /*logWriter.println("send task " + socket.toString());
                                logWriter.flush();*/

                                UpdateDocument teamOne = new UpdateDocument(toJudgeAboutTeamOne);
                                UpdateDocument teamTwo = new UpdateDocument(toJudgeAboutTeamTwo);

                                if (teamOne.pack() == null) {
                                    logWriter.println("null1" + socket.toString());
                                    logWriter.flush();
                                }

                                writer.println(teamOne.pack());
                                writer.flush();
                                /*Thread.sleep(1000);*/
                                if (teamTwo.pack() == null) {
                                    logWriter.println("null2" + socket.toString());
                                    logWriter.flush();
                                }

                                writer.println(teamTwo.pack());
                                writer.flush();


                                writer.println(conflict.textId);
                                writer.flush();

                                String request = reader.readLine();
                                if (request == null) {
                                    logWriter.println("done judge" + socket.toString());
                                    logWriter.flush();
                                    synchronized (judges) {
                                        socket.close();
                                        judges.remove(this);
                                    }
                                    break;
                                }
                            /*logWriter.println("get decision from judge" + socket.toString());
                            logWriter.flush();*/
                                int decision = Integer.parseInt(request);
                                judgeStore.putOneAction(action1, action2, conflict.textId, decision);

                                if (conflict.complete()) {
                                    logWriter.println("judge" + socket.toString() + " complete task");
                                    logWriter.flush();
                                }

                                task.compareAndSet(conflict, null, true, false);
                            }
                        } else {
                            Thread.sleep(1000);
                        }
                    } catch (IOException e) {
                        logWriter.println("done judge" + socket.toString());
                        logWriter.flush();
                        synchronized (judges) {
                            judges.remove(this);

                        }
                        System.err.println("judgeWorker :=: Error: " + e.getMessage());
                        break;
                    }
                }
            } catch (InterruptedException e) {
                System.err.println("judgeWorker" + socket.toString() + " :=: Error: " + e.getMessage());
            }
        };

    }

    void backupInfo() {
        PrintWriter backupWriter;
        try {
            backupWriter = new PrintWriter(backupName + DELIMETER + "backupInfo");
            backupWriter.println(textNumber);
            backupWriter.flush();
            Set<Integer> set = idToSocket.keySet();
            backupWriter.println(set.stream().map(Objects::toString).collect(Collectors.joining(" ")));
            backupWriter.flush();
            backupWriter.println(idToTextId.size());
            backupWriter.flush();
            idToTextId.forEach((k, v) -> {
                backupWriter.println(k + " " + v);
                backupWriter.flush();
            });
        } catch (FileNotFoundException e) {
            System.err.println("Can't find file " + backupName + DELIMETER + "backupInfo");
        }
    }

    private Runnable backupWorker = () -> {
        while (work.get()) {
            if (needBackUp.compareAndSet(true, false)) {
                backupInfo();
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public boolean finalRecover(String prefixOld) {
        BufferedReader backupReader;
        try {
            backupReader = new BufferedReader(new InputStreamReader(new FileInputStream(prefixOld + DELIMETER + "backupInfo"), "UTF-8"));
            String request = backupReader.readLine();
            textNumber.set(Integer.parseInt(request));
            request = backupReader.readLine();
            String[] ids = request.split(" ");
            for (String id : ids) {
                idToSocket.put(Integer.parseInt(id), nullSocket);
            }
            request = backupReader.readLine();
            int size = Integer.parseInt(request);
            for (int i = 0; i < size; i++) {
                request = backupReader.readLine();
                String[] nums = request.split(" ");
                idToTextId.put(Integer.parseInt(nums[0]), Integer.parseInt(nums[1]));
            }
            backupInfo();
            return true;
        } catch (Exception e) {
            System.err.println("Can't find file " + prefixOld + DELIMETER + "backupInfo");
            return false;
        }
    }

    class JudgeStoreFile {
        int id1;
        int id2;
        int textId;

        JudgeStoreFile(int id1, int id2, int textId) {
            this.id1 = id1;
            this.id2 = id2;
            this.textId = textId;
        }
    }

    public boolean judgeRecover(String prefixOld, String prefixNew) {
        BufferedReader gameReader;
        try {
            gameReader = new BufferedReader(new InputStreamReader(new FileInputStream(prefixOld + DELIMETER + "judgeStoreGames"), "UTF-8"));
            //gameReader = new BufferedReader(new FileReader("judgeStoreGames"));
            String fileName = gameReader.readLine();
            List<JudgeStoreFile> judgeStoreFiles = new ArrayList<>(0);
            while (fileName != null) {
                List<String> ids = Arrays.asList(fileName.split("vs"));
                List<String> ids2 = Arrays.asList(ids.get(1).split("text="));
                JudgeStoreFile tmp = new JudgeStoreFile(Integer.parseInt(ids.get(0)), Integer.parseInt(ids2.get(0)), Integer.parseInt(ids2.get(1)));
                judgeStoreFiles.add(tmp);
                fileName = gameReader.readLine();
            }
            judgeStoreFiles.sort(this::judgeStoreCompare);
            judgeStore.setJudgeWriter(prefixNew);
            for (JudgeStoreFile judgeStoreFile : judgeStoreFiles) {
                String file = prefixOld + DELIMETER + judgeStoreFile.id1 + "vs" + judgeStoreFile.id2 + "text=" + judgeStoreFile.textId;
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
                //BufferedReader reader = new BufferedReader(new FileReader(file));
                String line = reader.readLine();
                List<Action> teamOneActions = new CopyOnWriteArrayList<>();
                List<Action> teamTwoActions = new CopyOnWriteArrayList<>();
                List<Integer> decisions = new CopyOnWriteArrayList<>();
                PrintWriter writer = new PrintWriter(prefixNew + DELIMETER + judgeStoreFile.id1 + "vs" + judgeStoreFile.id2 + "text=" + judgeStoreFile.textId);
                while (line != null) {
                    List<String> list = Arrays.asList(line.split("@"));
                    teamOneActions.add(new Action(list.get(0)));
                    teamTwoActions.add(new Action(list.get(1)));
                    decisions.add(Integer.parseInt(list.get(2)));
                    line = reader.readLine();
                    writer.println(teamOneActions.get(teamOneActions.size() - 1).pack() + "@" + teamTwoActions.get(teamTwoActions.size() - 1).pack() + "@" + decisions.get(decisions.size() - 1));
                    writer.flush();
                }
                judgeStore.addNewRecoverGame(judgeStoreFile.id1, judgeStoreFile.id2, judgeStoreFile.textId, teamOneActions, teamTwoActions, decisions, writer);
                judgeStore.dumpWriter.println(judgeStoreFile.id1 + "vs" + judgeStoreFile.id2 + "text=" + judgeStoreFile.textId);
                judgeStore.dumpWriter.flush();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public int judgeStoreCompare(JudgeStoreFile tmp1, JudgeStoreFile tmp2) {
        return Integer.compare(tmp1.textId, tmp2.textId);
    }

    class ServerStoreFile {
        int idOne;
        int idTwo;
        int textId;

        ServerStoreFile(int idOne, int idTwo, int textId) {
            this.idOne = idOne;
            this.idTwo = idTwo;
            this.textId = textId;
        }
    }

    public int compareSS(ServerStoreFile o1, ServerStoreFile o2) {
        return Integer.compare(o1.textId, o2.textId);
    }

    public boolean serverRecover(String prefixOld, String prefixNew) {
        try (BufferedReader readerG = new BufferedReader(new FileReader(prefixOld + DELIMETER + "gamesServer"))) {
            String firstFile, secondFile;
            ArrayList<ServerStoreFile> files = new ArrayList<>();
            while ((firstFile = readerG.readLine()) != null) {
                secondFile = readerG.readLine();
                String[] splittedFirst = firstFile.split("text=");
                String[] splittedSecond = secondFile.split("text=");
                files.add(new ServerStoreFile(Integer.valueOf(splittedFirst[0]), Integer.valueOf(splittedSecond[0]), Integer.valueOf(splittedFirst[1])));
            }
            files.sort(this::compareSS);
            serverStore.setServerWriter(prefixNew);
            for (ServerStoreFile ssf : files) {
                try (BufferedReader readerFirst = new BufferedReader(new FileReader(prefixOld + DELIMETER + ssf.idOne + "text=" + ssf.textId));
                     BufferedReader readerSecond = new BufferedReader(new FileReader(prefixOld + DELIMETER + ssf.idTwo + "text=" + ssf.textId));
                     PrintWriter writerFirst = new PrintWriter(prefixNew + DELIMETER + ssf.idOne + "text=" + ssf.textId);
                     PrintWriter writerSecond = new PrintWriter(prefixNew + DELIMETER + ssf.idTwo + "text=" + ssf.textId)) {
                    ArrayList<Action> listFirst = new ArrayList<>();
                    ArrayList<Action> listSecond = new ArrayList<>();
                    String input;
                    while ((input = readerFirst.readLine()) != null) {
                        listFirst.add(new Action(input));
                        writerFirst.println(listFirst.get(listFirst.size() - 1).pack());
                        writerFirst.flush();
                    }
                    while ((input = readerSecond.readLine()) != null) {
                        listSecond.add(new Action(input));
                        writerSecond.println(listSecond.get(listSecond.size() - 1).pack());
                        writerSecond.flush();
                    }
                    List<Action> toDeleteFirst = new ArrayList<>();
                    List<Action> toDeleteSecond = new ArrayList<>();
                    for (int j = 0; j < judgeStore.games.size(); j++) {
                        if (judgeStore.games.get(j).teamOneId == ssf.idOne && judgeStore.games.get(j).teamTwoId == ssf.idTwo) {
                            toDeleteFirst = judgeStore.games.get(j).teamOneApproved;
                            toDeleteSecond = judgeStore.games.get(j).teamTwoApproved;
                            break;
                        } else if (judgeStore.games.get(j).teamOneId == ssf.idTwo && judgeStore.games.get(j).teamTwoId == ssf.idOne) {
                            toDeleteFirst = judgeStore.games.get(j).teamTwoApproved;
                            toDeleteSecond = judgeStore.games.get(j).teamOneApproved;
                            break;
                        }
                    }
                    listFirst.removeAll(toDeleteFirst);
                    listSecond.removeAll(toDeleteSecond);
                    serverStore.addNewGame(ssf.idOne, ssf.idTwo, ssf.textId, listFirst, listSecond, writerFirst, writerSecond);
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                serverStore.writer.println(ssf.idOne + "text=" + ssf.textId);
                serverStore.writer.flush();
                serverStore.writer.println(ssf.idTwo + "text=" + ssf.textId);
                serverStore.writer.flush();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}