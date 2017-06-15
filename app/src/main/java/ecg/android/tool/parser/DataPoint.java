package ecg.android.tool.parser;

public class DataPoint {
	
	private int volt;

	//Just a simple implementation of a point object
	public DataPoint(int volt)
	{
		this.volt = volt;
	}
	
	public int getVal(){
		return volt;
	}
	
}
