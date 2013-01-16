// internal representations of tokens;
// (could redo these as enum type (now in Java 1.5), but not worth the effort.)

public class TK {
    private final String name;

    // declaring constructor as private prevents outsiders
    // from creating new tokens;
    // and so can test equality using ==.
    private TK(String name) {
        this.name = name;
    }
    public String toString() { // make it printable for debugging
        return name;
    }

    // each token is represented as a TK object.
    public static TK LPAREN = new TK("TK.LPAREN");       /* ( */
    public static TK RPAREN = new TK("TK.RPAREN");       /* ) */
    public static TK QUOTE  = new TK("TK.QUOTE");        /* ' */
    public static TK DOT    = new TK("TK.DOT");          /* . */
    public static TK ID     = new TK("TK.ID");           /* identifier */
    public static TK NUM    = new TK("TK.NUM");          /* number */

    public static TK EOF    = new TK("TK.EOF");          /* end of file */

    // TK.ERROR special error token kind (for scanner to return to parser)
    public static final TK ERROR  = new TK("TK.ERROR");

    public static TK none   = new TK("TK.none");
        /* TK_none marks end of each first set in parsing. */
        /* you might not need this. */
}
