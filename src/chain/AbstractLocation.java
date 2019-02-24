package chain;

/**
 * This class provides some extra fields for {@code Location}.
 *
 * @author Vadim Baydyuk
 * @see Location
 * @see Chain
 */

public abstract class AbstractLocation implements Location {

    /**
     * Number of text that contains this part of chain.
     */
    private int textId;

    AbstractLocation(int textId) {
        this.textId = textId;
    }
    @Override
    public int getTextId() {
        return textId;
    }

    abstract public void getPositions(StringBuilder sb);

    @Override
    public void pack(StringBuilder sb) {
        sb.append("TextId: ").append(textId).append(' ').append('\n');
        getPositions(sb);
    }
}
