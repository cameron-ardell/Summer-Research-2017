


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
	public ProgramNode root;
	
	//needed for breeding
	public static int fitness;
	
	public GPTree(float min_const, float max_const, float max_depth, int max_seq){
		fitness = 0;
		this.root = null;
		ProgramTree.min_const = min_const;
		ProgramTree.max_const = max_const;
		ProgramTree.max_depth = max_depth;
		ProgramTree.max_seq = max_seq;
	}
	

}