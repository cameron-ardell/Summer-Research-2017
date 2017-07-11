



public class Swarm {

	public static Particle[] particles;  

	private static Neighborhood[] allNeighs;
	
	public static Solution globalBest;

	public int numDimensions;
	DoubleVector minDimValues = null;
	DoubleVector maxDimValues = null;
	
	public static GPTree gpTree = null;
	
//	public Swarm (int numParticles, 
//			int numParticlesIndex, 
//			int functionNum, 
//			int numDimensions, 	
//			PSO.Topology currentPSOTopology, 
//			PSO.SelfModel currentPSOSelfModel, 
//			PSO.InfluenceModel currentPSOInfluenceModel,
//			double currentMaxSpeed, 
//			double currentNormalSpeed,
//			double currentNeighRadius, 
//			double currentSeparationWeight,
//			double currentCohesionWeight, 
//			double currentAlignmentWeight,
//			double currentPacekeepingWeight, 
//			double currentRandMotionProb,
//			DataOutput[][] intervalData) {

	public Swarm (int numParticles, 
			int numParticlesIndex, 
			int functionNum, 
			int numDimensions, 	
			PSO.Topology currentPSOTopology, 
			PSO.SelfModel currentPSOSelfModel, 
			PSO.InfluenceModel currentPSOInfluenceModel,
			DataOutput[][] intervalData,
			GPTree gpTree) {
		
		this.gpTree = gpTree;
		
		this.numDimensions = numDimensions;
		minDimValues = new DoubleVector(numDimensions, Double.MAX_VALUE);
		maxDimValues = new DoubleVector(numDimensions, -Double.MAX_VALUE);
		
		// create arrays to hold particle and neighborhoods
		particles = new Particle[numParticles];   
		allNeighs = new Neighborhood[numParticles];		

		// array needed to get back function evaluation results from Particle constructor
		double[] initParticleData = new double[2];    

		// create the particles

//		particles[0] = new Particle(functionNum, numDimensions, 0, 
//				currentMaxSpeed, currentNormalSpeed, currentNeighRadius, currentSeparationWeight,
//				currentCohesionWeight, currentAlignmentWeight, currentPacekeepingWeight, currentRandMotionProb,
//				initParticleData);

		particles[0] = new Particle(functionNum, numDimensions, 0, initParticleData);
		
		// first one is the current global best
		int globalBestParticleNum = 0;
		double globalBestValue = initParticleData[TestFunctions.VAL_INDEX];
		double globalBestError = initParticleData[TestFunctions.ERR_INDEX];

		// getPosition does not return a copy, but that's okay because Solution constructor makes a copy of the DoubleVector sent in
		// NOTE: 0 is the iteration found
		globalBest = new Solution(particles[0].getPosition(), globalBestValue, globalBestError, 0, globalBestParticleNum);

		for (int particleID = 1 ; particleID < particles.length ; particleID++) {

//			particles[particleID] = new Particle(functionNum, numDimensions, particleID, 
//					currentMaxSpeed, currentNormalSpeed, currentNeighRadius, currentSeparationWeight,
//					currentCohesionWeight, currentAlignmentWeight, currentPacekeepingWeight, currentRandMotionProb,
//					initParticleData);
		
			particles[particleID] = new Particle(functionNum, numDimensions, particleID, initParticleData);
			
			// evaluate
			double particleValue = initParticleData[TestFunctions.VAL_INDEX];
			double particleError = initParticleData[TestFunctions.ERR_INDEX];


			if (particleValue < globalBest.getFunctionValue()) {  
				globalBest.copyFromPosition(particles[particleID].getPosition());
				globalBest.setFunctionValue(particleValue);
				globalBest.setError(particleError);
				globalBest.setIterationFound(0);
				globalBest.setParticleID(particleID);
			}

			// data is collected based on number of function evaluations; if we are counting iterations, things are set up in PSO.java
			// so that the number of FEs in each interval is equal to the number of iterations we want in each interval
			// NOTE: PSO.numInitialFEsIgnored is > 0 only if we are counting iterations, since the FEs when the particles are
			// created should not count towards the FEs used to count iterations
			if ((PSO.currentFENum - PSO.numInitialFEsIgnored) % PSO.numFEsPerOutputInterval == 0) {
				int intervalIndex = (PSO.currentFENum - PSO.numInitialFEsIgnored) / PSO.numFEsPerOutputInterval;				
				intervalData[intervalIndex][PSO.currentRunNum].copyDataFromSolution(globalBest);
				intervalData[intervalIndex][PSO.currentRunNum].setBoundingBoxVolume(calcBoundingBoxVolume());
//				System.out.printf("bbv = %e\n", intervalData[intervalIndex][PSO.currentRunNum].getBoundingBoxVolume());
			}

		}


		
		if (PSO.nBestDebug) {
			System.out.print("\nINIT global best particle    ");
			globalBest.println();
		}
		
		if (PSO.usingSPSO) {
			// create each particle's neighborhood
			// this is where the heavy lifting gets done, i.e. creating the actual list of particles for each particle's neighborhood
			createNeighborhoods(currentPSOTopology, currentPSOSelfModel, currentPSOInfluenceModel, numParticlesIndex);

			// for each particle create an array of the neighborhoods it is in
			createNeighLists();
		}
//		else {
//			if (PSO.currentRunNum == 0) {
//				program = buildProgram();
//			}
//		}
	}

