/*
 * 
 /* 
 * Stephen Majercik
 * Frank Mauceri
 * last updated 20 May 2013
 * 
 * Each Boid has its own  parameters, i.e. whatever parameters the boid was created with can
 * be modified on an individual basis.  this seems to be very important from the standpoint 
 * of creating interesting behaviors 
 * 
 * Methods that take care of the motion and rendering of a boid:
 * 	1) calculate the new velocity of a boid
 * 	2) move the boid
 * 	3) render the boid
 * 
 * In the process of moving the boids:
 * 	1) "collisions" (when two voids come closer to each other than the "proximityThreshold" may be reported to Max
 * 	2) lines may be drawn between boids in the same flock in the same neighborhood
 * 
 * 
 */



// Processing classes 
//import processing.core.PApplet;
//import processing.core.PConstants;
//import processing.core.PVector;

// data structure for the flock

public class Boid {

	// make components of initial velocity very small 
	private static final float INIT_VELOCITY_DIMENSION_MAGNITUDE = 1.0f;
	// if a random component is added to the motion, keep it small
	private static final float RANDOM_MOTION_DIMENSION_MAGNITUDE = 1.0f;
	// how close to the boundary does a Boid need to be before it "bounces back"?
	private static final int BOUDARY_SENSING_THRESHOLD = 5;

	// IDs for Boid and Flock
	public int flockID;
	public int boidID;

	// Boid parameters
	private float velocityScale;           // not sure what this is for; have to ask Frank...
	private float maxSpeed;                // speed limit
	private float normalSpeed;             // pacekeeping (see below), when used, tries to keep boid to this speed
	private float neighborRadius;          // determines which other Boids are neighbors of a given Boid    
	private float separationWeight;        // how strongly do Boids move away from every neighbor: range = [0.0, 100.0]
	private float alignmentWeight;         // how strongly do Boids align match average velocity of their neighbors: range = [0.0, 1.0]
	private float cohesionWeight;          // how strongly do Boids move toward the average position of their neighbors: range = [0.0, 1.0]
	private float pacekeepingWeight;       // how strongly do Boids stick to the normalSpeed: range = [0.0, 1.0]
	private float randomMotionProbability; // probability that a small random component is introduced into the motion (see Boid class for more info)
	private int age;                       // for purposes of removing boid if mortality being used
	
	
	private float numNeighborsOwnFlock = 0;
	private float numNeighborsAllFlocks = 0;
	
	
	// boid's location and velocity
//	public PVector location;  
//	public PVector velocity;
//	public PVector nextVelocity;  

	// needed for access to rendering methods
//	public PApplet parent;


	// for fooling around with various types of motion (not all of which are actually predator/prey) 
	public int myPreyID = 0;
	public Boid preyBoid = null;


	public Boid () {
		
	}
	
	// constructor
//	public Boid(int flockID, int boidID, PVector location, float velocityScale, 
//			float maxSpeed, float normalSpeed, float neighborRadius, float separationWeight, float alignWeight, 
//			float cohesionWeight, float pacekeepingWeight, float randomMotionProbability, PApplet parent) {
//
//
//		this.flockID = flockID;
//		this.boidID = boidID;
//		this.location = location.get();         // make a copy
//		this.velocityScale = velocityScale;
//		this.maxSpeed = maxSpeed;
//		this.normalSpeed= normalSpeed;
//		this.neighborRadius = neighborRadius;
//		this.separationWeight = separationWeight;
//		this.alignmentWeight = alignWeight;
//		this.cohesionWeight = cohesionWeight;
//		this.pacekeepingWeight = pacekeepingWeight;
//		this.randomMotionProbability = randomMotionProbability;
//		this.age = 0;
//
//		// start with small random velocity
//		velocity = new PVector(MusicSwarm.rand.nextFloat() * (2.0f * INIT_VELOCITY_DIMENSION_MAGNITUDE) - INIT_VELOCITY_DIMENSION_MAGNITUDE, 
//				MusicSwarm.rand.nextFloat() * (2.0f * INIT_VELOCITY_DIMENSION_MAGNITUDE) - INIT_VELOCITY_DIMENSION_MAGNITUDE,
//				MusicSwarm.rand.nextFloat() * (2.0f * INIT_VELOCITY_DIMENSION_MAGNITUDE) - INIT_VELOCITY_DIMENSION_MAGNITUDE);
//		nextVelocity = velocity.get();  
//
//		myPreyID = 0; 
//
//		// for rendering
//		this.parent = parent;  	
//
//
//		
//	}


	

