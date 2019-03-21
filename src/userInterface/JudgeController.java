package userInterface;

import chain.Blank;
import chain.Chain;
import chain.Location;
import chain.Phrase;
import com.sun.net.httpserver.Filter;

import java.util.List;
import java.util.Set;

public class JudgeController {

    public JudgeController() {
    }

    enum Confl {NEWCHAIN_EMPTY, CONTCHAIN_EMPTY, NEW_SAME, ADD_SAME, ADD_EMPTY_SAME, NEW_DIF, NEW_ADD}

    private int decision;
    private List<String> judgeText;
    private Set<Integer> firstHighlights, secondHighlights, firstWhite, secondWhite, firstSingle, secondSingle;
    private Chain firstChain, secondChain;
    private boolean isSingle;
    private Confl conflType = Confl.NEW_DIF;
    private String info = "Один из участников решил создать новую цеепочку, состоящю из нулевой анафоры, в то время как второй учатсник не использова эту нулвую анафору ни в одной из цепочек";
    private boolean isFirstEmpty;

    boolean isFirstEmpty() {
        return isFirstEmpty;
    }

    String getInfo() {
        return info;
    }

    Confl getConflType() {
        return conflType;
    }

    void setDecision(int decision) {
        this.decision = decision;
    }

    int getDecision() {
        return decision;
    }

    void setJudgeText(List<String> judgeText) {
        this.judgeText = judgeText;
    }

    List<String> getJudgeText() {
        return judgeText;
    }

    public void setFirstHighlights(Set<Integer> firstHighlights) {
        this.firstHighlights = firstHighlights;
    }

    public void setSecondHighlights(Set<Integer> secondHighlights) {
        this.secondHighlights = secondHighlights;
    }

    Set<Integer> getFirstHighlights() {
        return firstHighlights;
    }

    Set<Integer> getSecondHighlights() {
        return secondHighlights;
    }

    Set<Integer> getFirstWhite() {
        return firstWhite;
    }

    Set<Integer> getSecondWhite() {
        return secondWhite;
    }

    Set<Integer> getFirstSingle() {
        return firstSingle;
    }

