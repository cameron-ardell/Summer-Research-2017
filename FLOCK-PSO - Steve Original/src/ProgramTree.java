import java.util.Random;
import java.util.ArrayList;
import java.util.List;


public class ProgramTree {
	// How an object that will generate random values need to be instantiated
	static Random rand = new Random();
	
	// For the equivalent of null in doubles
	static double double_null = -Double.MAX_VALUE;
	
	// All the possible names of variables from Steve code to pick randomly
	public static String[] var_names = {"velocityScale", "maxSpeed", "normalSpeed", "neighborRadius",
										"separationWeight", "alignmentWeight", "cohesionWeight", "pacekeepingWeight",
										"randomMotionProbability", "numNeighborsOwnFlock", "numNeighborsAllFlocks"};
	
		
	// Values to create random constants within a range
	public static double min_const;
	public static double max_const;
	
	// Restriction for tree approximate tree depth
	// (because apparently this is an inverted tree)
	public static double max_depth;
	
	// Will be automatically set to null in constructor.
	// holds entire tree as insane linked list
	private ProgramNode root;
	
	//also need to store tree as array to pick random node for mutation and crossover
	public static List<ProgramNode> node_list;
	
	//needed for breeding
	public int fitness;
	
	// The greatest number of nodes that can be in a sequence
	public static int max_seq;
	
	//trying to copy shit
	public ProgramTree(ProgramTree another){
		this.root = null;
		ProgramTree.min_const = another.min_const;
		ProgramTree.max_const = another.max_const;
		ProgramTree.max_depth = another.max_depth;
		ProgramTree.max_seq = another.max_seq;
		ProgramTree.node_list = another.node_list;
		
		this.fitness = another.fitness;
	}
	
	// Constructor
	public ProgramTree(double min_const, double max_const, double max_depth, int max_seq) {
		this.root = null;
		ProgramTree.min_const = min_const;
		ProgramTree.max_const = max_const;
		ProgramTree.max_depth = max_depth;
		ProgramTree.max_seq = max_seq;
		ProgramTree.node_list = new ArrayList<ProgramNode>();
		
		fitness = 0;
	}
	
	// Keeping this because eclipse yelled at me 
	public ProgramTree(ProgramNode root){
		fitness = 0;
		this.root = root;
	}

