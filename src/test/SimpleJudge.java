package test;

import client.Conflict;
import client.Judge;

public class SimpleJudge {
    public static void main(String[] args) throws InterruptedException {
        Judge judge = new Judge(Integer.parseInt(args[0]), 3333, "192.168.43.126");
        if (judge.joinOnline() == 0) {
            while (true) {
                Conflict conflict = judge.getInfo();
                Thread.sleep(5000);
                judge.sendDecision(3);
            }
            /*conflict = judge.getInfo();
            Thread.sleep(5000);
            judge.sendDecision(3);*/
        }
    }
}
