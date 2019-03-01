package test;

import client.Conflict;
import client.Judge;

public class SimpleJudge {
    public static void main(String[] args) throws InterruptedException {
        Judge judge = new Judge(228, 3333, "localhost", null);
        if (judge.joinOnline() == 0) {
            Conflict conflict = judge.getInfo();
            Thread.sleep(5000);
            judge.sendDecision(3);
            conflict = judge.getInfo();
            Thread.sleep(5000);
            judge.sendDecision(3);
        }
    }
}
