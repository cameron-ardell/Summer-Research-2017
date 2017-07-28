



public class Particle {

	private int particleID;

	// PSO DATA

	private DoubleVector position;  
	private DoubleVector velocity;

	private Solution currSolution;
	private Solution personalBest;

	// the neighborhood that this particle is in
	// (which is included in neighsContainingParticle (next variable))
	private Neighborhood neighborhood;
	// a list of neighborhoods that contain this particle
	private Neighborhood[] neighsContainingParticle;
	// used in addNeigh when adding neighbors into the list;
	// keeps track of where the next one should go
	private int nextNeighIndex = 0;


	// FLOCK DATA

	private static final int FLOCK_SPACE_NUM_DIM = 2;
	private static final int FLOCK_SPACE_DIM_LENGTH = 500;

	// make components of initial velocity very small 
	private static final double FLOCK_INIT_VELOCITY_DIMENSION_MAGNITUDE = 1.0;
	// if a random component is added to the motion, keep it small
	private static final double FLOCK_RANDOM_MOTION_DIMENSION_MAGNITUDE = 1.0;
	// how close to the boundary does a particle need to be before it "bounces back"?
	private static final int FLOCK_BOUDARY_SENSING_THRESHOLD = 5;

	// flock parameters
	private double maxSpeed;              // speed limit
	private double normalSpeed;             // when pacekeeping (see below) used, tries to keep particle to this speed
	private double neighborRadius;          // determines which other particles are neighbors of a given particle    
	private double separationWeight;        // how strongly do particles move away from every neighbor: range = [0.0, 100.0]
	private double cohesionWeight;          // how strongly do particles move toward the average position of their neighbors: range = [0.0, 1.0]
	private double alignmentWeight;         // how strongly do particles align match average velocity of their neighbors: range = [0.0, 1.0]
	private double pacekeepingWeight;       // how strongly do particles stick to the normalSpeed: range = [0.0, 1.0]
	private double randomMotionProb;        // probability that a small random component is introduced into the motion (see particle class for more info)

	private double numNeighborsOwnFlock = 0;
	private double numNeighborsAllFlocks = 0;

	// flock locations and velocities
	private DoubleVector currFlockLocation;  
	private DoubleVector currFlockVelocity;


	public Particle(int functionNum, 
			int numDimensions, 
			int particleID, 
			double maxSpeed, 
			double normalSpeed,
			double neighborRadius, 
			double separationWeight,
			double cohesionWeight, 
			double alignmentWeight,
			double pacekeepingWeight, 
			double randomMotionProb,
			double[] sendBackResults) {

//	public Particle(int functionNum, 
//			int numDimensions, 
//			int particleID, 
//			double[] sendBackResults) {
		
		this.particleID = particleID;

		position = new DoubleVector(numDimensions);
		for(int i = 0 ; i < position.size() ; ++i) {
			position.set(i, TestFunctions.INIT_MIN_VALS[functionNum] + (TestFunctions.INIT_RANGES[functionNum] * PSO.rand.nextDouble()));
		}

		double[] results = TestFunctions.evalWithError(position, functionNum);
		// need to get the function value and error back to the Swarm constructor,
		// so we can determine the initial global best
		sendBackResults[TestFunctions.VAL_INDEX] =  results[TestFunctions.VAL_INDEX];
		sendBackResults[TestFunctions.ERR_INDEX] =  results[TestFunctions.ERR_INDEX];


		// can send position itself because the Solution constructor makes a copy of the position DoubleVector sent in
		// NOTE: 0 is the iteration found
		currSolution = new Solution(position, results[TestFunctions.VAL_INDEX], results[TestFunctions.ERR_INDEX], 0, particleID);
		personalBest = currSolution.getCopy();

		// start with small random velocity
		velocity = new DoubleVector(numDimensions);
		double minSpeed = 0.0;
		double speedRange = 0.0;
		// don't let the initial speed be greater than a small amount
		if (TestFunctions.UNIVERSAL_SPEED_RANGE < TestFunctions.SPEED_RANGES[functionNum]) {
			minSpeed = TestFunctions.UNIVERSAL_MIN_INIT_SPEED;
			speedRange = TestFunctions.UNIVERSAL_SPEED_RANGE;
		}
		else {
			minSpeed = TestFunctions.SPEED_MIN_VALS[functionNum];
			speedRange = TestFunctions.SPEED_RANGES[functionNum];			
		}
		for(int i = 0 ; i < velocity.size() ; ++i) {
			velocity.set(i, minSpeed + (speedRange * PSO.rand.nextDouble()));
		}

		nextNeighIndex = 0;

		// flock parameters
		this.maxSpeed = maxSpeed;
		this.normalSpeed = normalSpeed;
		this.neighborRadius = neighborRadius;
		this.separationWeight = separationWeight;
		this.cohesionWeight = cohesionWeight;
		this.alignmentWeight = alignmentWeight;
		this.pacekeepingWeight = pacekeepingWeight;
		this.randomMotionProb = randomMotionProb;

		// random location in flock space
		currFlockLocation = DoubleVector.randomVector(FLOCK_SPACE_NUM_DIM, FLOCK_SPACE_DIM_LENGTH/2);

		// start with small random velocity in flock space
		currFlockVelocity = DoubleVector.randomVector(FLOCK_SPACE_NUM_DIM, FLOCK_INIT_VELOCITY_DIMENSION_MAGNITUDE);
		
//		printFlockParameters();

	}


