package ecg.android.tool.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.os.Environment;

import ecg.android.tool.ecg.MitBitActivity;

public class CSVParser {
	
	File file;
	InputStreamReader reader=null;
	BufferedReader in=null;

	public CSVParser(String filename){

		final File root = new File(Environment.getExternalStorageDirectory(), MitBitActivity.FOLDER_NAME);
		if (!root.exists()) return;
		File f = new File(root,filename);

		if(!f.exists()) return;

	       try {
			   FileInputStream fin = new FileInputStream(f);
	    	   reader = new InputStreamReader(fin);
	           in = new BufferedReader(reader);
	           in.readLine();
	       } catch (IOException e) {
	           e.printStackTrace();
	       }
	}
	public Frame getFrame()
	{
		if(in==null) return null;
		Frame frame = new Frame();
		DataPoint p;
		int start = 0;
		int end = 0;
		String string;
		try {

           while ((string = in.readLine()) != null) {
             p = getVolt(string);
			 if(frame.DataPoints.size()!=0)
			 {
				 DataPoint pre = frame.DataPoints.get(frame.DataPoints.size()-1);
				 int len = p.getVal()-pre.getVal();
				 int delta = len/4;
				 for(int i=1;i<4;i++)
				 {
					 int val = pre.getVal()+i*delta;
					 frame.DataPoints.push(new DataPoint(val));
				 }
			 }
             frame.DataPoints.push(p);
           }

         } catch (IOException e) {
           e.printStackTrace();
         }
		
		return frame;
	}
	
	public DataPoint getVolt(String line)
	{

		String parts[] = line.split(",");
		float y = Float.parseFloat(parts[1]);
		
		y = (y+1)*2000;
		DataPoint p = new DataPoint((int)y);
		return p;
	}
}
