package ecg.android.tool.parser;

import android.content.Context;
import android.os.Environment;
import android.provider.Settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import ecg.android.tool.ecg.MitBitActivity;
import no.nordicsemi.android.nrftoolbox.utility.DebugLogger;

/**
 * Created by john on 1/18/2017.
 */

public class EcgAnalysis {

    private  final static  int ECG_DATA_BUFFERSIEZE = 6000;
    private  double[] m_EcgData = new double[ECG_DATA_BUFFERSIEZE];
    private  double[] m_Derivation = new double[ECG_DATA_BUFFERSIEZE]; // 1 order ecg's derivation;
    public  double[] m_PR = new double[ECG_DATA_BUFFERSIEZE];
    public  double[] m_QRS = new double[ECG_DATA_BUFFERSIEZE];
    public  double[] m_QT = new double[ECG_DATA_BUFFERSIEZE];
    public   double[] m_QRSMark = new double[ECG_DATA_BUFFERSIEZE];

    //Interval variable
    private  double m_PRInterval;
    private  double m_QTInterval;
    private  double m_QRSInterval;

    private  double m_BPM;

    public EcgAnalysis()
    {

    }
    public  double getPRInterval()
    {
        return m_PRInterval;
    }
    public double getQTInterval()
    {
        return m_QTInterval;
    }
    public double getQRSInterval()
    {
        return m_QRSInterval;
    }
    public void setEcgData(double[] ecgData)
    {
        System.arraycopy(m_EcgData,0,ecgData,0,ECG_DATA_BUFFERSIEZE);
        Clear();
    }
    public void readEcgData()
    {
        final File root = new File(Environment.getExternalStorageDirectory(), MitBitActivity.FOLDER_NAME);
        if (!root.exists()) {
            return;
        }

        File f = new File(root,"data.ecg");
        if(!f.exists()) return;
        try
        {
            final FileInputStream fin = new FileInputStream(f);
            int val = fin.read();
             fin.close();
        } catch (final IOException e)
        {
            DebugLogger.e("ECG Data Write Fail", "Error while copying HEX file " + e.toString());
        }
    }
    public void saveEcgData()
    {
        final File root = new File(Environment.getExternalStorageDirectory(), MitBitActivity.FOLDER_NAME);
        if (!root.exists()) {
            root.mkdir();
        }
        //byte[] buf = new byte[ECG_DATA_BUFFERSIEZE*4];
     //   System.arraycopy(buf,0,m_EcgData,0,ECG_DATA_BUFFERSIEZE*4);
        new File(root,"data.ecg").delete();

        File f = new File(root,"data.ecg");

        try
        {
            final FileOutputStream fos = new FileOutputStream(f);
            fos.write(100);
            fos.close();
        } catch (final IOException e) {
            DebugLogger.e("ECG Data Write Fail", "Error while copying HEX file " + e.toString());
        }
    }
    public void Clear()
    {
        for(int i=0;i<ECG_DATA_BUFFERSIEZE;i++)
        {
            m_QRS[i] = 0;
            m_PR[i] = 0;
            m_QT[i] = 0;
            m_QRSMark[i] = 0;
        }
    }
    public void Analyze()
    {
        readEcgData();
        saveEcgData();
        CalculateDerivation1();
        CalculateQRS();
        CalculatePR();
        CalculateQT();
    }
    public  void CalculateQRS()
    {
        int i;
        double dev = StandardDeviation();
        dev *= 1.2;

        double[] MAXMIN = new double[ECG_DATA_BUFFERSIEZE];

        for(i=0;i<ECG_DATA_BUFFERSIEZE;i++)
        {
            MAXMIN[i] = 0;
            if(m_Derivation[i]>dev)
            {
                MAXMIN[i] = 2;
            }

            if(m_Derivation[i]<-dev)
            {
                MAXMIN[i] = -2;
            }
        }

        int BPM = 0;
        int d_count;
        for(i=0;i<ECG_DATA_BUFFERSIEZE-1;i++)
        {
            d_count=0;
            if(MAXMIN[i]==2)
            {
                do{	i++;d_count++;}
                while(MAXMIN[i]==2);//Q-R
                if(d_count>3)
                {
                    int max=i;
                    do{max++;}
                    while(MAXMIN[max]==2);
                    MAXMIN[max] = m_EcgData[max];//fing max after delta+ //I think this point is R

                    do{max++;}
                    while(MAXMIN[max]<0);
                    MAXMIN[max+1] = m_EcgData[max];//find min after R max//I think this point is S

                    //Find end of S segment
                    do
                    {
                        max++;
                    }while(m_Derivation[max]>0);//I think this is T begin Point

                    m_QRS[max] = -200;

                    max = i;
                    do{max--;}
                    while(m_Derivation[max]>0);
                    m_Derivation[max+1] = m_EcgData[max+1];//find min before P d+
                    do
                    {
                        max--;
                    }while(m_Derivation[max]<0);

                    m_QRS[max] = 200;

                    BPM++;
                }
            }
        }
        m_BPM = BPM*10;
        int ms;
        int mean = 0;
        int count =0;
        for(i=0;i<ECG_DATA_BUFFERSIEZE;i++)
        {
            if(m_QRS[i]==200)
            {
                do
                {
                    i++;
                    count++;
                    m_QRSMark[i] = 200;
                }
                while(m_QRSMark[i]>-200);
                m_QRSMark[i] = -200;
                mean++;
            }
        }
        mean = Math.max(1,mean);
        ms = count/mean;
        m_QRSInterval = ms; // i recieved ecg value per 1 millisecond; interval = ms*1;
    }