	// For generating a brand new tree
	public ProgramNode generateRandomNewTree() {

		// Using root object, so need to set it with parameters
		//  sticking with initial IF statement for prototype
		ProgramNode.NodeType type = ProgramNode.NodeType.IF;
		double root_depth = 0;
		
		// Setting initially boring children array
		int numKids = determineChildrenNum(type);
		ProgramNode[] kids = new ProgramNode[numKids];
		
		for(int i = 0; i < numKids; i++){
			kids[i] = null;
		}
		
		// Actually creating root object.
		// The root has a type, no parents, tbd children, no variable name, no const value
		root = new ProgramNode(type, null, null, null, double_null, root_depth, ProgramNode.ReturnType.N);
		
		double child_depth = 1;
		ProgramNode.ReturnType t = ProgramNode.ReturnType.B;
		
		// Currently writing only IF statements for root node
		// so children rt is B, N, and optional N (for IF-ELSE)
		for(int i = 0; i < numKids; i++){
			//get required type
			if(i == 1){	t = ProgramNode.ReturnType.N; }
			
			ProgramNode newChild = generateRandomSubtree(child_depth, root, t);
			root.setChild(i, newChild); 
		}
		
		node_list.add(root); //Adds to node array list
				
		return root;
	}
		
	
	//for making subtrees based on a given node
	// the last one will likely be null and up to 
	public static ProgramNode generateRandomSubtree(double depth, ProgramNode parent, ProgramNode.ReturnType rt ) {
		
		// First need to determine probability that will be terminal
		double term_threshold = 100 * depth / max_depth;
		double randVal = randomVal(0, 100);
		
		//values that will be needed for creation of subrootine, regardless of case and NodeType
		ProgramNode.NodeType pick;
		ProgramNode subrootine = null;
		boolean terminal = false; // <-- to be used later when making recursive call
		double actual_depth = parent.getDepth() + 1;
		int rand_index;
		String var_name;
		
		//
		// TERMINAL CASE
		//
		if (randVal <= term_threshold) {
			terminal = true;
					
			// setting pick for all nodes, and subrootine for the edge cases
			if ( rt == ProgramNode.ReturnType.D){
					double flag = 1;
					pick = ProgramNode.terminal_type(flag);
					
					// accommodate VAR and CONST need assignments
					if (pick == ProgramNode.NodeType.CONST){
						double const_val = randomVal(min_const, max_const);
						subrootine = new ProgramNode(pick, parent, null,null, const_val, actual_depth, rt);
					}
					else if (pick == ProgramNode.NodeType.VAR){
						rand_index = randomVal(0, var_names.length);
						var_name = var_names[rand_index];
						subrootine = new ProgramNode(pick, parent, null, var_name, double_null, actual_depth, rt);
					}
					//other nodes will fit generic command
			}
			else if ( rt == ProgramNode.ReturnType.B){
				boolean flag = true;
				pick = ProgramNode.terminal_type(flag);
				//no edge cases
			}
			else if ( rt == ProgramNode.ReturnType.N){
				pick = ProgramNode.terminal_type();
			}
			else{
				System.out.println("Tried to create new terminal node at undefined return type");
				return null;
			}
			// set newNode for rest of nodes
			if(pick != ProgramNode.NodeType.CONST && pick != ProgramNode.NodeType.VAR){
				subrootine = new ProgramNode(pick, parent, null, null, double_null, actual_depth, rt); 
			}
			
			// setting parent variable for all applicable nodes
			if(parent != null){
				subrootine.setParent(parent);
			}
			// Children handled below
		}
		//
		// END OF TERMINAL SPECIFIC COMMANDS
		//
		// BEGINNING OF DYNAMIC COMMANDS
		//
		else{
			// setting pick for all nodes, and subrootine for the edge cases
			if ( rt == ProgramNode.ReturnType.D){
					double flag = 1;
					pick = ProgramNode.active_type(flag);
					
					// accommodate VAR and CONST need assignments
					if (pick == ProgramNode.NodeType.CONST){
						double const_val = randomVal(min_const, max_const);
						subrootine = new ProgramNode(pick, parent, null,null, const_val, actual_depth, rt);
					}
					else if (pick == ProgramNode.NodeType.VAR){
						rand_index = randomVal(0, var_names.length);
						var_name = var_names[rand_index];
						subrootine = new ProgramNode(pick, parent, null, var_name, double_null, actual_depth, rt);
					}
			}
			else if ( rt == ProgramNode.ReturnType.B){
				boolean flag = true;
				pick = ProgramNode.active_type(flag);
			}
			else if ( rt == ProgramNode.ReturnType.N){
				pick = ProgramNode.active_type();
			}
			else{
				System.out.println("Tried to create new active node at undefined return type");
				return null;
			}
			// set subrootine for rest of nodes
			if(pick != ProgramNode.NodeType.CONST && pick != ProgramNode.NodeType.VAR){
				subrootine = new ProgramNode(pick, parent, null, null, double_null, actual_depth, rt); 
			}

			// setting parent variable for all applicable nodes
			if(parent != null){
				subrootine.setParent(parent);
			}
		}
		
		// ADDING CHILDREN
		ProgramNode[] kids;
		if(terminal == false){
			kids = addChildren(depth+1, pick, subrootine);
		} else {
			kids = addChildren(max_depth, pick, subrootine);
		}
		subrootine.setChilden(kids);
		
		node_list.add(subrootine); //adding to arraylist of nodes
		
		return subrootine;
	}
	
