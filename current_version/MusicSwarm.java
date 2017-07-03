/* 
 * Stephen Majercik
 * Frank Mauceri
 * last updated 14 May 2013
 * 
 * 
 * MusicSwarm.java
 * NOTE:  the following are required by Processing:
 * 	1) MusicSwarm must extend the PApplet class
 * 	2) the must be a setup method to specify code that is executed when the applet starts up
 * 	3) there must be a draw method to specify code that is executed on each iteration
 * 
 * Contains constants,variables, and methods related to:
 * 	1) graphics
 * 		- sets up graphics environment
 * 		- includes code that implements a movable camera view of the simulation
 * 	2) flocks
 * 		- creation
 * 		- running (calls run method in Flock class)
 * 		
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


// Processing classes (including graphics)
//import processing.core.PApplet;

// for random numbers

public class MusicSwarm  { 

//	tried to make this a running thing on my computer
	public static void main(String args[]){
		Scanner scanner = new Scanner(System.in);
		// Population pop = new Population(4);
		// pop.time_mayfly();
		// pop.popt.get(0).build_a_tree();
		
		//
		// testing node class
		//
		/*
		GPNode n = new GPNode(GPNode.NodeType.IF, null, null, "", 38, GPNode.ReturnType.B);
		System.out.println("return type should be B: " + n.rt);
		System.out.println("getting depth... should be 1: " + n.get_depth());
		int lo = 0;
		int hi = 2;
		System.out.println("random value int test, should be a value 0-2: " + GPNode.randomVal(lo, hi));
		float low = 0;
		float high = 2;
		System.out.println("random value float test, should be a value 0-2: " + GPNode.randomVal(low, high));
		GPNode no = new GPNode(GPNode.NodeType.LT, n, null, "", 38, GPNode.ReturnType.F);
		System.out.println("getting depth... should be 2: " + no.get_depth());
		ArrayList<GPNode> kid = new ArrayList<GPNode>();
		kid.add(no);
		n.children = kid;
		System.out.println("getting child rt of root, should be F: " + n.children.get(0).rt);
		GPNode nod = new GPNode(GPNode.NodeType.SEQUENCE, no, null, "", 389, GPNode.ReturnType.N);
		GPNode node = new GPNode(GPNode.NodeType.ADD, no, null, "", 389, GPNode.ReturnType.F);
		kid.add(nod);
		kid.remove(no);

		ArrayList kidclone = (ArrayList) kid.clone();
		no.children = kidclone;
		// no.children = kid;
		kid.add(node);

		System.out.println("getting 1st grandchild const val of root, should be 389.0: " + no.children.get(0).constValue);
		System.out.println("getting length of childless node children arraylist, should be 0 : " + node.children.size());
		*/
		

		//
		//testing tree class
		//
		 /*
		float minc = 0;
		float maxc = 1000;
		float maxd = 10;
		int maxs = 5;
		GPTree tree = new GPTree(minc,maxc,maxd,maxs);
		tree.generateNewTree();
		tree.printTree();
		ArrayList<GPNode> list = tree.toArrayList();
		int node_ind = 0;

		while(node_ind != 9999){
			System.out.print("enter node val to check depth of: ");
			node_ind = scanner.nextInt();
			int depth = list.get(node_ind).get_depth();
			System.out.println(String.format("the depth is %d", depth));
			System.out.println();
		}
		*/


		//
		// testing population
		//
		int numTrees = 2;
		Population popped = new Population(numTrees);
	}

	
//public class MusicSwarm extends PApplet { 

