package document;

import chain.Action;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Consist data about conflict
 */
public class ConflictInfo {
    public int textId;
    public int teamOneId;
    public int teamTwoId;

    public Action action1;
    public Action action2;

    public AtomicInteger status;
    public Thread counter;

    public ConflictInfo(ConflictData data) {
        this.action1 = data.action1;
        this.action2 = data.action2;
        this.teamOneId = data.teamOneId;
        this.teamTwoId = data.teamTwoId;
        this.textId = data.textId;
        this.status = new AtomicInteger(0);
        //System.out.println(textId);
    }

    public boolean complete() {

        return status.compareAndSet(1, 2);
    }

    public boolean apply() {
        if (status.compareAndSet(0, 1)) {
            counter = new Thread(() -> {
                while(status.get() != 2) {
                    try {
                        Thread.sleep(6000000);
                        System.out.println("conflict_info " + textId + " " + teamOneId + " " + teamTwoId  + " " + status + " " + this);
                        int localStatus = status.get();
                        if(localStatus == 1) {
                            if (status.compareAndSet(localStatus, 0)) {
                                break;
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            counter.start();
            return true;
        } else {
            return false;
        }
    }
}