	// calculate the new velocity, move, and render
//	void run(Flock[] allFlocks) {
//
//		// getting older....
//		++age;
//
//        // standard flocking behavior
//        //		calcNewVelocityStandard(allFlocks);
//        
//        // this is the behavior that the hand-created tree encodes
//		calcNewVelocityNiceBehaviorFromWorkshop(allFlocks);
//
//		// reset the Boid's velocity and move the Boid
//		velocity.set(nextVelocity.x, nextVelocity.y, nextVelocity.z);
//		location.add(velocity);
//
//		// render the Boid graphically
//		render();
//
//	}



	// nice behavior handcrafted at PERFORMAMATICS Workshop (January, 2013)
	// to calculate motions of Boids and detect proximity events
//	void calcNewVelocityNiceBehaviorFromWorkshop(Flock[] allFlocks) {
//
//		// the new acceleration
//		PVector acceleration = new PVector(0.0f,0.0f,0.0f);
//
//		// need sum of locations of Boids in the neighborhood for acceleration due to cohesion,
//		// since cohesion = acceleration toward the average location of Boids in the neighborhood,
//		// but for this behavior a Boid only coheres with Boids in its own flock
//		PVector sumNeighborLocationsOwnFlock = new PVector(0,0,0);   
//		
//		// need sum of velocities of Boids in the neighborhood for acceleration due to alignment,
//		// since alignment = acceleration toward the average velocity of Boids in the neighborhood,
//		// but for this behavior a Boid only aligns with Boids in its own flock
//		PVector sumNeighborVelocitiesOwnFlock = new PVector(0.0f,0.0f,0.0f);
//
//		// so we need to count boids in the boid's own flock as well as boids in all the flocks 
//		// (in the neighborhood)
//		numNeighborsOwnFlock = 0;
//		numNeighborsAllFlocks = 0;
//
//		// need to get info on all Boids in all Flocks in the neighborhood
//		for (int nextFlockID = 0 ; nextFlockID < allFlocks.length ; nextFlockID++) {
//
//			synchronized(allFlocks[nextFlockID].boids) {
//
//				// go to next iteration of the loop if this flock is empty
//				if (allFlocks[nextFlockID].flockEmpty())
//					continue;
//
//				List<Boid> boids = allFlocks[nextFlockID].boids;
//
//				// get info for all Boids in this Flock
//				for (int i = 0 ; i < boids.size(); i++) {
//
//					Boid otherBoid = (Boid) boids.get(i);
//					float dist = location.dist(otherBoid.location);
//
//					// dist > 0 so that a Boid does not count itself as a Boid in the neighborhood
//					if (dist > 0 && dist <= neighborRadius) {
//
//						// for cohesion and alignment, but only for boids in the same flock
//						if (nextFlockID == flockID) {
//							sumNeighborLocationsOwnFlock.add(otherBoid.location); 
//							sumNeighborVelocitiesOwnFlock.add(otherBoid.velocity);
//						}
//
//						// calculate and weight vector pointing away from a neighbor in any flock
//						PVector vectorToThisBoid = PVector.sub(location, otherBoid.location);
//						vectorToThisBoid.div(dist*dist);  
//						vectorToThisBoid.mult(separationWeight);
//						acceleration.add(vectorToThisBoid);  
//
//						// keep count of neighbors in the same flock
//						if (nextFlockID == flockID)
//							++numNeighborsOwnFlock; 
//
//						// also keep count of neighbors in all flocks
//						++numNeighborsAllFlocks;
//
//						// draw lines between Boids from the same flock in the same neighborhood
//						if (flockID == nextFlockID)
//							connectBoids(otherBoid);
//
//
//					}
//				} 
//			}
//		}
//
//
//		// a boid coheres and aligns with others ONLY IN ITS OWN FLOCK 
//		if (numNeighborsOwnFlock > 0) {
//			// cohesion steering: steer in the direction of the average location of your neighbors
//			PVector cohesionVector = PVector.div(sumNeighborLocationsOwnFlock, (float) numNeighborsOwnFlock);        
//			cohesionVector.sub(location);
//			cohesionVector.mult(cohesionWeight);
//			acceleration.add(cohesionVector);
//
//			// alignment steering: steer so as to align your velocity with the average velocity of your neighbors
//			PVector alignmentVector = PVector.div(sumNeighborVelocitiesOwnFlock, (float) numNeighborsOwnFlock);
//			alignmentVector.sub(velocity);
//			alignmentVector.mult(alignmentWeight);
//			acceleration.add(alignmentVector);
//		}
//
//		// handcrafted rules to obtain "interesting behavior found at PERFORMAMATICS Workshop (January, 2013)
//        // This is the behavior encoded in the hand-created program tree that is run right after the
//        // commented out code. 3/11/17
//        
////		if (numNeighborsOwnFlock < 20) {
////			neighborRadius += 1;
////			if (neighborRadius > 1000)   
////				neighborRadius = 1000;
////			cohesionWeight = 0.9f;
////			normalSpeed = 6;
////			maxSpeed = 6;
////		}
////		else if (numNeighborsOwnFlock > 20){
////			neighborRadius -= 1;			
////			if (neighborRadius < 0)
////				neighborRadius = 0;
////			cohesionWeight = 0.1f;
////			normalSpeed = 24;
////			maxSpeed = 24;
//
//        // The tree program is a static variable in the Flock class. Must send it a reference to
//        // this boid so it can call methods in here to get/change variable values, as necessary.
//		Flock.program.run(this);
//		
//		
//		// with the probability specified by the parameter randomMotionProbability, introduce a small
//		// random perturbation (magnitude defined by RANDOM_MOTION_DIMENSION_MAGNITUDE) into each 
//		// acceleration component
//		if (MusicSwarm.rand.nextFloat() < randomMotionProbability) {
//			acceleration.add(new PVector(MusicSwarm.rand.nextFloat() * (2.0f * RANDOM_MOTION_DIMENSION_MAGNITUDE) - RANDOM_MOTION_DIMENSION_MAGNITUDE, 
//					MusicSwarm.rand.nextFloat() * (2.0f * RANDOM_MOTION_DIMENSION_MAGNITUDE) - RANDOM_MOTION_DIMENSION_MAGNITUDE,
//					MusicSwarm.rand.nextFloat() * (2.0f * RANDOM_MOTION_DIMENSION_MAGNITUDE) - RANDOM_MOTION_DIMENSION_MAGNITUDE));
//		} 
//
//		// update velocity
//		nextVelocity.add(acceleration);
//
//		// make sure we don't exceed maxSpeed
//		if (nextVelocity.mag() > maxSpeed) {
//			float normalizationFactor = maxSpeed / nextVelocity.mag();
//			nextVelocity.mult(normalizationFactor);
//		}
//
//		// pacekeeping (stick to normalSpeed to the extent indicated by pacekeepingWeight)
//		PVector pacekeeping = 
//			PVector.mult(nextVelocity, ((normalSpeed - nextVelocity.mag()) / nextVelocity.mag() * pacekeepingWeight));
//		nextVelocity.add(pacekeeping);
//
//		// bounce back from the boundaries of the space
//		PVector boundaryAcc = new PVector(0,0,0);
//		if (location.x < (-MusicSwarm.WINDOW_HEIGHT/2) + BOUDARY_SENSING_THRESHOLD)         
//			boundaryAcc.x = maxSpeed;	
//		else if (location.x > MusicSwarm.WINDOW_HEIGHT/2 - BOUDARY_SENSING_THRESHOLD) 
//			boundaryAcc.x = -maxSpeed;
//		if (location.y < (-MusicSwarm.WINDOW_HEIGHT/2) + BOUDARY_SENSING_THRESHOLD)
//			boundaryAcc.y = maxSpeed;	
//		else if (location.y > MusicSwarm.WINDOW_HEIGHT/2 - BOUDARY_SENSING_THRESHOLD) 
//			boundaryAcc.y = -maxSpeed;	  
//		if (location.z < (-MusicSwarm.WINDOW_HEIGHT/2) + BOUDARY_SENSING_THRESHOLD)
//			boundaryAcc.z = maxSpeed;	
//		else if (location.z > MusicSwarm.WINDOW_DEPTH/2 - BOUDARY_SENSING_THRESHOLD) 
//			boundaryAcc.z = -maxSpeed;	     
//		nextVelocity.add(boundaryAcc);     
//
//	}

    
    // called by a GPNode when it needs the value of a variable in the Boid object
    public float getVarValue(String varName) {
        
        
        if (varName.equals("velocityScale")) {
            return velocityScale;
        }
        
        else if (varName.equals("maxSpeed")) {
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
            return randomMotionProbability;
        }
        
        else if (varName.equals("numNeighborsOwnFlock")) {
            return numNeighborsOwnFlock;
        }
        
        else if (varName.equals("numNeighborsAllFlocks")) {
            return numNeighborsAllFlocks;
        }
        
        
        else {
            System.out.println("error: unknown variable name in Boid.getVarValue: \"" + varName + "\"");
            System.exit(0);
        }
        
        return 0.0f;
        
    }
    
