/* *** This file is given as part of the programming assignment. *** */
import java.util.ArrayList;
public class Interpreter {

    // a scanner...
    private Scan scanner;

    // tok is global to all these parsing methods;
    // scan just calls the scanner's scan method and saves the result in tok.
    private Token tok; // the current token
    private void scan() {
        tok = scanner.scan();
    }

    // note on the first sets:
    // we cheat -- there is only a single token in the set,
    // so we just compare tkrep with the first token.

    // level at which parsing;
    // used to handle the problem of scan-ahead, given interactive system.
    private int level;

    // for error handling
    // to make this a bit friendlier in interactive environment;
    // handle parse errors by jumping back to main loop.

    class ParsingExpressionException extends Exception {
        public ParsingExpressionException(String msg){
//            super(msg); // call constructor in superclass (i.e., base class);
            // it outputs message and a bit more.
        }
    }

    class ExprTreeNode{
      protected ExprTreeNode left;
      protected ExprTreeNode right;
      public String print(){
        return left.print()+" "+right.print();
      }
      public void AddLeft(ExprTreeNode n){
        this.left=n;
      }
      public void AddRight(ExprTreeNode n){
        this.right=n;
      }
    }

    class AtomTreeNode extends ExprTreeNode{
      
    }

    class NumberTreeNode extends AtomTreeNode{
      private int value;
      public void set(int i){
        value=i;
      }
      @Override public String print(){
        return ""+value;
      }
      public NumberTreeNode(int i){
        value =i;
      }
    }

    class IdTreeNode extends AtomTreeNode{
      private String value;
      public IdTreeNode(String s){
        value=s;
      }
      public void set(String s){
        value=s;
      }
      @Override public String print(){
        return value;
      }
    }

    class ListTreeNode extends ExprTreeNode{
      @Override public String print(){
        return "( "+left.print()+" "+right.print()+" )";
      }
    }

    class ExprListTreeNode extends ListTreeNode{
      private ArrayList<ExprTreeNode> arr; 
      public void add(ExprTreeNode e){
        arr.add(e);
      }
    } 

    public Interpreter(String args[]) {
        scanner = new Scan(args);
        while( true ) {
            System.out.print( "> ");
            System.out.flush();
            // always reset since might previous might have failed
            level = 0;
            scan();
            if ( is(TK.EOF) ) break;
            try {
                // read and parse expression.
                expr();
                // in later parts:
                //   print out expression
                //   evaluate expression
                //   print out value of evaluated expression
                // note that an error in evaluating (at any level) will
                // return nil and evaluation will continue.
            }
            catch (ParsingExpressionException e) {
                System.out.println( "trying to recover from parse error");
                gobble();
            }
        }
    }

    void expr() throws ParsingExpressionException {
        if( is(TK.LPAREN) )
            list();
        else if( is(TK.ID) || is(TK.NUM) )
            atom();
        else if( is(TK.QUOTE) ) {
// add some code here in part 6
        }
        else {
            parse_error("bad start of expression:"+tok);
            /*NOTREACHED*/
        }
        
    }
    
    void list() throws ParsingExpressionException {
        level++;
        mustbe(TK.LPAREN);
		    if(! is(TK.RPAREN)){
          expr_list();
        }
        level--;
        mustbe(TK.RPAREN);
    }
	
	void expr_list() throws ParsingExpressionException{
		expr();
		while(! is(TK.RPAREN)){
      System.out.println(tok.string);
			expr();
		}
	}
    
    
    void atom() throws ParsingExpressionException {
        if( is(TK.ID) ) {
            mustbe(TK.ID);
        }
        else if( is(TK.NUM) ) {
            mustbe(TK.NUM);
        }
        else {
            parse_error("oops -- bad atom");
            /*NOTREACHED*/
        }
    }
    
    // is current token what we want?
    private boolean is(TK tk) {
        return tk == tok.kind;
    }

    // ensure current token is tk and skip over it.
    void mustbe(TK tk) throws ParsingExpressionException {
        if( !is(tk) ) {
            System.err.println( "mustbe: want " + tk + ", got " + tok );
            parse_error( "missing token (mustbe)" );
        }
        // read ahead to next token only if not at top level.
        // this enables returning to main loop after parse entire expression;
        // otherwise would need to wait for user to type first
        // part of next expression before evaluating current expression,
        // which wouldn't be so good in interactive environment.
        // (so main loop always calls scan before calling expr)
        if (level > 0) scan();
    }
    
    void parse_error(String msg) throws ParsingExpressionException {
        System.err.println( "can't parse: " + msg );
        throw new ParsingExpressionException("problem parsing");
    }
    
    // used in recovering from errors.
    // gobble up all tokens up until something that could start an expression.
    // obviously, not entirely effective...
    // another possibility would be to gobble up to matching ) or ]
    // but that's not 100% effective either.
    void gobble() {
        while( level > 0 &&
               !is(TK.LPAREN) &&
               !is(TK.ID) &&
               !is(TK.NUM) &&
               !is(TK.EOF) ) {
            scan();
        }
    }

}
