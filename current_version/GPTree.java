


public class GPTree {
	
	// For the equivalent of null in floats
	static float float_null = -Float.MAX_VALUE;
	
	// All the possible names of variables from Steve code to pick randomly
	public static String[] var_names = {"velocityScale", "maxSpeed", "normalSpeed", "neighborRadius",
										"separationWeight", "alignmentWeight", "cohesionWeight", "pacekeepingWeight",
										"randomMotionProbability", "numNeighborsOwnFlock", "numNeighborsAllFlocks"};
			
	// Values to create random constants within a range
	public static float min_const;
	public static float max_const;

	// The greatest number of nodes that can be in a sequence
	public static int max_seq;
	
	// Restriction for tree approximate tree depth
	// (because apparently this is an inverted tree)
	public static float max_depth;
	
	// Will be automatically set to null in constructor.
	// holds entire tree as insane linked list
	public GPNode root;
	
	//needed for breeding
	public static int fitness;
	
	public GPTree(float min_const, float max_const, float max_depth, int max_seq){
		fitness = 0;
		this.root = null;
		GPTree.min_const = min_const;
		GPTree.max_const = max_const;
		GPTree.max_depth = max_depth;
		GPTree.max_seq = max_seq;
	}
	

	//for generating new tree without an existing root
	public GPNode generateNewTree() {
		GPNode.NodeType type = GPNode.NodeType.IF;

		//determine number of kids
		int numKids = determineKidNum(type);

		// The root has a type, no parents, no children, no variable name, no const value
		root = new GPNode(type, null, null, null, float_null, GPNode.ReturnType.N);

		GPNode.ReturnType t = GPNode.ReturnType.B;

		// Currently writing only IF statements for root node
		// so children rt is B, N, and optional N (for IF-ELSE)
		for(int i = 0; i < numKids; i++){
			//get required type
			if(i == 1){	t = GPNode.ReturnType.N; }
			
			float depth = 2;
			GPNode newChild = generateSubtree(depth, root, t);
			root.children.add(newChild); 
		}

		return root;
	}

	public GPNode generateSubtree(float depth, GPNode parent, GPNode.ReturnType rt){

		return null;
	}


	public int determineKidNum(GPNode.NodeType type){
		int numKids = 0;
		if(type == GPNode.NodeType.IF){
			numKids = GPNode.randomVal(2,3);
		}
		else if (type == GPNode.NodeType.SEQUENCE){
			numKids = GPNode.randomVal(2, max_seq+1);
			// if computer is stupid, switch 2 to 1
		}
		else{
			System.out.println("tried to make random number of kids for node of type: " + type);
		}
		return numKids;
	}

}