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

    private Queue<AddTask> tasks;
    
    
    private volatile ServerStore serverStore;
    private volatile JudgeStore judgeStore;


    Map<Integer, Socket> idToSocket;

    Map<Socket, Integer> socketToId;
    
    Map<Socket, Integer> reconnectMap;

    Map<Integer, Integer> idToTextId;
    
    private Queue<Socket> reconnectQueue;
    
    AtomicBoolean work;

    volatile PrintWriter logWriter;

    Socket nullSocket;

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
        tasks = new ConcurrentLinkedQueue<>();
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

        try {
            logWriter = new PrintWriter("server.log");
        } catch (FileNotFoundException e) {
            System.err.println("file server.log not found");
        }
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

        listenerThread.start();
        schedulerThread.start();
        userSchedulerThread.start();
        onlineUsersSchedulerThread.start();
        offlineUsersSchedulerThread.start();
        serverStoreWorkerThread.start();
        conflictInfoSchedulerThread.start();
        reconnectWorkerThread.start();
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

                    String fileName = id + "text=" + textId;

                    BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), StandardCharsets.UTF_8));

                    List<Action> actionList = new ArrayList<>();

                    while (fileReader.ready()) {
                        actionList.add(new Action(fileReader.readLine()));
                    }

                    Data data = new Data(texts.get(textId), actionList);

                    localWriter.println(data.toString());
                    localWriter.flush();

                    idToSocket.put(id, client);
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
        AtomicInteger textNumber = new AtomicInteger();
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

                        BufferedReader reader1 = new BufferedReader(new InputStreamReader(client1.getInputStream()));
                        BufferedReader reader2 = new BufferedReader(new InputStreamReader(client2.getInputStream()));

                        PrintWriter writer1 = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client1.getOutputStream())));
                        PrintWriter writer2 = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client2.getOutputStream())));
                        writer1.println(texts.get(text));
                        writer2.println(texts.get(text));

                        writer1.flush();
                        writer2.flush();

                        serverStore.addNewGame(socketToId.get(client1), socketToId.get(client2), text);
                        judgeStore.addNewGame(socketToId.get(client1), socketToId.get(client2), text);
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
        int textNumber = 0;
        while (work.get()) {
            try {

                if (!offlineUsers.isEmpty()) {
                    Socket client = offlineUsers.poll();
                    System.out.println("offlineUsersScheduler :=: " + client.toString());
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                        PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())));
                        int text = textNumber;

                        textNumber++;

                        if (textNumber > texts.size()) {
                            textNumber = 0;
                        }

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

    public void judgeRecover() {
        BufferedReader gameReader;
        try {
            gameReader = new BufferedReader(new InputStreamReader(new FileInputStream("judgeStoreGames"), "UTF-8"));
            String fileName = gameReader.readLine();
            List<JudgeStoreFile> judgeStoreFiles = new ArrayList<>(0);
            while (fileName != null) {
                List<String> ids = Arrays.asList(fileName.split(".*vs.*text=.*"));
                JudgeStoreFile tmp = new JudgeStoreFile(Integer.parseInt(ids.get(0)), Integer.parseInt(ids.get(1)), Integer.parseInt(ids.get(2)));
                judgeStoreFiles.add(tmp);
                fileName = gameReader.readLine();
            }
            judgeStoreFiles.sort(this::judgeStoreCompare);
            for (JudgeStoreFile judgeStoreFile : judgeStoreFiles) {
                String file = judgeStoreFile.id1 + "vs" + judgeStoreFile.id2 + "text=" + judgeStoreFile.textId;
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
                String line = reader.readLine();
                List<Action> teamOneActions = new CopyOnWriteArrayList<>();
                List<Action> teamTwoActions = new CopyOnWriteArrayList<>();
                List<Integer> decisions = new CopyOnWriteArrayList<>();
                while (line != null) {
                    List<String> list = Arrays.asList(line.split("$"));
                    teamOneActions.add(new Action(list.get(0)));
                    teamTwoActions.add(new Action(list.get(1)));
                    decisions.add(Integer.parseInt(list.get(2)));
                    line = reader.readLine();
                }
                judgeStore.addNewRecoverGame(judgeStoreFile.id1, judgeStoreFile.id2, judgeStoreFile.textId, teamOneActions, teamTwoActions, decisions, fileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int judgeStoreCompare(JudgeStoreFile tmp1, JudgeStoreFile tmp2) {
        return Integer.compare(tmp1.textId, tmp2.textId);
    }

}
