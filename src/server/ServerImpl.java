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
 * @see Server
 */
public class ServerImpl implements Server {
    /**
     * Server's port for user connection.
     */
    private int portForUser;

    /**
     * Server's port for judge connection.
     */
    private int portForJudge;

    /**
     * An internal endpoint for sending or receiving data from user.
     */
    private ServerSocket socketForUsers;

    /**
     * An internal endpoint for sending or receiving data from judge.
     */
    private ServerSocket socketForJudges;

    /**
     * Texts.
     */
    private List<String> texts;

    /**
     * Pool of clients.
     */
    private Queue<Socket> clients;

    /**
     * Pool of clients.
     */
    private Queue<Socket> judgesQueue;

    /**
     * Pool of judges.
     */
    private Set<JudgeInfo> judges;

    /**
     * List of conflicts
     */
    static List<Queue<ConflictInfo>> conflicts;

    private Map<String, Integer> idToLocalServerId; // NEED
    private Map<Integer, String> localServerIdToId; // NEED BY FIRST

    private Map<Integer, Integer> idToTextId; // NEED

    private Map<Integer, AtomicInteger> idToStatus;

    private Map<Integer, Socket> idToSocket;

    private Map<Integer, Thread> idToThread;

    private Queue<Pair<Integer, Socket>> reconnectQueue;

    private Queue<Pair<Integer, Socket>> connectedUsers;

    private Map<String, Integer> idToUsername; // NEED

    static final char DELIMITER = '/';

    private volatile ServerStore serverStore;
    private volatile JudgeStore judgeStore;

    private static volatile PrintWriter logWriter;

    Socket nullSocket = new Socket();
    Thread nullThread = new Thread();

    private String backupName;

    private AtomicInteger textNumber = new AtomicInteger(0);

    private AtomicInteger clientNumber = new AtomicInteger(0);

    private AtomicBoolean needBackUp = new AtomicBoolean(false);
    private AtomicBoolean leaderBoardNeed = new AtomicBoolean(false);


    private Map<String, Integer> leaderBoard;