    // This is the Java code that buildProgam creates a tree for.
//	if (numNeighborsOwnFlock < 20) {
//		neighborRadius += 1;
//		if (neighborRadius > 1000)
//			neighborRadius = 1000;
//		cohesionWeight = 0.9f;
//		//						separationWeight = 10;
//		normalSpeed = 6;
//		maxSpeed = 6;
//	}
//	else if (numNeighborsOwnFlock > 20){
//		neighborRadius -= 1;
//		if (neighborRadius < 0)
//			neighborRadius = 0;
//		cohesionWeight = 0.1f;
//		//						separationWeight = 90;
//		normalSpeed = 24; //12
//		maxSpeed = 24;
//	}
	
//public GPTree buildProgram() {
//		
//		// root if
//		ProgramNode ifRoot = new ProgramNode(ProgramNode.NodeType.IF, null, null, null, 0);
//
//	
//		// numNeighborsOwnFlock < 20
//		ProgramNode ifLT = new ProgramNode(ProgramNode.NodeType.LT, null, null, null, 0);
//		ProgramNode ifLTVarName = new ProgramNode(ProgramNode.NodeType.VAR, null, null, "numNeighborsOwnFlock", 0);
//		ProgramNode ifLTConst = new ProgramNode(ProgramNode.NodeType.CONST, null, null, null, 20);
//		ProgramNode[] ifLTChildren = { ifLTVarName, ifLTConst };
//		ifLT.setChilden(ifLTChildren);
//		
//		
//		// overall if sequence
//		ProgramNode ifSEQ = new ProgramNode(ProgramNode.NodeType.SEQUENCE, null, null, null, 0);
//		
//		// ++neighborRadius
//		ProgramNode ifINC = new ProgramNode(ProgramNode.NodeType.INC, null, null, null, 0);
//		ProgramNode ifINCVarName = new ProgramNode(ProgramNode.NodeType.VAR, null, null, "neighborRadius", 0);
//		ProgramNode[] ifINCChildren = { ifINCVarName };
//		ifINC.setChilden(ifINCChildren);
//		
//		// if if 
//		ProgramNode ifif = new ProgramNode(ProgramNode.NodeType.IF, null, null, null, 0);
//		// neighborRadius > 1000
//		ProgramNode ififGT = new ProgramNode(ProgramNode.NodeType.GT, null, null, null, 0);
//		ProgramNode ififGTVarName = new ProgramNode(ProgramNode.NodeType.VAR, null, null, "neighborRadius", 0);
//		ProgramNode ififGTConst = new ProgramNode(ProgramNode.NodeType.CONST, null, null, null, 1000);
//		ProgramNode[] ififGTChildren = { ififGTVarName, ififGTConst };
//		ififGT.setChilden(ififGTChildren);
//		// neighborRadius = 1000
//		ProgramNode ififASSIGNRadius = new ProgramNode(ProgramNode.NodeType.ASSIGN, null, null, null, 0);
//		ProgramNode ififASSIGNRadiusVarName = new ProgramNode(ProgramNode.NodeType.VAR, null, null, "neighborRadius", 0);
//		ProgramNode ififASSIGNRadiusConst = new ProgramNode(ProgramNode.NodeType.CONST, null, null, null, 1000);
//		ProgramNode[] ififASSIGNRadiusChildren = { ififASSIGNRadiusVarName, ififASSIGNRadiusConst };
//		ififASSIGNRadius.setChilden(ififASSIGNRadiusChildren);
//		// set if if children
//		ProgramNode[] ififChildren = { ififGT, ififASSIGNRadius };
//		ifif.setChilden(ififChildren);
//		
//		// cohesionWeight = 0.9
//		ProgramNode ifASSIGNCohesion = new ProgramNode(ProgramNode.NodeType.ASSIGN, null, null, null, 0);
//		ProgramNode ifASSIGNCohesionVarName = new ProgramNode(ProgramNode.NodeType.VAR, null, null, "cohesionWeight", 0);
//		ProgramNode ifASSIGNCohesionConst = new ProgramNode(ProgramNode.NodeType.CONST, null, null, null, 0.9f);
//		ProgramNode[] ifASSIGNCohesionChildren = { ifASSIGNCohesionVarName, ifASSIGNCohesionConst };
//		ifASSIGNCohesion.setChilden(ifASSIGNCohesionChildren);
//		
//		// normalSpeed = 6
//		ProgramNode ifASSIGNNormalSpeed = new ProgramNode(ProgramNode.NodeType.ASSIGN, null, null, null, 0);
//		ProgramNode ifASSIGNNormalSpeedVarName = new ProgramNode(ProgramNode.NodeType.VAR, null, null, "normalSpeed", 0);
//		ProgramNode ifASSIGNNormalSpeedConst = new ProgramNode(ProgramNode.NodeType.CONST, null, null, null, 6);
//		ProgramNode[] ifASSIGNNormalSpeedChildren = { ifASSIGNNormalSpeedVarName, ifASSIGNNormalSpeedConst };
//		ifASSIGNNormalSpeed.setChilden(ifASSIGNNormalSpeedChildren);
//		
//		// maxSpeed = 6
//		ProgramNode ifASSIGNMaxSpeed = new ProgramNode(ProgramNode.NodeType.ASSIGN, null, null, null, 0);
//		ProgramNode ifASSIGNMaxSpeedVarName = new ProgramNode(ProgramNode.NodeType.VAR, null, null, "maxSpeed", 0);
//		ProgramNode ifASSIGNMaxSpeedConst = new ProgramNode(ProgramNode.NodeType.CONST, null, null, null, 6);
//		ProgramNode[] ifASSIGNMaxSpeedChildren = { ifASSIGNMaxSpeedVarName, ifASSIGNMaxSpeedConst };
//		ifASSIGNMaxSpeed.setChilden(ifASSIGNMaxSpeedChildren);
//		
//		// set if sequence children
//		ProgramNode[] ifSEQChildren = { ifINC, ifif, ifASSIGNCohesion, ifASSIGNNormalSpeed, ifASSIGNMaxSpeed };
//		ifSEQ.setChilden(ifSEQChildren);
//		
//		
//		// else if
//		ProgramNode elseif = new ProgramNode(ProgramNode.NodeType.IF, null, null, null, 0);
//		
//
//		// numNeighborsOwnFlock > 20
//		ProgramNode elseifGT = new ProgramNode(ProgramNode.NodeType.GT, null, null, null, 0);
//		ProgramNode elseifGTVarName = new ProgramNode(ProgramNode.NodeType.VAR, null, null, "numNeighborsOwnFlock", 0);
//		ProgramNode elseifGTConst = new ProgramNode(ProgramNode.NodeType.CONST, null, null, null, 20);
//		ProgramNode[] elseifGTChildren = { elseifGTVarName, elseifGTConst };
//		elseifGT.setChilden(elseifGTChildren);
//		
//		
//		// else if sequence
//		ProgramNode elseifSEQ = new ProgramNode(ProgramNode.NodeType.SEQUENCE, null, null, null, 0);
//		
//		// --neighborRadius
//		ProgramNode elseifDEC = new ProgramNode(ProgramNode.NodeType.DEC, null, null, null, 0);
//		ProgramNode elseifDECVarName = new ProgramNode(ProgramNode.NodeType.VAR, null, null, "neighborRadius", 0);
//		ProgramNode[] elseifDECChildren = { elseifDECVarName };
//		elseifDEC.setChilden(elseifDECChildren);
//		
//		// else if if
//		ProgramNode elseifif = new ProgramNode(ProgramNode.NodeType.IF, null, null, null, 0);	
//		// neighborRadius < 0
//		ProgramNode elseififLT = new ProgramNode(ProgramNode.NodeType.LT, null, null, null, 0);
//		ProgramNode elseififLTVarName = new ProgramNode(ProgramNode.NodeType.VAR, null, null, "neighborRadius", 0);
//		ProgramNode elseififLTConst = new ProgramNode(ProgramNode.NodeType.CONST, null, null, null, 0);
//		ProgramNode[] elseififLTChildren = { elseififLTVarName, elseififLTConst };
//		elseififLT.setChilden(elseififLTChildren);		
//		// neighborRadius = 0
//		ProgramNode elseififASSIGNRadius = new ProgramNode(ProgramNode.NodeType.ASSIGN, null, null, null, 0);
//		ProgramNode elseififASSIGNRadiusVarName = new ProgramNode(ProgramNode.NodeType.VAR, null, null, "neighborRadius", 0);
//		ProgramNode elseififASSIGNRadiousConst = new ProgramNode(ProgramNode.NodeType.CONST, null, null, null, 0);
//		ProgramNode[] elseififASSIGNRadiusChildren = { elseififASSIGNRadiusVarName, elseififASSIGNRadiousConst };
//		elseififASSIGNRadius.setChilden(elseififASSIGNRadiusChildren);	
//		// set else if if children
//		ProgramNode[] elseififChildren = { elseififLT, elseififASSIGNRadius };
//		elseifif.setChilden(elseififChildren);
//		
//		// cohesionWeight = 0.1
//		ProgramNode elseifASSIGNCohesion = new ProgramNode(ProgramNode.NodeType.ASSIGN, null, null, null, 0);
//		ProgramNode elseifASSIGNCohesionVarName = new ProgramNode(ProgramNode.NodeType.VAR, null, null, "cohesionWeight", 0);
//		ProgramNode elseifASSIGNCohesionConst = new ProgramNode(ProgramNode.NodeType.CONST, null, null, null, 0.1f);
//		ProgramNode[] elseifASSIGNCohesionChildren = { elseifASSIGNCohesionVarName, elseifASSIGNCohesionConst };
//		elseifASSIGNCohesion.setChilden(elseifASSIGNCohesionChildren);	
//		
//		// normalSpeed = 24
//		ProgramNode elseifASSIGNNormalSpeed = new ProgramNode(ProgramNode.NodeType.ASSIGN, null, null, null, 0);
//		ProgramNode elseifASSIGNNormalSpeedVarName = new ProgramNode(ProgramNode.NodeType.VAR, null, null, "normalSpeed", 0);
//		ProgramNode elseifASSIGNNormalSpeedConst = new ProgramNode(ProgramNode.NodeType.CONST, null, null, null, 24);
//		ProgramNode[] elseifASSIGNNormalSpeedChildren = { elseifASSIGNNormalSpeedVarName, elseifASSIGNNormalSpeedConst };
//		elseifASSIGNNormalSpeed.setChilden(elseifASSIGNNormalSpeedChildren);
//		
//		// maxSpeed = 24
//		ProgramNode elseifASSIGNMaxSpeed = new ProgramNode(ProgramNode.NodeType.ASSIGN, null, null, null, 0);
//		ProgramNode elseifASSIGNMaxSpeedVarName = new ProgramNode(ProgramNode.NodeType.VAR, null, null, "maxSpeed", 0);
//		ProgramNode elseifASSIGNMaxSpeedConst = new ProgramNode(ProgramNode.NodeType.CONST, null, null, null, 24);
//		ProgramNode[] elseifASSIGNMaxSpeedChildren = { elseifASSIGNMaxSpeedVarName, elseifASSIGNMaxSpeedConst };
//		elseifASSIGNMaxSpeed.setChilden(elseifASSIGNMaxSpeedChildren);
//		
//		// set else if sequence children
//		ProgramNode[] elseifSEQChildren = { elseifDEC, elseifif, elseifASSIGNCohesion, elseifASSIGNNormalSpeed, elseifASSIGNMaxSpeed };
//		elseifSEQ.setChilden(elseifSEQChildren);
//		
//		
//		// set else if children
//		ProgramNode[] elseifChildren = { elseifGT, elseifSEQ };
//		elseif.setChilden(elseifChildren);	
//		
//		
//		
//		// set root children
//		ProgramNode[] ifRootChildren = { ifLT, ifSEQ, elseif };
//		ifRoot.setChilden(ifRootChildren);
//		
//		
//		return new GPTree(ifRoot);
//		
//	}
	
	




