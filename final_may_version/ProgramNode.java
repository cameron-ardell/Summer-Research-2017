import java.util.Random;

//somehow is including class of ReturnValue

/*

SHOULD ADD FUNCTION TO CREATE RANDOM CONST VALUES?
Nodes should have a value called depth, probably an int

*/

public class ProgramNode {

	// all types of nodes
	public static enum NodeType {
		SEQUENCE, IF, ASSIGN, 
		VAR, CONST, 
		ADD, SUB, MULT, DIV, EXP, INC, DEC,
		LT, GT, EQ, LEQ, GEQ, 
		AND, OR, NEG
	}
	// Return types for nodes to pass recursively in tree construction
	// 	and for evolving
	// F - float; B - boolean; N - null
	public static enum ReturnType{F, B, N};
	
	//arrays for tree construction
	//top 3 are sorted by return value
	//last 3 are nodetypes that gurantee no more children immediately or in one more generation
	//  if the probability of selecting a terminal-ish nodetype is 100%
	public static NodeType[] allBoolReturn = {NodeType.LT, NodeType.GT, NodeType.LEQ, NodeType.GEQ, NodeType.EQ, NodeType.AND, NodeType.OR, NodeType.NEG};
	public static NodeType[] allFloatReturn = {NodeType.VAR, NodeType.CONST, NodeType.ADD, NodeType.SUB, NodeType.MULT, NodeType.DIV, NodeType.EXP};
	public static NodeType[] allNullReturn = {NodeType.SEQUENCE, NodeType.IF, NodeType.ASSIGN, NodeType.INC, NodeType.DEC};

	public static NodeType[] terminalBool =  {NodeType.LT, NodeType.GT, NodeType.LEQ, NodeType.GEQ, NodeType.EQ};
	public static NodeType[] terminalFloat = {NodeType.VAR, NodeType.CONST};
	public static NodeType[] terminalNull = {NodeType.IF, NodeType.ASSIGN, NodeType.INC, NodeType.DEC};

	//
	// Arrays for mutation 
	//
	public static NodeType[] allMathOp = {NodeType.ADD, NodeType.SUB, NodeType.MULT, NodeType.DIV, NodeType.EXP};
	public static NodeType[] allCompareOp = {NodeType.LT, NodeType.GT, NodeType.LEQ, NodeType.GEQ, NodeType.EQ};
	

	public static final float FLOAT_FLAG = 0.0f;
	public static final boolean BOOLEAN_FLAG = false;
	public static final String STRING_FLAG = "";

	
	public NodeType nodeType = null;
	public ProgramNode parent;
	public ProgramNode[] children;
	public String varName = null;
	public float constValue;
	public ReturnType rt = null;
	
	//for tree creation
	public float true_depth;

	
	// new constructor for node
	ProgramNode(NodeType nodeType, ProgramNode parent, ProgramNode[] children,
				String varName, float constValue, float true_depth, ReturnType rt) {

			this.nodeType = nodeType;
			this.parent = parent;
			this.children = children;
			this.varName = "";
			this.constValue = 0;
			this.true_depth = true_depth;
			this.rt = rt;

			if(nodeType == NodeType.VAR) { this.varName = varName; }
			//setting return value for constants
			if(nodeType == NodeType.CONST) { this.constValue = constValue; }

		}
	
	public float onePlus(float x){
			return x+1;
	}
	//dummy constructor
	ProgramNode(float hi){
		this.constValue = hi;
	}

	
	//old constructor for node, left so Steve code compatible
	ProgramNode(NodeType nodeType, ProgramNode parent, ProgramNode[] children,
			String varName, float constValue) {

		this.nodeType = nodeType;
		this.parent = parent;
		this.children = children;
		this.varName = "";
		this.constValue = 0;
		this.true_depth = 0;

		if(nodeType == NodeType.VAR) { this.varName = varName; }
		//setting return value for constants
		if(nodeType == NodeType.CONST) { this.constValue = constValue; }

	}

