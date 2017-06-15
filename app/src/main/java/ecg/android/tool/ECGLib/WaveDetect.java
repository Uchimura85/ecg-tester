package ecg.android.tool.ECGLib;

/**
 * Created by john on 2/4/2017.
 */

public class WaveDetect {
    public int samplesPerSec;
    public int amplitudeQueueSize;
    public RotationQueue amplitudeQueue;
    public int amplitudeQueueMiddle;

    public int pwavePos1;
    public int pwavePos2;

    public int amplitudeThres1;
    public int amplitudeThres2;

    public boolean isRWaveDetected;
    public int frontLen;

    public int posCounter1forRRI;
    public int posCounter2forRRI;

    public int prePosCounter1forRRI;
    public int prePosCounter2forRRI;

    public boolean doDectectionStart =false;

    public int RR1Pos = 0;
    public int prevRR1Pos = 0;
    public int RR1Value;
    public int prevRR1Value;

    public int RR2Pos = 0;
    public int prevRR2Pos = 0;
    public int RR2Value;
    public int prevRR2Value;
    public int DectectInterval;
    public WaveChars waveChars;

    public WaveDetect(int samplesPerSec,WaveChars waveChars)
    {
        this.waveChars = waveChars;
        this.samplesPerSec = samplesPerSec;
        amplitudeQueueSize = (int)(2*Math.floor(samplesPerSec*0.12*0.5))+1;
        amplitudeQueueMiddle = amplitudeQueueSize/2;
        amplitudeQueue = new RotationQueue();
        amplitudeQueue.init(amplitudeQueueSize);

        pwavePos1 = ((amplitudeQueueSize*10923)>>16) - (amplitudeQueueSize>>15);
        pwavePos2 = pwavePos1;

        doDectectionStart = true;
        amplitudeThres1 = 500;
        amplitudeThres2 = 1000;
        isRWaveDetected = false;
        frontLen = (int)(samplesPerSec*0.25);

        posCounter1forRRI = 0;
        posCounter2forRRI = 0;
        RR1Value = -1;
        prevRR1Value = -1;
        RR2Value = -1;
        prevRR2Value = -1;

        RR1Pos = -1;
        RR2Pos = -1;
        prevRR1Pos = -1;
        prevRR2Pos = -1;

        DectectInterval = 0;
    }
    public boolean doRestart()
    {
        if(!isRWaveDetected && DectectInterval>samplesPerSec*10) return true;
        return false;
    }
    public void DecideQRS()
    {
        posCounter1forRRI++;
        posCounter2forRRI++;
        if(!amplitudeQueue.isFull) return;
        int middleVal = amplitudeQueue.getFromEnd(-amplitudeQueueMiddle);
        float diffbeforFirst,diffafterFirst,diffbeforSecond,diffafterSecond;
        float tan;
        if(doDectectionStart)
        {
            tan = (amplitudeQueue.getFromEnd(-amplitudeQueueMiddle+pwavePos1) - middleVal)/pwavePos1;
            diffbeforFirst = middleVal - amplitudeQueue.getFromEnd(-amplitudeQueueMiddle-2*pwavePos1);
            diffafterFirst = amplitudeQueue.getFromEnd(-amplitudeQueueMiddle+2*pwavePos1) - middleVal;

            diffbeforSecond = middleVal - amplitudeQueue.getFromEnd(-amplitudeQueueMiddle-3*pwavePos1);
            diffafterSecond = amplitudeQueue.getFromEnd(-amplitudeQueueMiddle+3*pwavePos1) - middleVal;

            if(posCounter1forRRI>frontLen && middleVal<amplitudeThres1 && (middleVal-amplitudeQueue.getFromEnd(-amplitudeQueueMiddle-1))<=40
               && (amplitudeQueue.getFromEnd(-amplitudeQueueMiddle+2)-middleVal)>2 && amplitudeThres1*0.1>=(middleVal-amplitudeQueue.getFromEnd(-amplitudeQueueMiddle-pwavePos1))
               && tan>4 && 0.15*amplitudeThres1>=diffbeforFirst && diffafterFirst>= 0.2*amplitudeThres1
               && 0.25*amplitudeThres1>=diffbeforSecond && diffafterSecond>= 0.34*amplitudeThres1     )//When p wave come or Q
            {
                prePosCounter1forRRI = posCounter1forRRI;
                posCounter1forRRI = 0;
                doDectectionStart = false;
                prevRR1Pos = RR1Pos;
                RR1Pos = amplitudeQueue.index - amplitudeQueueMiddle;
                prevRR1Value = RR1Value;
                RR1Value = amplitudeQueue.getFromEnd(-amplitudeQueueMiddle);
                return;
            }
        }
        tan = (middleVal - amplitudeQueue.getFromEnd(-amplitudeQueueMiddle-pwavePos2))/pwavePos2;
        diffafterFirst = amplitudeQueue.getFromEnd(-amplitudeQueueMiddle+pwavePos2) - middleVal;
        diffafterSecond = amplitudeQueue.getFromEnd(-amplitudeQueueMiddle+2*pwavePos2) - middleVal;
        diffbeforSecond = middleVal - amplitudeQueue.getFromEnd(-amplitudeQueueMiddle-2*pwavePos2);
        float diffbeforThird,diffafterThird;
        diffbeforThird = middleVal - amplitudeQueue.getFromEnd(-amplitudeQueueMiddle-3*pwavePos2);
        diffafterThird = amplitudeQueue.getFromEnd(-amplitudeQueueMiddle+3*pwavePos2) - middleVal;

        if(posCounter2forRRI>frontLen && middleVal>=amplitudeThres1 && (amplitudeQueue.getFromEnd(-amplitudeQueueMiddle+1)-middleVal)<=30 //When T wave come or S
                && (middleVal-amplitudeQueue.getFromEnd(-amplitudeQueueMiddle-2))>4 && diffafterFirst<=amplitudeThres1*0.15 && tan>3
                && 0.2*amplitudeThres1>=diffafterSecond && 0.3*amplitudeThres1<=diffbeforSecond && diffafterThird<=0.3*amplitudeThres1 && diffbeforThird>=0.4*amplitudeThres1)
        {
            prePosCounter2forRRI = posCounter2forRRI;
            posCounter2forRRI = 0;

            prevRR2Pos = RR2Pos;
            RR2Pos = amplitudeQueue.index - amplitudeQueueMiddle;
            prevRR2Value = RR2Value;
            RR2Value = amplitudeQueue.getFromEnd(-amplitudeQueueMiddle);
            if(doDectectionStart)
            {
                if((RR2Pos-prevRR2Pos)>=0.5*samplesPerSec)
                {
                    isRWaveDetected = true;
                    posCounter1forRRI = 0;
                    amplitudeThres2 = (int)Math.floor(amplitudeThres2+(middleVal-amplitudeThres2)*0.36);
                    amplitudeThres1 = (int)Math.floor(amplitudeThres2*0.3);
                    prevRR1Value = RR1Value;
                    RR1Value = -1;
                    prevRR1Pos = RR1Pos;
                    RR1Pos = (int)Math.floor(RR2Pos - samplesPerSec*0.09);
                    DectectInterval =0;
                    waveChars.RR1Pos = RR1Pos;
                    waveChars.RR1Value = -1;
                    waveChars.RR2Pos = RR2Pos;
                    waveChars.RR2Value =RR2Value;
                }
                else
                {
                    DectectInterval++;
                    isRWaveDetected = false;
                    RR2Pos = prevRR2Pos;
                    RR2Value = prevRR2Value;
                    posCounter2forRRI = prePosCounter2forRRI;
                }
                return;
            }
            amplitudeThres2 = (int)Math.floor(amplitudeThres2+(middleVal-amplitudeThres2)*0.36);
            amplitudeThres1 = (int)Math.floor(amplitudeThres2*0.3);
            if( (RR2Pos-prevRR2Pos)>=0.5*samplesPerSec || amplitudeThres2*0.48<=middleVal)
            {
                isRWaveDetected = true;
                doDectectionStart = true;
                DectectInterval =0;
                waveChars.RR1Pos = RR1Pos;
                waveChars.RR1Value = RR1Value;
                waveChars.RR2Pos = RR2Pos;
                waveChars.RR2Value = RR2Value;
                return;
            }
            RR2Value = prevRR2Value;
            RR2Pos = prevRR2Pos;
            posCounter2forRRI = prePosCounter2forRRI;
            RR1Pos = prevRR1Pos;
        }
        else
        {
            if(doDectectionStart || posCounter1forRRI<0.25*samplesPerSec)
            {
                DectectInterval++;
                isRWaveDetected = false;
                return;
            }
            if( (RR1Pos-prevRR1Pos)>=0.5*samplesPerSec)
            {
                if((RR1Pos-RR2Pos)>=0.5*samplesPerSec)
                {
                    prevRR2Value = RR2Value;
                    prevRR2Pos = RR2Pos;
                    isRWaveDetected = true;
                    DectectInterval =0;
                    posCounter2forRRI = 0;
                    doDectectionStart = true;
                    RR2Value = -1;
                    RR2Pos=(int)Math.floor(samplesPerSec*0.09+RR1Pos);
                    waveChars.RR1Pos = RR1Pos;
                    waveChars.RR1Value = RR1Value;
                    waveChars.RR2Pos = RR2Pos;
                    waveChars.RR2Value = -1;
                    return;
                }
                RR1Pos = prevRR1Pos;
            }
        }
        DectectInterval++;
        RR1Value = prevRR1Value;
        doDectectionStart = true;
        isRWaveDetected = false;
        posCounter1forRRI = prePosCounter1forRRI;

    }
    public void process(int val)
    {
        amplitudeQueue.push(val);
        DecideQRS();
        if(isRWaveDetected) CalDurandInt();
    }
    public void restart()
    {
        doDectectionStart = true;
        amplitudeThres1 = 500;
        amplitudeThres2 = 1000;
        isRWaveDetected = false;
        posCounter1forRRI = 0;
        posCounter2forRRI = 0;
        RR1Value = 0;
        prevRR1Value = 0;
        RR2Value = 0;
        prevRR2Value = 0;
        DectectInterval = 0;
        amplitudeQueue.reset();
        waveChars.reset();
    }
    public void CalDurandInt()
    {
        if(RR1Pos==-1 && prevRR1Pos==-1) return;
        int stride = RR1Pos-prevRR1Pos;
        if(stride<=0 ) return;
        waveChars.bpm = 60*samplesPerSec/stride;
        waveChars.RRInterval = 1000*stride/samplesPerSec;// unit is millisecond
        stride = RR2Pos-RR1Pos;
        waveChars.QRSBandWidth = 1000*stride/samplesPerSec;
    }
}
