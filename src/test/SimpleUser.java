package test;

import chain.Action;
import chain.Phrase;
import client.User;

import java.util.Arrays;

public class SimpleUser {
    public static void main(String[] args) throws InterruptedException {
        User user = new User(Integer.parseInt(args[0]), 3333, "localhost");
        user.joinOnline();
        Thread.sleep(1000);
        System.out.println(user.getText());
        user.sendUpdates(Arrays.asList(new Action(0, 0, new Phrase("q", 1)),
                new Action(1, 0, new Phrase("q", 2))));
        user.close();
    }
}
