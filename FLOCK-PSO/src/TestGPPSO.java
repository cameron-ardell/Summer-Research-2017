
public class TestGPPSO {

	public static void main(String[] args) {
		int numGens = 3;
		int numTrees = 6;
		int numRuns = 10;
		Population pop = new Population(numGens, numTrees, numRuns);
		pop.run(numGens, numRuns);
	}

}
