import java.io.*;

// Note this code is written to aid the student in learning Java.
// A Java expert could certainly improve the efficiency, conciseness, and
// style of this code.

// the scanner is given as a class
// so that its implementation can be changed without affecting
// its users, which is also the motivation for providing
// the access (get_) functions; they allow the actual
// internal representation to be hidden from the user.
// in this case, the internal representation is very close to the
// interface provided by the access function, as it is likely to be in
// any scanner implementation, but using hiding in this manner
// is good programming practicd and serves as a good intro to Java.

public class Scan {

    // default max length of identifiers; truncate extra.
    public static final int MAXLEN_ID = 12;

    public Scan(String args[]) { // the constructor
        // open an input file if one specified on command line.
        // o.w., use standard input.
        try {
            if (args.length == 0) {
                InputStreamReader isr = new InputStreamReader(System.in);
                br = new BufferedReader(isr);
            }
            else if (args.length > 1) {
                System.err.println("too many command line arguments("+
                                   args.length+"); want 0 or 1" );
                System.exit(1);
            }
            else {
                FileReader fr = new FileReader(args[0]);
                br = new BufferedReader(fr);
            }
        }
        catch (Exception oops) {
            System.err.println("Exception opening file "+args[0]+"\n");
            oops.printStackTrace();
        }

        // initially, we pretend that we just read a newline.
        // hence linenumber is initialized to 0.
        c = '\n';
        linenumber = 0;
        putback = true;
        got_eof = false;
        token = "*NoToKEn*";
        tkrep = TK.none; // any value okay here
    }

    // internal state of scanner
    private BufferedReader br;  // input stream
    private String token;    // the token as a string
    private TK tkrep;        // the kind of token
    private int linenumber;  // line number

    private boolean got_eof; // true iff have seen EOF
    private boolean putback; // true iff put a char back
    private int c;           // current or putback char
                             // (int rather than char to handle EOF)
    private static final int EOF = -1;

    // call to advance token stream.
    // acts as a generator (iterator) over input.
    // returns Token
    public Token scan() {
        if( got_eof ) {
                System.err.println("scan: oops -- called after eof.");
                return new Token(TK.ERROR, "called after eof", linenumber);
        }

        while(true) {
                if( putback) {
                        putback = false;
                }
                else {
                    c = getchar();
                }
                if ( myisalpha((char) c) ) {
                        /* identifier. */
                                return new Token(TK.ID, buildID(), linenumber);
                }
                else if ( myisdigit((char) c) ) {
                        /* number. */
                                return new Token(TK.NUM, buildNUM(),
                                                 linenumber);
                }
                else {
                        switch( c ) {
                                case '(':
                                        return ccase('(',TK.LPAREN);
                                case ')':
                                        return ccase(')',TK.RPAREN);
                                // you'll need to handle quote
                                // in final part of your program.
                                case '\'':
                                        return ccase('\'',TK.QUOTE);
                                // dot not used in your program,
                                // except possibly in final part.
                                case '.':
                                        return ccase('.',TK.DOT);
                                case EOF:
                                        got_eof = true;
                                        return new Token(TK.EOF,
                                                         new String("*EOF*"),
                                                         linenumber);
                                case '\n':
                                        linenumber++;
                                        break;
                                case ' ':
                                case '\t':
                                case '\r': // for Windows (lines end in \r\n)
                                        break; // whitespace is easy to ignore
                                case ';': // gobble comments
                                        do {
                                                c = getchar();
                                        } while( c != '\n' && c != EOF );
                                        putback = true;
                                        break;
                                default:
                                    System.err.println(
                                         "scan: line "+linenumber+
                                         " bad char (ASCII " + c
                                         + ")");
                                        break;
                        }
                }

        }
    }


    private int getchar() {
        int c = EOF;
        try {
            c = br.read();
        } catch (java.io.IOException e) {
            System.err.println("oops ");
            e.printStackTrace();
        }
        return c;
    }

    private Token ccase(char c, TK r) {
        return new Token(r, new String(String.valueOf(c)), linenumber);
    }

    // rather than duplicating code, as done below,
    // could use method "pointer" technique for these build methods.

    // build up an ID str
    // (could use StringBuffer to make this more efficient...)
    private String buildID() {
        int k = 0;
        String str = "";
        do {
            str += (char) c;
            k++;
            c = getchar();
        } while( myisalpha((char) c) && k < MAXLEN_ID );
        putback = true;
        if( myisalpha((char) c) && k == MAXLEN_ID ) {
            do { c = getchar(); } while(myisalpha((char) c));
            System.err.println("scan: token too long -- truncated to "
                               + str);
        }
        return str;
    }

    // build up a NUM str
    // (could use StringBuffer to make this more efficient...)
    private String buildNUM() {
        int k = 0;
        String str = "";
        do {
            str += (char) c;
            k++;
            c = getchar();
        } while( myisdigit((char) c) && k < MAXLEN_ID );
        putback = true;
        if( myisdigit((char) c) && k == MAXLEN_ID ) {
            do { c = getchar(); } while(myisdigit((char) c));
            System.err.println("scan: token too long -- truncated to "
                               + str);
        }
        return str;
    }


    // Little Lisp's idea of what can form an identifier
    private static final String otheralphas = new String("+-*/<>=");
    private static boolean myisalpha(char c) {
        if (Character.isLetter(c)) return true;
        if (otheralphas.indexOf(c) >= 0) return true;
        return false;
    }

    // Little Lisp's idea of what can form a number
    // (could instead directly call Character.isDigit)
    private static boolean myisdigit(char c) {
        return Character.isDigit(c);
    }
}