    public ServerImpl(int portForUser, int portForJudge, String prefixOld, String prefixNew) {
        logFileInitialize();
        this.portForJudge = portForJudge;
        this.portForUser = portForUser;
        try {
            socketForUsers = new ServerSocket(portForUser);
            socketForJudges = new ServerSocket(portForJudge);
        } catch (IOException e) {
            log("ServerImplRecover", e.getMessage());
        }

        texts = new CopyOnWriteArrayList<>();

        judges = new CopyOnWriteArraySet<>();
        clients = new ConcurrentLinkedQueue<>();
        judgesQueue = new ConcurrentLinkedQueue<>();

        serverStore = new ServerStore();
        judgeStore = new JudgeStore();

        conflicts = new CopyOnWriteArrayList<>();

        leaderBoard = new ConcurrentHashMap<>();

        idToSocket = new ConcurrentHashMap<>();
        idToLocalServerId = new ConcurrentHashMap<>();
        localServerIdToId = new ConcurrentHashMap<>();
        idToTextId = new ConcurrentHashMap<>();
        idToThread = new ConcurrentHashMap<>();
        idToStatus = new ConcurrentHashMap<>();
        idToUsername = new ConcurrentHashMap<>();

        reconnectQueue = new ConcurrentLinkedQueue<>();
        connectedUsers = new ConcurrentLinkedQueue<>();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("users"),
                    StandardCharsets.UTF_8));
            while (reader.ready()) {
                String request = reader.readLine();
                String[] str = request.split("\\|");
            }
        } catch (IOException e) {
            log("ServerImplRecover", e.getMessage());
        }
        File path = new File(prefixNew);
        path.mkdir();
        backupName = prefixNew;
        if (judgeRecover(prefixOld, prefixNew)) {
            if (serverRecover(prefixOld, prefixNew)) {
                if (finalRecover(prefixOld)) {
                    log("ServerImplRecover", "Successful reconnect server");
                } else {
                    log("ServerImplRecover", "Can't reconnect server: finalRecover error");
                }
            } else {
                log("ServerImplRecover", "Can't reconnect server: serverRecover error");
            }
        } else {
            log("ServerImplRecover", "Can't reconnect server: judgeRecover error");
        }
    }


    public ServerImpl(int portForUser, int portForJudge) {
        logFileInitialize();
        this.portForJudge = portForJudge;
        this.portForUser = portForUser;

        try {
            socketForUsers = new ServerSocket(portForUser);
            socketForJudges = new ServerSocket(portForJudge);
        } catch (IOException e) {
            log("ServerImpl", e.getMessage());
        }

        texts = new CopyOnWriteArrayList<>();

        judges = new CopyOnWriteArraySet<>();
        clients = new ConcurrentLinkedQueue<>();
        judgesQueue = new ConcurrentLinkedQueue<>();

        serverStore = new ServerStore();
        judgeStore = new JudgeStore();


        idToSocket = new ConcurrentHashMap<>();

        conflicts = new CopyOnWriteArrayList<>();

        leaderBoard = new ConcurrentHashMap<>();

        idToSocket = new ConcurrentHashMap<>();
        idToLocalServerId = new ConcurrentHashMap<>();
        localServerIdToId = new ConcurrentHashMap<>();
        idToTextId = new ConcurrentHashMap<>();
        idToThread = new ConcurrentHashMap<>();
        idToStatus = new ConcurrentHashMap<>();
        idToUsername = new ConcurrentHashMap<>();

        reconnectQueue = new ConcurrentLinkedQueue<>();
        connectedUsers = new ConcurrentLinkedQueue<>();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("users"),
                    StandardCharsets.UTF_8));
            while (reader.ready()) {
                String request = reader.readLine();
                String[] str = request.split("|");
            }
        } catch (IOException e) {
            log("ServerImpl", e.getMessage());
        }

        File path = new File("prefix");
        path.mkdir();
        backupName = "prefix";
        judgeStore.setJudgeWriter("prefix");
        serverStore.setServerWriter("prefix");
        backupInfo();
        leaderBoardBackUp();
        log("ServerImpl", "Successful start server");
    }

    public void loadTexts(List<String> filenames) {
        for (String filename : filenames) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename),
                        StandardCharsets.UTF_8));

                StringBuilder text = new StringBuilder();
                String help = "";

                while ((help = reader.readLine()) != null) {
                    text.append(help + " ");
                }

                texts.add(text.toString());
            } catch (IOException e) {
                log("loadTexts", e.getMessage());
            }
        }
    }

    private static void logFileInitialize() {
        try {
            logWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream("server.log"),
                    StandardCharsets.UTF_8)));
        } catch (FileNotFoundException e) {
            log("logFileInitialize", e.getMessage());
        }
    }

    public static void log(String from, String what) {
        StringBuilder builder = new StringBuilder();
        builder.append("[").append(new Date()).append("]").append(" :==: ").append(from).append(" :==: ").append(what);
        if (logWriter != null) {
            logWriter.println(builder.toString());
            logWriter.flush();
        }
        System.out.println(builder.toString());
    }

    Thread userListenerThread;
    Thread judgeListenerThread;
    Thread userConnectionThread;
    Thread judgeConnectionThread;
    Thread userReconnectionThread;
    Thread userSchedulerThread;
    Thread serverStoreWorkerThread;
    Thread conflictInfoSchedulerThread;
    Thread backupThread;
    Thread leaderboardThread;

    ExecutorService userConnectionExecutor = Executors.newFixedThreadPool(4);
    ExecutorService userReConnectionExecutor = Executors.newFixedThreadPool(4);
    ExecutorService userSchedulerExecutor = Executors.newFixedThreadPool(4);
    /**
     * Start server
     */
    public void run() {
        userListenerThread = new Thread(userListener);
        judgeListenerThread = new Thread(judgeListener);
        serverStoreWorkerThread = new Thread(serverStore.worker);
        conflictInfoSchedulerThread = new Thread(conflictInfoScheduler);
        backupThread = new Thread(backupWorker);
        leaderboardThread = new Thread(leaderBoardWorker);
        judgeConnectionThread = new Thread(judgeConnection);

        for (int i = 0; i < 4; i++) {
            userConnectionExecutor.execute(userConnection);
        }

        for (int i = 0; i < 4; i++) {
            userSchedulerExecutor.execute(userScheduler);
        }

        for (int i = 0; i < 4; i++) {
            userReConnectionExecutor.execute(userReconnection);
        }

        userListenerThread.start();
        judgeListenerThread.start();
        serverStoreWorkerThread.start();
        conflictInfoSchedulerThread.start();
        backupThread.start();
        leaderboardThread.start();
        judgeConnectionThread.start();
        try {
            userListenerThread.join();
        } catch (InterruptedException e) {
            log("run", e.getMessage());
        }
    }

    public void close() {

    }

    /**
     * Connect clients to server
     */
    private Runnable userListener = () -> {
        while (true) {
            try {
                Socket client = socketForUsers.accept();
                log("userListener", client.toString());
                clients.add(client);
            } catch (IOException e) {
                log("userListener", e.getMessage());
            }
        }
    };

    private Runnable judgeListener = () -> {
        while (true) {
            try {
                Socket client = socketForJudges.accept();
                log("judgeListener", client.toString());
                judgesQueue.add(client);
            } catch (IOException e) {
                log("judgeListener", e.getMessage());
            }
        }
    };

    private Runnable judgeConnection = () -> {
        while (true) {
            try {
                if (!judgesQueue.isEmpty()) {
                    Socket client = judgesQueue.poll();
                    if (client == null) {
                        continue;
                    }
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
                        PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8)));

                        String stringId = reader.readLine();
                        log("judgeConnection", client.toString() + " id=" + stringId);
                        if (stringId.equals("228")) {
                            writer.write(0);
                            writer.flush();
                            judges.add(new JudgeInfo(client, stringId));
                        } else {
                            writer.write(1);
                            writer.flush();
                        }
                    } catch (Exception e) {
                        log("judgeConnection", e.getMessage());
                    }
                } else {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                log("judgeConnection", e.getMessage());
                break;
            }
        }
    };

    /**
     * Split clients on clients or judges
     */
    private Runnable userConnection = () -> {
        while (true) {
            try {
                if (!clients.isEmpty()) {
                    Socket client = clients.poll();
                    if (client == null) {
                        continue;
                    }
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
                        PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8)));

                        String stringId = reader.readLine();
                        log("userConnection", client.toString() + " id=" + stringId);
                        if (idToLocalServerId.containsKey(stringId)) {
                            int id = idToLocalServerId.get(stringId);
                            if (idToStatus.get(id).compareAndSet(0, 1)) {
                                log("userConnection", stringId + " serverAns=1");
                                writer.write(1);
                                writer.flush();
                                reconnectQueue.add(new Pair<>(id, client));
                            } else {
                                log("userConnection", stringId + " serverAns=2");
                                writer.write(2);
                                writer.flush();
                            }
                        } else {
                            int id = clientNumber.getAndIncrement();
                            idToLocalServerId.put(stringId, id);
                            localServerIdToId.put(id, stringId);
                            log("userConnection", stringId + " serverAns=0");
                            writer.write(0);
                            writer.flush();
                            idToSocket.put(id, client);
                            idToStatus.put(id, new AtomicInteger(1));
                            connectedUsers.add(new Pair<>(id, client));
                        }

                    } catch (Exception e) {
                        log("userConnection", e.getMessage());
                    }
                } else {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                log("userConnection", e.getMessage());
                break;
            }
        }
    };

    private Runnable userReconnection = () -> {
        while (true) {
            try {
                if (!reconnectQueue.isEmpty()) {
                    Pair<Integer, Socket> pair = reconnectQueue.poll();
                    if (pair == null) {
                        continue;
                    }
                    Socket client = pair.getValue();
                    int id = pair.getKey();

                    if (!idToTextId.containsKey(id)) {
                        continue;
                    }

                    int textId = idToTextId.get(id);

                    BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
                    PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8)));

                    String fileName = backupName + DELIMITER + id + "text=" + textId;

                    BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), StandardCharsets.UTF_8));

                    List<Action> actionList = new ArrayList<>();

                    while (fileReader.ready()) {
                        actionList.add(new Action(fileReader.readLine()));
                    }

                    Data data = new Data(texts.get(textId), actionList);

                    writer.println(data.toString());
                    writer.flush();

                    idToSocket.put(id, client);
                    needBackUp.compareAndSet(false, true);
                    idToStatus.get(id).compareAndSet(1, 2);
                    log("userReconnection", localServerIdToId.get(id) + " reconnection successful");
                    startThreadWorker(id, textId, reader, writer);
                } else {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                log("userReconnection", e.getMessage());
                break;
            } catch (Exception e) {
                log("userReconnection", e.getMessage());
            }
        }
    };

    /**
     * Give task for online users
     */

    private int getNewTextId() {
        return textNumber.getAndIncrement() / 2;
    }


    private Runnable userScheduler = () -> {
        while (true) {
            try {
                if (!connectedUsers.isEmpty()) {
                    Pair<Integer, Socket> pair = connectedUsers.poll();
                    if (pair == null) {
                        continue;
                    }
                    int id = pair.getKey();
                    Socket client = pair.getValue();
                    try {
                        int textId = getNewTextId();

                        idToTextId.put(id, textId);
                        leaderBoard.put(localServerIdToId.get(id), 0);

                        needBackUp.compareAndSet(false, true);

                        BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));

                        PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8)));

                        writer.println(texts.get(textId));
                        writer.flush();
                        log("userScheduler", localServerIdToId.get(id) + " get text with id=" + textId);

                        serverStore.addSample(id, textId, backupName);
                        judgeStore.addNewTeam(id, textId, backupName);
                        /*serverStore.addNewGame(socketToId.get(client1), socketToId.get(client2), text, backupName);
                        judgeStore.addNewGame(socketToId.get(client1), socketToId.get(client2), text, backupName);*/
                        idToStatus.get(id).compareAndSet(1, 2);

                        startThreadWorker(id, textId, reader, writer);

                    } catch (IOException e) {
                        log("userScheduler", e.getMessage());
                    }
                } else {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                log("userScheduler", e.getMessage());
                break;
            }
        }
    };

    private void startThreadWorker(int id, int textId, BufferedReader reader, PrintWriter writer) {
        Thread thread = new Thread(() -> {
            idToThread.put(id, Thread.currentThread());
            while (true) {
                try {
                    int count = 0;
                    while (!reader.ready() && count < 3) {
                        Thread.sleep(5000);
                        writer.write(0);
                        writer.flush();
                        count++;
                    }

                    String stringData = null;
                    int out = reader.read();
                    if (count < 2 && out == 1) {
                        continue;
                    } else if (count < 2 && out == 0) {
                        stringData = reader.readLine();
                    }

                    if (stringData == null) {
                        idToSocket.put(id, nullSocket);
                        idToStatus.get(id).compareAndSet(2, 0);
                        idToThread.put(id, nullThread);
                        log("startThreadWorker", localServerIdToId.get(id) + " lost connection");
                        break;
                    }
                    System.out.println(stringData);
                    UpdateDocument doc = new UpdateDocument(stringData);
                    List<Action> actions = doc.getActions();
                    serverStore.putActions(actions, textId, id);
                    log("startThreadWorker", localServerIdToId.get(id) + " added actions");
                } catch (IOException | InterruptedException e) {
                    log("startThreadWorker", e.getMessage());
                    idToSocket.put(id, nullSocket);
                    idToStatus.get(id).compareAndSet(2, 0);
                    idToThread.put(id, nullThread);
                    log("startThreadWorker", localServerIdToId.get(id) + " lost connection");
                    break;

                }
            }
        });
        thread.start();
    }

    private Runnable conflictInfoScheduler = () -> {
        while (true) {
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
                                for (JudgeInfo judgeInfo : judgeInfoList) {
                                    if (!judgeInfo.task.isMarked()) {
                                        if (judgeInfo.setTask(conflict)) {

                                            f = true;
                                            break;
                                        }
                                    }
                                }
                            } else if (conflict.status.get() == 1) {
                            } else {
                                conflictInfoQueue.poll();
                            }
                        }
                    }
                } else {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                log("conflictInfoScheduler", e.getMessage());
                break;
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
            List<Pair<String, Integer>> local = leaderBoard.entrySet().stream()
                    .map(k -> new Pair<>(k.getKey(), k.getValue())).sorted(this::comparePairs).collect(Collectors.toList());
            Collections.reverse(local);
            for (int i = 0; i < local.size(); i++) {
                htmlBuilder.append(
                        "       <tr>\n           <td>").append(idToUsername.get(local.get(i).getKey())).append("</td>\n").append(
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
                PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream("leaderboard.html"), StandardCharsets.UTF_8)));
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

    private int comparePairs(Pair<String, Integer> o1, Pair<String, Integer> o2) {
        if (o1.getValue() > o2.getValue()) {
            return 1;
        } else if (o1.getValue() == o2.getValue()) {
            return 0;
        } else {
            return -1;
        }
    }

    class JudgeInfo {
        Socket socket;
        String id;
        /**
         * reference has mark set false if judge is free otherwise true
         */
        AtomicMarkableReference<ConflictInfo> task;
        Thread worker;

        JudgeInfo(Socket socket, String id) throws IOException {
            this.id = id;
            this.socket = socket;
            task = new AtomicMarkableReference<>(null, false);
            try {
                PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)));
                writer.println(texts.size());
                writer.flush();
                for (String string : texts) {
                    writer.println(string);
                    writer.flush();
                }
            } catch (IOException e) {
                System.out.println("Can't send texts to judge" + socket.toString());
                throw e;
            }
            worker = new Thread(judgeWorker);
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
                                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                                PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)));

                                ConflictInfo conflict = task.getReference();

                                List<Action> teamOneActions = judgeStore.getTeamList(conflict.textId, conflict.teamOneId);
                                List<Action> teamTwoActions = judgeStore.getTeamList(conflict.textId, conflict.teamTwoId);
                                List<Integer> decisions = judgeStore.getDecisionList(conflict.textId);

                                Action action1 = conflict.action1;
                                Action action2 = conflict.action2;

                                List<Action> toJudgeAboutTeamOne = new CopyOnWriteArrayList<>();
                                List<Action> toJudgeAboutTeamTwo = new CopyOnWriteArrayList<>();

                                toJudgeAboutTeamOne.add(action1);
                                toJudgeAboutTeamTwo.add(action2);


                                if (!action1.isEmpty()) {
                                    for (int i = teamOneActions.size() - 1; i >= 0 && i >= teamOneActions.size() - 200 &&
                                            toJudgeAboutTeamOne.size() < 8; i--) {
                                        if (!teamOneActions.get(i).isEmpty() && (decisions.get(i) == 1 || decisions.get(i) == 3) && teamOneActions.get(i).getChainId() == action1.getChainId()) {
                                            toJudgeAboutTeamOne.add(0, teamOneActions.get(i));
                                        }
                                    }
                                }

                                if (!action2.isEmpty()) {
                                    for (int i = teamTwoActions.size() - 1; i >= 0 && i >= teamTwoActions.size() - 200 &&
                                            toJudgeAboutTeamTwo.size() < 8; i--) {
                                        if (!teamTwoActions.get(i).isEmpty() && (decisions.get(i) == 2 || decisions.get(i) == 3) && teamTwoActions.get(i).getChainId() == action2.getChainId()) {
                                            toJudgeAboutTeamTwo.add(0, teamTwoActions.get(i));
                                        }
                                    }
                                }

                                /*if (toJudgeAboutTeamOne.size() == 1 && toJudgeAboutTeamTwo.size() == 1 && !f && !action1.isEmpty() && !action2.isEmpty()) {
                                    for (int i = teamOneActions.size() - 100; i >= 0; i--) {
                                        if (!teamOneActions.get(i).isEmpty() && (decisions.get(i) == 1 || decisions.get(i) == 3)) {
                                            toCheckAboutTeamOne.add(teamOneActions.get(i));
                                            break;
                                        }
                                    }

                                    for (int i = teamTwoActions.size() - 150; i >= 0; i--) {
                                        if (!teamTwoActions.get(i).isEmpty() && (decisions.get(i) == 1 || decisions.get(i) == 3)) {
                                            toCheckAboutTeamTwo.add(teamTwoActions.get(i));
                                            break;
                                        }
                                    }

                                    if (toCheckAboutTeamOne.size() != toCheckAboutTeamTwo.size()) {
                                        break;
                                    } else {

                                    }
                                }*/

                                if (!action1.isEmpty() && !action2.isEmpty() && toJudgeAboutTeamOne.size() >= 2 && toJudgeAboutTeamTwo.size() >= 2 && action1.getLocation().equals(action2.getLocation())) {
                                    Action first = toJudgeAboutTeamOne.get(toJudgeAboutTeamOne.size() - 2);
                                    Action second = toJudgeAboutTeamTwo.get(toJudgeAboutTeamTwo.size() - 2);
                                    if (first.getLocation().equals(second.getLocation())) {
                                        if (conflict.complete()) {
                                            leaderBoard.put(localServerIdToId.get(conflict.teamOneId), leaderBoard.get(localServerIdToId.get(conflict.teamOneId)) + 5);
                                            leaderBoard.put(localServerIdToId.get(conflict.teamTwoId), leaderBoard.get(localServerIdToId.get(conflict.teamTwoId)) + 5);
                                            leaderBoardNeed.compareAndSet(false, true);
                                            judgeStore.putOneAction(conflict.teamOneId, action1, conflict.teamTwoId, action2, conflict.textId, 3);
                                            log("judgeWorker", "judge id=" + id + " automatically complete task");
                                        }

                                        task.compareAndSet(conflict, null, true, false);
                                        continue;
                                    }
                                }

                                UpdateDocument teamOne = new UpdateDocument(toJudgeAboutTeamOne);
                                UpdateDocument teamTwo = new UpdateDocument(toJudgeAboutTeamTwo);

                                if (teamOne.pack() == null) {
                                    log("judgeWorker", "UpdateDocument::pack return null for teamOne");
                                }

                                writer.println(teamOne.pack());
                                writer.flush();
                                /*Thread.sleep(1000);*/
                                if (teamTwo.pack() == null) {
                                    log("judgeWorker", "UpdateDocument::pack return null for teamTwo");
                                }

                                writer.println(teamTwo.pack());
                                writer.flush();


                                writer.println(conflict.textId);
                                writer.flush();

                                String request = reader.readLine();
                                if (request == null) {
                                    log("judgeWorker", "null request from judge id=" + id);
                                    synchronized (judges) {
                                        socket.close();
                                        judges.remove(this);
                                    }
                                    break;
                                }
                            /*logWriter.println("get decision from judge" + socket.toString());
                            logWriter.flush();*/
                                int decision = Integer.parseInt(request);

                                if (conflict.complete()) {
                                    judgeStore.putOneAction(conflict.teamOneId, action1, conflict.teamTwoId, action2, conflict.textId, decision);
                                    synchronized (leaderBoard) {
                                        if (decision == 2 && !action2.isEmpty()) {
                                            leaderBoard.put(localServerIdToId.get(conflict.teamOneId), leaderBoard.get(localServerIdToId.get(conflict.teamOneId)) - 20);
                                        }
                                        if (decision == 1 && !action1.isEmpty()) {
                                            leaderBoard.put(localServerIdToId.get(conflict.teamTwoId), leaderBoard.get(localServerIdToId.get(conflict.teamTwoId)) - 20);
                                        }
                                        if (decision == 1 || decision == 3) {
                                            leaderBoard.put(localServerIdToId.get(conflict.teamOneId), leaderBoard.get(localServerIdToId.get(conflict.teamOneId)) + 5);
                                        }
                                        if (decision == 2 || decision == 3) {
                                            leaderBoard.put(localServerIdToId.get(conflict.teamTwoId), leaderBoard.get(localServerIdToId.get(conflict.teamTwoId)) + 5);
                                        }
                                        if (decision == 0 && !action1.isEmpty()) {
                                            leaderBoard.put(localServerIdToId.get(conflict.teamOneId), leaderBoard.get(localServerIdToId.get(conflict.teamOneId)) - 20);
                                        }
                                        if (decision == 0 && !action2.isEmpty()) {
                                            leaderBoard.put(localServerIdToId.get(conflict.teamTwoId), leaderBoard.get(localServerIdToId.get(conflict.teamTwoId)) - 20);
                                        }
                                    }
                                    leaderBoardNeed.compareAndSet(false, true);
                                    log("judgeWorker", "judge id=" + id + " complete task with decision " + decision);
                                }

                                task.compareAndSet(conflict, null, true, false);
                            }
                        } else {
                            Thread.sleep(1000);
                        }
                    } catch (IOException e) {
                        log("judgeWorker", "judge id=" + id + " lost connection");
                        synchronized (judges) {
                            judges.remove(this);
                        }
                        log("judgeWorker", "judge id=" + id + " Error: " + e.getMessage());
                        break;
                    }
                }
            } catch (InterruptedException e) {
                log("judgeWorker", "judge id=" + id + " Error: " + e.getMessage());
            }
        };

    }

    void backupInfo() {
        PrintWriter backupWriter;
        try {
            backupWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(backupName + DELIMITER + "backupInfo"), StandardCharsets.UTF_8)));
            backupWriter.println(clientNumber);
            backupWriter.flush();
            backupWriter.println(textNumber);
            backupWriter.flush();
            backupWriter.println(idToLocalServerId.size());
            backupWriter.flush();
            idToLocalServerId.forEach((k, v) -> {
                backupWriter.println(k + " " + v);
                backupWriter.flush();
            });
            backupWriter.println(idToTextId.size());
            backupWriter.flush();
            idToTextId.forEach((k, v) -> {
                backupWriter.println(k + " " + v);
                backupWriter.flush();
            });
            backupWriter.println(idToUsername.size());
            backupWriter.flush();
            idToUsername.forEach((k, v) -> {
                backupWriter.println(k + " " + v);
                backupWriter.flush();
            });
        } catch (FileNotFoundException e) {
            log("backupInfo", e.getMessage() + " :==: Can't find file " + backupName + DELIMITER + "backupInfo");
        }
    }

    private Runnable backupWorker = () -> {
        while (true) {
            backupInfo();
            /*if (needBackUp.compareAndSet(true, false)) {
                backupInfo();
            } else {*/
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    void leaderBoardBackUp() {
        PrintWriter leaderboardWriter;
        try {
            leaderboardWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(backupName + DELIMITER + "leaderboardInfo"), StandardCharsets.UTF_8)));
            leaderboardWriter.println(leaderBoard.size());
            leaderboardWriter.flush();
            leaderBoard.forEach((k, v) -> {
                leaderboardWriter.println(k + " " + v);
                leaderboardWriter.flush();
            });
        } catch (FileNotFoundException e) {
            log("backupInfo", e.getMessage() + " :==: Can't find file " + backupName + DELIMITER + "leaderboardInfo");
        }
    }

    private Runnable leaderBoardWorker = () -> {
        while (true) {
            leaderBoardBackUp();
            /*if (leaderBoardNeed.compareAndSet(true, false)) {
                leaderBoardBackUp();
            } else {*/
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * private Map<String, Integer> idToLocalServerId; // NEED
     * private Map<Integer, String> localServerIdToId; // NEED BY FIRST
     * <p>
     * private Map<Integer, Integer> idToTextId; // NEED
     * <p>
     * private Map<String, Integer> idToUsername; // NEED
     */

    public boolean finalRecover(String prefixOld) {
        BufferedReader backupReader;
        try {
            backupReader = new BufferedReader(new InputStreamReader(new FileInputStream(prefixOld + DELIMITER + "backupInfo"), StandardCharsets.UTF_8));
            String request = backupReader.readLine();
            clientNumber.set(Integer.parseInt(request));
            request = backupReader.readLine();
            textNumber.set(Integer.parseInt(request));
            request = backupReader.readLine();
            int size = Integer.parseInt(request);
            for (int i = 0; i < size; i++) {
                request = backupReader.readLine();
                String[] data = request.split(" ");
                idToLocalServerId.put(data[0], Integer.parseInt(data[1]));
                localServerIdToId.put(Integer.parseInt(data[1]), data[0]);
            }
            request = backupReader.readLine();
            size = Integer.parseInt(request);
            for (int i = 0; i < size; i++) {
                request = backupReader.readLine();
                String[] nums = request.split(" ");
                idToTextId.put(Integer.parseInt(nums[0]), Integer.parseInt(nums[1]));
            }
            request = backupReader.readLine();
            size = Integer.parseInt(request);
            for (int i = 0; i < size; i++) {
                request = backupReader.readLine();
                String[] data = request.split(" ");
                idToUsername.put(data[0], Integer.parseInt(data[1]));
            }
            BufferedReader leaderboardReader = new BufferedReader(new InputStreamReader(new FileInputStream(prefixOld + DELIMITER + "leaderboardInfo"), StandardCharsets.UTF_8));
            request = leaderboardReader.readLine();
            size = Integer.parseInt(request);
            for (int i = 0; i < size; i++) {
                request = leaderboardReader.readLine();
                String[] data = request.split(" ");
                leaderBoard.put(data[0], Integer.parseInt(data[1]));
            }
            backupInfo();
            leaderBoardBackUp();
            return true;
        } catch (Exception e) {
            log("finalRecover", e.getMessage() + " :==: Can't find file " + prefixOld + DELIMITER + "backupInfo");
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
            gameReader = new BufferedReader(new InputStreamReader(new FileInputStream(prefixOld + DELIMITER + "judgeStoreGames"), StandardCharsets.UTF_8));
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
                String file = prefixOld + DELIMITER + judgeStoreFile.id1 + "vs" + judgeStoreFile.id2 + "text=" + judgeStoreFile.textId;
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
                String line = reader.readLine();
                List<Action> teamOneActions = new CopyOnWriteArrayList<>();
                List<Action> teamTwoActions = new CopyOnWriteArrayList<>();
                List<Integer> decisions = new CopyOnWriteArrayList<>();
                PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(prefixNew + DELIMITER + judgeStoreFile.id1 + "vs" + judgeStoreFile.id2 + "text=" + judgeStoreFile.textId), StandardCharsets.UTF_8)));
                while (line != null) {
                    List<String> list = Arrays.asList(line.split("@"));
                    teamOneActions.add(new Action(list.get(0)));
                    teamTwoActions.add(new Action(list.get(1)));
                    decisions.add(Integer.parseInt(list.get(2)));
                    line = reader.readLine();
                    writer.println(list.get(0) + "@" + list.get(1) + "@" + list.get(2));
                    writer.flush();
                }
                judgeStore.addNewRecoverGame(judgeStoreFile.id1, judgeStoreFile.id2, judgeStoreFile.textId, teamOneActions, teamTwoActions, decisions, writer, prefixNew);
                judgeStore.dumpWriter.println(judgeStoreFile.id1 + "vs" + judgeStoreFile.id2 + "text=" + judgeStoreFile.textId);
                judgeStore.dumpWriter.flush();
            }
            gameReader = new BufferedReader(new InputStreamReader(new FileInputStream(prefixOld + DELIMITER + "gamesServer"), StandardCharsets.UTF_8));
            String firstFile;
            ArrayList<ServerStoreFile> files = new ArrayList<>();
            while ((firstFile = gameReader.readLine()) != null) {
                String[] splittedFirst = firstFile.split("text=");
                files.add(new ServerStoreFile(Integer.valueOf(splittedFirst[0]), Integer.valueOf(splittedFirst[1])));
            }
            files.sort(this::compareSS);
            for (int i = 0; i < files.size(); i++) {
                ServerStoreFile ssf = files.get(i);
                if (i + 1 < files.size() && files.get(i + 1).textId == ssf.textId) {
                    boolean f = false;
                    for (int j = 0; j < judgeStore.games.size(); j++) {
                        if (judgeStore.games.get(j).textNum == ssf.textId) {
                            f = true;
                            break;
                        }
                    }
                    if (!f) {
                        ServerStoreFile ssf2 = files.get(i + 1);
                        judgeStore.addNewTeam(ssf.teamId, ssf.textId, prefixNew);
                        judgeStore.addNewTeam(ssf2.teamId, ssf2.textId, prefixNew);
                    }
                    i++;
                } else {
                    judgeStore.addNewTeam(ssf.teamId, ssf.textId, prefixNew);
                }
            }
            return true;
        } catch (Exception e) {
            log("judgeRecover", e.getMessage());
            return false;
        }
    }

    public int judgeStoreCompare(JudgeStoreFile tmp1, JudgeStoreFile tmp2) {
        return Integer.compare(tmp1.textId, tmp2.textId);
    }


    class ServerStoreFile {
        //int idOne;
        //int idTwo;
        int teamId;
        int textId;

        ServerStoreFile(int teamId, int textId) {
            //this.idOne = idOne;
            //this.idTwo = idTwo;
            this.teamId = teamId;
            this.textId = textId;
        }
    }

    public int compareSS(ServerStoreFile o1, ServerStoreFile o2) {
        return Integer.compare(o1.textId, o2.textId);
    }

    public boolean serverRecover(String prefixOld, String prefixNew) {
        try (BufferedReader readerG = new BufferedReader(new InputStreamReader(new FileInputStream(prefixOld + DELIMITER + "gamesServer"), StandardCharsets.UTF_8))) {
            String firstFile;
            ArrayList<ServerStoreFile> files = new ArrayList<>();
            while ((firstFile = readerG.readLine()) != null) {
                String[] splittedFirst = firstFile.split("text=");
                files.add(new ServerStoreFile(Integer.valueOf(splittedFirst[0]), Integer.valueOf(splittedFirst[1])));
            }
            files.sort(this::compareSS);
            serverStore.setServerWriter(prefixNew);
            for (int k = 0; k < files.size(); k++) {
                ServerStoreFile ssf = files.get(k);
                if ((k + 1 < files.size()) && (ssf.textId == files.get(k + 1).textId)) {
                    ServerStoreFile ssf2 = files.get(k + 1);
                    k++;
                    try (BufferedReader readerFirst = new BufferedReader(new InputStreamReader(new FileInputStream(prefixOld + DELIMITER + ssf.teamId + "text=" + ssf.textId), StandardCharsets.UTF_8));
                         BufferedReader readerSecond = new BufferedReader(new InputStreamReader(new FileInputStream(prefixOld + DELIMITER + ssf2.teamId + "text=" + ssf.textId), StandardCharsets.UTF_8));
                         PrintWriter writerFirst = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(prefixNew + DELIMITER + ssf.teamId + "text=" + ssf.textId), StandardCharsets.UTF_8)));
                         PrintWriter writerSecond = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(prefixNew + DELIMITER + ssf2.teamId + "text=" + ssf.textId), StandardCharsets.UTF_8)))) {
                        ArrayList<Action> listFirst = new ArrayList<>();
                        ArrayList<Action> listSecond = new ArrayList<>();
                        String input;

                        List<String> lines = readerFirst.lines().filter(s -> !s.trim().isEmpty()).collect(Collectors.toList());
                        for (String s : lines) {
                            UpdateDocument doc = new UpdateDocument(s);
                            listFirst.addAll(doc.getActions());
                            writerFirst.println(s);
                            writerFirst.flush();
                        }


                        lines = readerSecond.lines().filter(s -> !s.trim().isEmpty()).collect(Collectors.toList());
                        for (String s : lines) {
                            UpdateDocument doc = new UpdateDocument(s);
                            listSecond.addAll(doc.getActions());
                            writerSecond.println(s);
                            writerSecond.flush();
                        }

                        List<Action> toDeleteFirst = new ArrayList<>();
                        List<Action> toDeleteSecond = new ArrayList<>();
                        for (int j = 0; j < judgeStore.games.size(); j++) {
                            if (judgeStore.games.get(j).teamIdList.get(0) == ssf.teamId && judgeStore.games.get(j).teamIdList.get(1) == ssf2.teamId) {
                                toDeleteFirst = judgeStore.games.get(j).idToTeamApprovedList.get(ssf.teamId);
                                toDeleteSecond = judgeStore.games.get(j).idToTeamApprovedList.get(ssf2.teamId);
                                break;
                            } else if (judgeStore.games.get(j).teamIdList.get(0) == ssf2.teamId && judgeStore.games.get(j).teamIdList.get(1) == ssf.teamId) {
                                toDeleteFirst = judgeStore.games.get(j).idToTeamApprovedList.get(ssf2.teamId);
                                toDeleteSecond = judgeStore.games.get(j).idToTeamApprovedList.get(ssf.teamId);
                                break;
                            }
                        }
                        /*
                        for (int j = 0; j < judgeStore.games.size(); j++) {
                            if (judgeStore.games.get(j).teamOneId == ssf.teamId && judgeStore.games.get(j).teamTwoId == ssf2.teamId) {
                                toDeleteFirst = judgeStore.games.get(j).teamOneApproved;
                                toDeleteSecond = judgeStore.games.get(j).teamTwoApproved;
                                break;
                            } else if (judgeStore.games.get(j).teamOneId == ssf2.teamId && judgeStore.games.get(j).teamTwoId == ssf.teamId) {
                                toDeleteFirst = judgeStore.games.get(j).teamTwoApproved;
                                toDeleteSecond = judgeStore.games.get(j).teamOneApproved;
                                break;
                            }
                        }*/
                        List<Action> firstActionNeeded = new ArrayList<>();
                        List<Action> secondActionNeeded = new ArrayList<>();

                        for (int i = 0; i < listFirst.size(); i++) {
                            if (toDeleteFirst.isEmpty() || !listFirst.get(i).getLocation().equals(toDeleteFirst.get(0).getLocation())) {
                                firstActionNeeded.add(listFirst.get(i));
                            } else {
                                toDeleteFirst.remove(0);
                            }
                        }

                        for (int i = 0; i < listSecond.size(); i++) {
                            if (toDeleteSecond.isEmpty() || !listSecond.get(i).getLocation().equals(toDeleteSecond.get(0).getLocation())) {
                                secondActionNeeded.add(listSecond.get(i));
                            } else {
                                toDeleteSecond.remove(0);
                            }
                        }

                        conflicts.add(new ConcurrentLinkedQueue<>());
                        serverStore.addFullRecoverGame(ssf.teamId, ssf2.teamId, ssf.textId, firstActionNeeded, secondActionNeeded, writerFirst, writerSecond, prefixNew);
                    } catch (IOException e2) {
                        log("serverRecover", e2.getMessage());
                    }
                    serverStore.writer.println(ssf.teamId + "text=" + ssf.textId);
                    serverStore.writer.flush();
                    serverStore.writer.println(ssf2.teamId + "text=" + ssf2.textId);
                    serverStore.writer.flush();
                } else {
                    try (BufferedReader readerFirst = new BufferedReader(new InputStreamReader(new FileInputStream(prefixOld + DELIMITER + ssf.teamId + "text=" + ssf.textId), StandardCharsets.UTF_8));
                         PrintWriter writerFirst = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(prefixNew + DELIMITER + ssf.teamId + "text=" + ssf.textId), StandardCharsets.UTF_8)));
                    ) {
                        ArrayList<Action> listFirst = new ArrayList<>();
                        List<String> lines = readerFirst.lines().filter(s -> !s.trim().isEmpty()).collect(Collectors.toList());
                        for (String s : lines) {
                            UpdateDocument doc = new UpdateDocument(s);
                            listFirst.addAll(doc.getActions());
                            writerFirst.println(s);
                            writerFirst.flush();
                        }
                        serverStore.addHalfRecoverGame(ssf.teamId, ssf.textId, listFirst, writerFirst, prefixNew);
                    } catch (IOException e2) {
                        log("serverRecover", e2.getMessage());
                    }
                    serverStore.writer.println(ssf.teamId + "text=" + ssf.textId);
                    serverStore.writer.flush();
                }
            }
            return true;
        } catch (IOException e) {
            log("serverRecover", e.getMessage());
            return false;
        }
    }
}