    // called by a GPNode when it needs to assign a value to variable in the Boid object
    public void assignVariable(String varName, float value) {

        
        if (varName.equals("velocityScale")) {
            velocityScale = value;
        }
        
        else if (varName.equals("maxSpeed")) {
            maxSpeed = value;
        }
        
        else if (varName.equals("normalSpeed")) {
            normalSpeed = value;
        }
        
        else if (varName.equals("neighborRadius")) {
            neighborRadius = value;
        }
        
        else if (varName.equals("separationWeight")) {
            separationWeight = value;
        }
        
        else if (varName.equals("alignmentWeight")) {
            alignmentWeight = value;
        }
        
        else if (varName.equals("cohesionWeight")) {
            cohesionWeight = value;
        }
        
        else if (varName.equals("pacekeepingWeight")) {
            pacekeepingWeight = value;
        }
        
        else if (varName.equals("randomMotionProbability")) {
            randomMotionProbability = value;
        }
        
        else if (varName.equals("numNeighborsOwnFlock")) {
            numNeighborsOwnFlock = value;
        }
        
        else if (varName.equals("numNeighborsAllFlocks")) {
            numNeighborsAllFlocks = value;
        }
        
        else {
            System.out.println("error: unknown variable name in Boid.assignVariable: \"" + varName + "\"");
            System.exit(0);
        }
        
        
    }
    
