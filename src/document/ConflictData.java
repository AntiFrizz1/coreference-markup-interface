package document;

import chain.Letter;

public class ConflictData {
    public int textId;
    public int teamOneId;
    public int teamTwoId;

    public Letter letter1;
    public Letter letter2;

    public ConflictData(Letter letter1, Letter letter2, int textId, int teamOneId, int teamTwoId) {
        this.letter1 = letter1;
        this.letter2 = letter2;
        this.teamOneId = teamOneId;
        this.teamTwoId = teamTwoId;
        this.textId = textId;
    }

    public ConflictData(String string) {
        String[] strings = string.split("\\|");
        this.letter1 = new Letter(strings[0]);
        this.letter2 = new Letter(strings[1]);
        this.textId = Integer.parseInt(strings[2]);
        this.teamOneId = Integer.parseInt(strings[3]);
        this.teamTwoId = Integer.parseInt(strings[4]);
    }

    @Override
    public String toString() {
        return letter1.toString() + "|" + letter2.toString() + "|" + textId + "|" + teamOneId + "|" + teamTwoId;
    }

}
