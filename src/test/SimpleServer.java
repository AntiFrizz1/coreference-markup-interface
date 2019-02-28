package test;

import server.ServerImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class SimpleServer {
    public static void main(String[] args) {
        ServerImpl server = new ServerImpl(3333);

        List<String> texts = Arrays.asList("texts/Рахманинов.txt", "texts/Серов.txt");

        server.loadTexts(texts);
        server.run();

        Scanner in = new Scanner(System.in);

        String ans = in.nextLine();

        server.close();
    }
}
