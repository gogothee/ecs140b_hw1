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
		public ExprTreeNode getLeaf(){
			if(child != null){
				return child.getLeaf();
			}
			return null;
		}
		public ArrayList<ExprTreeNode> tail(){
			return null;
		}
		public ArrayList<ExprTreeNode> arr(){
			return null;
		}
		public ArrayList<ExprTreeNode> appendFront(ExprTreeNode e){
			return null;
		}
		public ArrayList<ExprTreeNode> appendEnd(ArrayList<ExprTreeNode> a){
			return null;
		}
		public int value(){
			if(child !=null){
				return child.value();
			}
			return -1;
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
	 @Override public int value(){
		if(child !=null){
				return child.value();
			}
			return -1;
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
		@Override public int value(){
			return value;
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
		public ListTreeNode(ExprTreeNode n){
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
        public ArrayList<ExprTreeNode> arr;
	    public ExprListTreeNode(){
			arr = new ArrayList<ExprTreeNode>();
	  	}
		public ExprListTreeNode(ArrayList<ExprTreeNode> a){
			arr = a;
		}
      	public void add(ExprTreeNode e){
        	arr.add(e);
      	}
	  	@Override public String toString(){
			String s = "";
			for(ExprTreeNode e: arr){
				s += e + " ";
			}
			if(s.length()>1){
				s=s.substring(0,s.length()-1);
			}
			return s;
	  	}
		@Override public ArrayList<ExprTreeNode>
			appendFront(ExprTreeNode e){
			arr.add(0,e);
			return arr;
		}
		@Override public ExprTreeNode getLeaf(){
			if(arr.size()>0){
				return arr.get(0);
			}
			return null;
		}
		@Override public ArrayList<ExprTreeNode> tail(){
			arr.remove(0);
			if(arr.size()>1){
				return null;
			}
			return arr;
		}
		@Override public ArrayList<ExprTreeNode>
			appendEnd(ArrayList<ExprTreeNode> array){
			arr.addAll(array);
			return arr;
		}
		@Override public ArrayList<ExprTreeNode> arr(){
			return arr;
		}
		@Override public ExprTreeNode eval(){
			ExprTreeNode first = arr.get(0);
			if(first.child.child instanceof NumberTreeNode){
				System.out.println("can't use number as function name");
				return new ListTreeNode();
			}else if(first.child.child instanceof IdTreeNode){
				if(	! b.hash.containsKey(""+first.child.child)){
					System.out.println("\""+first.child.child+"\" is not bound as a function");
					return new ListTreeNode();
				}else{
					return b.hash.get(""+first.child.child).eval(arr);
				}
			}else if(first.child instanceof ListTreeNode){
				if( first.child.child == null){
					System.out.println("null car in eval");
					return new ListTreeNode();
				}else if(first.child instanceof
						ListTreeNode){
					System.out.println("bad cons'ed object as function/lambda");
					return new ListTreeNode();
				}
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
			// changed grammer by making expresiion ::=atom|list|"'" list
			ExprListTreeNode n = new ExprListTreeNode(); 
			n.add(new ExprTreeNode(new AtomTreeNode(new
							IdTreeNode("quote"))));
			scan();
			n.add(expr());
			e = new ListTreeNode(n);
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



//built in functions
class bi_fun{
	protected String name;
	protected String special;
	protected int arity;
	public bi_fun(){
	}
	
	public bi_fun(String n, String s, int a){
		name=n;
		special=s;
		arity=a;
	}
	public void print(){
		System.out.format("%15s %12s %4d      builtin\n", name,
				special, arity);
	}
	public ExprTreeNode eval(ArrayList<ExprTreeNode> arg){
		return null;
	}
}
class show_fun extends bi_fun{
	public show_fun(String n, String s, int a){
		name=n;
		special=s;
		arity=a;
	}

	@Override public ExprTreeNode eval(ArrayList<ExprTreeNode> arg){
		b.print();
		return new ListTreeNode();
	}
}
class quote_fun extends bi_fun{
	public quote_fun(String n, String s, int a){
		name=n;
		special=s;
		arity=a;
	}

	@Override public ExprTreeNode eval(ArrayList<ExprTreeNode> arg){
		arg.remove(0);
		if(arg.isEmpty()){
			System.out.println("quote given 0 args, but needs 1 args");
			return new ListTreeNode();
		}
		return arg.get(0);
	}
}
class list_fun extends bi_fun{
	public list_fun(String n, String s, int a){
		name=n;
		special=s;
		arity=a;
	}

	@Override public ExprTreeNode eval(ArrayList<ExprTreeNode> arg){
		arg.remove(0);
		if(arg.isEmpty()){
			return new ListTreeNode();
		}
		ExprListTreeNode n = new ExprListTreeNode();
		for(ExprTreeNode e:arg){
			n.add(e.eval());
		}
		return new ListTreeNode(n);
	}
}
class listp_fun extends bi_fun{
	public listp_fun(String n, String s, int a){
		name=n;
		special=s;
		arity=a;
	}

	@Override public ExprTreeNode eval(ArrayList<ExprTreeNode> arg){
		arg.remove(0);
		if(arg.isEmpty()){
			System.out.println("listp given 0 args, but needs 1 args");
			return new ListTreeNode();
		}
		ExprListTreeNode n = new ExprListTreeNode();
		for(ExprTreeNode e:arg){
			n.add(e.eval());
		}
		ExprTreeNode e = n.arr.get(0);
		if(n.arr.get(0).child !=null){
			e = n.arr.get(0).child;
		}
		if(e instanceof ListTreeNode){
			return new NumberTreeNode(-999);
		}
		return new ListTreeNode();
	}
}
class atom_fun extends bi_fun{
	public atom_fun(String n, String s, int a){
		name=n;
		special=s;
		arity=a;
	}

	@Override public ExprTreeNode eval(ArrayList<ExprTreeNode> arg){
		arg.remove(0);
		if(arg.isEmpty()){
			System.out.println("atom given 0 args, but needs 1 args");
			return new ListTreeNode();
		}
		ExprListTreeNode n = new ExprListTreeNode();
		for(ExprTreeNode e:arg){
			n.add(e.eval());
		}
		ExprTreeNode e = n.arr.get(0);
		if(n.arr.get(0).child !=null){
			e = n.arr.get(0).child;
		}
		if(e instanceof AtomTreeNode || e.child == null){
			return new NumberTreeNode(-999);
		}
		return new ListTreeNode();
	}
}
class null_fun extends bi_fun{
	public null_fun(String n, String s, int a){
		name=n;
		special=s;
		arity=a;
	}

	@Override public ExprTreeNode eval(ArrayList<ExprTreeNode> arg){
		arg.remove(0);
		if(arg.isEmpty()){
			System.out.println("null given 0 args, but needs 1 args");
			return new ListTreeNode();
		}
		ExprListTreeNode n = new ExprListTreeNode();
		for(ExprTreeNode e:arg){
			n.add(e.eval());
		}
		ExprTreeNode e = n.arr.get(0);
		if(n.arr.get(0).child !=null){
			e = n.arr.get(0).child;
		}
		if( e.child == null && e instanceof ListTreeNode){
			return new NumberTreeNode(-999);
		}
		return new ListTreeNode();
	}
}
class integerp_fun extends bi_fun{
	public integerp_fun(String n, String s, int a){
		name=n;
		special=s;
		arity=a;
	}

	@Override public ExprTreeNode eval(ArrayList<ExprTreeNode> arg){
		arg.remove(0);
		if(arg.isEmpty()){
			System.out.println("null given 0 args, but needs 1 args");
			return new ListTreeNode();
		}
		ExprListTreeNode n = new ExprListTreeNode();
		for(ExprTreeNode e:arg){
			n.add(e.eval());
		}
		ExprTreeNode e = n.arr.get(0);
		if(n.arr.get(0).child !=null){
			e = n.arr.get(0).child;
		}
		if(e instanceof NumberTreeNode){
			return new NumberTreeNode(-999);
		}
		return new ListTreeNode();
	}
}
class car_fun extends bi_fun{
	public car_fun(String n, String s, int a){
		name=n;
		special=s;
		arity=a;
	}

	@Override public ExprTreeNode eval(ArrayList<ExprTreeNode> arg){
		arg.remove(0);
		if(arg.isEmpty()){
			System.out.println("null given 0 args, but needs 1 args");
			return new ListTreeNode();
		}
		ExprListTreeNode n = new ExprListTreeNode();
		for(ExprTreeNode e:arg){
			n.add(e.eval());
		}
		if(n.arr.get(0).child != null){
			return n.arr.get(0).getLeaf();
		}
		return new ListTreeNode();
	}
}
class cdr_fun extends bi_fun{
	public cdr_fun(String n, String s, int a){
		name=n;
		special=s;
		arity=a;
	}

	@Override public ExprTreeNode eval(ArrayList<ExprTreeNode> arg){
		arg.remove(0);
		if(arg.isEmpty()){
			System.out.println("null given 0 args, but needs 1 args");
			return new ListTreeNode();
		}
		ExprListTreeNode n = new ExprListTreeNode();
		for(ExprTreeNode e:arg){
			n.add(e.eval());
		}
		if(n.arr.get(0).child != null){
			ArrayList<ExprTreeNode> e =
				n.arr.get(0).child.child.tail();
			return new ListTreeNode( new
				ExprListTreeNode(e));
		}
		return new ListTreeNode();
	}
}
class cons_fun extends bi_fun{
	public cons_fun(String n, String s, int a){
		name=n;
		special=s;
		arity=a;
	}

	@Override public ExprTreeNode eval(ArrayList<ExprTreeNode> arg){
		arg.remove(0);
		if(arg.isEmpty()){
			System.out.println("null given 0 args, but needs 1 args");
			return new ListTreeNode();
		}

		ExprListTreeNode n = new ExprListTreeNode();
		for(ExprTreeNode e:arg){
			n.add(e.eval());
		}
		if(n.arr.size() == 2){
			 ExprTreeNode car = n.arr.get(0);
			 ExprTreeNode cdr = n.arr.get(1);
			 if(cdr instanceof NumberTreeNode ){
				System.out.println("cons's 2nd argument is non-list");
				return new ListTreeNode();
			 }else if(cdr.child == null){
				return new ListTreeNode(car);
			 }
			 return new ListTreeNode( new
				 ExprListTreeNode(cdr.child.child.appendFront(car)));
		}
		return new ListTreeNode();
	}
}
class length_fun extends bi_fun{
	public length_fun(String n, String s, int a){
		name=n;
		special=s;
		arity=a;
	}

	@Override public ExprTreeNode eval(ArrayList<ExprTreeNode> arg){
		arg.remove(0);
		if(arg.isEmpty()){
			System.out.println("null given 0 args, but needs 1 args");
			return new ListTreeNode();
		}

		ExprListTreeNode n = new ExprListTreeNode();
		for(ExprTreeNode e:arg){
			n.add(e.eval());
		}
		if(n.arr.size() > 1){
			System.out.println("length given 2 args, but needs 1 args");
			return new ListTreeNode();
		}else if(n.arr.get(0) instanceof NumberTreeNode){
			System.out.println("length given a non-list or an impure list (dotted pair at end of list)");
			return new ListTreeNode();
		}
		if(n.arr.get(0).child == null){
			return new NumberTreeNode(0);
		}else if(n.arr.get(0).child.child == null){
			if(n.arr.get(0).child.arr() == null){
				return new NumberTreeNode(1);
			}
			return new
				NumberTreeNode(n.arr.get(0).child.arr().size());
		}

		return new NumberTreeNode(n.arr.get(0).child.child.arr().size());
	}
}
class append_fun extends bi_fun{
	public append_fun(String n, String s, int a){
		name=n;
		special=s;
		arity=a;
	}

	@Override public ExprTreeNode eval(ArrayList<ExprTreeNode> arg){
		arg.remove(0);
		if(arg.isEmpty()){
			return new ListTreeNode();
		}

		ExprListTreeNode n = new ExprListTreeNode();
		for(ExprTreeNode e:arg){
			n.add(e.eval());
		}
		int count = 0;
		ExprListTreeNode o = new ExprListTreeNode();
		for(ExprTreeNode t:n.arr){
			if(t.child != null){
				if(t.child instanceof AtomTreeNode){
					System.out.println("append given non-list");
					return new ListTreeNode();
				}
				if(t.child instanceof ExprListTreeNode){
					o.appendEnd(t.child.arr());
				}else{
					o.appendEnd(t.child.child.arr());
				}
			}
			if(count > 0 && t instanceof NumberTreeNode){
				System.out.println("append given non-list");
				return new ListTreeNode();
			}
			count++;
		}

		return new ListTreeNode(o);
	}
}
class plus_fun extends bi_fun{
	public plus_fun(String n, String s, int a){
		name=n;
		special=s;
		arity=a;
	}

	@Override public ExprTreeNode eval(ArrayList<ExprTreeNode> arg){
		arg.remove(0);
		if(arg.size() != arity){
			System.out.println(name+" given "+arg.size()+" args, but needs "+arity+" args");
			return new ListTreeNode();
		}
		ExprListTreeNode n = new ExprListTreeNode();
		for(ExprTreeNode e:arg){
			n.add(e.eval());
		}
		return new
			NumberTreeNode(n.arr.get(0).value() + n.arr.get(1).value());
	}
}
class minus_fun extends bi_fun{
	public minus_fun(String n, String s, int a){
		name=n;
		special=s;
		arity=a;
	}

	@Override public ExprTreeNode eval(ArrayList<ExprTreeNode> arg){
		arg.remove(0);
		if(arg.size() != arity){
			System.out.println(name+" given "+arg.size()+" args, but needs "+arity+" args");
			return new ListTreeNode();
		}
		ExprListTreeNode n = new ExprListTreeNode();
		for(ExprTreeNode e:arg){
			n.add(e.eval());
		}
		return new
			NumberTreeNode(n.arr.get(0).value() - n.arr.get(1).value());
	}
}
class mult_fun extends bi_fun{
	public mult_fun(String n, String s, int a){
		name=n;
		special=s;
		arity=a;
	}

	@Override public ExprTreeNode eval(ArrayList<ExprTreeNode> arg){
		arg.remove(0);
		if(arg.size() != arity){
			System.out.println(name+" given "+arg.size()+" args, but needs "+arity+" args");
			return new ListTreeNode();
		}
		ExprListTreeNode n = new ExprListTreeNode();
		for(ExprTreeNode e:arg){
			n.add(e.eval());
		}
		return new
			NumberTreeNode(n.arr.get(0).value() * n.arr.get(1).value());
	}
}
class divide_fun extends bi_fun{
	public divide_fun(String n, String s, int a){
		name=n;
		special=s;
		arity=a;
	}

	@Override public ExprTreeNode eval(ArrayList<ExprTreeNode> arg){
		arg.remove(0);
		if(arg.size() != arity){
			System.out.println(name+" given "+arg.size()+" args, but needs "+arity+" args");
			return new ListTreeNode();
		}
		ExprListTreeNode n = new ExprListTreeNode();
		for(ExprTreeNode e:arg){
			n.add(e.eval());
		}
		int divisor = n.arr.get(1).value();
		if (divisor == 0){
			System.out.println("divide by zero");
			return new NumberTreeNode(-9999999);
		}
		return new
			NumberTreeNode(n.arr.get(0).value() / divisor );
	}
}
class equal_fun extends bi_fun{
	public equal_fun(String n, String s, int a){
		name=n;
		special=s;
		arity=a;
	}

	@Override public ExprTreeNode eval(ArrayList<ExprTreeNode> arg){
		arg.remove(0);
		if(arg.size() != arity){
			System.out.println(name+" given "+arg.size()+" args, but needs "+arity+" args");
			return new ListTreeNode();
		}
		ExprListTreeNode n = new ExprListTreeNode();
		for(ExprTreeNode e:arg){
			n.add(e.eval());
		}
		int first =  n.arr.get(0).value();
		int second =  n.arr.get(1).value();
		if((!(n.arr.get(0) instanceof NumberTreeNode) || !(n.arr.get(1)
				instanceof NumberTreeNode))&&(first==-1||second==-1)){
			System.out.println("builtin arithmetic rel op given non-number");
			return new ListTreeNode();
		}
		if(first == second){
			return new
				NumberTreeNode(-999);
		}
		return new ListTreeNode();
	}
}
class notequal_fun extends bi_fun{
	public notequal_fun(String n, String s, int a){
		name=n;
		special=s;
		arity=a;
	}

	@Override public ExprTreeNode eval(ArrayList<ExprTreeNode> arg){
		arg.remove(0);
		if(arg.size() != arity){
			System.out.println(name+" given "+arg.size()+" args, but needs "+arity+" args");
			return new ListTreeNode();
		}
		ExprListTreeNode n = new ExprListTreeNode();
		for(ExprTreeNode e:arg){
			n.add(e.eval());
		}
		int first =  n.arr.get(0).value();
		int second =  n.arr.get(1).value();
		if(first != second){
			return new
				NumberTreeNode(-999);
		}
		return new ListTreeNode();
	}
}
class lt_fun extends bi_fun{
	public lt_fun(String n, String s, int a){
		name=n;
		special=s;
		arity=a;
	}

	@Override public ExprTreeNode eval(ArrayList<ExprTreeNode> arg){
		arg.remove(0);
		if(arg.size() != arity){
			System.out.println(name+" given "+arg.size()+" args, but needs "+arity+" args");
			return new ListTreeNode();
		}
		ExprListTreeNode n = new ExprListTreeNode();
		for(ExprTreeNode e:arg){
			n.add(e.eval());
		}
		int first =  n.arr.get(0).value();
		int second =  n.arr.get(1).value();
		if(first < second){
			return new
				NumberTreeNode(-999);
		}
		return new ListTreeNode();
	}
}
class gt_fun extends bi_fun{
	public gt_fun(String n, String s, int a){
		name=n;
		special=s;
		arity=a;
	}

	@Override public ExprTreeNode eval(ArrayList<ExprTreeNode> arg){
		arg.remove(0);
		if(arg.size() != arity){
			System.out.println(name+" given "+arg.size()+" args, but needs "+arity+" args");
			return new ListTreeNode();
		}
		ExprListTreeNode n = new ExprListTreeNode();
		for(ExprTreeNode e:arg){
			n.add(e.eval());
		}
		int first =  n.arr.get(0).value();
		int second =  n.arr.get(1).value();
		if(first > second){
			return new
				NumberTreeNode(-999);
		}
		return new ListTreeNode();
	}
}
class lte_fun extends bi_fun{
	public lte_fun(String n, String s, int a){
		name=n;
		special=s;
		arity=a;
	}

	@Override public ExprTreeNode eval(ArrayList<ExprTreeNode> arg){
		arg.remove(0);
		if(arg.size() != arity){
			System.out.println(name+" given "+arg.size()+" args, but needs "+arity+" args");
			return new ListTreeNode();
		}
		ExprListTreeNode n = new ExprListTreeNode();
		for(ExprTreeNode e:arg){
			n.add(e.eval());
		}
		int first =  n.arr.get(0).value();
		int second =  n.arr.get(1).value();
		if(first <= second){
			return new
				NumberTreeNode(-999);
		}
		return new ListTreeNode();
	}
}
class gte_fun extends bi_fun{
	public gte_fun(String n, String s, int a){
		name=n;
		special=s;
		arity=a;
	}

	@Override public ExprTreeNode eval(ArrayList<ExprTreeNode> arg){
		arg.remove(0);
		if(arg.size() != arity){
			System.out.println(name+" given "+arg.size()+" args, but needs "+arity+" args");
			return new ListTreeNode();
		}
		ExprListTreeNode n = new ExprListTreeNode();
		for(ExprTreeNode e:arg){
			n.add(e.eval());
		}
		int first =  n.arr.get(0).value();
		int second =  n.arr.get(1).value();
		if(first >= second){
			return new
				NumberTreeNode(-999);
		}
		return new ListTreeNode();
	}
}
class cond_fun extends bi_fun{
	public cond_fun(String n, String s, int a){
		name=n;
		special=s;
		arity=a;
	}

	@Override public ExprTreeNode eval(ArrayList<ExprTreeNode> arg){
		arg.remove(0);
		if(arg.isEmpty()){
			return new ListTreeNode();
		}
		for(ExprTreeNode e:arg){
			if(e.child != null&&e.child.child!=null){
				ArrayList<ExprTreeNode> arr= e.child.child.arr();
				if(arr.get(0).eval().value()==-999||arr.get(0).child.child
						instanceof NumberTreeNode){
					if(arr.size()==1){
						return new NumberTreeNode(-999);
					}
					return arr.get(arr.size()-1).eval();
				}
			}
		}

		return new ListTreeNode();
	}
}

// Add built in functions into hash
class bi_hash{
	private ArrayList<bi_fun> arr = new ArrayList<bi_fun>();
	public Hashtable<String, bi_fun> hash = new Hashtable<String,
		bi_fun>();
	public bi_hash (){
		arr.add(new show_fun( "show", "special", 0));
		arr.add(new cons_fun( "cons", "non-special", 2));
		arr.add(new car_fun( "car", "non-special", 1));
		arr.add(new cdr_fun( "cdr", "non-special", 1));
		arr.add(new quote_fun( "quote", "special", 1));
		arr.add(new list_fun( "list", "non-special", -1));
		arr.add(new append_fun( "append", "non-special", -1));
		arr.add(new length_fun( "length", "non-special", 1));
		arr.add(new plus_fun( "+", "non-special", 2));
		arr.add(new minus_fun( "-", "non-special", 2));
		arr.add(new mult_fun( "*", "non-special", 2));
		arr.add(new divide_fun( "/", "non-special", 2));
		arr.add(new equal_fun( "=", "non-special", 2));
		arr.add(new notequal_fun( "/=", "non-special", 2));
		arr.add(new lt_fun( "<", "non-special", 2));
		arr.add(new gt_fun( ">", "non-special", 2));
		arr.add(new lte_fun( "<=", "non-special", 2));
		arr.add(new gte_fun( ">=", "non-special", 2));
		arr.add(new null_fun( "null", "non-special", 1));
		arr.add(new atom_fun( "atom", "non-special", 1));
		arr.add(new listp_fun( "listp", "non-special", 1));
		arr.add(new integerp_fun( "integerp", "non-special", 1));
		arr.add(new cond_fun( "cond", "special", -1));
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
}