    public void CalculatePR()
    {
        int i;
        int ms;
        for( i=0;i<ECG_DATA_BUFFERSIEZE;i++)
        {
            int count=0;
            int q = 0;
            if(m_QRS[i]==200)
            {
                do
                {
                    q++;
                }
                while(m_Derivation[i+q]<=0);
                m_PR[i+q] = -100;
                do
                {
                    q++;
                }
                while(m_EcgData[i+q]<=0); // if result is incorrect,i'll process low filter

                do
                {
                    count++;
                }
                while(m_Derivation[i-count]<=0);
                m_PR[i-count] = 100;
            }
        }
        int mean = 0;
        int count =0;
        for(i=0;i<ECG_DATA_BUFFERSIEZE;i++)
        {
            if(m_PR[i]==100)
            {
                do
                {
                    m_PR[i]=100;
                    count++;
                    i++;
                }
                while((m_PR[i]>-100)&(i<ECG_DATA_BUFFERSIEZE-1));
                mean++;

            }
        }

        if(mean==0)mean=1;
        ms = count/mean;
        m_PRInterval = ms;
    }
    public  void CalculateQT()
    {
        int i;
        int ms = 0;
        int count = 0;
        for( i=0;i<ECG_DATA_BUFFERSIEZE;i++)
        {
            if(m_QRS[i]==-200)
            {
                m_QT[i] = m_EcgData[i];
                count=0;
                //Find the next QRS
                //This count will be our interval to search T wave
                do
                {
                    count++;
                }
                //very important check for i+count<lenght!!!!!!!!!!!!!!!!!
                while((m_QRS[i+count]<200)&(i+count<ECG_DATA_BUFFERSIEZE));
                //After finding the next Q fix it!
                m_QT[i+count] = 100;

                double max = m_EcgData[i];
                int max_index = 0;
                for(int j=i;j<i+count;j++)
                {
                    //finds the maximum value
                    //May be a T wave?
                    if(m_EcgData[j]>=max)
                    {
                        max = m_EcgData[j];
                        max_index = j;
                    }

                }
                //Finding the end of QT interval
                m_QT[max_index] = m_EcgData[max_index];
                do
                {
                    max_index++;
                    m_QT[max_index] = 100;
                }
                //	while((arECG[max_index+10]>=0));
                //can use two while!
                while((m_Derivation[max_index]<=0)&(m_EcgData[max_index]>=0));
                m_QT[max_index] = -100;
            }

        }
        //calculate QT interval meav
        int mean = 0;
        count =0;
        for(i=0;i<ECG_DATA_BUFFERSIEZE;i++)
        {
            if(m_QT[i]==100)
            {
                do
                {
                    m_QT[i]=100;
                    count++;
                    i++;
                }
                while((m_QT[i]>-100)&(i<ECG_DATA_BUFFERSIEZE-1));
                mean++;
            }
        }
        if(mean==0)mean=1;
        ms = count/mean;
        m_QTInterval = ms;
    }
    // function purpose:process 1 order derivation with ecgdata
    public void CalculateDerivation1()
    {
        for(int i=0;i<ECG_DATA_BUFFERSIEZE-1;i++)
        {
            m_Derivation[i] = m_EcgData[i+1] - m_EcgData[i];
        }
        m_Derivation[ECG_DATA_BUFFERSIEZE-1] = m_Derivation[ECG_DATA_BUFFERSIEZE-2];
    }
    public void CalculateDerivation2()
    {
        for(int i=0;i<ECG_DATA_BUFFERSIEZE-2;i++)
        {
            m_Derivation[i] = m_EcgData[i+2] - 2*m_EcgData[i+1] + m_EcgData[i];
        }
        m_Derivation[ECG_DATA_BUFFERSIEZE-2] = m_Derivation[ECG_DATA_BUFFERSIEZE-3];
        m_Derivation[ECG_DATA_BUFFERSIEZE-1] = m_Derivation[ECG_DATA_BUFFERSIEZE-2];
    }
    //calculate derivation average value
    public double Mean()
    {
        double mean = 0;
        for(int i=0;i<ECG_DATA_BUFFERSIEZE;i++)
            mean += m_Derivation[i];
        mean /= ECG_DATA_BUFFERSIEZE;
        return mean;
    }
    public double StandardDeviation()
    {
        double dev = 0;
        double mean = Mean();
        for(int i=0;i<ECG_DATA_BUFFERSIEZE;i++)
        {
            dev = dev + (m_Derivation[i]-mean)*(m_Derivation[i]-mean);
        }
        dev = dev/ECG_DATA_BUFFERSIEZE;
        dev = Math.sqrt(dev);
        return dev;
    }

}
