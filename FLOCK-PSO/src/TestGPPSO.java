
public class TestGPPSO {

	public static void main(String[] args) {
		int numGens = 100;
		int numTrees = 20;
		int numRuns = 50;
		Population pop = new Population(numGens, numTrees, numRuns);
		pop.run(numGens, numRuns);
	}

}
