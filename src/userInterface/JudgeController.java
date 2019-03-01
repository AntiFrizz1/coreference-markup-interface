package userInterface;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class JudgeController {
    private int decision;
    private String judgeText = "Приехав с утренним поездом в Москву, Левин остановился у своего " +
            "старшего брата по матери Кознышева и, переодевшись, вошел к нему в кабинет, намереваясь тотчас же " +
            "рассказать ему, для чего он приехал, и просить его совета: но брат был не один. У него сидел известный " +
            "профессор философии, приехавший из Харькова, собственно, затем, чтобы разъяснить недоразумение, " +
            "возникшее между ними по весьма важному философскому вопросу. Профессор вел жаркую полемику против " +
            "материалистов, а Сергей Кознышев с интересом следил за этою полемикой и, прочтя последнюю статью " +
            "профессора, написал ему в письме свои возражения; он упрекал профессора за слишком большие уступки " +
            "материалистам. И профессор тотчас же приехал, чтобы столковаться. Речь шла о модном вопросе: есть ли " +
            "граница между психическими и физиологическими явлениями в деятельности человека и где она?";
    private Set<Integer> firstHighlights = new HashSet<>(Arrays.asList(1, 2, 3, 4)), secondHighlights = new HashSet<>(Arrays.asList(5, 6, 7, 8));

    public void setDecision(int decision) {
        this.decision = decision;
    }

    public int getDecision() {
        return decision;
    }

    public void setJudgeText(String judgeText) {
        this.judgeText = judgeText;
    }

    public String getJudgeText() {
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

    public void getInfo(String judgeText, Set<Integer> firstHighlights, Set<Integer> secondHighlights) {
        this.judgeText = judgeText;
        this.firstHighlights = firstHighlights;
        this.secondHighlights = secondHighlights;
    }
}
