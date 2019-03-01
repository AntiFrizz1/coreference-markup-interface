package userInterface;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JudgeController {
    private int decision;
    private List<String> judgeText;
    private Set<Integer> firstHighlights = new HashSet<>(Arrays.asList(1, 2, 3, 4)), secondHighlights = new HashSet<>(Arrays.asList(5, 6, 7, 8));

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

    public void sendDecision() {

    }

    public void getInfo(List<String> judgeText, Set<Integer> firstHighlights, Set<Integer> secondHighlights) {
        this.judgeText = judgeText;
        this.firstHighlights = firstHighlights;
        this.secondHighlights = secondHighlights;
    }
}
