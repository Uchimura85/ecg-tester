package ecg.android.tool.ecg;

import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import no.nordicsemi.android.nrftoolbox.utility.DebugLogger;

/**
 * Created by john on 1/25/2017.
 */

public class Signal {
    public String filename=null;
    public int bitsPerSample;
    public String id=null;
    public int firstVal;
    public RandomAccessFile IOFile=null;
    public int stride;
    public int indexInFile=0;
    public long fileLen=0;
    public void Open()
    {
        final File root = new File(Environment.getExternalStorageDirectory(), MitBitActivity.FOLDER_NAME);
        if (!root.exists()) {
            root.mkdir();
        }
        File f;

        f = new File(root,filename);
        if(!f.exists()) return;

        switch (bitsPerSample)
        {
            case 12:
                stride = 3;
                break;
            default:
                break;
        }
        try {
              IOFile = new RandomAccessFile(f,"rw");
            byte[] buf = new byte[stride];
            IOFile.read(buf);
            int[] rst = parse(buf);

            if(bitsPerSample==12)
            {
                if(rst[0]==firstVal) indexInFile = 0;
                if(rst[1]==firstVal) indexInFile = 1;
            }
            fileLen = IOFile.length();

        } catch (IOException e) {
             e.printStackTrace();
        }


    }
    public int[] parse(byte[] bytes)
    {
        if(bitsPerSample==12)
        {
            int first = (bytes[0]&0xff)+(bytes[1]&0xf)*256;
            if(first>2048) first = first-4096;
            int second = ((bytes[1]>>8)&0xf)*256+(bytes[2]&0xff);
            if(second>2048) second = second-4096;
            int[] rst = new int[2];
            rst[0] = first;
            rst[1] = second;
            return rst;
        }
        return  null;
    }
    public void Close()
    {
        if(IOFile==null) return;
        try {
            IOFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void Skip(long pos)
    {
        if(IOFile==null) return;
        try {
            IOFile.seek(pos*stride);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public int Read(int[] buffer,int len)
    {
        if(IOFile==null) return 0;
        byte[] bytes = new byte[stride];
        int readBytes=0;
        try {
            long curFileOffset = IOFile.getFilePointer();
            readBytes = (int)(fileLen - curFileOffset);
            readBytes = Math.max(0,readBytes);

        } catch (IOException e) {
            e.printStackTrace();
        }
        readBytes /= 3;
        readBytes = Math.min(readBytes,len);
        for(int i=0;i<readBytes;i++)
        {
            try {
                IOFile.read(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
            int[] rst = parse(bytes);
            buffer[i] = rst[indexInFile]*4+(int)(Math.pow(2,bitsPerSample-1));

        }
        return readBytes;
    }
}