	public void update (int currentFunctionNum, PSO.Topology currentPSOTopology, PSO.SelfModel currentPSOSelfModel, PSO.InfluenceModel currentPSOInfluenceModel,
			DataOutput[][] intervalData) {
		
		for (int particleID = 0 ; particleID < particles.length ; particleID++) {

			if (PSO.regularDebug || PSO.nBestDebug) {
				System.out.print("\nUPDATING PARTICLE " + particleID);
				System.out.print(" at ");
				particles[particleID].getPosition().print();
				System.out.println();
			}
			
//			if (PSO.sendMemoryUsageDataToFile && !PSO.usingSPSO) {
//				PSO.memoryUsageDataFile.printf("%3d  ", particleID);
//			}
			
			Solution newSolution = particles[particleID].update(currentFunctionNum, currentPSOTopology, currentPSOSelfModel, currentPSOInfluenceModel);  
			double newParticleValue = newSolution.getFunctionValue();      

			if (newParticleValue < globalBest.getFunctionValue()) {

				globalBest.copyFromPosition(newSolution.getPosition());
				globalBest.setFunctionValue(newParticleValue);
				globalBest.setError(newSolution.getError());
				globalBest.setIterationFound(PSO.currentIterNum);
				globalBest.setParticleID(particleID);
				


			} // if new global best

			
			// data is collected based on number of function evaluations; if we are counting iterations, things are set up in PSO.java
			// so that the number of FEs in each interval is equal to the number of iterations we want in each interval
			// NOTE: PSO.numInitialFEsIgnored is > 0 only if we are counting iterations, since the FEs when the particles are
			// created should not count towards the FEs used to count iterations
			if ((PSO.currentFENum - PSO.numInitialFEsIgnored) % PSO.numFEsPerOutputInterval == 0) {
				int intervalIndex = (PSO.currentFENum - PSO.numInitialFEsIgnored) / PSO.numFEsPerOutputInterval;
				intervalData[intervalIndex][PSO.currentRunNum].copyDataFromSolution(globalBest);
				intervalData[intervalIndex][PSO.currentRunNum].setBoundingBoxVolume(calcBoundingBoxVolume());
//				System.out.printf("bbv = %e\n", intervalData[intervalIndex][PSO.currentRunNum].getBoundingBoxVolume());
				//				System.out.println("putting item at " + (PSO.currentFunctionEvaluationNum - PSO.numInitialFEsIgnored) + " FEs (not including FEs to create swarm) in index " + intervalRunDataIndex + " of intervalRunData");
			}

		}  // end for-loop particles
		



	}   