	// create the neighborhood list of the specified size
	public void initializeNeighsList (int numNeighsContainingParticle) {
		neighsContainingParticle = new Neighborhood[numNeighsContainingParticle];
	}


	// used in createNeighLists in Swarm.java when creating, for each particle, a list
	// of Neighborhoods that it is a member of
	public void addNeigh (Neighborhood neigh) {
		neighsContainingParticle[nextNeighIndex++] = neigh;
	}



	// the velocity and position update
	public Solution update(int functionNum, PSO.Topology currentPSOTopology, PSO.SelfModel currentPSOSelfModel, 
			PSO.InfluenceModel currentPSOInfluenceModel) {


		// acceleration starts at 0.0
		DoubleVector acceleration = new DoubleVector(position.size(), 0.0);


		// NEIGH BEST
		if (currentPSOInfluenceModel == PSO.InfluenceModel.NEIGH_BEST) {

			DoubleVector nBestComponent;

			if (PSO.usingSPSO) {
				nBestComponent = neighborhood.getVectorToNeighBestPosition(this, currentPSOTopology, currentPSOSelfModel);
			}
			else {
				Neighborhood flockNeigh = moveFlockAndGetNeighbors();
//				System.out.println("size = " + flockNeigh.sizeNeigh());
				nBestComponent = flockNeigh.getVectorToNeighBestPosition(this, currentPSOTopology, currentPSOSelfModel);
			}

			// NBEST
			nBestComponent.multRandomScalar(0.0, PSO.nBestTheta);
			acceleration.addVector(nBestComponent);

			// PBEST
			DoubleVector pBestComponent = DoubleVector.sub(personalBest.getPosition(), position);
			pBestComponent.multRandomScalar(0.0, PSO.pBestTheta);
			acceleration.addVector(pBestComponent);


		}

		// FIPS
		else if (currentPSOInfluenceModel == PSO.InfluenceModel.FIPS) {

			if (PSO.usingSPSO) {
				acceleration = neighborhood.getFIPSAcceleration (this);
			}
			else {
				Neighborhood flockNeigh = moveFlockAndGetNeighbors();
				acceleration = flockNeigh.getFIPSAcceleration (this);
			}


		}




		// update the velocity and apply the constriction factor
		velocity.addVector(acceleration);
		velocity.multScalar(PSO.constrictionFactor);


		if (PSO.regularDebug) {		
			System.out.print("nextVelocity      :  ");
			velocity.println();
		}


		// bound velocity
		for (int i = 0 ; i < velocity.size() ; ++i) {
			if (velocity.get(i) < TestFunctions.SPEED_MIN_VALS[functionNum])
				velocity.set(i, TestFunctions.SPEED_MIN_VALS[functionNum]);
			else if (velocity.get(i) > TestFunctions.SPEED_MAX_VALS[functionNum])
				velocity.set(i, TestFunctions.SPEED_MAX_VALS[functionNum]);
		}


		// move the particle 
		position.addVector(velocity); 
		//		updateMinMaxDimValues(position);


		if (PSO.regularDebug) {
			System.out.print("currPosition END:  ");
			position.println();
		}


		// evaluate the new position and set currentSolution
		double[] results = TestFunctions.evalWithError(position, functionNum);
		double newPositionValue = results[TestFunctions.VAL_INDEX];
		double newPositionError = results[TestFunctions.ERR_INDEX];

		currSolution.copyFromPosition(position);
		currSolution.setFunctionValue(newPositionValue);
		currSolution.setError(newPositionError);
		currSolution.setIterationFound(PSO.currentIterNum);


		// update the personal best, if necessary
		if (newPositionValue < personalBest.getFunctionValue()) {
			personalBest.copyFromPosition(position);
			personalBest.setFunctionValue(newPositionValue);
			personalBest.setError(newPositionError);
			personalBest.setIterationFound(PSO.currentIterNum);
		}


		return currSolution;
	}		



