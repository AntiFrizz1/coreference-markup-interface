package document;

import chain.Action;
import server.ServerImpl;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * Consist data about conflict
 */
public class ConflictInfo {
    public int textId;
    public int teamOneId;
    public int teamTwoId;

    public Action action1;
    public Action action2;

    public Thread counter;

    public AtomicStampedReference<ServerImpl.JudgeInfo> status;

    public ConflictInfo(ConflictData data) {
        this.action1 = data.action1;
        this.action2 = data.action2;
        this.teamOneId = data.teamOneId;
        this.teamTwoId = data.teamTwoId;
        this.textId = data.textId;
        this.status = new AtomicStampedReference<>(null, 0);
    }

    public boolean complete(ServerImpl.JudgeInfo judge) {
        return status.compareAndSet(judge, null, 1, 2);
    }

    public boolean apply(ServerImpl.JudgeInfo judge) {
        if (status.compareAndSet(null, judge, 0, 1)) {
            counter = new Thread(() -> {
                while(status.getStamp() != 2) {
                    try {
                        Thread.sleep(120000);
                        int localStatus = status.getStamp();
                        if(localStatus == 1) {
                            if (status.compareAndSet(status.getReference(), null, localStatus, 0)) {
                                ServerImpl.log("ConflictInfo::apply", "conflict=" + toString() + " timed out", 0);
                                break;
                            }
                        }
                    } catch (InterruptedException e) {
                        ServerImpl.log("ConflictInfo::apply", "conflict=" + toString() + " get Error: " + e.getMessage(), 0);
                        break;
                    }
                }
            });
            counter.start();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "[action1={" + action1.pack() + "}, action2={" + action2.pack() + "}, teamOneId=" +
                teamOneId + ", teamTwoId=" + teamTwoId + ", textId=" + textId + "]";
    }
}
