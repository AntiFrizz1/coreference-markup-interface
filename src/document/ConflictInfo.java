package document;

import chain.Action;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Consist data about conflict
 */
public class ConflictInfo {
    int textId;
    int teamOneId;
    int teamTwoId;

    Action action1;
    Action action2;

    AtomicInteger status;
    Thread counter;

    public ConflictInfo(ConflictData data) {
        this.action1 = data.action1;
        this.action2 = data.action2;
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
