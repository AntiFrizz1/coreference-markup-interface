package server;

/**
 * This class describes interaction protocol of Server
 *
 * @author Vadim Baydyuk
 * @TODO describe interaction protocol. In particular:
 * @TODO 1) Pull of tasks for Judges.
 * @TODO 2) Class Listener for getting updates from client
 * @TODO 3) Two threads for each pair of users. The first
 * @TODO one should listen for updates, the second one
 * @TODO should send information about conflict and wait
 * @TODO for the answers from users. What if users don't
 * @TODO answer for a long time e.t.c.
 */

public interface Server {
}
