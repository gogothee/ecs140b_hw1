/* *** This file is given as part of the programming assignment. *** */
import java.util.ArrayList;
import java.util.*;
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
	private	bi_hash b=new bi_hash();
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
      	protected ExprTreeNode child;
      	@Override public String toString(){
			return "" + child;
      	}
	  	public ExprTreeNode(){
	  	}
	 	public ExprTreeNode(ExprTreeNode n){
			child = n;
	  	}
	  	public ExprTreeNode eval(){
			return child.eval(); 
	  	}
    }

    class AtomTreeNode extends ExprTreeNode{
 	 public AtomTreeNode(){
	 }
	 @Override public String toString(){
		return ""+child;
	 }
     public AtomTreeNode(AtomTreeNode n){
		 child = n;
	 }
	 @Override public ExprTreeNode eval(){
		 return child.eval();
	 }
    }

    class NumberTreeNode extends AtomTreeNode{
        private int value;
  	    public void set(int i){
        	value=i;
        }
        @Override public String toString(){
        	return "" + value;
      	}
      	public NumberTreeNode(int i){
        	value =i;
      	}
	  	@Override public ExprTreeNode eval(){
	  		return this;
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
      @Override public String toString(){
        return value;
      }
	  @Override public ExprTreeNode eval(){
		  if(! b.hash.containsKey(value)){
				System.out.println("\""+value+"\" is not bound as a parameter");
			return new ListTreeNode();
		  }
		  return this;
	  }
    }

    class ListTreeNode extends ExprTreeNode{
		@Override public String toString(){
        	if(child==null){
				return "()";
			}
			return "(" + child  + ")";
		}
	  	public ListTreeNode(){
	  	}
		public ListTreeNode(ListTreeNode n){
			child=n;
	  	}
		@Override public ExprTreeNode eval(){
			if(child==null){
				return this;
			}
			return child.eval();

		}
    }

    class ExprListTreeNode extends ListTreeNode{
        private ArrayList<ExprTreeNode> arr;
	    public ExprListTreeNode(){
			arr = new ArrayList<ExprTreeNode>();
	  	}
      	public void add(ExprTreeNode e){
        	arr.add(e);
      	}
	  	@Override public String toString(){
			String s = "";
			for(ExprTreeNode e: arr){
				s += e + " ";
			}
			s=s.substring(0,s.length()-1);
			return s;
	  	}
		@Override public ExprTreeNode eval(){
			ExprTreeNode first = arr.get(0);
			if(first.child.child instanceof NumberTreeNode){
				System.out.println("can't use number as function name");
				return new ListTreeNode();
			}else if(first.child.child instanceof IdTreeNode &&
					! b.hash.containsKey(first.child.child)){
				System.out.println("\""+first.child.child+"\" is not bound as a parameter");
				return new ListTreeNode();
			}
			return this;
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
				ExprTreeNode e = expr();
                System.out.println(e);
                // in later parts:
                //   print out expression
                //   evaluate expression
                //   print out value of evaluated expression
                // note that an error in evaluating (at any level) will
                // return nil and evaluation will continue.
				System.out.println(e.eval());

            }
			
            catch (ParsingExpressionException e) {
                System.out.println( "trying to recover from parse error");
                gobble();
            }
        }
    }

    ExprTreeNode expr() throws ParsingExpressionException {
        ExprTreeNode e = new ExprTreeNode();
		if( is(TK.LPAREN) )
            e = new ExprTreeNode(list());
        else if( is(TK.ID) || is(TK.NUM) )
            e = new ExprTreeNode(atom());
        else if( is(TK.QUOTE) ) {
// add some code here in part 6
			e = new IdTreeNode(tok.string);

			scan();
        }
        else {
            parse_error("bad start of expression:"+tok);
            /*NOTREACHED*/
        }
        return e;
    }
    
    ListTreeNode list() throws ParsingExpressionException {
        ListTreeNode n = new ListTreeNode();
		level++;
        mustbe(TK.LPAREN);
		if(! is(TK.RPAREN)){
          		ListTreeNode e = expr_list();
				n = new ListTreeNode(e);
      	}
        level--;
        mustbe(TK.RPAREN);
		return n;
    }
	
	ExprListTreeNode expr_list() throws ParsingExpressionException{
		ExprListTreeNode n = new ExprListTreeNode();
		while(! is(TK.RPAREN)){
			ExprTreeNode e = expr();
			n.add(e);
		}
		return n;
	}
    
    
    AtomTreeNode atom() throws ParsingExpressionException {
     	AtomTreeNode n = new AtomTreeNode();
		if( is(TK.ID) ) {
			n = new AtomTreeNode( new IdTreeNode(tok.string));
            mustbe(TK.ID);
        }
        else if( is(TK.NUM) ) {
			n = new AtomTreeNode(new
					NumberTreeNode(Integer.parseInt(tok.string)));
            mustbe(TK.NUM);
		}
        else {
            parse_error("oops -- bad atom");
            /*NOTREACHED*/
        }
		return n;
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

//built in functions
class bi_fun{
	protected String name;
	protected String special;
	protected int arity;
	
	public bi_fun(String n, String s, int a){
		name=n;
		special=s;
		arity=a;
	}
	public void print(){
		System.out.format("%15s %12s %4d      builtin\n", name,
				special, arity);
	}
}
class bi_hash{
	private ArrayList<bi_fun> arr = new ArrayList<bi_fun>();
	public Hashtable<String, bi_fun> hash = new Hashtable<String,
		bi_fun>();
	public bi_hash (){
		arr.add(new bi_fun( "show", "special", 0));
		arr.add(new bi_fun( "cons", "non-special", 2));
		arr.add(new bi_fun( "car", "non-special", 1));
		arr.add(new bi_fun( "cdr", "non-special", 1));
		arr.add(new bi_fun( "quote", "special", 1));
		arr.add(new bi_fun( "list", "non-special", -1));
		arr.add(new bi_fun( "append", "non-special", -1));
		arr.add(new bi_fun( "length", "non-special", 1));
		arr.add(new bi_fun( "+", "non-special", 2));
		arr.add(new bi_fun( "-", "non-special", 2));
		arr.add(new bi_fun( "*", "non-special", 2));
		arr.add(new bi_fun( "/", "non-special", 2));
		arr.add(new bi_fun( "=", "non-special", 2));
		arr.add(new bi_fun( "/=", "non-special", 2));
		arr.add(new bi_fun( "<", "non-special", 2));
		arr.add(new bi_fun( ">", "non-special", 2));
		arr.add(new bi_fun( "<=", "non-special", 2));
		arr.add(new bi_fun( ">=", "non-special", 2));
		arr.add(new bi_fun( "null", "non-special", 1));
		arr.add(new bi_fun( "atom", "non-special", 1));
		arr.add(new bi_fun( "listp", "non-special", 1));
		arr.add(new bi_fun( "integerp", "non-special", 1));
		arr.add(new bi_fun( "cond", "special", -1));
		for(bi_fun b: arr){
			hash.put(b.name,b);
		}
	}
	public void print(){
		for(bi_fun b:arr){
			b.print();
		}
	}
}
