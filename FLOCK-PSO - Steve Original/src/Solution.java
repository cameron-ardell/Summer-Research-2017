
public class Solution {

	
	private DoubleVector position;
	private double value;
	private double error;
	private int iterationFound;
	private int particleID;

//	private int mostRecIterUsedOnPBestsList;
//	private int timesUsedOnPBestsList;
	
	
	public Solution () {
		this.position = null;
		this.value = 0.0;
		this.error = 0.0;
		this.iterationFound = 0;
		this.particleID = 0;


	}

	public Solution (DoubleVector position, double value, double error, int iterationFound, int particleID) {
		this.position = position.getCopy();
		this.value = value;
		this.error = error;
		this.iterationFound = iterationFound;
		this.particleID = particleID;	
//		this.mostRecIterUsedOnPBestsList = 0;
//		this.timesUsedOnPBestsList = 0;

	}

	


	
	public Solution getCopy() {	
		Solution retSol = new Solution(position, value, error, iterationFound, particleID);
//		retSol.setMostRecIterUsed(mostRecIterUsedOnPBestsList);
//		retSol.setTimesUsed(timesUsedOnPBestsList);
		
		return retSol;
		
	}

	
	public void copyFrom(Solution s) {
		this.position = s.getPositionCopy();
		this.value = s.getFunctionValue();
		this.error = s.getError();
		this.iterationFound = s.getIterationFound();
		this.particleID = s.getParticleID();
//		this.mostRecIterUsedOnPBestsList = s.getMostRecIterUsed();

	}
	
	
	public double distance (Solution s) {
		return position.distance(s.getPosition());
	}
	
	
//	public void averageIn(Solution sol, int functionNum) {
//		position.averageIn(sol.getPosition());
//		double[] results = TestFunctions.evalWithError(position, functionNum);
//		value = results[TestFunctions.RESULTS_VAL_INDEX];
//		error = results[TestFunctions.RESULTS_ERR_INDEX];
////		value = Double.MAX_VALUE;
////		error = Double.MAX_VALUE;
////		iterationNumCreated = PSO.iterationNum;
//		particleID = -1;
//	}
	
	public int getParticleID() {
		return particleID;
	}

	public void setParticleID(int particleID) {
		this.particleID = particleID;
	}


	// return the actual position
	public DoubleVector getPosition() {
		return position;
	}

	// return a copy of the position
	public DoubleVector getPositionCopy() {
		return position.getCopy();
	}

	// copy from a given position to this position
	public void copyFromPosition(DoubleVector inPosition) {
		this.position.copyFrom(inPosition);
	}

	
//	public int getMostRecIterUsed() {
//		return mostRecIterUsedOnPBestsList;
//	}
//
//	public void setMostRecIterUsed(int mostRecIterUsed) {
//		this.mostRecIterUsedOnPBestsList = mostRecIterUsed;
//	}
//
//	public int getTimesUsed() {
//		return timesUsedOnPBestsList;
//	}
//
//	public void setTimesUsed(int timesUsed) {
//		this.timesUsedOnPBestsList = timesUsed;
//	}

	public double getFunctionValue() {
		return value;
	}

	public void setFunctionValue(double value) {
		this.value = value;
	}

	
	public double getError() {
		return error;
	}

	public double getAbsValError() {
		return Math.abs(error);
	}

	public void setError(double error) {
		this.error = error;
	}

	public int getIterationFound() {
		return iterationFound;
	}
	
	public void setIterationFound(int iterationFound) {
		this.iterationFound = iterationFound;
	}

	
//	public boolean getApproximated() {
//		return approximated;
//	}
//
//	public void setApproximated(boolean approximated) {
//		this.approximated = approximated;
//	}

	
//	public boolean getAlreadyUsed() {
//		return alreadyUsed;
//	}
//
//	public void setAlreadyUsed(boolean alreadyUsed) {
//		this.alreadyUsed = alreadyUsed;
//	}

	
	
	public double calcSolutionValue (int functionNum) {
		double[] results = TestFunctions.evalWithError(position , functionNum);
		return results[TestFunctions.VAL_INDEX];
	}

	public double calcSolutionError (int functionNum) {
		double[] results = TestFunctions.evalWithError(position , functionNum);
		return results[TestFunctions.ERR_INDEX];
	}



	public void print() {
		position.print();
		System.out.printf("%s%2d%s%.8e%s%.8e%s%d", "  particleID = ", particleID, "  val = ", value,"  err = ", error, "  iter = ", iterationFound);
	}

	public void println() {
		print();
		System.out.println();
	}



//	public void printExp() {
//		position.printExp();
//		System.out.printf("%s%2d%s%.8e%s%.8e%s%d", "  particleID = ", particleID, "  val = ", value,"  err = ", error, "  iter = ", iterationCreated);
//	}
//
//	public void printlnExp() {
//		printExp();
//		System.out.println();
//	}






}