	private Neighborhood moveFlockAndGetNeighbors () {

		Particle[] particles = Swarm.particles; 

		// flock member new acceleration
		DoubleVector acceleration = new DoubleVector(FLOCK_SPACE_NUM_DIM, 0.0);

		// need sum of locations of particles in the neighborhood for acceleration due to cohesion,
		// since cohesion = acceleration toward the average location of particles in the neighborhood
		DoubleVector sumNeighborLocations = new DoubleVector(FLOCK_SPACE_NUM_DIM, 0.0);

		// need sum of velocities of particles in the neighborhood for acceleration due to alignment,
		// since alignment = acceleration toward the average velocity of particles in the neighborhood
		DoubleVector sumNeighborVelocities = new DoubleVector(FLOCK_SPACE_NUM_DIM, 0.0);



		Particle[] neighParticles= new Particle[particles.length];
		int numNeighbors = 0;
		numNeighborsOwnFlock = 0;

		// get info for all particles 
		for (int i = 0 ; i < particles.length; i++) {

			if (i == particleID) {
				neighParticles[numNeighbors++] = particles[i];
				continue;				
			}

			Particle otherParticle = particles[i];
			double dist = currFlockLocation.distance(otherParticle.currFlockLocation);
			//			System.out.println("dist = " + dist);
			//			neighbors[i] = false;


			if (dist > 0.0 && dist <= neighborRadius) {

				++numNeighborsOwnFlock;
				
				neighParticles[numNeighbors++] = particles[i];
//				System.out.println("neighParticles[" + (numNeighbors-1) + "] = " + particles[i]);
//				System.out.println("particles[" + i + "] = " + particles[i]);

				// sum locations for cohesion calculation after all neighbors have been processed
				sumNeighborLocations.addVector(otherParticle.currFlockLocation); 

				// sum velocities for alignment calculation after all neighbors have been processed
				sumNeighborVelocities.addVector(otherParticle.currFlockLocation);

				// for separation:
				// calculate and weight vector pointing away from neighbor; add to acceleration
				DoubleVector vectorToThisParticle = DoubleVector.sub(currFlockLocation, otherParticle.currFlockLocation);
				// separation force is inversely proportional to the square of the distance
				vectorToThisParticle.divScalar(dist*dist);  
				// but some experiments indicate to me that we might want to consider reducing the
				// denominator to dist^1.5, or possibly even dist. using dist^2 seems to weaken the 
				// separation force to an extent that makes it very difficult for separation to have 
				// any impact when the cohesion is at its max; I would think that even when cohesion 
				// is high, if the separation weight is >50, it should loosen tight clusters significantly,
				// which does not happen currently 
				//						vectorToThisparticle.div((float) Math.pow(dist, 1.5));  
				//						vectorToThisparticle.div(dist);  
				vectorToThisParticle.multScalar(separationWeight);
				acceleration.addVector(vectorToThisParticle);  

			}
		} 


		Neighborhood flockNeigh = new Neighborhood(numNeighbors, particleID);
//		System.out.println("numNeighbors =  " + numNeighbors);
		for (int i = 0 ; i < numNeighbors; i++) {
			flockNeigh.addNeighbor(neighParticles[i]);
//			System.out.println("neighParticles[" + i + "] = " + neighParticles[i]);
		}


		if (numNeighbors > 0) {
			// cohesion steering: steer in the direction of the average location of your neighbors
			DoubleVector cohesionVector = DoubleVector.divVectorScalar(sumNeighborLocations, numNeighbors);        
			cohesionVector.subVector(currFlockLocation);
			cohesionVector.multScalar(cohesionWeight);
			acceleration.addVector(cohesionVector);

			// alignment steering: steer so as to align your velocity with the average velocity of your neighbors
			DoubleVector alignmentVector = DoubleVector.divVectorScalar(sumNeighborVelocities, numNeighbors);
			alignmentVector.subVector(currFlockVelocity);
			alignmentVector.multScalar(alignmentWeight);
			acceleration.addVector(alignmentVector);
		}


		// with the probability specified by the parameter randomMotionProbability, introduce a small
		// random perturbation (magnitude defined by RANDOM_MOTION_DIMENSION_MAGNITUDE) into each 
		// acceleration component
		if (PSO.rand.nextFloat() < randomMotionProb) {
			acceleration.addRandomScalarMagnitude(FLOCK_RANDOM_MOTION_DIMENSION_MAGNITUDE);
		} 


		// update velocity
		currFlockVelocity.addVector(acceleration);

		// make sure we don't exceed maxSpeed
		if (currFlockVelocity.mag() > maxSpeed) {
			currFlockVelocity.multScalar(maxSpeed / currFlockVelocity.mag());
		}


		// pacekeeping (stick to normalSpeed to the extent indicated by pacekeepingWeight)
		DoubleVector pacekeeping = 
				DoubleVector.multVectorScalar(currFlockVelocity, ((normalSpeed - currFlockVelocity.mag()) / currFlockVelocity.mag() * pacekeepingWeight));
		currFlockVelocity.addVector(pacekeeping);

		// bounce back from the boundaries of the space
		DoubleVector boundaryAcc = new DoubleVector(FLOCK_SPACE_NUM_DIM, 0.0);
		for (int d = 0 ; d < FLOCK_SPACE_NUM_DIM ; ++d) {
			if (currFlockLocation.get(d) < (-FLOCK_RANDOM_MOTION_DIMENSION_MAGNITUDE/2) + FLOCK_BOUDARY_SENSING_THRESHOLD)         
				boundaryAcc.set(d, maxSpeed);	
			else if (currFlockLocation.get(d) > FLOCK_RANDOM_MOTION_DIMENSION_MAGNITUDE/2 - FLOCK_BOUDARY_SENSING_THRESHOLD) 
				boundaryAcc.set(d, -maxSpeed);
		}
		currFlockVelocity.addVector(boundaryAcc);   


		// move the particle
		currFlockLocation.addVector(currFlockVelocity);


		// this is where the program tree would be executed
		// The tree program is a static variable in the Swarm class. Must send it a reference to
		// this Particle so it can call methods in here to get/change variable values, as necessary.
		if (!PSO.usingSPSO) {
			Swarm.gpTree.run(this);
			if (particleID == 0) {
//				System.out.println("HERE");
//				printFlockParameters();
//				System.out.println();
//				Swarm.gpTree.printTree();
			}
		}

		return flockNeigh;

	}


