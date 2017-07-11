/*
to simplify return value of given node on tree to avoid issues
with holding both booleans and doubles
 */

public class ReturnValue {
	
	private float valueFloat = 0;
	private boolean valueBoolean = false;
	private String valueString = null;

	public ReturnValue(){
		valueFloat = 0;
		valueBoolean = false;
		this.valueString = null;
	}

	public ReturnValue(float valueFloat){
		this.valueFloat = valueFloat;
		valueBoolean = false;
		this.valueString = null;
	}

	public ReturnValue(boolean valueBoolean){
		valueFloat = 0;
		this.valueBoolean = valueBoolean;
		this.valueString = null;
	}

	public ReturnValue(String valueString){
		valueFloat = 0;
		valueBoolean = false;
		this.valueString = valueString;
	}


	public void setReturnVal(float valueFloat) {
		this.valueFloat = valueFloat;		
	}

	public void setReturnVal(boolean valueBoolean) {
		this.valueBoolean = valueBoolean;		
	}

	public void setReturnVal(String valueString) {
		this.valueString = valueString;		
	}


	public float getReturnVal(float floatFlag) {
		return valueFloat;
	}

	public boolean getReturnVal(boolean booleanFlag) {
		return valueBoolean;
	}

	public String getReturnVal(String stringFlag) {
		return valueString;
	}

}