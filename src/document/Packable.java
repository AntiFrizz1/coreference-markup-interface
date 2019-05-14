package document;

/**
 * This interface simplifies sending and getting information.
 *
 * @author Vadim Baydyuk
 */
public interface Packable {
    /**
     * Pack all information in one string.
     * It simplifies sending of message.
     *
     * @return the string with all information.
     */
    String pack();

    String packSB(StringBuilder sb);


}