	public void printFlockParameters() {
		
		System.out.printf("NS = %5.3f  NR = %5.3f  SEP = %5.3f  COH = %5.3f  ALI = %5.3f  PACE = %5.3f  RAND = %5.3f NNOWN = %5.3f NNALL = %5.3f \n",
				normalSpeed,
				neighborRadius, 
				separationWeight,
				cohesionWeight, 
				alignmentWeight,
				pacekeepingWeight, 
				randomMotionProb,
				numNeighborsOwnFlock,
				numNeighborsAllFlocks);
		
	}


	// called by a ProgramNode when it needs the value of a variable in the Particle object
	public double getVarValue(String varName) {

		if (varName.equals("maxSpeed")) {
			return maxSpeed;
		}

		else if (varName.equals("normalSpeed")) {
			return normalSpeed;
		}

		else if (varName.equals("neighborRadius")) {
			return neighborRadius;
		}

		else if (varName.equals("separationWeight")) {
			return separationWeight;
		}

		else if (varName.equals("alignmentWeight")) {
			return alignmentWeight;
		}

		else if (varName.equals("cohesionWeight")) {
			return cohesionWeight;
		}

		else if (varName.equals("pacekeepingWeight")) {
			return pacekeepingWeight;
		}

		else if (varName.equals("randomMotionProbability")) {
			return randomMotionProb;
		}

		else if (varName.equals("numNeighborsOwnFlock")) {
			return numNeighborsOwnFlock;
		}

		else if (varName.equals("numNeighborsAllFlocks")) {
			return numNeighborsAllFlocks;
		}


		else {
			System.out.println("error: unknown variable name in Particle.getVarValue: \"" + varName + "\"");
			System.exit(0);
		}

		return 0.0f;

	}

