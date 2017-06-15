package ecg.android.tool.ecg;

import android.os.Environment;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

import no.nordicsemi.android.nrftoolbox.utility.DebugLogger;

/**
 * Created by john on 1/23/2017.
 */

public class ECGFile {

    public long length = 0;
    public RandomAccessFile IOFile=null;
    public final int BUF_SIZE = 100;
    public byte[] buffer = new byte[BUF_SIZE*4];
    public int   bufferCounter = 0;
    public  ECGFile()
    {

    }
    public void Open(String filename,Boolean readMode)
    {
        final File root = new File(Environment.getExternalStorageDirectory(), MitBitActivity.FOLDER_NAME);
        if (!root.exists()) {
            root.mkdir();
        }
        File f;
        if(readMode)
        {
            f = new File(root,filename);
            if(!f.exists()) return;

            try {
                IOFile = new RandomAccessFile(f,"rw");
                length = IOFile.length();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        new File(root,filename).delete();

        f = new File(root,filename);

        try
        {
            IOFile = new RandomAccessFile(f,"rw");

        } catch (final IOException e) {
            DebugLogger.e("ECG Data Write Fail", "Error while copying HEX file " + e.toString());
        }
    }
    public void Skip(long fops)
    {
        if(IOFile==null) return;
        try {
            IOFile.seek(fops);
        } catch (IOException e) {

        }
    }
    public int Read()
    {
        if(IOFile==null) return -1;
        byte[] buf = new byte[4];
        try {
            IOFile.read(buf);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int res = ((buf[0] & 0xff)<<24) + ((buf[1] & 0xff)<<16) + ((buf[2] & 0xff)<<8) + (buf[3]&0xff);
        return res;
     }
    public long GetLength()
    {
        if(length==0 || IOFile==null) return 0;
        int len = 0;
        Skip(length-4);
        len = Read();
        Skip(0);

        return len;
    }
    public void Write(int val)
    {
        if(IOFile==null) return;
        buffer[bufferCounter*4+0] = (byte)((val >>24)&0xff);
        buffer[bufferCounter*4+1] = (byte)((val >>16)&0xff);
        buffer[bufferCounter*4+2] = (byte)((val >>8)&0xff);
        buffer[bufferCounter*4+3] = (byte)(val & 0xff);

        bufferCounter++;
        if(bufferCounter!=BUF_SIZE) return;
        bufferCounter = 0;
        try {
            IOFile.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public int getPosition()
    {
        if(IOFile==null) return -1;
        int pos = 0;
        try {
            pos = (int)IOFile.getFilePointer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pos;
    }
    public void Close()
    {
        if(IOFile==null) return;

        try {
            IOFile.write(buffer,0,bufferCounter*4);
            IOFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
