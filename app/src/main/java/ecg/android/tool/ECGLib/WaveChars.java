package ecg.android.tool.ECGLib;

/**
 * Created by john on 2/5/2017.
 */

public class WaveChars {

    public int type;
    public int bpm;
    public int RRInterval;
    public int QRSBandWidth;

    public int RR1Pos;
    public int RR1Value;

    public int RR2Pos;
    public int RR2Value;

    public int transformValue;
    public int transformedQRSBand;

    public WaveChars()
    {
        type = 46;
        RR1Pos = -1;
        RR1Value = -1;
        RR2Pos = -1;
        RR2Value = -1;
        RRInterval = -1;
        QRSBandWidth = -1;
        transformValue = -1;
        transformedQRSBand = -1;
        bpm = 0;
    }
    public void reset()
    {
        RR1Pos = -1;
        RR1Value = -1;
        RR2Pos = -1;
        RR2Value = -1;
        RRInterval = -1;
        QRSBandWidth = -1;
        bpm = 0;
    }
    public void set(WaveChars in)
    {
        type = in.type;
        bpm = in.bpm;
        RRInterval = in.RRInterval;
        QRSBandWidth = in.QRSBandWidth;

        RR1Pos = in.RR1Pos;
        RR1Value = in.RR1Value;

        RR2Pos = in.RR2Pos;
        RR2Value = in.RR2Value;

        transformValue = in.transformValue;
        transformedQRSBand = in.transformedQRSBand;
    }


}
