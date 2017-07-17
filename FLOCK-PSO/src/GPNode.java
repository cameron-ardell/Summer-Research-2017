import java.util.ArrayList;
import java.util.Random;

//somehow is including class of ReturnValue

/*

SHOULD ADD FUNCTION TO CREATE RANDOM CONST VALUES?
Nodes should have a value called depth, probably an int

*/

public class GPNode {

	//
	// All types of nodes
	//
	public static enum NodeType {
		SEQUENCE, IF, ASSIGN, 
		VAR, CONST, 
		ADD, SUB, MULT, DIV, EXP, INC, DEC,
		LT, GT, EQ, LEQ, GEQ, 
		AND, OR, NEG
	}


	//
	// Return types for nodes to pass recursively in tree construction
	// 	and for evolving
	// F - double; B - boolean; N - null
	//
	public static enum ReturnType{F, B, N, S};
	
	//
	// Arrays for tree construction.
	// Top 3 are all nodes that return booleans and doubles respecitively, then all nodes.
	// Last 3 are nodetypes that gurantee no more children immediately or in one more generation
	//  iff the probability of selecting a terminal-ish nodetype is 100%.
	//
	public static NodeType[] allBoolReturn = {NodeType.LT, NodeType.GT, NodeType.LEQ, NodeType.GEQ, NodeType.EQ, NodeType.AND, NodeType.OR, NodeType.NEG};
	public static NodeType[] alldoubleReturn = {NodeType.VAR, NodeType.CONST, NodeType.ADD, NodeType.SUB, NodeType.MULT, NodeType.DIV, NodeType.EXP};
	public static NodeType[] allReturn = {
								NodeType.LT, NodeType.GT, NodeType.LEQ, NodeType.GEQ, NodeType.EQ,
								NodeType.AND, NodeType.OR, NodeType.NEG,
								NodeType.VAR, NodeType.CONST, NodeType.ADD, NodeType.SUB,
								NodeType.MULT, NodeType.DIV, NodeType.EXP,
								NodeType.IF, NodeType.ASSIGN, NodeType.INC, NodeType.DEC
							};
	public static NodeType[] sequenceReturn = {NodeType.IF, NodeType.ASSIGN, NodeType.INC, NodeType.DEC};
	public static NodeType[] terminalBool =  {NodeType.LT, NodeType.GT, NodeType.LEQ, NodeType.GEQ, NodeType.EQ};
	public static NodeType[] terminaldouble = {NodeType.VAR, NodeType.CONST};
	public static NodeType[] terminalAll = {NodeType.ASSIGN, NodeType.INC, NodeType.DEC};
	
	//Null return type arrays no longer using
	//public static NodeType[] allNullReturn = {NodeType.SEQUENCE, NodeType.IF, NodeType.ASSIGN, NodeType.INC, NodeType.DEC};
	//public static NodeType[] terminalNull = {NodeType.IF, NodeType.ASSIGN, NodeType.INC, NodeType.DEC};
	//public static NodeType[] termialAll = {NodeType.LT, NodeType.GT, NodeType.LEQ, NodeType.GEQ, NodeType.EQ,
	//										NodeType.VAR, NodeType.CONST,
	//										NodeType.ASSIGN, NodeType.INC, NodeType.DEC};


	//
	// Arrays for mutation 
	//
	public static NodeType[] allMathOp = {NodeType.ADD, NodeType.SUB, NodeType.MULT, NodeType.DIV, NodeType.EXP};
	public static NodeType[] allCompareOp = {NodeType.LT, NodeType.GT, NodeType.LEQ, NodeType.GEQ, NodeType.EQ};
	

	//
	// Constants
	//
	public static final double DOUBLE_FLAG = 0.0f;
	public static final boolean BOOLEAN_FLAG = false;
	public static final String STRING_FLAG = "";
	static Random rand = new Random();

	//
	// Attributes each node has
	//
	public NodeType nodeType;
	public GPNode parent;
	public ArrayList<GPNode> children;
	public String varName = "";
	public double constValue = 0;
	public ReturnType rt;

	// Only for conversion to arraylist
	public int index;