    Set<Integer> getSecondSingle() {
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

    public void getChains(Chain firstChain, Chain secondChain) {
        this.firstChain = firstChain;
        this.secondChain = secondChain;
        isSingle = firstChain.getLocations().isEmpty() || secondChain.getLocations().isEmpty();
        kowalskiAnalysis();
    }

    private String nonEmptyCap() {
        return isFirstEmpty ? "Первый" : "Второй";
    }

    private String nonEmpty() {
        return isFirstEmpty ? "первый" : "второй";
    }

    private String emptyCap() {
        return isFirstEmpty ? "Второй" : "Первый";
    }

    private String empty() {
        return isFirstEmpty ? "второй" : "первый";
    }


    private void kowalskiAnalysis() {
        if (isSingle) {
            isFirstEmpty = firstChain.getLocations().isEmpty();
            Chain notEmpty = isFirstEmpty ? secondChain : firstChain;
            if (notEmpty.getLocations().size() == 1) {
                conflType = Confl.NEWCHAIN_EMPTY;
                if (notEmpty.getLocations().get(0) instanceof Blank) {
                    info = nonEmptyCap().concat(" участник решил создать новую цепочку, состоящю из нулевой анафоры, в то время как").concat(empty()).concat(" участник не использовал эту нулевую анафору ни в одной из цепочек.");
                } else {
                    info = nonEmptyCap().concat(" участник решил создать новую цепочку, состоящю из фразы ").concat(notEmpty.getLocations().get(0).toString()).concat(", в то время как").concat(empty()).concat(" участник не использовал эту фразу ни в одной из цепочек.");
                }
            } else {
                conflType = Confl.CONTCHAIN_EMPTY;
                Location last = notEmpty.getLocations().get(notEmpty.getLocations().size() - 1);

                if (last instanceof Blank) {
                    info = nonEmptyCap().concat(" участник решил продолжить цепочку нулевой анафорой, в то время как").concat(empty()).concat(" участник не использовал эту нулевую анафору ни в одной из цепочек.");
                } else {
                    info = nonEmptyCap().concat(" участник решил продолжить цепочку фразой ").concat(notEmpty.getLocations().get(0).toString()).concat(", в то время как").concat(empty()).concat(" участник не использовал эту фразу ни в одной из цепочек.");
                }
            }
        } else {
            if (firstChain.getLocations().size() == 1 && secondChain.getLocations().size() == 1) {
                Location firstLast = firstChain.getLocations().get(0);
                Location secondLast = secondChain.getLocations().get(0);
                if (firstLast.equals(secondLast)) {
                    conflType = Confl.NEW_SAME;

                    if (firstLast instanceof Blank) {
                        info = "Оба участника решили создать новую цепочку из нулевой анафоры";
                    } else {
                        info = "Оба участника решили создать новую цепочку из фразы ".concat(firstLast.toString());
                    }
                } else {
                    conflType = Confl.NEW_DIF;
                    if (firstLast instanceof Blank) {
                        if (secondLast instanceof Blank) {
                            info = "Первый уастник решил создать новую цепочку из одной нулевой анафоры, а второй участник решил создать новую цепочку из другой";
                        } else {
                            info = "Первый участник решил создать новую цепочку из нулевой анафоры, а второй участник решил создать новую цепоку из фразы ".concat(secondLast.toString());
                        }
                    } else {
                        if (secondLast instanceof Blank) {
                            info = "Первый уастник решил создать новую цепочку из фразы ".concat(firstLast.toString()).concat(", а второй участник решил создать новую цепочку из нулевой анафоры");
                        } else {
                            info = "Первый уастник решил создать новую цепочку из фразы ".concat(firstLast.toString()).concat(", а второй участник решил создать новую цепочку из фразы ").concat(secondLast.toString());
                        }
                    }
                }
            } else {
                if (firstSingle.isEmpty() || secondSingle.isEmpty()) {
                    conflType = Confl.ADD_EMPTY_SAME;
                    isFirstEmpty = firstSingle.isEmpty();
                    Chain notEmpty = isFirstEmpty ? firstChain : secondChain;

                    Location last = notEmpty.getLocations().get(notEmpty.getLocations().size() - 1);

                    if (last instanceof Blank) {
                        info = nonEmptyCap().concat(" участник решил продолжить цепочку нулевой анафорой, в то время как ".concat(empty()).concat(" участник не вносил эту нулевую анафору в эту цепочку"));
                    } else {
                        info = nonEmptyCap().concat(" участник решил продолжить цепочку фразой ").concat(last.toString()).concat(", в то время как ".concat(empty()).concat(" участник не вносил эту фразу в эту цепочку"));
                    }
                } else {
                    if (firstChain.getLocations().size() == 1 || secondChain.getLocations().size() == 1){
                        conflType = Confl.NEW_ADD;
                        isFirstEmpty = firstChain.getLocations().size() == 1;
                        Chain notEmpty = isFirstEmpty ? firstChain : secondChain;

                        Location last = notEmpty.getLocations().get(notEmpty.getLocations().size() - 1);

                        if (last instanceof Blank){
                            info = nonEmptyCap().concat(" участник решил продолжить цепочку нулевой анафорой, в то время как ".concat(empty()).concat(" участник решил создать новую цепочку из этой же нулевой анафор."));
                        } else {
                            info = nonEmptyCap().concat(" участник решил продолжить цепочку фразой".concat(last.toString()).concat(", в то время как ".concat(empty()).concat(" участник решил создать новую цепочку из этой же фразы.")));
                        }
                    } else {
                        conflType = Confl.ADD_SAME;
                        Location last = firstChain.getLocations().get(firstChain.getLocations().size() - 1);

                        if (last instanceof Blank) {
                            info = "Один из участников решил добавить нулевую анафору в одну цепочку, в то время как второй участник решил добавить ее же в другую";
                        } else {
                            info = "Один из участников решил добавить фразу " + last.toString() + ", в то время как второкй участнки решил добавить ее же в другую";
                        }
                    }
                }
            }
        }
    }
}