package test;

import chain.Action;
import chain.Phrase;
import client.User;
import document.Data;

import java.util.Arrays;

public class SimpleUser {
    public static void main(String[] args) throws InterruptedException {
        User user = new User(Integer.parseInt(args[0]), 3333, "localhost");
        int status = user.joinOnline();
        Thread.sleep(1000);
        if (status == 0) {
            System.out.println(user.getText());
        } else if (status == 1) {
            Data data = user.getData();
            System.out.println(data.getText());
        }
        int i = 0;
        user.sendUpdates(Arrays.asList(
                new Action(0, 0, new Phrase("a", i + 1)),
                new Action(1, 0, new Phrase("b", i + 2)),
                new Action(1, 0, new Phrase("c", i + 3)),
                new Action(1, 0, new Phrase("d", i + 4)),
                new Action(1, 0, new Phrase("e", i + 5)),
                new Action(1, 0, new Phrase("f", i + 6)),
                new Action(1, 0, new Phrase("g", i + 7)),
                new Action(1, 0, new Phrase("h", i + 8))
        ));
        Thread.sleep(10000);


        user.close();
    }
}
