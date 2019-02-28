package client;

import chain.*;

import java.util.Arrays;
import java.util.List;

public class ConflictImpl implements Conflict {
    Chain firstSolution, secondSolution;
    Location collisionOne, collisionTwo;

    public ConflictImpl(Chain firstSolution, Chain secondSolution, Location collisionOne, Location collisionTwo) {
        this.firstSolution = firstSolution;
        this.secondSolution = secondSolution;
        this.collisionOne = collisionOne;
        this.collisionTwo = collisionTwo;
    }

    public ConflictImpl(String info) {
        List<String> list = Arrays.asList(info.split("\n\n"));
        firstSolution = new ChainImpl(list.get(0));
        secondSolution = new ChainImpl(list.get(1));
        if (list.get(2).contains("Blank")) {
            collisionOne = new Blank(list.get(2));
        } else {
            collisionOne = new Phrase(list.get(2));
        }

        if (list.get(3).contains("Blank")) {
            collisionTwo = new Blank(list.get(3));
        } else {
            collisionTwo = new Phrase(list.get(3));
        }
    }

    @Override
    public Chain getFirstSolution() {
        return firstSolution;
    }

    @Override
    public Chain getSecondSolution() {
        return secondSolution;
    }


    @Override
    public String pack() {
        StringBuilder sb = new StringBuilder();
        sb.append(firstSolution.pack()).append("\n\n").
                append(secondSolution.pack()).append("\n\n").
                append(collisionOne.pack()).append("\n\n").
                append(collisionTwo.pack());
        return sb.toString();
    }

    @Override
    public String packSB(StringBuilder sb) {
        return null;
    }
}
