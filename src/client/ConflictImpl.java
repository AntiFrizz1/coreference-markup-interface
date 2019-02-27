package client;

import chain.Chain;
import chain.Location;

public class ConflictImpl implements Conflict {
    int firstUserId, secondUserId;
    Chain firstSolution, secondSolution;
    Location collision;

    ConflictImpl(int firstUserId, int secondUserId, Chain firstSolution, Chain secondSolution) {
        this.secondSolution = secondSolution;
        this.firstSolution = firstSolution;
        this.firstUserId = firstUserId;
        this.secondUserId = secondUserId;
    }

    public ConflictImpl(Chain firstSolution, Chain secondSolution, Location collision) {
        this.firstSolution = firstSolution;
        this.secondSolution = secondSolution;
        this.collision = collision;
    }

    ConflictImpl(String info) {
        
    }

    @Override
    public Location getCollision() {
        return collision;
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