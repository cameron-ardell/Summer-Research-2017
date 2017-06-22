
public class DataOutput {

	// holds the function value and error for a single run
	
	private double functionValue;
	private double error;
	private double absValError;
	private double boundingBoxVolume;

	
	public DataOutput () {
		this.functionValue = 0.0;
		this.error = 0.0;
		this.absValError = 0.0;
		this.boundingBoxVolume = 0.0;
	}

//	public DataOutput (double value, double error, double absValError) {
//		this.functionValue = value;
//		this.error = error;
//		this.absValError = absValError;
//	}
	
	
	public DataOutput (double value, double error, double absValError, double boundingBoxVolume) {
		this.functionValue = value;
		this.error = error;
		this.absValError = absValError;
		this.boundingBoxVolume = boundingBoxVolume;
	}
	
	
	public DataOutput getCopy() {
		return new DataOutput(functionValue, error, absValError, boundingBoxVolume);
	}

	
	public void copyFrom (DataOutput fromDataOutput) {
		this.functionValue = fromDataOutput.getFunctionValue();
		this.error = fromDataOutput.getError();
		this.absValError = fromDataOutput.getAbsValError();
		this.boundingBoxVolume = fromDataOutput.getBoundingBoxVolume();
	}

		
	public void copyDataFromSolution(Solution s) {
		functionValue = s.getFunctionValue();
		error = s.getError();
		absValError = Math.abs(error);
	}


	
	public double getFunctionValue() {
		return functionValue;
	}

	public void setFunctionValue(double functionValue) {
		this.functionValue = functionValue;
	}

	
	public double getError() {
		return error;
	}

	public void setError(double error) {
		this.error = error;
	}

	
	public double getAbsValError() {
		return absValError;
	}

	public void setAbsValError(double absValError) {
		this.absValError = absValError;
	}

	public double getBoundingBoxVolume() {
		return boundingBoxVolume;
	}

	public void setBoundingBoxVolume(double boundingBoxVolume) {
		this.boundingBoxVolume = boundingBoxVolume;
	}

}
