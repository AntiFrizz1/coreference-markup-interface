package test;

import server.ServerImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SimpleServer {
    public static void main(String[] args) {
        ServerImpl server;
        if (args.length == 4) {
            server = new ServerImpl(3334, 3333, args[0], args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]));
        } else if (args.length == 1){
            server = new ServerImpl(3334, 3333, Integer.parseInt(args[0]), 1);
        } else  {
            server = new ServerImpl(3334, 3333, 0, 1);
        }
        List<String> texts = new ArrayList<>(0);
        for (int i = 1; i <= 9; i++) {
            texts.add("texts/0" + i + ".txt");
        }
        for (int i = 10; i <= 60; i++) {
            texts.add("texts/" + i + ".txt");
        }
        server.loadTexts(texts);
        server.run();

    }
}