	// called by a ProgramNode when it needs to assign a value to variable in the Particle object
	public void assignVariable(String varName, double value) {

//		System.out.println(varName);

		
		
		if (varName.equals("maxSpeed")) {
//			if (particleID == 0) System.out.println("maxSpeed = " + value);
			maxSpeed = value;
		}

		else if (varName.equals("normalSpeed")) {
//			if (particleID == 0) System.out.println("normalSpeed = " + value);
			normalSpeed = value;
		}

		else if (varName.equals("neighborRadius")) {
//			if (particleID == 0) System.out.println("neighborRadius = " + value);
			neighborRadius = value;
		}

		else if (varName.equals("separationWeight")) {
//			if (particleID == 0) System.out.println("separationWeight = " + value);
			separationWeight = value;
		}

		else if (varName.equals("alignmentWeight")) {
//			if (particleID == 0) System.out.println("alignmentWeight = " + value);
			alignmentWeight = value;
		}

		else if (varName.equals("cohesionWeight")) {
//			if (particleID == 0) System.out.println("cohesionWeight = " + value);
			cohesionWeight = value;
		}

		else if (varName.equals("pacekeepingWeight")) {
//			if (particleID == 0) System.out.println("pacekeepingWeight = " + value);
			pacekeepingWeight = value;
		}

		else if (varName.equals("randomMotionProbability")) {
//			if (particleID == 0) System.out.println("randomMotionProbability = " + value);
			randomMotionProb = value;
		}

		else if (varName.equals("numNeighborsOwnFlock")) {
//			if (particleID == 0) System.out.println("numNeighborsOwnFlock = " + value);
			numNeighborsOwnFlock = value;
		}

		else if (varName.equals("numNeighborsAllFlocks")) {
//			if (particleID == 0) System.out.println("numNeighborsAllFlocks = " + value);
			numNeighborsAllFlocks = value;
		}

		else {
			if (particleID == 0) System.out.println("error: unknown variable name in Particle.assignVariable: \"" + varName + "\"");
			System.exit(0);
		}

//		if (particleID == 0) {
//			//System.out.println("\nnumNeighborsOwnFlock = " + numNeighborsOwnFlock);
//			printFlockParameters();
//			System.out.println();
////			Swarm.gpTree.printTree();
//		}

	}

