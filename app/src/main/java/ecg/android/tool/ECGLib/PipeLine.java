package ecg.android.tool.ECGLib;

import android.util.Log;

/**
 * Created by john on 2/4/2017.
 */

public class PipeLine {
    public TypeClassification typeClassification;
    public Proprocess proprocess;
    public WaveDetect waveDetect;
    public WaveChars  waveChars;
    public boolean isRWaveDetect = false;
    public boolean bOdd=false;
    public boolean bReset = false;
    public int samplesPerSec;
    public RotationQueue hrQueue;
    public int hrSum =0;
    public int hrCounter = 0;
    public int hrQueueSize = 5;
    public int bpm;
    public int hrShowPeriod;
    public int hrShowCounter;
    public int heartRate = 0;
    public PipeLine()
    {

    }
    public void init(int samplesPerSec,int period)
    {
        hrShowCounter = period-1;
        hrShowPeriod = period;
        bpm = 0;
        hrQueue = new RotationQueue();
        hrQueue.init(hrQueueSize);

        this.samplesPerSec = samplesPerSec;
        proprocess = new Proprocess(samplesPerSec);
        waveChars = new WaveChars();
        waveDetect = new WaveDetect(samplesPerSec,waveChars);
        typeClassification = new TypeClassification(waveChars);

    }
    public void add(int val)
    {
        Log.d("pipeline", ""+val);

        proprocess.process(val);
        if(proprocess.isProcessable)
        {
            waveDetect.process(proprocess.processedValue);
            if(waveDetect.doRestart())
            {
                bReset = true;
                waveDetect.restart();
                proprocess.restart();
            }
            isRWaveDetect = waveDetect.isRWaveDetected;
            if(isRWaveDetect)
            {
                CalcHeartRate();
                if(bpm==0)
                {
                    waveDetect.isRWaveDetected = false;
                    isRWaveDetect = false;
                    return;
                }
                bpm = 60*samplesPerSec/bpm;
                if(hrShowCounter == hrShowPeriod)
                {
                    heartRate = bpm;
                    hrShowCounter = 0;
                }
                else
                {
                    hrShowCounter++;
                }
                typeClassification.classification();
            }
        }
    }
    public void CalcHeartRate()
    {
        Log.d("rrinterval",""+waveDetect.waveChars.RRInterval);
        int hr = waveDetect.waveChars.RRInterval*samplesPerSec/1000;
        if(hr ==-1 || hr>2*samplesPerSec || hr<60*samplesPerSec/200.f ) return;
        hrQueue.push(hr);

        if(hrCounter<hrQueueSize)
        {
            hrSum += hr;
            hrCounter++;
            bpm = hrSum/hrCounter;
            return;
        }
        int prevhr = hrQueue.getFromEnd(-hrQueueSize);
        hr -= prevhr;
        hrSum += hr;
        bpm = hrSum/hrQueueSize;
    }
    public int getHeartRate()
    {
        return heartRate;
    }
    public void restart()
    {
        heartRate = 0;
        hrShowCounter = hrShowPeriod-1;
        bpm = 0;
        hrSum = 0;
        hrCounter = 0;
        proprocess.restart();
        waveDetect.restart();
        isRWaveDetect = false;
    }
    public boolean isDected()
    {
        return isRWaveDetect;
    }
}
