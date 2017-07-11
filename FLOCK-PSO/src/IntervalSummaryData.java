
import java.util.Arrays;

public class IntervalSummaryData {


	private double averageFunctionValue = 0.0;
	private double stdDevFunctionValue = 0.0;
	private double rootMeanSqrErrFunctionValue = 0.0;
	private double minimumFunctionValue = 0.0;
	private double firstQuartileFunctionValue = 0.0;
	private double medianFunctionValue = 0.0;
	private double thirdQuartileFunctionValue = 0.0;
	private double maximumFunctionValue = 0.0;
	
	private double averageAbsValError = 0.0;
	private double minimumAbsValError = 0.0;
	private double firstQuartileAbsValError = 0.0;
	private double medianAbsValError = 0.0;
	private double thirdQuartileAbsValError = 0.0;
	private double maximumAbsValError = 0.0;

	private double averageBoundingBoxVolume = 0.0;
	private double minimumBoundingBoxVolume = 0.0;
	private double firstQuartileBoundingBoxVolume = 0.0;
	private double medianBoundingBoxVolume = 0.0;
	private double thirdQuartileBoundingBoxVolume = 0.0;
	private double maximumBoundingBoxVolume = 0.0;
	
	public IntervalSummaryData () {
		
	}
	
	
	public IntervalSummaryData getCopy() {
		
		IntervalSummaryData returnISD = new IntervalSummaryData();

		returnISD.copyFrom(this);

		return returnISD;
	}
	