	/*
	 * In this function, given a node, want to create its children recursively
	 * Input:	depth - depth of the new children (not parent who called the function). Not depth property in node, but 
	 * 						depth that implies assignment within the tree  (so more about probability of terminal)
	 * 			node_type - type of node parent is, will be used for switch statement
	 * 			par_node - node that made this call (the parent), though without children currently appended (but we can
	 * 						still make a reference to it)
	 * Output:	array of new children
	 */
	public static ProgramNode[] addChildren(double depth, ProgramNode.NodeType node_type, ProgramNode par_node){
		
		ProgramNode[] kids = null;		//what will actually be returned
		
		//parameters needed to make recursive call
		int numKids = 0;
		ProgramNode.ReturnType childRT = ProgramNode.ReturnType.N;	// default ProgramNode.ReturnType
		int rand_index;
		String var_name;
		double actual_depth = par_node.getDepth() + 1;
		
		// making recursive call for every kind but VAR and CONST (those have no children)
		switch(node_type) {
			// sequence
			case SEQUENCE:
				numKids = randomVal(2, 5); 
				kids = new ProgramNode[numKids];
				
				for(int i = 0; i < numKids; i++){
					kids[i] = generateRandomSubtree(depth, par_node, childRT);
				}
				break;
			// if
			case IF:
				numKids = randomVal(2, 3);  //<-- deciding if IF or IF/ ELSE statement
				kids = new ProgramNode[numKids];
				
				kids[0] = generateRandomSubtree(depth, par_node, ProgramNode.ReturnType.B);		//<-- for the IF
				
				for(int i = 0; i < numKids; i++){
					kids[i] = generateRandomSubtree(depth, par_node, childRT);
				}
				break;	
			
			// assigning new value to Steve variable
			case ASSIGN:
				rand_index = randomVal(0, var_names.length);
				var_name = var_names[rand_index];
				kids = new ProgramNode[2];
				
				//don't need to recurse on first child since always be variable being modified
				kids[0] =  new ProgramNode(ProgramNode.NodeType.VAR, par_node, null, var_name, double_null, actual_depth, ProgramNode.ReturnType.D);
				//second child is value that will be assigned to a Steve variable
				kids[1] = generateRandomSubtree(depth, par_node, ProgramNode.ReturnType.D);
				break;

			// all math operations
			case ADD:
			case SUB:
			case MULT:
			case DIV:
			case EXP:
				kids = new ProgramNode[2];
				kids[0] = generateRandomSubtree(depth, par_node, ProgramNode.ReturnType.D);
				kids[1] = generateRandomSubtree(depth, par_node, ProgramNode.ReturnType.D);
				break;
	
			case INC:
			case DEC:
				kids = new ProgramNode[1];
				kids[0] = generateRandomSubtree(depth, par_node, ProgramNode.ReturnType.D);
				break;
				
			case EQ:
			case LT:
			case GT:
			case LEQ:
			case GEQ:
				kids = new ProgramNode[2];
				kids[0] = generateRandomSubtree(depth, par_node, ProgramNode.ReturnType.D);
				
				//at least one child must be a VAR
				if(kids[0].nodeType == ProgramNode.NodeType.VAR){
					kids[1] = generateRandomSubtree(depth, par_node, ProgramNode.ReturnType.D);
				} else {
					rand_index = randomVal(0, var_names.length);
					var_name = var_names[rand_index];
					kids[1] = new ProgramNode(ProgramNode.NodeType.VAR, par_node, null, var_name, double_null, actual_depth, ProgramNode.ReturnType.D);
				}
				break;	
				
			case AND:
			case OR:
				kids = new ProgramNode[2];
				kids[0] = generateRandomSubtree(depth, par_node, ProgramNode.ReturnType.B);
				kids[1] = generateRandomSubtree(depth, par_node, ProgramNode.ReturnType.B);
				break;

			case NEG:
				kids = new ProgramNode[1];
				kids[0] = generateRandomSubtree(depth, par_node, ProgramNode.ReturnType.B);
				break;
	
			default:
				System.out.println("error: undefined NodeType in child construction " + node_type);
				System.exit(0);
			}
			return kids;
	}
	
	
	
