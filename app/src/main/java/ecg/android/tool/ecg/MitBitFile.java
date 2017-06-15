package ecg.android.tool.ecg;

import android.net.Uri;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Stack;

/**
 * Created by john on 1/25/2017.
 */
public class MitBitFile {

    public String filename=null;
    public long totalSamples;
    public int  samplesPerSecond;
    public int  signalNums;
    public String datfilename;
    public Stack<Signal> signals = new Stack<Signal>();
    public Signal selSignal;
    public long   sampleCounter = 0;
    public MitBitFile()
    {

    }
    public void Skip(long pos)
    {
        sampleCounter = pos;
        selSignal.Skip(pos);
    }
    public int Read(int[] buf,int len)
    {
        int readbytes = selSignal.Read(buf,len);
        sampleCounter += readbytes;
        return readbytes;
    }

    public void Open(String filename)
    {
        final File root = new File(Environment.getExternalStorageDirectory(), MitBitActivity.FOLDER_NAME);
        if (!root.exists()) return;
        File f = new File(root,filename);

        if(!f.exists()) return;

        this.filename = filename;
        InputStreamReader reader=null;
        BufferedReader in=null;
        String line;

        try {
            FileInputStream fin = new FileInputStream(f);
            reader = new InputStreamReader(fin);
            in = new BufferedReader(reader);
            line = in.readLine();
            String parts[] = line.split(" ");
            signalNums = Integer.parseInt(parts[1]);
            samplesPerSecond = Integer.parseInt(parts[2]);
            totalSamples = Integer.parseInt(parts[3]);
            for(int i=0;i<signalNums;i++)
            {
                line = in.readLine();
                String part[] = line.split(" ");
                Signal signal=new Signal();
                signal.filename = part[0];
                signal.bitsPerSample = Integer.parseInt(part[3]);
                signal.firstVal = Integer.parseInt(part[5]);
                signals.push(signal);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        selSignal = signals.get(0);
        selSignal.Open();
    }

    public void OpenFromUrl(Uri uriFilename)
    {
        final File root = new File(Environment.getExternalStorageDirectory(), MitBitActivity.FOLDER_NAME);
        if (!root.exists()) return;
        File f = new File(uriFilename.getPath());

        if(!f.exists()) return;

        this.filename = uriFilename.getPath();
        InputStreamReader reader=null;
        BufferedReader in=null;
        String line;

        try {
            FileInputStream fin = new FileInputStream(f);
            reader = new InputStreamReader(fin);
            in = new BufferedReader(reader);
            line = in.readLine();
            String parts[] = line.split(" ");
            signalNums = Integer.parseInt(parts[1]);
            samplesPerSecond = Integer.parseInt(parts[2]);
            totalSamples = Integer.parseInt(parts[3]);
            for(int i=0;i<signalNums;i++)
            {
                line = in.readLine();
                String part[] = line.split(" ");
                Signal signal=new Signal();
                signal.filename = part[0];
                signal.bitsPerSample = Integer.parseInt(part[3]);
                signal.firstVal = Integer.parseInt(part[5]);
                signals.push(signal);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        selSignal = signals.get(0);
        selSignal.Open();
    }


    public void Close()
    {
        selSignal.Close();
    }
}
