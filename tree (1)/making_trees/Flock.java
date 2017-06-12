/* 
 * Stephen Majercik
 * Frank Mauceri
 * last updated 20 May 2013
 * 
 * 
 * Each flock is created with either default parameters or randomly generated parameters;
 * note that these parameters can be changed at the individual Boid level, if desired
 * 
 * Methods in this class:
 * 	1) call run method in Boid class to run each boid (update velocity and position, and render)
 * 	2) change the flock size, when necessary, including removing dead boids (if mortality is being used)
 * 	3) calculate some statistics for the flock and send them to Max
 * 		- calculates mean location and velocity for each flock
 * 		- average deviation for location and velocity for each flock
 *	 	- velocity magnitude (mean and average deviation)
 * 
 */




// Processing classes 
import processing.core.PApplet;
import processing.core.PVector;

// data structures for the flock
import java.util.ArrayList;


public class Flock {

	public ArrayList<Boid> boids;  
	private int flockID = 0;

	// default flock characteristics 
	private float velocityScale = 1.0f;           // not sure what this is for; have to ask Frank...
	private float maxSpeed = 12.0f;               // speed limit
	private float normalSpeed = 10.0f;            // pacekeeping (see below), when used, tries to keep boid to this speed 
	private float neighborRadius = 200.0f;        // determines which other Boids are neighbors of a given Boid    
	private float separationWeight = 100.0f;      // how strongly do Boids move away from every neighbor: range = [0.0, 100.0]
	private float alignmentWeight = 0.5f;         // how strongly do Boids align match average velocity of their neighbors: range = [0.0, 1.0]
	private float cohesionWeight = 0.5f;          // how strongly do Boids move toward the average position of their neighbors: range = [0.0, 1.0]
	private float pacekeepingWeight = 0.5f;       // how strongly do Boids stick to the normalSpeed: range = [0.0, 1.0]
	private float randomMotionProbability = 0.0f; // probability that a small random component is introduced into the motion (see Boid class for more info)

	// do boids in this flock have a limited lifespan?
	private static boolean boidMortality = false;
	private static final int BOID_LIFESPAN = 200;
 

	// not needed in this class, but must be sent to the Boid
	// constructor; needed for rendering calls in that class
	private PApplet parent;


	
	public static ProgramTree program = null;



	// constructor
	Flock (int flockID, int numBoids, int flockType, PApplet parent) {


		boids = new ArrayList<Boid>(numBoids);  

		// generate random flock if so indicated
		// BUT NOT proximityThreshold. which is set above
		if (flockType == MusicSwarm.RANDOM_FLOCK) {   
			velocityScale = Math.max(MusicSwarm.rand.nextFloat(), 0.1f);            // range = [0.0, 0.1]
			maxSpeed = Math.max(MusicSwarm.rand.nextFloat() * 10.0f, 2.0f);         // range = [2.0, 10.0]
			normalSpeed = Math.max(MusicSwarm.rand.nextFloat() * maxSpeed, 1.0f);   // range = [1.0, maxSpeed]
			neighborRadius = MusicSwarm.rand.nextInt(91) + 10;                      // range = [10.0, 100.0]
			separationWeight = MusicSwarm.rand.nextFloat() * 100.0f;                // range = [0.0, 100.0]
			alignmentWeight = MusicSwarm.rand.nextFloat();                          // range = [0.0, 1.0]
			cohesionWeight = MusicSwarm.rand.nextFloat();                           // range = [0.0, 1.0]
			pacekeepingWeight = MusicSwarm.rand.nextFloat();                        // range = [0.0, 1.0]
			randomMotionProbability = MusicSwarm.rand.nextFloat() / 2.0f;           // range = [0.0, 0.5]
		}


		// create the Boids
		for (int boidID = 0 ; boidID < numBoids; boidID++) {

			PVector boidLocation = new PVector(MusicSwarm.rand.nextInt(MusicSwarm.WINDOW_WIDTH+1) - MusicSwarm.WINDOW_WIDTH/2, 
											   MusicSwarm.rand.nextInt(MusicSwarm.WINDOW_HEIGHT+1) - MusicSwarm.WINDOW_HEIGHT/2, 
											   MusicSwarm.rand.nextInt(MusicSwarm.WINDOW_DEPTH+1) - MusicSwarm.WINDOW_DEPTH/2);
			
			Boid boid = new Boid(flockID, boidID, boidLocation, velocityScale, maxSpeed, normalSpeed, neighborRadius, 
					separationWeight, alignmentWeight, cohesionWeight, pacekeepingWeight, randomMotionProbability, parent);
			
			boids.add(boid); 
		}

		// need to save the parent and communication (with Max) information 
		// to send to the Boid constructor if we create new Boids later 
		this.parent = parent;
		this.flockID = flockID;
		
		program = buildProgram();


	}