	/*
	 * This function exists to make sure node_list is correctly maintained. Given a node, remove it from list.
	 * Input:  removed_node - node object that should 100% be in arraylist of nodes
	 * Output: none
	 */
	public void removeNodeFromList(ProgramNode removed_node){
		if (removed_node == null) {
			System.out.println("tried to remove node, was null");
			return;
		}
		else if (!(removed_node instanceof ProgramNode)){
			System.out.println("tried to remove node, wasn't type ProgramNode");
			return;
		}
		else if(node_list.indexOf(removed_node) == -1){
			System.out.println("tried to remove node, was not in list");
			return;
		}
		else{
			node_list.remove(removed_node);
		}
	}
	
	/*
	 * Recursive node removal from list: removes node from list
	 * 	once all of its children are removed from list
	 * Input:  removed_node - node object that should 100% be in arraylist of nodes
	 * Output: arraylist of all removed nodes (only need for crossover)
	 */
	public List<ProgramNode> removeSubtreeFromList(ProgramNode removed_node){
		if(node_list.indexOf(removed_node) == -1){
			System.out.println("tried to remove node tree, was not in list");
			return null;
		}
		//make empty list
		List<ProgramNode> rm_nodes = new ArrayList<ProgramNode>();
		
		//get info on current kids
		int num_kids = (int)removed_node.getNumKids();
		ProgramNode[] kids = removed_node.children;
		//remove kids from list
		for(int i = 0; i < num_kids; i++){
			rm_nodes.addAll(removeSubtreeFromList(kids[i])); // I THINK THIS COMBINES 2 ARRAYLISTS SMOOTHLY
															 // OBVIOUSLY NOT CONFIDENT
		}
		//once any kids gone, can remove this node
		rm_nodes.add(removed_node);
		removeNodeFromList(removed_node);
		
		return rm_nodes;
	}
	
	public int getFitness(){
		return fitness;
	}
	
	public void run(Particle particle) {
		ReturnValue retVal = new ReturnValue();
		root.run(retVal, particle);
	}

	public ProgramNode getRoot() {
		return root;
	}

	public void setRoot(ProgramNode root) {
		this.root = root;
	}
	
	
	
	/*
	 * 
	 * ALL RANDOM OPERATIONS FOR WORKING WITH A TREE
	 * 
	 * 
	 */
	
	
	//
	//figured would be easier than writing a random number function each time
	//will pick random number within range from min to max inclusive
	//
	public static int randomVal(int min, int max) {
		int newVal = rand.nextInt((max - min) + 1) + min;
		return newVal;
	}
	public static double randomVal(double min, double max) {
		double newVal = (double)rand.nextInt(((int)max - (int)min) + 1) + min;
		//max-min * randDouble + min
		return newVal;
	}
	
	//determines how many children a node that varies will have
	public int determineChildrenNum(ProgramNode.NodeType type){
		int numKids = 0;
		if(type == ProgramNode.NodeType.IF){
			numKids = randomVal(2,3);
		}
		else if (type == ProgramNode.NodeType.SEQUENCE){
			numKids = randomVal(2, max_seq);
			// if computer is stupid, switch 2 to 1
		}
		else{
			System.out.println("tried to make random number of kids for node of type: " + type);
		}
		return numKids;
	}

	
	
	//print the tree components
	public void build_a_tree(){
		
		for(int i = 0; i < node_list.size(); i++){
			ProgramNode curNode = node_list.get(i);
			System.out.println("Node: " + i);
			int p_ind = node_list.indexOf(curNode);
			System.out.println("Parent: " + p_ind);
			int numKids = (int)curNode.getNumKids();
			
			for(int h = 0; h<numKids; h++){
				int k_ind = node_list.indexOf(curNode.children[h]);
				System.out.println("Kid at index: " + k_ind);
			}
			
			ProgramNode.NodeType nt = curNode.nodeType;
			System.out.println("NodeType: " + nt);
			if(nt == ProgramNode.NodeType.CONST){
				System.out.println("Value: " + curNode.constValue);
			}
			else if(nt == ProgramNode.NodeType.VAR){
				System.out.println("Name: " + curNode.varName);
			}
			System.out.println();
		}
		
		
			
	}
	
	
	
	
	
	
}