	public void copyFrom (IntervalSummaryData fromISD) {

		averageFunctionValue = fromISD.getAverageFunctionValue();
		stdDevFunctionValue = fromISD.getStdDevFunctionValue();
		rootMeanSqrErrFunctionValue = fromISD.getRootMeanSqrErrFunctionValue();
		minimumFunctionValue = fromISD.getMinimumFunctionValue();
		firstQuartileFunctionValue = fromISD.getFirstQuartileFunctionValue();
		medianFunctionValue = fromISD.getMedianFunctionValue();
		thirdQuartileFunctionValue = fromISD.getThirdQuartileFunctionValue();
		maximumFunctionValue = fromISD.getMaximumFunctionValue();
		
		averageAbsValError = fromISD.getAverageAbsValError();
		minimumAbsValError = fromISD.getMinimumAbsValError();
		firstQuartileAbsValError = fromISD.getFirstQuartileAbsValError();
		medianAbsValError = fromISD.getMedianAbsValError();
		thirdQuartileAbsValError = fromISD.getThirdQuartileAbsValError();
		maximumAbsValError = fromISD.getMaximumAbsValError();
		
		averageBoundingBoxVolume = fromISD.getAverageBoundingBoxVolume();
		minimumBoundingBoxVolume = fromISD.getMinimumBoundingBoxVolume();
		firstQuartileBoundingBoxVolume = fromISD.getFirstQuartileBoundingBoxVolume();
		medianBoundingBoxVolume = fromISD.getMedianBoundingBoxVolume();
		thirdQuartileBoundingBoxVolume = fromISD.getThirdQuartileBoundingBoxVolume();
		maximumBoundingBoxVolume = fromISD.getMaximumBoundingBoxVolume();

	}
	

	
	// calculate summary statistics for the runs at a particular iteration (or number of FEs);
	// the data for that iteration is in runDataForInterval
	public void summarizeData(DataOutput[] runDataForInterval, int functionNum, int numDimensions) {

		double sumSquaredErrors = 0.0;
		
		averageFunctionValue = 0.0;
		minimumFunctionValue = Double.MAX_VALUE;
		maximumFunctionValue = -Double.MAX_VALUE;

		averageAbsValError = 0.0;
		minimumAbsValError = Double.MAX_VALUE;
		maximumAbsValError = -Double.MAX_VALUE;
		
		averageBoundingBoxVolume = 0.0;
		minimumBoundingBoxVolume = Double.MAX_VALUE;
		maximumBoundingBoxVolume = -Double.MAX_VALUE;

		int numRuns = runDataForInterval.length;
		
		for (int runNum = 0 ; runNum < numRuns ; ++runNum) {

			double functionValueForRun = runDataForInterval[runNum].getFunctionValue();
			averageFunctionValue += functionValueForRun;
			
			if (functionValueForRun < minimumFunctionValue)
				minimumFunctionValue = functionValueForRun;			
			if (functionValueForRun > maximumFunctionValue)
				maximumFunctionValue = functionValueForRun;
			
			double errorValueForRun = runDataForInterval[runNum].getError();
			double absValErrorForRun = Math.abs(errorValueForRun);			
			averageAbsValError += absValErrorForRun;
			
			if (absValErrorForRun < minimumAbsValError)
				minimumAbsValError = absValErrorForRun;			
			if (absValErrorForRun > maximumAbsValError)
				maximumAbsValError = absValErrorForRun;
			
			sumSquaredErrors += Math.pow(errorValueForRun, 2.0);
			
			double boundingBoxVolumeForRun = runDataForInterval[runNum].getBoundingBoxVolume();
			averageBoundingBoxVolume += boundingBoxVolumeForRun;
			
			if (boundingBoxVolumeForRun < minimumBoundingBoxVolume)
				minimumBoundingBoxVolume = boundingBoxVolumeForRun;			
			if (boundingBoxVolumeForRun > maximumBoundingBoxVolume)
				maximumBoundingBoxVolume = boundingBoxVolumeForRun;
			
		}
		
		averageFunctionValue /= numRuns;
		averageAbsValError /= numRuns;
		rootMeanSqrErrFunctionValue = Math.sqrt(sumSquaredErrors / numRuns);
		averageBoundingBoxVolume /= numRuns;

		double sumSquaredMeanDiffs = 0.0;
		for (int runNum = 0 ; runNum < numRuns ; ++runNum) {
			sumSquaredMeanDiffs += Math.pow(runDataForInterval[runNum].getFunctionValue() - averageFunctionValue, 2.0);
		}
		
		// unlike RMSE, std dev uses a denominator of n-1 to correct for bias
		stdDevFunctionValue = Math.sqrt(sumSquaredMeanDiffs / (numRuns - 1));

		
		
		// CALCULATE QUARTILES FOR FUNCTION VALUE

		// sort the runs by ascending function value
		Arrays.sort(runDataForInterval, new DataOutputFunctionValueComparator());
		
		// q1
		if (numRuns % 4 == 1)
			firstQuartileFunctionValue = runDataForInterval[numRuns/4].getFunctionValue();
		else
			firstQuartileFunctionValue = (runDataForInterval[numRuns/4 - 1].getFunctionValue() + runDataForInterval[numRuns/4].getFunctionValue()) / 2.0;

		// q2
		if (numRuns % 2 == 1)
			// if odd number of runs, take the middle one
			medianFunctionValue = runDataForInterval[numRuns/2].getFunctionValue();
		else
			// if even number of runs, average the middle two
			medianFunctionValue = (runDataForInterval[numRuns/2 - 1].getFunctionValue() + runDataForInterval[numRuns/2].getFunctionValue()) / 2.0;
		
		// q3
		if (numRuns % 4 == 1)
			thirdQuartileFunctionValue = runDataForInterval[(numRuns*3)/4].getFunctionValue();
		else
			thirdQuartileFunctionValue = (runDataForInterval[(numRuns*3)/4 - 1].getFunctionValue() + runDataForInterval[(numRuns*3)/4].getFunctionValue()) / 2.0;



		// CALCULATE QUARTILES FOR ABSOLUTE VALUE OF ERROR

		// sort the runs by ascending absolute value of error 
		Arrays.sort(runDataForInterval, new DataOutputAbsValErrorComparator());
		
		// q1
		if (numRuns % 4 == 1)
			firstQuartileAbsValError = runDataForInterval[numRuns/4].getAbsValError();
		else
			firstQuartileAbsValError = (runDataForInterval[numRuns/4 - 1].getAbsValError() + runDataForInterval[numRuns/4].getAbsValError()) / 2.0;

		// q2
		if (numRuns % 2 == 1)
			// if odd number of runs, take the middle one
			medianAbsValError = runDataForInterval[numRuns/2].getAbsValError();
		else
			// if even number of runs, average the middle two
			medianAbsValError = (runDataForInterval[numRuns/2 - 1].getAbsValError() + runDataForInterval[numRuns/2].getAbsValError()) / 2.0;
		
		// q3
		if (numRuns % 4 == 1)
			thirdQuartileAbsValError = runDataForInterval[(numRuns*3)/4].getAbsValError();
		else
			thirdQuartileAbsValError = (runDataForInterval[(numRuns*3)/4 - 1].getAbsValError() + runDataForInterval[(numRuns*3)/4].getAbsValError()) / 2.0;

		

		// CALCULATE QUARTILES FOR BOUNDING BOX VOLUME

		// sort the runs by ascending bounding box volume 
		Arrays.sort(runDataForInterval, new DataOutputBoundingBoxVolumeComparator());
		
		// q1
		if (numRuns % 4 == 1)
			firstQuartileBoundingBoxVolume = runDataForInterval[numRuns/4].getBoundingBoxVolume();
		else
			firstQuartileBoundingBoxVolume = (runDataForInterval[numRuns/4 - 1].getBoundingBoxVolume() + runDataForInterval[numRuns/4].getBoundingBoxVolume()) / 2.0;

		// q2
		if (numRuns % 2 == 1)
			// if odd number of runs, take the middle one
			medianBoundingBoxVolume = runDataForInterval[numRuns/2].getBoundingBoxVolume();
		else
			// if even number of runs, average the middle two
			medianBoundingBoxVolume = (runDataForInterval[numRuns/2 - 1].getBoundingBoxVolume() + runDataForInterval[numRuns/2].getBoundingBoxVolume()) / 2.0;
		
		// q3
		if (numRuns % 4 == 1)
			thirdQuartileBoundingBoxVolume = runDataForInterval[(numRuns*3)/4].getBoundingBoxVolume();
		else
			thirdQuartileBoundingBoxVolume = (runDataForInterval[(numRuns*3)/4 - 1].getBoundingBoxVolume() + runDataForInterval[(numRuns*3)/4].getBoundingBoxVolume()) / 2.0;
		
	}




