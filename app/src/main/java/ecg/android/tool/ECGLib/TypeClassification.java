package ecg.android.tool.ECGLib;

/**
 * Created by john on 2/8/2017.
 */

public class TypeClassification {
    public RotationQueue apmQueue;
    public RotationQueue RRIntervalQueue;
    public RotationQueue qrsQueue;

    public RotationQueue bpmQueue;

    public double avgAmp;
    public double avgQRSBand;
    public double avgRRInterval;
    public WaveChars waveChars;
    public WaveChars processwaveChars;
    public int QRSCounter;
    public WaveChars[] waveCharsesArray;

    public int strideBPM = 3;
    public TypeClassification(WaveChars waveChars)
    {
        this.waveChars = waveChars;
        processwaveChars = new WaveChars();
        bpmQueue = new RotationQueue();
        bpmQueue.init(8);
        bpmQueue.fullwith(1000);

        apmQueue = new RotationQueue();
        apmQueue.init(8);
        apmQueue.fullwith(4000);

        RRIntervalQueue = new RotationQueue();
        RRIntervalQueue.init(8);
        RRIntervalQueue.fullwith(1000);

        qrsQueue = new RotationQueue();
        qrsQueue.init(8);
        qrsQueue.fullwith(80);

        avgAmp = 4000;
        avgQRSBand = 80;
        avgRRInterval = 1000;
        QRSCounter = 0;
        waveCharsesArray = new WaveChars[2];
        waveCharsesArray[0] = new WaveChars();
        waveCharsesArray[1] = new WaveChars();
    }
    public void restart()
    {
        avgAmp = 4000;
        avgQRSBand = 80;
        avgRRInterval = 1000;
        QRSCounter = 0;
    }
    public void classification()
    {
        QRSCounter++;
        processwaveChars.set(waveCharsesArray[0]);
        if(QRSCounter>2)
        {
            Diff();
            if(QRSCounter>7) DecidePrematureBeat();
            refresh();
        }
    }
    public void refresh()
    {
        bpmQueue.push(processwaveChars.RRInterval);

        double interval = processwaveChars.RRInterval;
        double prevInterval = bpmQueue.getFromEnd(-strideBPM);
        if(processwaveChars.RRInterval!=0)
        {
            double bpm = processwaveChars.bpm;
            bpm= bpm+ (60000.0/interval- 60000.0/prevInterval)/(double)strideBPM;
            processwaveChars.bpm = (int)bpm;
        }


        if(QRSCounter<=8)
        {
            if ( (processwaveChars.RR2Value- 10001) <= 39998 )
            {
                apmQueue.push(processwaveChars.RR2Value);
                avgAmp += (processwaveChars.RR2Value - apmQueue.getFromEnd(-5))/5.f;
            }
            if ( (processwaveChars.QRSBandWidth - 31) <= 68 )
            {
                qrsQueue.push(processwaveChars.QRSBandWidth);
                avgQRSBand +=(processwaveChars.QRSBandWidth - qrsQueue.getFromEnd(-5))/5.f;
            }
            if ( (processwaveChars.RRInterval - 151) <= 1648 )
            {
                RRIntervalQueue.push(processwaveChars.RRInterval);
                avgRRInterval +=(processwaveChars.RRInterval - RRIntervalQueue.getFromEnd(-5))/5.f;
            }
        }
        else
        {
            if(processwaveChars.type != 86)
            {
                apmQueue.push(processwaveChars.RR2Value);
                avgAmp += (processwaveChars.RR2Value - apmQueue.getFromEnd(-5))/5.f;
                qrsQueue.push(processwaveChars.QRSBandWidth);
                avgQRSBand +=(processwaveChars.QRSBandWidth - qrsQueue.getFromEnd(-5))/5.f;
            }
            if(processwaveChars.type != 83)
            {
                RRIntervalQueue.push(processwaveChars.RRInterval);
                avgRRInterval +=(processwaveChars.RRInterval - RRIntervalQueue.getFromEnd(-5))/5.f;
            }
        }



        waveCharsesArray[1].set(waveCharsesArray[0]);
        waveCharsesArray[0].set(waveChars);
    }
    public void DecidePrematureBeat()
    {
        if ( processwaveChars.transformValue > 199 && processwaveChars.transformedQRSBand > 4 )
        {
            processwaveChars.type = 86;

        }
        else
        {
            if ( 0.8 * avgRRInterval <= processwaveChars.RRInterval && (waveChars.RRInterval <= 1.3 * processwaveChars.RRInterval || avgRRInterval < processwaveChars.RRInterval))
            {
                if ( processwaveChars.RRInterval < avgRRInterval * 1.5 )
                {
                    processwaveChars.type = 78;
                }
                else
                {
                    processwaveChars.type = 83;
                }
            }
            else
            {
                if(waveCharsesArray[1].type !=86)
                {
                    processwaveChars.type = 65;
                }
            }
        }
    }
    public void Diff()
    {
       double diff = processwaveChars.RR1Value- avgAmp;
       if(diff<0)
           diff = -processwaveChars.RR1Value-avgAmp;
        diff = Math.floor(diff*100/avgAmp);

        processwaveChars.transformValue = (int)diff;

        diff = processwaveChars.QRSBandWidth-avgQRSBand;
        if(diff<0)
            diff = -processwaveChars.QRSBandWidth - avgQRSBand;
        diff = Math.floor(diff*100/avgQRSBand);
        processwaveChars.transformedQRSBand = (int)diff;

    }
}