//
//	// ****************  GRAPHICS  ******************
//
//	// window dimensions
//	public static final int WINDOW_WIDTH = 1600;
//	public static final int WINDOW_HEIGHT = 1600;
//	public static final int WINDOW_DEPTH = 1600;
//
//	// number of dimensions in rendering 
//	public static final int RENDER_2D = 1;
//	public static final int RENDER_3D = 2;
//	public static int renderMethod = RENDER_3D;
//
//	// camera movement
//	// to smooth out yaw and pitch changes
//	private static final float SCALE_DOWN_YAW_RATE_OF_CHANGE= 0.05f;
//	private static final float SCALE_DOWN_PITCH_RATE_OF_CHANGE= 0.05f;
//	// to set zoom scale and range
//	private static final int ZOOM_SCALING_FACTOR = 150;
//	private static final int ZOOM_RANGE = 10;
//	// accept messages from Max that control camera movement
//	private float pitch;  //rotate around X
//	private float yaw;	  //rotate around Y
//	private float zoom;
//
//
//	// boid-specific rendering
//	public static final int BOID_SIZE = 5;
//	// opacity of the fill for Boids
//	public static final int B_ALPHA = 150;
//	// are the boids in the same neighborhood connected by a line graphically?
//	public static final boolean CONNECTED_COMPONENTS = false;
//
//
//	// ****************  FLOCKS  ******************
//
//	// different modes of flock creation
//	public static final int DEFAULT_FLOCK = 1; // creates flock with the same default parameters, which are specified in the Flock class
//	public static final int RANDOM_FLOCK = 2;  // creates each flock with its own random parameters, generated in the Flock class
//	private static int flockType = DEFAULT_FLOCK;	
//
//	// flocks
//	private static final int NUM_FLOCKS = 6;                      
//	private static Flock[] allFlocks = new Flock[NUM_FLOCKS];   
//	// ************************************************************************************************
//	// NOTE (5/14/13): ALL 6 flocks are always created: 
//	// if all flocks are being used and all are the same size, set useDefaultFlockSize to true 
//	//     and set defaultFlockSize to desired size
//	// if flock sizes are different or some flocks are not being used, set useDefaultFlockSize to false
//	//     and set flock sizes in nonDefaultInitialFlockSizes (0 if flock not being used)
//	// ************************************************************************************************
//	private static boolean useDefaultFlockSize = false;
//	private static int defaultFlockSize = 50;
//	private static int[] nonDefaultInitialFlockSizes = {500, 0, 0, 0, 0, 0};
//
//	// flock colors
//	static final int[][] FLOCKCOLOR = { 	
//		{255,  0,  0},
//		{0,  255,  0},
//		{0,  0,  255},
//		{255, 255, 0},
//		{255, 0, 255},
//		{0, 255, 255}
//	};
//
//
//	// ****************  MISCELLANEOUS  ******************
//
//	// for random numbers
//	// static so accessible from other classes (not actually used in this class)
//	static Random rand = new Random();
//	//rand.setSeed(43453445);        
//
//	// for delimiting behaviors over time
//	public static int timeStep = 0;
//
//	
//	
//
//	// setting up the simulation
//	public void setup() {
//
//		// window size and graphics mode
//		size(WINDOW_WIDTH, WINDOW_HEIGHT, P3D); 
//		
//		timeStep = 0;
//		
//		// create the Flocks  
//		for (int flockID = 0; flockID < NUM_FLOCKS; flockID++){    
//			int flockSize = useDefaultFlockSize? defaultFlockSize: nonDefaultInitialFlockSizes[flockID];
//			allFlocks[flockID] = new Flock(flockID, flockSize, flockType, this);
//		}
//
//
//
//	}
//
//	
//	
//
//	// the "loop forever" method in processing
//	public void draw() {
//
//		++timeStep;
//
//		// background is black
//		// (need to completely redraw the simulation at each time step)
//		background(0);
//
//		// set the camera point of view
//		float zoomZ = (zoom - ZOOM_SCALING_FACTOR) * ZOOM_RANGE;
//		translate(WINDOW_WIDTH/2,WINDOW_HEIGHT/2,zoomZ);
//		rotateY(yaw * SCALE_DOWN_YAW_RATE_OF_CHANGE);
//		rotateX(pitch * SCALE_DOWN_PITCH_RATE_OF_CHANGE);
//		// draw the cube defining the flock space
//		// show lines
//		stroke(200,200);
//		// but don't fill the cube 
//		noFill();
//		box(WINDOW_HEIGHT);
//
//		// update all the Flocks
//		// NOTE: need to send all the Flocks, so we have access to all the Boids in every Boid's neighborhood
//		for(int flockID = 0; flockID < NUM_FLOCKS; flockID++) {
//			allFlocks[flockID].run(allFlocks);
//		}
//
//		// remove dead Boids
//		for(int flockID = 0; flockID < NUM_FLOCKS; flockID++) {
//			allFlocks[flockID].removeDeadBoids();
//		}
//
//	}


}



