package ecg.android.tool.parser;

import java.util.Stack;

public class Frame {

	public Stack<DataPoint> DataPoints;
	
	public Frame()
	{
		DataPoints = new Stack<DataPoint>();
	}
	
	public void AddPoint(DataPoint p)
	{
		DataPoints.add(p);
	}
	public int getValue()
	{
		int val = -1;
		if(!DataPoints.empty())
		{
			val = DataPoints.get(0).getVal();
			DataPoints.removeElementAt(0);
		}
		return val;
	}
}