	public double getAverageFunctionValue() {
		return averageFunctionValue;
	}

	public void setAverageFunctionValue(double averageFunctionValue) {
		this.averageFunctionValue = averageFunctionValue;
	}

	public double getStdDevFunctionValue() {
		return stdDevFunctionValue;
	}

	public void setStdDevFunctionValue(double stdDevFunctionValue) {
		this.stdDevFunctionValue = stdDevFunctionValue;
	}

	public double getRootMeanSqrErrFunctionValue() {
		return rootMeanSqrErrFunctionValue;
	}

	public void setRootMeanSqrErrFunctionValue(double rootMeanSqrErrFunctionValue) {
		this.rootMeanSqrErrFunctionValue = rootMeanSqrErrFunctionValue;
	}

	public double getMinimumFunctionValue() {
		return minimumFunctionValue;
	}

	public void setMinimumFunctionValue(double minimumFunctionValue) {
		this.minimumFunctionValue = minimumFunctionValue;
	}

	public double getFirstQuartileFunctionValue() {
		return firstQuartileFunctionValue;
	}

	public void setFirstQuartileFunctionValue(double firstQuartileFunctionValue) {
		this.firstQuartileFunctionValue = firstQuartileFunctionValue;
	}

	public double getMedianFunctionValue() {
		return medianFunctionValue;
	}

	public void setMedianFunctionValue(double medianFunctionValue) {
		this.medianFunctionValue = medianFunctionValue;
	}

	public double getThirdQuartileFunctionValue() {
		return thirdQuartileFunctionValue;
	}

	public void setThirdQuartileFunctionValue(double thirdQuartileFunctionValue) {
		this.thirdQuartileFunctionValue = thirdQuartileFunctionValue;
	}

	public double getMaximumFunctionValue() {
		return maximumFunctionValue;
	}

	public void setMaximumFunctionValue(double maximumFunctionValue) {
		this.maximumFunctionValue = maximumFunctionValue;
	}

	public double getAverageAbsValError() {
		return averageAbsValError;
	}

	public void setAverageAbsValError(double averageAbsValError) {
		this.averageAbsValError = averageAbsValError;
	}