	// Constructor for if have all info
	GPNode(NodeType nodeType, GPNode parent, ArrayList<GPNode> children,
			String varName, double constValue, ReturnType rt) {

		this.nodeType = nodeType;
		this.parent = parent;
		
		if(children == null){
			this.children = new ArrayList<GPNode>();
		} else{
			this.children = children;
		}
		
		this.varName = varName;
		this.constValue = constValue;
		this.rt = rt;
	}

	// Constructor for if have only return type, parent
	GPNode(GPNode parent, ReturnType rt) {

		//assigning any type for now
		if (rt == ReturnType.F) {
			this.nodeType = NodeType.ADD;
		} else{
			this.nodeType = NodeType.GT;
		}
		this.parent = parent;
		this.children = new ArrayList<GPNode>();
		this.rt = rt;
		this.index = 0;
	}


	//evaluates node and gives return value
	public void run(ReturnValue retVal, Particle particle){

		ReturnValue retVal0 = new ReturnValue();
		ReturnValue retVal1 = new ReturnValue();
		ReturnValue retVal2 = new ReturnValue();

		
		switch(nodeType) {

		// sequence
		case SEQUENCE:
			for(int i = 0; i < children.size(); i++){
				children.get(i).run(retVal0, particle);
			}
			return;
	
		// if
		case IF:
			children.get(0).run(retVal0, particle);

			if (retVal0.getReturnVal(BOOLEAN_FLAG)) {
				children.get(1).run(retVal1, particle);
				return;
			}

			if (children.size() < 3) {
				return;
			}

			children.get(2).run(retVal2, particle);
			return;

		// variable 
		case VAR:
			retVal.setReturnVal(varName);
			retVal.setReturnVal(particle.getVarValue(varName));
			return;

		// constant
		case CONST:
			retVal.setReturnVal(constValue);
			return;

		// assigning new value to steve variable
		case ASSIGN:
			children.get(0).run(retVal0, particle);
			children.get(1).run(retVal1, particle);
			String ASSIGNvarName = retVal0.getReturnVal(STRING_FLAG);
			double value = retVal1.getReturnVal(DOUBLE_FLAG);
			particle.assignVariable(ASSIGNvarName, value);
			return;

		// addition
		case ADD:
			children.get(0).run(retVal0, particle);
			children.get(1).run(retVal1, particle);
			retVal.setReturnVal(retVal0.getReturnVal(DOUBLE_FLAG) + retVal1.getReturnVal(DOUBLE_FLAG));
			return;
			
		// subtraction
		case SUB:
			children.get(0).run(retVal0, particle);
			children.get(1).run(retVal1, particle);
			retVal.setReturnVal(retVal0.getReturnVal(DOUBLE_FLAG) - retVal1.getReturnVal(DOUBLE_FLAG));
			return;
			
		// multiplication
		case MULT:
			children.get(0).run(retVal0, particle);
			children.get(1).run(retVal1, particle);
			retVal.setReturnVal(retVal0.getReturnVal(DOUBLE_FLAG) * retVal1.getReturnVal(DOUBLE_FLAG));
			return;
			
		// division
		case DIV:
			children.get(0).run(retVal0, particle);
			children.get(1).run(retVal1, particle);
			if (retVal1.getReturnVal(DOUBLE_FLAG) != 0) {
				retVal.setReturnVal(retVal0.getReturnVal(DOUBLE_FLAG) / retVal1.getReturnVal(DOUBLE_FLAG));
			}
			return;
			
		// exponent
		case EXP:
			children.get(0).run(retVal0, particle);
			children.get(1).run(retVal1, particle);
			retVal.setReturnVal((double) Math.pow(retVal0.getReturnVal(DOUBLE_FLAG), retVal1.getReturnVal(DOUBLE_FLAG)));
			return;

		// ++
		case INC:
			children.get(0).run(retVal0, particle);
			String INCvarName = retVal0.getReturnVal(STRING_FLAG);
			particle.increment(INCvarName);
			return;

		// --
		case DEC:
			children.get(0).run(retVal0, particle);
			String DECvarName = retVal0.getReturnVal(STRING_FLAG);
			particle.decrement(DECvarName);
			return;
			
		// less than
		case LT:
			children.get(0).run(retVal0, particle);
			children.get(1).run(retVal1, particle);
			retVal.setReturnVal(retVal0.getReturnVal(DOUBLE_FLAG) < retVal1.getReturnVal(DOUBLE_FLAG));
			return;

		// greater than
		case GT:
			children.get(0).run(retVal0, particle);
			children.get(1).run(retVal1, particle);
			retVal.setReturnVal(retVal0.getReturnVal(DOUBLE_FLAG) > retVal1.getReturnVal(DOUBLE_FLAG));
			return;

		// less than or equal to
		case LEQ:
			children.get(0).run(retVal0, particle);
			children.get(1).run(retVal1, particle);
			retVal.setReturnVal(retVal0.getReturnVal(DOUBLE_FLAG) <= retVal1.getReturnVal(DOUBLE_FLAG));
			return;

		// greater than or equal to
		case GEQ:
			children.get(0).run(retVal0, particle);
			children.get(1).run(retVal1, particle);
			retVal.setReturnVal(retVal0.getReturnVal(DOUBLE_FLAG) >= retVal1.getReturnVal(DOUBLE_FLAG));
			return;

		// equal
		case EQ:
			children.get(0).run(retVal0, particle);
			children.get(1).run(retVal1, particle);
			retVal.setReturnVal(retVal0.getReturnVal(DOUBLE_FLAG) == retVal1.getReturnVal(DOUBLE_FLAG));
			return;

		// and
		case AND:
			children.get(0).run(retVal0, particle);
			children.get(1).run(retVal1, particle);
			retVal.setReturnVal(retVal0.getReturnVal(BOOLEAN_FLAG) && retVal1.getReturnVal(BOOLEAN_FLAG));
			return;

		// or
		case OR:
			children.get(0).run(retVal0, particle);
			children.get(1).run(retVal1, particle);
			retVal.setReturnVal(retVal0.getReturnVal(BOOLEAN_FLAG) || retVal1.getReturnVal(BOOLEAN_FLAG));
			return;

		// negate
		case NEG:
			children.get(0).run(retVal0, particle);
			retVal.setReturnVal(!retVal0.getReturnVal(BOOLEAN_FLAG));
			return;


		default:
			System.out.println("error: undefined NodeType");
			System.exit(0);
		}
	}



