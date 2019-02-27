package client;

import chain.Chain;
import chain.Location;

public class ConflictImpl implements Conflict {
    int firstUserId, secondUserId;
    Chain firstSolution, secondSolution;
    Location collisionOne, collisionTwo;

    ConflictImpl(int firstUserId, int secondUserId, Chain firstSolution, Chain secondSolution) {
        this.secondSolution = secondSolution;
        this.firstSolution = firstSolution;
        this.firstUserId = firstUserId;
        this.secondUserId = secondUserId;
    }

    public ConflictImpl(Chain firstSolution, Chain secondSolution, Location collisionOne, Location collisionTwo) {
        this.firstSolution = firstSolution;
        this.secondSolution = secondSolution;
        this.collisionOne = collisionOne;
        this.collisionTwo = collisionTwo;
    }

    ConflictImpl(String info) {
        
    }

    @Override
    public Chain getFirstSolution() {
        return firstSolution;
    }

    @Override
    public Chain getSecondSolution() {
        return secondSolution;
    }


}