	public void createNeighborhoods(PSO.Topology currentTopology, PSO.SelfModel currentSelfModel, PSO.InfluenceModel currentInfluenceModel, int numParticlesIndex) {

		// this is bad for GBEST, because it's going to create a distinct Neighborhood
		// object for every particle, in spite of the fact that there is only one neighborhood...
		// but go with it for now

		for (int particleID = 0 ; particleID < particles.length ; particleID++) {

			// create the actual neighborhood with list of references to Particles in neighborhood
			Neighborhood neigh = new Neighborhood(particles, numParticlesIndex, particleID, currentTopology, currentSelfModel, currentInfluenceModel);
			
			allNeighs[particleID] = neigh;
			particles[particleID].setNeighborhood(neigh);
		}

	}


	// for each particle, create an array of neighborhoods that it is in
	public void createNeighLists() {

		// do it for every particle
		for (int particleID = 0 ; particleID < particles.length ; particleID++) {
			Particle thisParticle = particles[particleID];

			// make an array of the neighborhoods the particle is in by going through all the 
			// neighborhoods and checking if this particle is in them; make the array big enough 
			// to hold *all* neighborhoods
			// NOTE: this way of doing it takes care of *any* neighborhood type (e.g. asymmetric 
			//       neighborhoods), since we are actually going through each neighborhood and
			//       checking who is in there *after* the neighborhoods have been created
			int numNeighsContainingParticle = 0;
			Neighborhood[] neighsContainingParticle = new Neighborhood[allNeighs.length];

			for (int neighID = 0 ; neighID < allNeighs.length ; neighID++) {	
				Neighborhood thisNeigh = allNeighs[neighID];
				if (thisNeigh.containsParticle(thisParticle)) {
					neighsContainingParticle[numNeighsContainingParticle++] = thisNeigh;
				}
			}

			// now create the right size array neighsContainingParticle in the particle
			// NOTE: this is bad for the gbest neighborhood, since every particle will have the
			//   same list of all particles (unless the neighborhood does not include self)
			thisParticle.initializeNeighsList(numNeighsContainingParticle);			
			// go through the first numNeighsContainingParticle elements in the array and
			// put the neighborhoods contained therein in the neighsContainingParticle array
			// of the particle
			for (int n = 0 ; n < numNeighsContainingParticle ; ++n) {
				thisParticle.addNeigh(neighsContainingParticle[n]);
			}
		}
	}



