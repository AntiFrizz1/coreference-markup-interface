package server;

import chain.Chain;
import chain.ChainImpl;

import java.net.Socket;
import java.util.*;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class StoreImpl implements Store {
    class Game {
        Socket teamOneSocket;
        Socket teamTwoSocket;
        Map<String, String> names;
        List<Chain> approved;
        List<Chain> teamOneList;
        List<Chain> teamTwoList;

        Game(Socket socket1, Socket socket2) {
            teamOneSocket = socket1;
            teamTwoSocket = socket2;
            names = new HashMap<>();
            approved = new ArrayList<>(0);
            teamOneList = new ArrayList<>(0);
            teamTwoList = new ArrayList<>(0);
        }
    }

    List<Game> texts;
    AtomicIntegerArray atomicIntegerArray;

    StoreImpl() {
        texts = new ArrayList<>(0);
        atomicIntegerArray = new AtomicIntegerArray(0);
    }

    public void put(Chain chain, int textNum, int teamNum) {
        if (atomicIntegerArray.compareAndSet(textNum, 0, 1)) {
            if (teamNum == 1) {
                texts.get(textNum).teamOneList.add(chain);
            } else {
                texts.get(textNum).teamTwoList.add(chain);
            }
            atomicIntegerArray.compareAndSet(textNum, 1, 0);
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
                    chain.mergeWith(union);
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
                        // send conflict {chain1 & chain2} to clients
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
                        chain2.mergeWith(chain1);
                    }
                }
            }
            atomicIntegerArray.compareAndSet(teamNum, 1, 0);
            return true;
        } else {
            return false;
        }
    }

}