package userInterface;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JudgeController {
    private int decision;
    private List<String> judgeText;
    private Set<Integer> firstHighlights, secondHighlights, firstWhite, secondWhite, firstSingle, secondSingle;

    public void setDecision(int decision) {
        this.decision = decision;
    }

    public int getDecision() {
        return decision;
    }

    public void setJudgeText(List<String> judgeText) {
        this.judgeText = judgeText;
    }

    public List<String> getJudgeText() {
        return judgeText;
    }

    public void setFirstHighlights(Set<Integer> firstHighlights) {
        this.firstHighlights = firstHighlights;
    }

    public void setSecondHighlights(Set<Integer> secondHighlights) {
        this.secondHighlights = secondHighlights;
    }

    public Set<Integer> getFirstHighlights() {
        return firstHighlights;
    }

    public Set<Integer> getSecondHighlights() {
        return secondHighlights;
    }

    public Set<Integer> getFirstWhite() {
        return firstWhite;
    }

    public Set<Integer> getSecondWhite() {
        return secondWhite;
    }

    public Set<Integer> getFirstSingle() {
        return firstSingle;
    }

    public Set<Integer> getSecondSingle() {
        return secondSingle;
    }

    public void getInfo(List<String> judgeText, Set<Integer> firstHighlights, Set<Integer> secondHighlights, Set<Integer> firstWhite, Set<Integer> secondWhite, Set<Integer> firstSingle, Set<Integer> secondSingle) {
        this.firstSingle = firstSingle;
        this.secondSingle = secondSingle;
        this.firstWhite = firstWhite;
        this.secondWhite = secondWhite;
        this.judgeText = judgeText;
        this.firstHighlights = firstHighlights;
        this.secondHighlights = secondHighlights;
    }
}
