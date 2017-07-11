


public class Neighborhood {


//	private int particleID;       // the particle whose neighborhood this is
	private int neighID;
	private Particle[] neighParticles;  
	private int nextNeighIndex = 0;   // only used when adding neighborhoods; keeps track of index of next one



	// this constructor just creates an array big enough to hold all the neighbors -- 
	// neighbors are added using the addNeighbor;

	public Neighborhood(int size, int particleID) {
//		this.particleID = particleID;
		this.neighID = particleID;
		nextNeighIndex = 0;
		neighParticles = new Particle[size];
	}

	
	// after the particles are created in the OptSwarm constructor, the constructor calls the createNeighborhoods method, 
	// which calls this constructor for each particle to create the Neighborhood in that particle ("neigh")
	public Neighborhood (Particle[] particles, int numParticlesIndex, int particleID, 
			PSO.Topology currentTopology, PSO.SelfModel currentSelfModel, PSO.InfluenceModel currentInfluenceModel) {


//		this.particleID = particleID;
		
		this.neighID = particleID;
		nextNeighIndex = 0;


		if (currentTopology == PSO.Topology.GBEST) {

			if (currentSelfModel == PSO.SelfModel.INCLUDE_SELF) {
				neighParticles = new Particle[particles.length];
				for (int partID = 0 ; partID < particles.length ; partID++) {
					neighParticles[partID] = particles[partID];
				}
			}

			else {
				int nextParticleIndex = 0;  // need this to keep track when self is not included and that partID is skipped
				neighParticles = new Particle[particles.length-1];
				for (int partID = 0 ; partID < particles.length ; partID++) {
					if (partID != particleID)
						neighParticles[nextParticleIndex++] = particles[partID];
				}

			}


		}

		else if (currentTopology == PSO.Topology.RING) {

			// the non-self particles are always the first two in the list; 
			// if self is included, make the array size 3 and put the self at the end
			if (currentSelfModel == PSO.SelfModel.INCLUDE_SELF) {
				neighParticles = new Particle[3];
				neighParticles[2] = particles[particleID];
			}
			else {
				neighParticles = new Particle[2];
			}

			int leftIndex = particleID == 0? particles.length - 1: particleID - 1;				
			int rightIndex = particleID == particles.length - 1? 0: particleID + 1;

			neighParticles[0] = particles[leftIndex];
			neighParticles[1] = particles[rightIndex];

		}

		else if (currentTopology == PSO.Topology.vonNEUMANN) {

			// the non-self particles are always the first four in the list; 
			// if self is included, make the array size 5 and put the self at the end
			if (currentSelfModel == PSO.SelfModel.INCLUDE_SELF) {
				neighParticles = new Particle[5];
				neighParticles[4] = particles[particleID];
			}
			else {
				neighParticles = new Particle[4];
			}

			// get the dimensions of the torus from PSO, where they are set by hand
			// for each possible # of particles
			int numRowsVonNeumann = PSO.numRowsVonNeumannAndMooreList[numParticlesIndex];
			int numColsVonNeumann = PSO.numColsVonNeumannAndMooreList[numParticlesIndex];

			int row = particleID  / numColsVonNeumann;
			int col = particleID  % numColsVonNeumann;

			int northRow = row - 1 < 0? numRowsVonNeumann - 1: row - 1;
			int northCol = col;
			int particleNum = (northRow * numColsVonNeumann) + northCol;
			neighParticles[0] = particles[particleNum];

			int eastRow = row;
			int eastCol = (col + 1) % numColsVonNeumann;
			particleNum = (eastRow * numColsVonNeumann) + eastCol;
			neighParticles[1] = particles[particleNum];

			int southRow = (row + 1) % numRowsVonNeumann;
			int southCol = col;
			particleNum = (southRow * numColsVonNeumann) + southCol;
			neighParticles[2] = particles[particleNum];

			int westRow = row;
			int westCol = col - 1 < 0? numColsVonNeumann - 1: col - 1;
			particleNum = (westRow * numColsVonNeumann) + westCol;
			neighParticles[3] = particles[particleNum];			

		}

		else if (currentTopology == PSO.Topology.MOORE) {

			// the non-self particles are always the first eight in the list; 
			// if self is included, make the array size 9 and put the self at the end
			if (currentSelfModel == PSO.SelfModel.INCLUDE_SELF) {
				neighParticles = new Particle[9];
				neighParticles[8] = particles[particleID];
			}
			else {
				neighParticles = new Particle[8];
			}

			// get the dimensions of the torus from PSO, where they are set by hand
			// for each possible # of particles
			int numRowsMoore = PSO.numRowsVonNeumannAndMooreList[numParticlesIndex];
			int numColsMoore = PSO.numColsVonNeumannAndMooreList[numParticlesIndex];

			int row = particleID  / numColsMoore;
			int col = particleID  % numColsMoore;

			int nextParticleIndex = 0;
			for (int rDelta = -1 ; rDelta <= 1 ; ++rDelta) {
				for (int cDelta = -1 ; cDelta <= 1 ; ++cDelta) {

					// don't do this for the particle itself
					if (rDelta != 0 || cDelta != 0) {
						int neighRow = row + rDelta;
						int neighCol = col + cDelta;

						if (neighRow < 0)
							neighRow = numRowsMoore - 1;
						else if (neighRow == numRowsMoore) {
							neighRow = 0;
						}

						if (neighCol < 0)
							neighCol = numColsMoore - 1;
						else if (neighCol == numColsMoore) {
							neighCol = 0;
						}

						int particleNum = (neighRow * numColsMoore) + neighCol;
						neighParticles[nextParticleIndex++] = particles[particleNum];
					}
				}
			}
		}
	}


	
	// **********************************************************************************************************************************
	// these three methods do *not* need to be static because they will be called only when the neighborhood is static (in the 
	// neighborhood sense, i.e. not changing each iteration), in which case there will be a Neighborhood object
	// through which this can be called
	public DoubleVector getVectorToNeighBestPosition(Particle particle, PSO.Topology currentTopology, PSO.SelfModel currentSelfModel) {

//		System.out.println("neighParts = " + neighParticles.length);
		DoubleVector neighBestPosition = getNeighBestPosition(particle, currentTopology, currentSelfModel);
		DoubleVector vectorToNeighBestPosition = DoubleVector.sub(neighBestPosition, particle.getPosition());

		return vectorToNeighBestPosition;

	}

	
	public DoubleVector getNeighBestPosition(Particle particle, PSO.Topology currentTopology, PSO.SelfModel currentSelfModel) {

		
		// if it's the standard gbest including self, it's faster to just return the
		// true global best that we're keeping track of in Swarm.java
		if (currentTopology == PSO.Topology.GBEST && currentSelfModel == PSO.SelfModel.INCLUDE_SELF) {
			return Swarm.globalBest.getPosition();
		}

		// whether the self is included was dealt with when the neighborhood was created;
		// if the self is not supposed to be in the neighborhood, it's not (see constructor for details)
		// so just go through the list of particles and find the best one
		Particle bestParticle = null;
		double bestPBestFuncVal = Double.MAX_VALUE;

//		int farthestPBestID = -1;
		double distanceFarthestPBest = 0.0;
		
		int numPartsInNeigh = neighParticles.length;
//		System.out.println("numPartsInNeigh = " + numPartsInNeigh);

		int bestID = -1;
		for (int p = 0 ; p < numPartsInNeigh ; ++p) {
			Particle nextParticle = neighParticles[p];
//			System.out.println(p + "  " + nextParticle);
			double nextPBestFuncVal = nextParticle.getPersonalBest().getFunctionValue();
			if (nextPBestFuncVal < bestPBestFuncVal) {
				bestParticle = nextParticle;
				bestID = bestParticle.getParticleID();
				bestPBestFuncVal = nextPBestFuncVal;
			}
			double dist = particle.getPosition().distance(nextParticle.getPersonalBest().getPosition());
			if (dist >= distanceFarthestPBest) {
				distanceFarthestPBest = dist;
//				farthestPBestID = nextParticle.getParticleID();
			}
		}

		return bestParticle.getPersonalBest().getPosition();
		
	}

	
	
	
	public DoubleVector getFIPSAcceleration (Particle particle) {

		int numPartsInNeigh = neighParticles.length;
		
		double componentTheta = PSO.theta / numPartsInNeigh;
		DoubleVector position = particle.getPosition();
		DoubleVector acceleration = new DoubleVector(position.size(), 0.0);

		// whether the self is included was dealt with when the neighborhood was created;
		// if the self is not supposed to be in the neighborhood, it's not (see constructor for details)
		// so just go through the list of particles and do the standard FIPS calculation
		for (int p = 0 ; p < numPartsInNeigh ; ++p) {
			Particle nextParticle = neighParticles[p];
			DoubleVector vectorToNextPBest = DoubleVector.sub(nextParticle.getPosition(), position);
			vectorToNextPBest.multRandomScalar(0.0, componentTheta);
			acceleration.addVector(vectorToNextPBest);
		}

		return acceleration;
	}
	

	// **********************************************************************************************************************************


	
	// reset particles encountered
	public static void setArrayAllFalse (boolean[] array) {
		
		for (int n = 0 ; n < array.length ; ++n) {
			array[n] = false;
		}

	}


		
	
	public boolean containsParticle (Particle particle) {

		for (int p = 0 ; p < neighParticles.length ; ++p) {
			if (neighParticles[p] == particle)	{
				return true;
			}
		}

		return false;
	}



	public void addNeighbor(Particle particle) {
		neighParticles[nextNeighIndex++] = particle;
	}



	
	public Particle[] getNeighParticles() {

		return neighParticles;

	}

	
	public int sizeNeigh() {
		return neighParticles.length;
	}


	public int getNeighID () {
		return neighID;
	}

	


}
