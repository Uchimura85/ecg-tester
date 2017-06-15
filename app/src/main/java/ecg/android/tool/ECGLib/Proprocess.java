package ecg.android.tool.ECGLib;

/**
 * Created by john on 2/4/2017.
 */

public class Proprocess {
    public int inFactorCount;
    public short[] inFactor=null;
    public int outFactorCount;
    public short[] outFactor=null;
    public RotationQueue inQueue;
    public RotationQueue outQueue;
    public RotationQueue diffQueue;
    public int diffFactorCount;
    public short[] diffFactor;
    public RotationQueue integralQueue;
    public int integralLen;

    public double avg;
    public double squareAvg;
    public boolean isProcessable;
    public int samplesPerSecond;
    public int waitingSamples;
    public int sampleCounter;
    public int transformValue;
    public int processedValue;
    public Proprocess(int samplesPerSecond)
    {
        waitingSamples = samplesPerSecond*5;// to calculate avg and squareavg correctly wait for 5 seconds
        this.samplesPerSecond = samplesPerSecond;
        inFactorCount = (((5243 * samplesPerSecond) >> 16) >> 2) - (samplesPerSecond >> 15);
        inFactorCount = inFactorCount*2 + 1;
        inFactor = new short[inFactorCount];
        outFactorCount = 3;
        outFactor = new short[outFactorCount];
        outFactor[0] = 1;
        outFactor[1] = -2;
        outFactor[2] = 1;
        for(int i=0;i<inFactorCount;i++)
            inFactor[i] = 0;
        inFactor[0] = 1;
        inFactor[inFactorCount-1] = 1;
        inFactor[inFactorCount/2] = -2;

        inQueue = new RotationQueue();
        inQueue.init(inFactorCount);
        inQueue.fullwith(0);

        outQueue = new RotationQueue();
        outQueue.init(outFactorCount);
        outQueue.fullwith(0);
        isProcessable = false;
        sampleCounter = 0;
        avg = 0;
        squareAvg = 0;
        diffFactorCount = 5;
        diffFactor = new short[diffFactorCount];
        diffFactor[0] = -1;
        diffFactor[1] = -2;
        diffFactor[2] = 0;
        diffFactor[3] = 2;
        diffFactor[4] = 1;
        diffQueue = new RotationQueue();
        diffQueue.init(5);

        integralLen = (int)Math.floor(samplesPerSecond*0.18);
        integralQueue = new RotationQueue();
        integralQueue.init(integralLen);
        processedValue = -1;
     }
    public void restart()
    {
        inQueue.reset();
        outQueue.reset();
        diffQueue.reset();
        integralQueue.reset();
        isProcessable = false;
        avg = 0;
        squareAvg = 0;
        processedValue = -1;
        sampleCounter = 0;
    }
    public int normalize(int val)
    {
        double rst = 0;
        if(isProcessable)
        {
            rst = val - avg;
            rst /= squareAvg;
            rst *= 500;
            rst = Math.floor(rst);
            return (int)rst;
        }
        avg *= sampleCounter;
        squareAvg *= sampleCounter;
        avg += val;
        squareAvg += val*val;
        sampleCounter++;
        avg /= (float)sampleCounter;
        squareAvg /= (float)sampleCounter;
        if(sampleCounter==waitingSamples)
        {
            squareAvg -= avg*avg;
            squareAvg = Math.sqrt(squareAvg);
            isProcessable = true;
            rst = val - avg;
            rst /= squareAvg;
            rst *= 500;
            rst = Math.floor(rst);
        }
        return (int)rst;
    }
    public void Filter(int val)
    {
        transformValue = 0;
        inQueue.push(0);
        if(!inQueue.isFull) return;
        inQueue.setAtlast(val);
        for(int i=0;i<inFactorCount;i++)
        {
            transformValue += inQueue.getFromEnd(-i)*inFactor[i];
        }
        if(outFactorCount>1)
        {
            for(int i=1;i<outFactorCount;i++)
            {
                transformValue -= outQueue.getFromEnd(-i+1)*outFactor[i];
            }
         }
        outQueue.push(transformValue);

    }
    public void Diff()
    {
        diffQueue.push(0);
        if(!diffQueue.isFull)
        {
            transformValue = 0;
            return;
        }
        diffQueue.setAtlast(transformValue);
        transformValue = 0;
        for(int i=0;i<diffFactorCount;i++)
        {
           transformValue += diffQueue.getFromEnd(-i)*diffFactor[diffFactorCount-1-i];
        }
        transformValue *= samplesPerSecond*0.125*0.0001;
    }
    public void Integral()
    {
        processedValue = 0;
        integralQueue.push(transformValue*transformValue);
        if(integralQueue.isFull)
        {
            int sum  = 0;
            for(int i=0;i<integralLen;i++)
                sum += integralQueue.getFromEnd(-i);
            sum /= integralLen;
            processedValue = sum;
        }
    }
    public void process(int val)
    {
        int norm = normalize(val);
        if(isProcessable)
        {
            Filter(norm);
            Diff();
            Integral();
        }
    }
}