	// called by a ProgramNode when it needs to increment a variable in the Particle object
	public void increment(String varName) {

		if (varName.equals("maxSpeed")) {
			++maxSpeed;
		}

		else if (varName.equals("normalSpeed")) {
			++normalSpeed;
		}

		else if (varName.equals("neighborRadius")) {
			++neighborRadius;
		}

		else if (varName.equals("separationWeight")) {
			++separationWeight;
		}

		else if (varName.equals("alignmentWeight")) {
			++alignmentWeight;
		}

		else if (varName.equals("cohesionWeight")) {
			++cohesionWeight;
		}

		else if (varName.equals("pacekeepingWeight")) {
			++pacekeepingWeight;
		}

		else if (varName.equals("randomMotionProbability")) {
			++randomMotionProb;
		}

		else if (varName.equals("numNeighborsOwnFlock")) {
			++numNeighborsOwnFlock;
		}

		else if (varName.equals("numNeighborsAllFlocks")) {
			++numNeighborsAllFlocks;
		}

		else {
			System.out.println("error: unknown variable name in Particle.increment: \"" + varName + "\"");
			System.exit(0);
		}

	}


	// called by a ProgramNode when it needs to decrement a variable in the Particle object
	public void decrement(String varName) {

		if (varName.equals("maxSpeed")) {
			--maxSpeed;
		}

		else if (varName.equals("normalSpeed")) {
			--normalSpeed;
		}

		else if (varName.equals("neighborRadius")) {
			--neighborRadius;
		}

		else if (varName.equals("separationWeight")) {
			--separationWeight;
		}

		else if (varName.equals("alignmentWeight")) {
			--alignmentWeight;
		}

		else if (varName.equals("cohesionWeight")) {
			--cohesionWeight;
		}

		else if (varName.equals("pacekeepingWeight")) {
			--pacekeepingWeight;
		}

		else if (varName.equals("randomMotionProbability")) {
			--randomMotionProb;
		}

		else if (varName.equals("numNeighborsOwnFlock")) {
			--numNeighborsOwnFlock;
		}

		else if (varName.equals("numNeighborsAllFlocks")) {
			--numNeighborsAllFlocks;
		}

		else {
			System.out.println("error: unknown variable name in Particle.decrement: \"" + varName + "\"");
			System.exit(0);
		}


	}






	public int getParticleID () {
		return particleID;
	}


	public DoubleVector getPosition() {
		return position;
	}



	public Solution getCurrSolution() {
		return currSolution;
	}


	public void setCurrSolution(Solution currSolution) {
		this.currSolution = currSolution;
	}


	public Solution getPersonalBest() {
		return personalBest;
	}



	public void setPersonalBest(Solution personalBest) {
		this.personalBest = personalBest;
	}



	public Neighborhood getNeighborhood() {
		return neighborhood;
	}



	public void setNeighborhood(Neighborhood neighborhood) {
		this.neighborhood = neighborhood;
	}


	//	public void updateMinMaxDimValues(DoubleVector position) {
	//		
	//		for (int i = 0; i < position.size(); ++i) {
	//			if (position.get(i) < PSO.cumMinDimValues.get(i)) {
	//				PSO.cumMinDimValues.set(i, position.get(i));
	//			}
	//			if (position.get(i) > PSO.cumMaxDimValues.get(i)) {
	//				PSO.cumMaxDimValues.set(i, position.get(i));
	//			}		
	//		}
	//		
	//	}


	public void printPosition() {
		position.print();
	}


	public void printlnPosition() {
		position.println();
	}




}