    // called by a GPNode when it needs increment a variable in the Boid object
    public void increment(String varName) {
        
        
        if (varName.equals("velocityScale")) {
            ++velocityScale;
        }
        
        else if (varName.equals("maxSpeed")) {
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
            ++randomMotionProbability;
        }
        
        else if (varName.equals("numNeighborsOwnFlock")) {
            ++numNeighborsOwnFlock;
        }
        
        else if (varName.equals("numNeighborsAllFlocks")) {
            ++numNeighborsAllFlocks;
        }
        
        else {
            System.out.println("error: unknown variable name in Boid.increment: \"" + varName + "\"");
            System.exit(0);
        }
        
    }
    
    
    // called by a GPNode when it needs decrement a variable in the Boid object
    public void decrement(String varName) {

        
        
        if (varName.equals("velocityScale")) {
            --velocityScale;
        }
        
        else if (varName.equals("maxSpeed")) {
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
            --randomMotionProbability;
        }
        
        else if (varName.equals("numNeighborsOwnFlock")) {
            --numNeighborsOwnFlock;
        }
        
        else if (varName.equals("numNeighborsAllFlocks")) {
            --numNeighborsAllFlocks;
        }
        
        else {
            System.out.println("error: unknown variable name in Boid.decrement: \"" + varName + "\"");
            System.exit(0);
        }
        
        
    }



    
//    
//    // calculate the new velocity of the Boid;
//    // also detects and reports proximity events, but this has not been used for quite a while (as of 5/15/13)
//    void calcNewVelocityStandard(Flock[] allFlocks) {
//        
//        // the new acceleration
//        PVector acceleration = new PVector(0.0f,0.0f,0.0f);
//        
//        // need sum of locations of Boids in the neighborhood for acceleration due to cohesion,
//        // since cohesion = acceleration toward the average location of Boids in the neighborhood
//        PVector sumNeighborLocations = new PVector(0.0f,0.0f,0.0f);
//        
//        // need sum of velocities of Boids in the neighborhood for acceleration due to alignment,
//        // since alignment = acceleration toward the average velocity of Boids in the neighborhood
//        PVector sumNeighborVelocities = new PVector(0.0f,0.0f,0.0f);
//        
//        int numNeighbors = 0;
//        
//        // need to get info on all Boids in all Flocks in the neighborhood
//        for (int nextFlockID = 0 ; nextFlockID < allFlocks.length ; nextFlockID++) {
//            
//            
//            // go to next iteration of the loop if this flock is empty
//            if (allFlocks[nextFlockID].flockEmpty())
//                continue;
//            
//            List<Boid> boids = allFlocks[nextFlockID].boids;
//            
//            // get info for all Boids in this Flock
//            for (int i = 0 ; i < boids.size(); i++) {
//                
//                Boid otherBoid = (Boid) boids.get(i);
//                float dist = location.dist(otherBoid.location);
//                
//                // dist > 0 so that a Boid does not count itself as a Boid in the neighborhood
//                if (dist > 0 && dist <= neighborRadius) {
//                    
//                    // keep count of neighbors
//                    ++numNeighbors;
//                    
//                    // sum locations for cohesion calculation after all neighbors have been processed
//                    sumNeighborLocations.add(otherBoid.location);
//                    
//                    // sum velocities for alignment calculation after all neighbors have been processed
//                    sumNeighborVelocities.add(otherBoid.velocity);
//                    
//                    // for separation:
//                    // calculate and weight vector pointing away from neighbor; add to acceleration
//                    PVector vectorToThisBoid = PVector.sub(location, otherBoid.location);
//                    // separation force is inversely proportional to the square of the distance
//                    vectorToThisBoid.div(dist*dist);
//                    // but some experiments indicate to me that we might want to consider reducing the
//                    // denominator to dist^1.5, or possibly even dist. using dist^2 seems to weaken the
//                    // separation force to an extent that makes it very difficult for separation to have
//                    // any impact when the cohesion is at its max; I would think that even when cohesion
//                    // is high, if the separation weight is >50, it should loosen tight clusters significantly,
//                    // which does not happen currently
//                    //						vectorToThisBoid.div((float) Math.pow(dist, 1.5));
//                    //						vectorToThisBoid.div(dist);
//                    vectorToThisBoid.mult(separationWeight);
//                    acceleration.add(vectorToThisBoid);
//                    
//                    // draw lines between Boids from the same flock in the same neighborhood
//                    if (flockID == nextFlockID)
//                        connectBoids(otherBoid);
//                    
//                    
//                }
//            }
//        }
//        
//        
//        if (numNeighbors > 0) {
//            // cohesion steering: steer in the direction of the average location of your neighbors
//            PVector cohesionVector = PVector.div(sumNeighborLocations, (float) numNeighbors);
//            cohesionVector.sub(location);
//            cohesionVector.mult(cohesionWeight);
//            acceleration.add(cohesionVector);
//            
//            // alignment steering: steer so as to align your velocity with the average velocity of your neighbors
//            PVector alignmentVector = PVector.div(sumNeighborVelocities, (float) numNeighbors);
//            alignmentVector.sub(velocity);
//            alignmentVector.mult(alignmentWeight);
//            acceleration.add(alignmentVector);
//        }
//        
//        
//        // with the probability specified by the parameter randomMotionProbability, introduce a small
//        // random perturbation (magnitude defined by RANDOM_MOTION_DIMENSION_MAGNITUDE) into each
//        // acceleration component
//        if (MusicSwarm.rand.nextFloat() < randomMotionProbability) {
//            acceleration.add(new PVector(MusicSwarm.rand.nextFloat() * (2.0f * RANDOM_MOTION_DIMENSION_MAGNITUDE) - RANDOM_MOTION_DIMENSION_MAGNITUDE,
//                                         MusicSwarm.rand.nextFloat() * (2.0f * RANDOM_MOTION_DIMENSION_MAGNITUDE) - RANDOM_MOTION_DIMENSION_MAGNITUDE,
//                                         MusicSwarm.rand.nextFloat() * (2.0f * RANDOM_MOTION_DIMENSION_MAGNITUDE) - RANDOM_MOTION_DIMENSION_MAGNITUDE));
//        }
//        
//        // update velocity
//        nextVelocity.add(acceleration);
//        
//        
//        
//        // make sure we don't exceed maxSpeed
//        if (nextVelocity.mag() > maxSpeed) {
//            nextVelocity.mult(maxSpeed / nextVelocity.mag());
//        }
//        
//        // pacekeeping (stick to normalSpeed to the extent indicated by pacekeepingWeight)
//        PVector pacekeeping = 
//        PVector.mult(nextVelocity, ((normalSpeed - nextVelocity.mag()) / nextVelocity.mag() * pacekeepingWeight));
//        nextVelocity.add(pacekeeping);
//        
//        // bounce back from the boundaries of the space
//        PVector boundaryAcc = new PVector(0,0,0);
//        if (location.x < (-MusicSwarm.WINDOW_HEIGHT/2) + BOUDARY_SENSING_THRESHOLD)         
//            boundaryAcc.x = maxSpeed;	
//        else if (location.x > MusicSwarm.WINDOW_HEIGHT/2 - BOUDARY_SENSING_THRESHOLD) 
//            boundaryAcc.x = -maxSpeed;
//        if (location.y < (-MusicSwarm.WINDOW_HEIGHT/2) + BOUDARY_SENSING_THRESHOLD)
//            boundaryAcc.y = maxSpeed;	
//        else if (location.y > MusicSwarm.WINDOW_HEIGHT/2 - BOUDARY_SENSING_THRESHOLD) 
//            boundaryAcc.y = -maxSpeed;	  
//        if (location.z < (-MusicSwarm.WINDOW_HEIGHT/2) + BOUDARY_SENSING_THRESHOLD)
//            boundaryAcc.z = maxSpeed;	
//        else if (location.z > MusicSwarm.WINDOW_DEPTH/2 - BOUDARY_SENSING_THRESHOLD) 
//            boundaryAcc.z = -maxSpeed;	     
//        nextVelocity.add(boundaryAcc);     
//        
//        // velocity scale from Max
//        // not sure why we have this....
//        nextVelocity.mult(velocityScale);     
//        
//    }
//    
//    
//
//    
//	// draw a line between two boids;
//	// the method is called in the calcNewVelocity method(s),
//	// but only if the two boids are in the same neighborhood and in ths same flock
//	void connectBoids(Boid otherBoid) {
//
//		PVector otherBoidLocation;
//		if (MusicSwarm.CONNECTED_COMPONENTS) {
//			otherBoidLocation = otherBoid.location;
//			if (MusicSwarm.renderMethod == MusicSwarm.RENDER_3D) {
//				parent.line(location.x,location.y, location.z, otherBoidLocation.x,otherBoidLocation.y, otherBoidLocation.z);
//				parent.stroke(MusicSwarm.FLOCKCOLOR[flockID][0],MusicSwarm.FLOCKCOLOR[flockID][1],MusicSwarm.FLOCKCOLOR[flockID][2]);	
//			}
//			if (MusicSwarm.renderMethod == MusicSwarm.RENDER_2D) {
//				parent.line(location.x,location.y, otherBoidLocation.x,otherBoidLocation.y);
//				parent.stroke(MusicSwarm.FLOCKCOLOR[flockID][0],MusicSwarm.FLOCKCOLOR[flockID][1],MusicSwarm.FLOCKCOLOR[flockID][2]);	
//			}
//		}
//	}
//
//
//
//
//
//
//	// redraw the boid
//	void render() {
//
//		if (MusicSwarm.renderMethod == MusicSwarm.RENDER_2D) {
//			// Draw a triangle rotated in the direction of velocity
//			float theta = velocity.heading2D() + (float) Math.PI/2;
//			parent.noStroke();
//			parent.fill(MusicSwarm.FLOCKCOLOR[flockID][0], MusicSwarm.FLOCKCOLOR[flockID][1], MusicSwarm.FLOCKCOLOR[flockID][2], MusicSwarm.B_ALPHA);
//			parent.pushMatrix();
//			parent.translate(location.x, location.y);
//			parent.rotate(theta);
//			parent.beginShape(PConstants.TRIANGLES);
//			parent.vertex(0, -MusicSwarm.BOID_SIZE*2);
//			parent.vertex(-MusicSwarm.BOID_SIZE, MusicSwarm.BOID_SIZE*2);
//			parent.vertex(MusicSwarm.BOID_SIZE, MusicSwarm.BOID_SIZE*2);
//			parent.endShape();
//			parent.popMatrix();
//		}
//
//		//3d render
//		else if (MusicSwarm.renderMethod == MusicSwarm.RENDER_3D) {
//			parent.pushMatrix();
//			parent.translate(location.x,location.y,location.z);
//			parent.rotateY(PApplet.atan2(-velocity.z,velocity.x));
//			parent.rotateZ(PApplet.asin(velocity.y/velocity.mag()));
//			parent.noStroke();
//			parent.fill(MusicSwarm.FLOCKCOLOR[flockID][0], MusicSwarm.FLOCKCOLOR[flockID][1], MusicSwarm.FLOCKCOLOR[flockID][2], MusicSwarm.B_ALPHA);
//
//			//drawing boids
//			parent.beginShape(PConstants.TRIANGLES);
//
//			parent.vertex(3*MusicSwarm.BOID_SIZE,0,0);
//			parent.vertex(-3*MusicSwarm.BOID_SIZE,2*MusicSwarm.BOID_SIZE,0);
//			parent.vertex(-3*MusicSwarm.BOID_SIZE,-2*MusicSwarm.BOID_SIZE,0);
//
//			parent.vertex(3*MusicSwarm.BOID_SIZE,0,0);
//			parent.vertex(-3*MusicSwarm.BOID_SIZE,2*MusicSwarm.BOID_SIZE,0);
//			parent.vertex(-3*MusicSwarm.BOID_SIZE,0,2*MusicSwarm.BOID_SIZE);
//
//			parent.vertex(3*MusicSwarm.BOID_SIZE,0,0);
//			parent.vertex(-3*MusicSwarm.BOID_SIZE,0,2*MusicSwarm.BOID_SIZE);
//			parent.vertex(-3*MusicSwarm.BOID_SIZE,-2*MusicSwarm.BOID_SIZE,0);
//
//			parent.vertex(-3*MusicSwarm.BOID_SIZE,0,2*MusicSwarm.BOID_SIZE);
//			parent.vertex(-3*MusicSwarm.BOID_SIZE,2*MusicSwarm.BOID_SIZE,0);
//			parent.vertex(-3*MusicSwarm.BOID_SIZE,-2*MusicSwarm.BOID_SIZE,0);
//
//			parent.endShape();
//			parent.popMatrix();
//		}
//
//	}
//
//	
//	// print current parameters of this boid
//	public void printParameters() {
//
//		System.out.printf("               ms = %4.1f ns = %4.1f nr = %5.1f se = %5.1f al = %3.1f  co = %3.1f pk = %3.1f rm = %3.1f\n", 
//				maxSpeed,             
//				normalSpeed,
//				neighborRadius,        
//				separationWeight,
//				alignmentWeight,
//				cohesionWeight,
//				pacekeepingWeight,
//				randomMotionProbability);
//
//	}
//
//
//	// getters
//	
//	public PVector getLocation() {
//		return location;
//	}
//
//	public PVector getVelocity() {
//		return velocity;
//	}
//	
//	public int getAge() {
//		return age;
//	}
//	
	
	

}






