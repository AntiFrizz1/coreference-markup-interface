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
        for (int i = 0; i < 40; i += 8) {
            user.sendUpdates(Arrays.asList(
                    new Action(0, 0, new Phrase("a", i + 1), "qq"),
                    new Action(1, 0, new Phrase("b", i + 2), "qq"),
                    new Action(1, 0, new Phrase("c", i + 3), "qq"),
                    new Action(1, 0, new Phrase("d", i + 4), "qq"),
                    new Action(1, 0, new Phrase("e", i + 5), "qq"),
                    new Action(1, 0, new Phrase("f", i + 6), "qq"),
                    new Action(1, 0, new Phrase("g", i + 7), "qq"),
                    new Action(1, 0, new Phrase("h", i + 8), "qq")
            ));
            Thread.sleep(2000);
        }


        user.close();
    }
}
