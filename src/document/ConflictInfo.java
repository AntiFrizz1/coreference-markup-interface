package document;

import chain.Action;
import chain.Letter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Consist data about conflict
 */
public class ConflictInfo {
    public int textId;
    public int teamOneId;
    public int teamTwoId;

    public Letter letter1;
    public Letter letter2;

    public AtomicInteger status;
    public Thread counter;

    public ConflictInfo(ConflictData data) {
        this.letter1 = data.letter1;
        this.letter2 = data.letter2;
        this.teamOneId = data.teamOneId;
        this.teamTwoId = data.teamTwoId;
        this.textId = data.textId;
        this.status = new AtomicInteger(0);
    }

    boolean complete() {

        return status.compareAndSet(1, 2);
    }

    public boolean apply() {
        if (status.compareAndSet(0, 1)) {
            counter = new Thread(() -> {
                while(status.get() != 2) {
                    try {
                        Thread.sleep(100000);
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
