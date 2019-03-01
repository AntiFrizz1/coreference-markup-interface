package test;

import server.ServerImpl;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class SimpleServer {
    public static void main(String[] args) {
        ServerImpl server = new ServerImpl(3333);

        List<String> texts = Arrays.asList("texts/Рахманинов.txt", "texts/Серов.txt",
                "texts/Рахманинов (3-я копия).txt",
                "texts/Рахманинов (4-я копия).txt",
                "texts/Рахманинов (5-я копия).txt",
                "texts/Рахманинов (6-я копия).txt",
                "texts/Рахманинов (7-я копия).txt",
                "texts/Рахманинов (8-я копия).txt",
                "texts/Рахманинов (9-я копия).txt",
                "texts/Рахманинов (10-я копия).txt");
        server.loadTexts(texts);
        server.run();

        Scanner in = new Scanner(System.in);

        String ans = in.nextLine();

        server.close();
    }
}
