package client;

import chain.Chain;
import chain.Location;

public class ConflictWithSuffix implements Conflict {
    int firstUserId, secondUserId;
    Chain firstSolution, secondSolution;
    Location collision;
    Chain followingFirst, followingSecond;

    public ConflictWithSuffix(Chain firstSolution, Chain secondSolution, Location collision, Chain followingFirst, Chain followingSecond) {
        this.firstSolution = firstSolution;
        this.secondSolution = secondSolution;
        this.collision = collision;
        this.followingFirst = followingFirst;
        this.followingSecond = followingSecond;
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