	// update the velocity and location of each boid in the flock;
	// calculate some flock statistics and send them to Max
	void run(Flock[] allFlocks) {


			if (flockEmpty())
				return;

			for (int i = 0; i < boids.size(); i++) {
				Boid b = (Boid) boids.get(i);  

				// need to send all the flocks to the run method in the boid class, 
				// so we can calculate all the boids in every boid's neighborhood
				b.run(allFlocks);

			}

	}


    // This is the Java code that buildProgam creates a tree for.
//    		if (numNeighborsOwnFlock < 20) {
//    			neighborRadius += 1;
//    			if (neighborRadius > 1000)
//    				neighborRadius = 1000;
//    			cohesionWeight = 0.9f;
//    			//						separationWeight = 10;
//    			normalSpeed = 6;
//    			maxSpeed = 6;
//    		}
//    		else if (numNeighborsOwnFlock > 20){
//    			neighborRadius -= 1;
//    			if (neighborRadius < 0)
//    				neighborRadius = 0;
//    			cohesionWeight = 0.1f;
//    			//						separationWeight = 90;
//    			normalSpeed = 24; //12
//    			maxSpeed = 24;
//    		}
	
	public ProgramTree buildProgram() {
		
		// root if
		ProgramNode ifRoot = new ProgramNode(ProgramNode.NodeType.IF, null, null, null, 0);

	
		// numNeighborsOwnFlock < 20
		ProgramNode ifLT = new ProgramNode(ProgramNode.NodeType.LT, null, null, null, 0);
		ProgramNode ifLTVarName = new ProgramNode(ProgramNode.NodeType.VAR, null, null, "numNeighborsOwnFlock", 0);
		ProgramNode ifLTConst = new ProgramNode(ProgramNode.NodeType.CONST, null, null, null, 20);
		ProgramNode[] ifLTChildren = { ifLTVarName, ifLTConst };
		ifLT.setChilden(ifLTChildren);
		
		
		// overall if sequence
		ProgramNode ifSEQ = new ProgramNode(ProgramNode.NodeType.SEQUENCE, null, null, null, 0);
		
		// ++neighborRadius
		ProgramNode ifINC = new ProgramNode(ProgramNode.NodeType.INC, null, null, null, 0);
		ProgramNode ifINCVarName = new ProgramNode(ProgramNode.NodeType.VAR, null, null, "neighborRadius", 0);
		ProgramNode[] ifINCChildren = { ifINCVarName };
		ifINC.setChilden(ifINCChildren);
		
		// if if 
		ProgramNode ifif = new ProgramNode(ProgramNode.NodeType.IF, null, null, null, 0);
		// neighborRadius > 1000
		ProgramNode ififGT = new ProgramNode(ProgramNode.NodeType.GT, null, null, null, 0);
		ProgramNode ififGTVarName = new ProgramNode(ProgramNode.NodeType.VAR, null, null, "neighborRadius", 0);
		ProgramNode ififGTConst = new ProgramNode(ProgramNode.NodeType.CONST, null, null, null, 1000);
		ProgramNode[] ififGTChildren = { ififGTVarName, ififGTConst };
		ififGT.setChilden(ififGTChildren);
		// neighborRadius = 1000
		ProgramNode ififASSIGNRadius = new ProgramNode(ProgramNode.NodeType.ASSIGN, null, null, null, 0);
		ProgramNode ififASSIGNRadiusVarName = new ProgramNode(ProgramNode.NodeType.VAR, null, null, "neighborRadius", 0);
		ProgramNode ififASSIGNRadiusConst = new ProgramNode(ProgramNode.NodeType.CONST, null, null, null, 1000);
		ProgramNode[] ififASSIGNRadiusChildren = { ififASSIGNRadiusVarName, ififASSIGNRadiusConst };
		ififASSIGNRadius.setChilden(ififASSIGNRadiusChildren);
		// set if if children
		ProgramNode[] ififChildren = { ififGT, ififASSIGNRadius };
		ifif.setChilden(ififChildren);
		
		// cohesionWeight = 0.9
		ProgramNode ifASSIGNCohesion = new ProgramNode(ProgramNode.NodeType.ASSIGN, null, null, null, 0);
		ProgramNode ifASSIGNCohesionVarName = new ProgramNode(ProgramNode.NodeType.VAR, null, null, "cohesionWeight", 0);
		ProgramNode ifASSIGNCohesionConst = new ProgramNode(ProgramNode.NodeType.CONST, null, null, null, 0.9f);
		ProgramNode[] ifASSIGNCohesionChildren = { ifASSIGNCohesionVarName, ifASSIGNCohesionConst };
		ifASSIGNCohesion.setChilden(ifASSIGNCohesionChildren);
		
		// normalSpeed = 6
		ProgramNode ifASSIGNNormalSpeed = new ProgramNode(ProgramNode.NodeType.ASSIGN, null, null, null, 0);
		ProgramNode ifASSIGNNormalSpeedVarName = new ProgramNode(ProgramNode.NodeType.VAR, null, null, "normalSpeed", 0);
		ProgramNode ifASSIGNNormalSpeedConst = new ProgramNode(ProgramNode.NodeType.CONST, null, null, null, 6);
		ProgramNode[] ifASSIGNNormalSpeedChildren = { ifASSIGNNormalSpeedVarName, ifASSIGNNormalSpeedConst };
		ifASSIGNNormalSpeed.setChilden(ifASSIGNNormalSpeedChildren);
		
		// maxSpeed = 6
		ProgramNode ifASSIGNMaxSpeed = new ProgramNode(ProgramNode.NodeType.ASSIGN, null, null, null, 0);
		ProgramNode ifASSIGNMaxSpeedVarName = new ProgramNode(ProgramNode.NodeType.VAR, null, null, "maxSpeed", 0);
		ProgramNode ifASSIGNMaxSpeedConst = new ProgramNode(ProgramNode.NodeType.CONST, null, null, null, 6);
		ProgramNode[] ifASSIGNMaxSpeedChildren = { ifASSIGNMaxSpeedVarName, ifASSIGNMaxSpeedConst };
		ifASSIGNMaxSpeed.setChilden(ifASSIGNMaxSpeedChildren);
		
		// set if sequence children
		ProgramNode[] ifSEQChildren = { ifINC, ifif, ifASSIGNCohesion, ifASSIGNNormalSpeed, ifASSIGNMaxSpeed };
		ifSEQ.setChilden(ifSEQChildren);
		
		
		// else if
		ProgramNode elseif = new ProgramNode(ProgramNode.NodeType.IF, null, null, null, 0);
		

		// numNeighborsOwnFlock > 20
		ProgramNode elseifGT = new ProgramNode(ProgramNode.NodeType.GT, null, null, null, 0);
		ProgramNode elseifGTVarName = new ProgramNode(ProgramNode.NodeType.VAR, null, null, "numNeighborsOwnFlock", 0);
		ProgramNode elseifGTConst = new ProgramNode(ProgramNode.NodeType.CONST, null, null, null, 20);
		ProgramNode[] elseifGTChildren = { elseifGTVarName, elseifGTConst };
		elseifGT.setChilden(elseifGTChildren);
		
		
		// else if sequence
		ProgramNode elseifSEQ = new ProgramNode(ProgramNode.NodeType.SEQUENCE, null, null, null, 0);
		
		// --neighborRadius
		ProgramNode elseifDEC = new ProgramNode(ProgramNode.NodeType.DEC, null, null, null, 0);
		ProgramNode elseifDECVarName = new ProgramNode(ProgramNode.NodeType.VAR, null, null, "neighborRadius", 0);
		ProgramNode[] elseifDECChildren = { elseifDECVarName };
		elseifDEC.setChilden(elseifDECChildren);
		
		// else if if
		ProgramNode elseifif = new ProgramNode(ProgramNode.NodeType.IF, null, null, null, 0);	
		// neighborRadius < 0
		ProgramNode elseififLT = new ProgramNode(ProgramNode.NodeType.LT, null, null, null, 0);
		ProgramNode elseififLTVarName = new ProgramNode(ProgramNode.NodeType.VAR, null, null, "neighborRadius", 0);
		ProgramNode elseififLTConst = new ProgramNode(ProgramNode.NodeType.CONST, null, null, null, 0);
		ProgramNode[] elseififLTChildren = { elseififLTVarName, elseififLTConst };
		elseififLT.setChilden(elseififLTChildren);		
		// neighborRadius = 0
		ProgramNode elseififASSIGNRadius = new ProgramNode(ProgramNode.NodeType.ASSIGN, null, null, null, 0);
		ProgramNode elseififASSIGNRadiusVarName = new ProgramNode(ProgramNode.NodeType.VAR, null, null, "neighborRadius", 0);
		ProgramNode elseififASSIGNRadiousConst = new ProgramNode(ProgramNode.NodeType.CONST, null, null, null, 0);
		ProgramNode[] elseififASSIGNRadiusChildren = { elseififASSIGNRadiusVarName, elseififASSIGNRadiousConst };
		elseififASSIGNRadius.setChilden(elseififASSIGNRadiusChildren);	
		// set else if if children
		ProgramNode[] elseififChildren = { elseififLT, elseififASSIGNRadius };
		elseifif.setChilden(elseififChildren);
		
		// cohesionWeight = 0.1
		ProgramNode elseifASSIGNCohesion = new ProgramNode(ProgramNode.NodeType.ASSIGN, null, null, null, 0);
		ProgramNode elseifASSIGNCohesionVarName = new ProgramNode(ProgramNode.NodeType.VAR, null, null, "cohesionWeight", 0);
		ProgramNode elseifASSIGNCohesionConst = new ProgramNode(ProgramNode.NodeType.CONST, null, null, null, 0.1f);
		ProgramNode[] elseifASSIGNCohesionChildren = { elseifASSIGNCohesionVarName, elseifASSIGNCohesionConst };
		elseifASSIGNCohesion.setChilden(elseifASSIGNCohesionChildren);	
		
		// normalSpeed = 24
		ProgramNode elseifASSIGNNormalSpeed = new ProgramNode(ProgramNode.NodeType.ASSIGN, null, null, null, 0);
		ProgramNode elseifASSIGNNormalSpeedVarName = new ProgramNode(ProgramNode.NodeType.VAR, null, null, "normalSpeed", 0);
		ProgramNode elseifASSIGNNormalSpeedConst = new ProgramNode(ProgramNode.NodeType.CONST, null, null, null, 24);
		ProgramNode[] elseifASSIGNNormalSpeedChildren = { elseifASSIGNNormalSpeedVarName, elseifASSIGNNormalSpeedConst };
		elseifASSIGNNormalSpeed.setChilden(elseifASSIGNNormalSpeedChildren);
		
		// maxSpeed = 24
		ProgramNode elseifASSIGNMaxSpeed = new ProgramNode(ProgramNode.NodeType.ASSIGN, null, null, null, 0);
		ProgramNode elseifASSIGNMaxSpeedVarName = new ProgramNode(ProgramNode.NodeType.VAR, null, null, "maxSpeed", 0);
		ProgramNode elseifASSIGNMaxSpeedConst = new ProgramNode(ProgramNode.NodeType.CONST, null, null, null, 24);
		ProgramNode[] elseifASSIGNMaxSpeedChildren = { elseifASSIGNMaxSpeedVarName, elseifASSIGNMaxSpeedConst };
		elseifASSIGNMaxSpeed.setChilden(elseifASSIGNMaxSpeedChildren);
		
		// set else if sequence children
		ProgramNode[] elseifSEQChildren = { elseifDEC, elseifif, elseifASSIGNCohesion, elseifASSIGNNormalSpeed, elseifASSIGNMaxSpeed };
		elseifSEQ.setChilden(elseifSEQChildren);
		
		
		// set else if children
		ProgramNode[] elseifChildren = { elseifGT, elseifSEQ };
		elseif.setChilden(elseifChildren);	
		
		
		
		// set root children
		ProgramNode[] ifRootChildren = { ifLT, ifSEQ, elseif };
		ifRoot.setChilden(ifRootChildren);
		
		
		return new ProgramTree(ifRoot);
		
	}
	
	
	
	
	// sets the flock size; can be larger or smaller than current size
	void setFlockSize(int newSize) {


			if (newSize == boids.size())
				return;

			int oldSize = boids.size();

			// add boids, if necessary
			if (newSize > oldSize) {
				int numNewBoids = newSize - oldSize;
				for (int i = 1 ; i <= numNewBoids ; i++) {
					PVector location = new PVector(MusicSwarm.rand.nextInt(MusicSwarm.WINDOW_WIDTH+1) - MusicSwarm.WINDOW_WIDTH/2, 
												   MusicSwarm.rand.nextInt(MusicSwarm.WINDOW_HEIGHT+1) - MusicSwarm.WINDOW_HEIGHT/2, 
												   MusicSwarm.rand.nextInt(MusicSwarm.WINDOW_DEPTH+1) - MusicSwarm.WINDOW_DEPTH/2);
					addNewBoid(location);
				}
			}

			// remove boids, if necessary
			else if (newSize < oldSize) {
				int numBoidsDestroyed = oldSize - newSize;
				for(int i = 1 ; i <= numBoidsDestroyed ; ++i) {
					// remove the one with the highest index
					boids.remove(boids.size() - 1);
				}
			}     

	}


	// create a new Boid at the specified location
	void addNewBoid(PVector location) {


			boids.ensureCapacity(boids.size() + 1); 
			
			Boid boid = new Boid(flockID, boids.size(), location, velocityScale, maxSpeed,  normalSpeed,  neighborRadius, 
					separationWeight, alignmentWeight, cohesionWeight, pacekeepingWeight, randomMotionProbability, parent);
			
			boids.add(boid); 

	}


	// test age of Boids and remove dead boids from arraylist
	void removeDeadBoids() {

		// Boids might not have a fixed lifespan
		if (!boidMortality)
			return;


			for (int i = 0; i < boids.size(); i++) {				
				Boid b = (Boid) boids.get(i);  
				if (b.getAge() > BOID_LIFESPAN)
					boids.remove(i);
			}
		
	}



	
	public boolean flockEmpty() {
		return boids.size() == 0;
	}

	



}