	//
	// Functions needed to create random trees
	//

	// for generating a nodeType when making trees
	public static NodeType terminal_type(double doubleFlag){
		int randVal = randomVal(0, terminaldouble.length);
		return terminaldouble[randVal];
	}
	public static NodeType terminal_type(boolean boolFlag){
		int randVal = randomVal(0, terminalBool.length);
		return terminalBool[randVal];
	}
	public static NodeType terminal_type(){
		int randVal = randomVal(0, terminalAll.length);
		return terminalAll[randVal];
	}

	// for nodes that just won't end
	public static NodeType active_type(double doubleFlag){
		int randVal = randomVal(0, alldoubleReturn.length);
		return alldoubleReturn[randVal];
	}
	public static NodeType active_type(boolean boolFlag){
		int randVal = randomVal(0, allBoolReturn.length);
		return allBoolReturn[randVal];
	}
	public static NodeType active_type(){
		int randVal = randomVal(0, allReturn.length);
		return allReturn[randVal];
	}
	public static NodeType active_type(String strFlag){
		int randVal = randomVal(0, sequenceReturn.length);
		return sequenceReturn[randVal];
	}

	
	// to get random value in range
	public static int randomVal(int min, int max) {
		int newVal = rand.nextInt((max - min)) + min;
		return newVal;
	}
	public static double randomVal(double min, double max) {
		double newVal = (double)rand.nextInt(((int)max - (int)min) ) + min;
		return newVal;
	}


	//get depth of node recursively
	public int get_depth(){
		int depth = 0;

		if(this.parent != null){
			depth += 1  + this.parent.get_depth();
		}
		return depth;
	}



}