	public double getMinimumAbsValError() {
		return minimumAbsValError;
	}

	public void setMinimumAbsValError(double minimumAbsValError) {
		this.minimumAbsValError = minimumAbsValError;
	}

	public double getFirstQuartileAbsValError() {
		return firstQuartileAbsValError;
	}

	public void setFirstQuartileAbsValError(double firstQuartileAbsValError) {
		this.firstQuartileAbsValError = firstQuartileAbsValError;
	}

	public double getMedianAbsValError() {
		return medianAbsValError;
	}

	public void setMedianAbsValError(double medianAbsValError) {
		this.medianAbsValError = medianAbsValError;
	}

	public double getThirdQuartileAbsValError() {
		return thirdQuartileAbsValError;
	}

	public void setThirdQuartileAbsValError(double thirdQuartileAbsValError) {
		this.thirdQuartileAbsValError = thirdQuartileAbsValError;
	}

	public double getMaximumAbsValError() {
		return maximumAbsValError;
	}

	public void setMaximumAbsValError(double maximumAbsValError) {
		this.maximumAbsValError = maximumAbsValError;
	}

	
	
	public double getAverageBoundingBoxVolume() {
		return averageBoundingBoxVolume;
	}


	public void setAverageBoundingBoxVolume(double averageBoundingBoxVolume) {
		this.averageBoundingBoxVolume = averageBoundingBoxVolume;
	}


	public double getMinimumBoundingBoxVolume() {
		return minimumBoundingBoxVolume;
	}


	public void setMinimumBoundingBoxVolume(double minimumBoundingBoxVolume) {
		this.minimumBoundingBoxVolume = minimumBoundingBoxVolume;
	}


	public double getFirstQuartileBoundingBoxVolume() {
		return firstQuartileBoundingBoxVolume;
	}


	public void setFirstQuartileBoundingBoxVolume(double firstQuartileBoundingBoxVolume) {
		this.firstQuartileBoundingBoxVolume = firstQuartileBoundingBoxVolume;
	}


	public double getMedianBoundingBoxVolume() {
		return medianBoundingBoxVolume;
	}


	public void setMedianBoundingBoxVolume(double medianBoundingBoxVolume) {
		this.medianBoundingBoxVolume = medianBoundingBoxVolume;
	}


	public double getThirdQuartileBoundingBoxVolume() {
		return thirdQuartileBoundingBoxVolume;
	}


	public void setThirdQuartileBoundingBoxVolume(double thirdQuartileBoundingBoxVolume) {
		this.thirdQuartileBoundingBoxVolume = thirdQuartileBoundingBoxVolume;
	}


	public double getMaximumBoundingBoxVolume() {
		return maximumBoundingBoxVolume;
	}


	public void setMaximumBoundingBoxVolume(double maximumBoundingBoxVolume) {
		this.maximumBoundingBoxVolume = maximumBoundingBoxVolume;
	}


	
	public static double getMean(double[] vals, int numVals) {
		
		return getSum(vals, numVals) / numVals;
		
	}

	public static double getSum(double[] vals, int numVals) {
		
		double sum = 0.0;
		for (int i = 0 ; i < numVals ; ++i) {
			sum += vals[i];
		}
		return sum;
		
	}





	public static double getStdDev(double[] vals, int numVals) {
		
		double mean = getMean(vals, numVals);
		
		double sumSquaredMeanDiffs = 0.0;
		for (int i = 0 ; i < numVals ; ++i) {
			sumSquaredMeanDiffs += Math.pow(vals[i] - mean, 2.0);
		}
		
		// unlike RMSE, std dev uses a denominator of n-1 to correct for bias
		return Math.sqrt(sumSquaredMeanDiffs / (numVals - 1));

	}
	
}
















//private double[] deciles = new double[11];



//if (runData.length >= 10) {
//	for (int i = 1 ; i <= 10 ; ++i) {
//		deciles[i] = runData[(runData.length*i/10)-1].getFunctionValue();
//	}
//}



//public double[] getDeciles() {
//	return deciles;
//}
//
//
//public double getDecile(int i) {
//	return deciles[i];
//}







