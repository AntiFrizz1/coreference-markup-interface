package server;

import chain.Chain;
import chain.ChainImpl;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReference;

public class StoreImpl implements Store {
    class Game {
        Socket teamOneSocket;
        Socket teamTwoSocket;

        Map<String, String> names;

        List<Chain> approved;
        List<Chain> teamOneList;
        List<Chain> teamTwoList;

        AtomicReference<String> ans1;
        AtomicReference<String> ans2;

        Game(Socket socket1, Socket socket2) {
            teamOneSocket = socket1;
            teamTwoSocket = socket2;

            names = new HashMap<>();

            approved = new ArrayList<>(0);
            teamOneList = new ArrayList<>(0);
            teamTwoList = new ArrayList<>(0);

            ans1 = new AtomicReference<>(null);
            ans2 = new AtomicReference<>(null);
        }
    }

    List<Game> texts;
    AtomicIntegerArray atomicIntegerArray;

    StoreImpl() {
        texts = new ArrayList<>(0);
        atomicIntegerArray = new AtomicIntegerArray(100);
    }

    public boolean put(Chain chain, int textNum, int teamNum) {
        if (atomicIntegerArray.compareAndSet(textNum, 0, 1)) {
            if (teamNum == 1) {
                texts.get(textNum).teamOneList.add(chain);
            } else {
                texts.get(textNum).teamTwoList.add(chain);
            }
            atomicIntegerArray.compareAndSet(textNum, 1, 0);
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method trying to find collision in two given lists
     * If it found collision, it write it to {@code chain1} and {@code chain2} and return true
     * else it add names of chains to {@code names}
     *
     * @param names    match name of chain from teamOneList to teamTwoList
     * @param approved contains chains that both clients have
     * @param teamOne  list of chains from team one
     * @param teamTwo  list of chains from team two
     * @param chain1   empty chain from start, if collision is found then contains chain from team one
     * @param chain2   empty chain from start, if collision is found then contains chain from team two
     * @return true if found collision, false otherwise
     */
    public boolean compareChains(Map<String, String> names, List<Chain> approved, List<Chain> teamOne, List<Chain> teamTwo, Chain chain1, Chain chain2) {
        return false;
    }

    private void doApproved(Game tmp) {
        for (String first : tmp.names.keySet()) {
            String second = tmp.names.get(first);
            Chain chainOne = null;
            Chain chainTwo = null;
            for (Chain chain : tmp.teamOneList) {
                if (first.equals(chain.getName())) {
                    chainOne = chain;
                    break;
                }
            }
            for (Chain chain : tmp.teamTwoList) {
                if (second.equals(chain.getName())) {
                    chainTwo = chain;
                    break;
                }
            }
            if (chainOne == null || chainTwo == null) {
                continue;
            }
            //unite chainOne and chainTwo
            Chain union = null;
            String ans = first + "|" + second;

            boolean added = false;
            for (Chain chain : tmp.approved) {
                if (chain.getName().equals(ans)) {
                    added = true;
                    chain.addAll(union.getLocations());
                }
            }
        }
    }

    public void get() {
        while (true) {
            for (int i = 0; i < texts.size(); i++) {
                if (atomicIntegerArray.compareAndSet(i, 0, 1)) {
                    Game curGame = texts.get(i);
                    Chain chain1 = new ChainImpl();
                    Chain chain2 = new ChainImpl();
                    if (compareChains(curGame.names, curGame.approved, curGame.teamOneList, curGame.teamTwoList, chain1, chain2)) {
                        try {
                            PrintWriter writer1 = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                                    curGame.teamOneSocket.getOutputStream())));
                            PrintWriter writer2 = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                                    curGame.teamTwoSocket.getOutputStream())));

                            writer1.println(chain1.toString());
                            writer1.flush();
                            writer1.println(chain2.toString());
                            writer1.flush();

                            writer2.println(chain2.toString());
                            writer2.flush();
                            writer2.println(chain1.toString());
                            writer2.flush();

                            while (curGame.ans1.get() == null || curGame.ans2.get() == null) {
                                Thread.sleep(1000);
                            }
                            String ans1 = curGame.ans1.getAndSet(null);
                            String ans2 = curGame.ans2.getAndSet(null);

                            String name1 = chain1.getName();
                            String name2 = chain2.getName();
                            String approvedName = name1 + "|" + name2;

                            Chain chainApproved = null;
                            boolean f = false;

                            if (ans1.equals("{1}") && ans2.equals("{2}")) {
                                f = true;
                                chainApproved = chain1;
                            } else if (ans1.equals("{2}") && ans2.equals("{1}")) {
                                f = true;
                                chainApproved = chain2;
                            } else if (ans1.startsWith("{3}") && ans2.startsWith("{3}") && ans1.equals(ans2)) {
                                f = true;
                                String newChainString = ans1.substring(3);
                                //convert String to Chain
                                //chainApproved = newChain;
                            }

                            if (f) {
                                for (Chain chain: curGame.approved) {
                                    if (chain.getName().equals(approvedName)) {
                                        chain.addAll(chainApproved.getLocations());
                                    }
                                }
                            } else {
                                //send to Judge
                            }

                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        doApproved(curGame);
                        // transfer chains to approved
                        // delete chains from teamlists
                        // add names to map (must be done in compareChains)
                        atomicIntegerArray.compareAndSet(i, 1, 0);
                    }
                }
            }
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    @Override
    public boolean update(List<Chain> chains, int textNum, int teamNum) {
        if (atomicIntegerArray.compareAndSet(textNum, 0, 1)) {
            Game curGame = texts.get(textNum);
            List<Chain> curChains;
            if (teamNum == 1) {
                curChains = curGame.teamOneList;
            } else {
                curChains = curGame.teamTwoList;
            }
            for (Chain chain1 : chains) {
                for (Chain chain2 : curChains) {
                    if (chain1.getName().equals(chain2.getName())) {
                        chain2.addAll(chain1.getLocations());
                    }
                }
            }
            atomicIntegerArray.compareAndSet(teamNum, 1, 0);
            return true;
        } else {
            return false;
        }
    }

    public boolean putAns(String ans, int textNum, int teamNum) {
        if (atomicIntegerArray.get(textNum) == 1) {
            Game curGame = texts.get(textNum);
            if (teamNum == 1) {
                if (curGame.ans1.compareAndSet(null, ans)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                if (curGame.ans2.compareAndSet(null, ans)) {
                    return true;
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    @Override
    synchronized public void addNewGame(Socket socket1, Socket socket2) {
        Game newGame = new Game(socket1, socket2);
        texts.add(newGame);
    }
}