	public void showAllNeighborhoods () {

		for (int n = 0 ; n < allNeighs.length ; ++n) {
			showNeighborhood(allNeighs[n]);
		}

	}


	public static void showNeighborhood (Neighborhood neigh) {

		Particle[] particlesInNeighborhood = neigh.getNeighParticles();

		System.out.println("neighborhoodID = " + neigh.getNeighID() + " includes particles:");
		for (int i = 0 ; i < particlesInNeighborhood.length ; i++) {
			System.out.println("particleID = " + particlesInNeighborhood[i].getParticleID());
		}

	}


	public double calcBoundingBoxVolume() {

		minDimValues.setAll(Double.MAX_VALUE);
		maxDimValues.setAll(-Double.MAX_VALUE);
		
		for (int particleID = 0 ; particleID < particles.length ; particleID++) {
			Particle p = particles[particleID];
			DoubleVector position = p.getPosition();

			for (int i = 0; i < position.size(); ++i) {
				if (position.get(i) < minDimValues.get(i)) {
					minDimValues.set(i, position.get(i));
				}
				if (position.get(i) > maxDimValues.get(i)) {
					maxDimValues.set(i, position.get(i));
				}		
			}
		}

		double volume = 1;
		for (int i = 0 ; i < minDimValues.size() ; ++i) {
			double dimensionSize = maxDimValues.get(i) - minDimValues.get(i);
			volume *= dimensionSize;
		}

		return volume;
	}

	
	
	
	public void printParticles () {

		for(int p = 0 ; p < particles.length ; ++p) {
			System.out.println("particle " + p + " = " + particles[p]);
		}

	}




}




