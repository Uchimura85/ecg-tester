package ecg.android.tool.ECGLib;

/**
 * Created by john on 2/4/2017.
 */

public class RotationQueue {
    public int originSize;
    public int   size;
    public int   counter;
    public int[] values;
    public int   index;
    public int   indexSize;
    public boolean   isIndexFull;
    public boolean isFull;
    public RotationQueue()
    {
        index = 0;
        isIndexFull = false;
        indexSize = 100000000;
        values = null;
        isFull = false;
    }
    public void init(int len)
    {
        originSize = len;
        for(size=1;size<len;size *=2);

        values = new int[size];
        counter = -1;
        index = 0;
        isIndexFull = false;
        indexSize = 100000000;
        isFull = false;
    }
    public void fullwith(int val)
    {
        for(int i=0;i<size;i++)
            push(val);

    }
    public void reset()
    {
        counter = -1;
        isFull = false;
    }
    public int totalSum()
    {
        int sum = 0;
        for(int i =0;i<size;i++)
          sum += values[i];
        return sum;
    }
    public void push(int val)
    {
        if(values==null) return;
        counter++;
        if(counter>=originSize) isFull = true;
        counter %= size;
        values[counter] = val;
        index++;
        if(index>indexSize)
        {
            index = 0;
            isIndexFull = true;
        }
    }
    public void setAtlast(int value)
    {
        if(counter<0 || counter>=size) return;
        values[counter]= value;
    }
    public int get(int index)
    {
        if(values==null) return -1;
        return values[index];
    }
    public int getFromEnd(int index)
    {
        int ofs = counter+index;
        if(ofs<0)
        {
            ofs++;
            ofs %= size;
            ofs += size-1;
        }
        else ofs %= size;
        return values[ofs];
    }
 }