	//evaluates node and gives return value
	//this isn't right
	public void run(ReturnValue retVal, Boid boid){

		ReturnValue retVal0 = new ReturnValue();
		ReturnValue retVal1 = new ReturnValue();
		ReturnValue retVal2 = new ReturnValue();

		
		switch(nodeType) {

		// sequence
		case SEQUENCE:
			for(int i = 0; i < children.length; i++){
				children[i].run(retVal0, boid);
			}
			return;
	
		// if
		case IF:
			children[0].run(retVal0, boid);

			if (retVal0.getReturnVal(BOOLEAN_FLAG)) {
				children[1].run(retVal1, boid);
				return;
			}

			if (children.length < 3) {
				return;
			}

			children[2].run(retVal2, boid);
			return;

		// variable 
		case VAR:
			retVal.setReturnVal(varName);
			retVal.setReturnVal(boid.getVarValue(varName));
			return;

		// constant
		case CONST:
			retVal.setReturnVal(constValue);
			return;

		// assigning new value to steve variable
		case ASSIGN:
			children[0].run(retVal0, boid);
			children[1].run(retVal1, boid);
			String ASSIGNvarName = retVal0.getReturnVal(STRING_FLAG);
			float value = retVal1.getReturnVal(FLOAT_FLAG);
			boid.assignVariable(ASSIGNvarName, value);
			return;

		// addition
		case ADD:
			children[0].run(retVal0, boid);
			children[1].run(retVal1, boid);
			retVal.setReturnVal(retVal0.getReturnVal(FLOAT_FLAG) + retVal1.getReturnVal(FLOAT_FLAG));
			return;
			
		// subtraction
		case SUB:
			children[0].run(retVal0, boid);
			children[1].run(retVal1, boid);
			retVal.setReturnVal(retVal0.getReturnVal(FLOAT_FLAG) - retVal1.getReturnVal(FLOAT_FLAG));
			return;
			
		// multiplication
		case MULT:
			children[0].run(retVal0, boid);
			children[1].run(retVal1, boid);
			retVal.setReturnVal(retVal0.getReturnVal(FLOAT_FLAG) * retVal1.getReturnVal(FLOAT_FLAG));
			return;
			
		// division
		case DIV:
			children[0].run(retVal0, boid);
			children[1].run(retVal1, boid);
			if (retVal1.getReturnVal(FLOAT_FLAG) != 0) {
				retVal.setReturnVal(retVal0.getReturnVal(FLOAT_FLAG) / retVal1.getReturnVal(FLOAT_FLAG));
			}
			return;
			
		// exponent
		case EXP:
			children[0].run(retVal0, boid);
			children[1].run(retVal1, boid);
			retVal.setReturnVal((float) Math.pow(retVal0.getReturnVal(FLOAT_FLAG), retVal1.getReturnVal(FLOAT_FLAG)));
			return;

		// ++
		case INC:
			children[0].run(retVal0, boid);
			String INCvarName = retVal0.getReturnVal(STRING_FLAG);
			boid.increment(INCvarName);
			return;

		// --
		case DEC:
			children[0].run(retVal0, boid);
			String DECvarName = retVal0.getReturnVal(STRING_FLAG);
			boid.decrement(DECvarName);
			return;
			
		// less than
		case LT:
			children[0].run(retVal0, boid);
			children[1].run(retVal1, boid);
			retVal.setReturnVal(retVal0.getReturnVal(FLOAT_FLAG) < retVal1.getReturnVal(FLOAT_FLAG));
			return;

		// greater than
		case GT:
			children[0].run(retVal0, boid);
			children[1].run(retVal1, boid);
			retVal.setReturnVal(retVal0.getReturnVal(FLOAT_FLAG) > retVal1.getReturnVal(FLOAT_FLAG));
			return;

		// less than or equal to
		case LEQ:
			children[0].run(retVal0, boid);
			children[1].run(retVal1, boid);
			retVal.setReturnVal(retVal0.getReturnVal(FLOAT_FLAG) <= retVal1.getReturnVal(FLOAT_FLAG));
			return;

		// greater than or equal to
		case GEQ:
			children[0].run(retVal0, boid);
			children[1].run(retVal1, boid);
			retVal.setReturnVal(retVal0.getReturnVal(FLOAT_FLAG) >= retVal1.getReturnVal(FLOAT_FLAG));
			return;

		// equal
		case EQ:
			children[0].run(retVal0, boid);
			children[1].run(retVal1, boid);
			retVal.setReturnVal(retVal0.getReturnVal(FLOAT_FLAG) == retVal1.getReturnVal(FLOAT_FLAG));
			return;

		// and
		case AND:
			children[0].run(retVal0, boid);
			children[1].run(retVal1, boid);
			retVal.setReturnVal(retVal0.getReturnVal(BOOLEAN_FLAG) && retVal1.getReturnVal(BOOLEAN_FLAG));
			return;

		// or
		case OR:
			children[0].run(retVal0, boid);
			children[1].run(retVal1, boid);
			retVal.setReturnVal(retVal0.getReturnVal(BOOLEAN_FLAG) || retVal1.getReturnVal(BOOLEAN_FLAG));
			return;

		// negate
		case NEG:
			children[0].run(retVal0, boid);
			retVal.setReturnVal(!retVal0.getReturnVal(BOOLEAN_FLAG));
			return;


		default:
			System.out.println("error: undefined NodeType");
			System.exit(0);
		}


	}


	//
	//getters and setters for children
	//
	public ProgramNode[] getChildren() { return children; }
	public void setChilden(ProgramNode[] children){	this.children = children; }
	public ProgramNode getChild(int childIndex) { return children[childIndex]; }
	public void setChild(int childIndex, ProgramNode newChild){
		children[childIndex] = newChild;
	}


	//
	//getters and setters for parent node
	//
	public ProgramNode getParent() { return parent;	}
	public void setParent(ProgramNode parent) { this.parent = parent; }
	
	public float getDepth() { return true_depth; }
	public void setDepth(float newDepth){	this.true_depth = newDepth; }
	
	public ProgramNode.NodeType getType() { return nodeType; }
	public void setType(NodeType newType){	this.nodeType = newType; }
	
	public void printNode() {
		System.out.println("type = " + nodeType + " parent = " + parent + " children = " + 
				children + " varName = \"" + varName + "\" constValue = " + constValue);
	}
		
	//
	// Functions needed to create random trees
	//
	//returns length of child array
	public float getNumKids(){ return this.children.length;	}
	// for generating a nodeType when making trees
	public static NodeType terminal_type(float floatFlag){
		int randVal = randomVal(0, terminalFloat.length);
		return terminalFloat[randVal];
	}
	public static NodeType terminal_type(boolean boolFlag){
		int randVal = randomVal(0, terminalBool.length);
		return terminalBool[randVal];
	}
	public static NodeType terminal_type(){
		int randVal = randomVal(0, terminalNull.length);
		return terminalNull[randVal];
	}

	// for nodes that won't end
	public static NodeType active_type(float floatFlag){
		int randVal = randomVal(0, allFloatReturn.length);
		return allFloatReturn[randVal];
	}
	public static NodeType active_type(boolean boolFlag){
		int randVal = randomVal(0, allBoolReturn.length);
		return allBoolReturn[randVal];
	}
	public static NodeType active_type(){
		int randVal = randomVal(0, allNullReturn.length);
		return allNullReturn[randVal];
	}

	
	public static int randomVal(int min, int max) {
		Random rand = new Random();
		int newVal = rand.nextInt((max - min)) + min;
		return newVal;
	}
	
}