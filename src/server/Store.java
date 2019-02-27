package server;

import chain.Chain;

import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public interface Store {
    /**
     * This method put {@code chain} in List<Chain> using {@code textNum} and {@code teamNum}
     *
     * @param chain   that we want to put
     * @param textNum text number
     * @param teamNum team number (one or two)
     * @return true if put successful, false otherwise
     */
    boolean put(Chain chain, int textNum, int teamNum);

    /**
     * This method trying to find collision in two given lists
     * If it found collision, it write it to {@code chain1} and {@code chain2} and return true
     * else it add names of chains to {@code names}
     *
     * @param names    match name of chain from teamOne to teamTwo
     * @param approved contains chains that both clients have
     * @param teamOne  list of chains from team one
     * @param teamTwo  list of chains from team two
     * @param chain1   empty chain from start, if collision is found then contains chain from team one
     * @param chain2   empty chain from start, if collision is found then contains chain from team two
     * @return true if found collision, false otherwise
     */
    boolean compareChains(Map<String, String> names, List<Chain> approved, List<Chain> teamOne, List<Chain> teamTwo, Chain chain1, Chain chain2);

    /**
     * This method in endless loop trying to find collision in all texts
     */
    void get();

    /**
     * Update local List<Chain>
     * @param chains changed list of chains
     * @param textNum text number
     * @param teamNum team number (one or two)
     * @return true if update successful, false otherwise
     */
    boolean update(List<Chain> chains, int textNum, int teamNum);

    /**
     * Add new pair of teams to Store
     * @param socket1 first team
     * @param socket2 second team
     */
    void addNewGame(Socket socket1, Socket socket2);

    /**
     * Put answer about conflict
     * @param ans answer from client
     * @param textNum text number
     * @param teamNum team number (one or two)
     * @return true if put successful, false otherwise
     */
    boolean putAns(String ans, int textNum, int teamNum);
}
