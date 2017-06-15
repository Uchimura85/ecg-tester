package ecg.android.tool.ecg;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.heartbook.smct.EcgAlgo;

import java.io.File;
import java.util.List;

import ecg.android.tool.ECGLib.PipeLine;
import no.nordicsemi.android.nrftoolbox.R;

//import com.opencsv.CSVWriter;

public class MitBitActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, OnChartValueSelectedListener, View.OnTouchListener {
    private final static int REQUEST_PERMISSION_REQ_CODE = 34; // any 8-bit number
    public Spinner FileSpinner;
    public MitBitFile ecgFile = null;
    public int curPosition = 0;
    public int sampleCounter = 600;
    public int inCounter = 300;
    public static String FOLDER_NAME = "ecg";
    //Ecg Graph variable
    private LineChart mChart;
    private float MAX = 3000;
    private float MIN = 0;
    private final static int X_LENGTH = 250 * 3;

    //Thread variable
    private Thread feedThread;
    private int REFRESH_INTERVAL = 10;
    private boolean IsGraphProgress = false;
    private int FORE_LEN = 100;

    public EcgAlgo ecgAlgo;
    private boolean mOdd = false;

    private int bpm = 0;
    private int AFnums = 0;
    private int peakStart = 0;
    private TextView mTvHeartRate;
    private TextView mTvAFNumber;
    private Button playBtn;
    private boolean bStop = false;
    public PipeLine pipleline;

    //csv for exporting
//    public CSVWriter writer=null;
//    public FileWriter csvFile=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mit_bit);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        _checkPermission();
        init();
    }

    private boolean _checkPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // When user pressed Deny and still wants to use this functionality, show the rationale
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_REQ_CODE);
            }

        }
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // When user pressed Deny and still wants to use this functionality, show the rationale
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_REQ_CODE);
            }
        }
        return false;
    }

    protected void onDestroy() {
        IsGraphProgress = false;
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onDestroy();
//        csvClose();
    }
//    public void createCSV(String filename)
//    {
//        final File root = new File(Environment.getExternalStorageDirectory(),FOLDER_NAME);
//        if (!root.exists()) {
//            root.mkdir();
//        }
//        String filePath = Environment.getExternalStorageDirectory() + File.separator + FOLDER_NAME  + File.separator +filename;
//        File f = new File(filePath );
//        try
//        {
//            if(f.exists() && !f.isDirectory()){
//                csvFile = new FileWriter(filePath , false);
//                writer = new CSVWriter(csvFile);
//            }
//            else {
//                writer = new CSVWriter(new FileWriter(filePath));
//            }
//        }catch (final IOException e) {
//            DebugLogger.e("csv writing fail",e.toString());
//        }
//    }
//    public void csvClose()
//    {
//        try
//        {
//            if(writer!=null) writer.close();
//            if(csvFile!=null) csvFile.close();
//        }catch (final IOException e) {
//            DebugLogger.e("csv writing fail",e.toString());
//        }
//        writer = null;
//        csvFile = null;
//
//    }

    //    public void csvWrite(String str1,String str2)
//    {
//        writer.writeNext(new String[]{str1,str2});
//    }
    public void setPosition(int pos) {
        curPosition = pos;

        LineData ecgdata = mChart.getData();

        if (ecgdata != null) {

            ILineDataSet set = ecgdata.getDataSetByIndex(0);
            if (set == null) {
                set = createSet();
                ecgdata.addDataSet(set);
            } else {
                set.clear();
            }
        }
        ecgFile.Skip(pos);

        int[] hrs = new int[X_LENGTH * 10];
        int readNums = ecgFile.Read(hrs, X_LENGTH * 10);

        for (int i = 0; i < readNums; i++) {
            ecgdata.addEntry(new Entry(pos + i, hrs[i]), 0);
        }

        MIN = ecgdata.getYMin();
        MAX = ecgdata.getYMax();
        ecgdata.notifyDataChanged();
        mChart.notifyDataSetChanged();
        mChart.setVisibleXRangeMaximum(X_LENGTH);
        mChart.moveViewTo(pos, (MIN + MAX) / 2, mChart.getAxisLeft().getAxisDependency());

//*/
    }

    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "ECG");
        set.setCalcMinMaxWhenRemoving(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(ColorTemplate.getHoloBlue());
        set.setLineWidth(2f);
        set.setCircleRadius(1f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.BLACK);
        set.setValueTextColor(Color.RED);
        set.setValueTextSize(15f);
        set.setDrawValues(true);

        set.setDrawHighlightIndicators(false);
        set.setDrawFilled(false);
        set.setFillAlpha(20);
        set.setHighlightLineWidth(50f);
        return set;
    }

    public void SelectFile(String filename) {
        mAFNumber = 0;// init number of AF
        if (ecgFile != null && ecgFile.filename == filename) {
            ecgFile.Skip(0);
            return;
        }
        if (ecgFile != null) ecgFile.Close();

        ecgFile = new MitBitFile();
        ecgFile.Open(filename);

        curPosition = 0;
        inCounter = curPosition + 300;
        sampleCounter = inCounter;

        setPosition(curPosition);

        //*/
    }

    public void init() {
//        createCSV("AF.csv");
//        csvWrite("Start","End");
        pipleline = new PipeLine();
        pipleline.init(250, 5);
        mTvHeartRate = (TextView) findViewById(R.id.text_hrs);
        mTvAFNumber = (TextView) findViewById(R.id.text_af);

        FileSpinner = (Spinner) findViewById(R.id.spinnerMitbit);

        //Set an adapter so that we can add values to the spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, android.R.id.text1);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        FileSpinner.setAdapter(spinnerAdapter);

        try {

            File recordingFolder = new File(Environment.getExternalStorageDirectory(), FOLDER_NAME);
            if (recordingFolder.exists()) {
                File[] files = recordingFolder.listFiles();
                if (files != null) {
                    for (File file : files) {
                        String fileName = file.getName();
                        if (fileName.contains(".hea")) {
                            String sub = fileName.substring(0, fileName.length() - 4);
                            spinnerAdapter.add(sub);
                        }
                    }
                    spinnerAdapter.notifyDataSetChanged();
                } else {
                    spinnerAdapter.add("No files to display");
                }
            } else {
                recordingFolder.mkdir();
            }

            FileSpinner.setOnItemSelectedListener(this);

            setupChart();
            IsGraphProgress = true;
            feedMultiple();
            ecgAlgo = new EcgAlgo("com.heartbook.smct", 5);
            playBtn = (Button) findViewById(R.id.Playbtn);
            playBtn.setText("Stop");
            playBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bStop = !bStop;
                    if (bStop) {
                        mChart.setTouchEnabled(true);
                        playBtn.setText("Play");
                    } else {
                        mChart.setTouchEnabled(false);
                        playBtn.setText("Stop");
                    }
                }
            });
            //*/
        } catch (Exception e) {
            Log.e("initerror", e.toString());
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        String filename = ((TextView) FileSpinner.getSelectedView()).getText().toString();
        filename += ".hea";
        SelectFile(filename);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void setupChart() {
        mChart = (LineChart) findViewById(R.id.lineChart);
        mChart.setOnChartValueSelectedListener(this);
        // disable description text
        mChart.getDescription().setEnabled(false);

        mChart.setTouchEnabled(true);
        mChart.setOnTouchListener(this);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleXEnabled(true);
        mChart.setScaleYEnabled(false);

        mChart.setDrawGridBackground(false);
        // if disabled, scaling can be done on x- and y-axis separately
        //  mChart.setPinchZoom(true);

        // set an alternative background color
        mChart.setBackgroundColor(Color.TRANSPARENT);

        LineData data = new LineData();
        data.setValueTextColor(Color.BLACK);

        // add empty data
        mChart.setData(data);

        // get the legend (only possible after setting data)
        Legend legend = mChart.getLegend();

        // modify the legend ...
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextColor(Color.BLACK);
        legend.setEnabled(false);

        XAxis xl = mChart.getXAxis();

        xl.setTextColor(Color.BLACK);
        xl.setDrawGridLines(true);
        //xl.setAvoidFirstLastClipping(false);

        YAxis leftAxis;
        leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setAxisMaximum(MAX);
        leftAxis.setAxisMinimum(0);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    public void EcgFlow() {
        LineData ecgdata = mChart.getData();
        if (ecgdata == null) return;
        int maxX = (int) ecgdata.getXMax();
        int minX = (int) ecgdata.getXMin();
        if (maxX < 0 || minX < 0) return;
        curPosition++;
        curPosition = Math.max(minX, curPosition);
        curPosition = Math.min(maxX - X_LENGTH, curPosition);
        MIN = ecgdata.getYMin();
        MAX = ecgdata.getYMax();
        ecgdata.notifyDataChanged();
        mChart.notifyDataSetChanged();
        mChart.setVisibleXRangeMaximum(X_LENGTH);
        mChart.moveViewTo(curPosition, (MIN + MAX) / 2, mChart.getAxisLeft().getAxisDependency());
    }

    public void MonitorEcgIO() {
        LineData ecgdata = mChart.getData();
        if (ecgdata == null) return;
        int maxX = (int) ecgdata.getXMax();
        int minX = (int) ecgdata.getXMin();
        if (maxX < 0 || minX < 0) return;
        curPosition = Math.max(minX, curPosition);
        curPosition = Math.min(maxX - X_LENGTH, curPosition);
        int remain = maxX - curPosition;

        if (remain < FORE_LEN + X_LENGTH) {
            int[] buf = new int[1];
            int pos = (int) ecgFile.sampleCounter;
            int readbytes = ecgFile.Read(buf, 1);
            for (int i = 0; i < readbytes; i++) {
                ecgdata.addEntry(new Entry(pos + i, buf[i]), 0);
            }

            ecgdata.notifyDataChanged();
            mChart.notifyDataSetChanged();
            mChart.setVisibleXRangeMaximum(X_LENGTH);
        }
        ILineDataSet set = ecgdata.getDataSetByIndex(0);

        if (set.getEntryCount() > X_LENGTH * 10) {

            int popCount = set.getEntryCount() - X_LENGTH * 10;
            for (int j = 0; j < popCount; j++)
                set.removeFirst();

            ecgdata.notifyDataChanged();
            mChart.notifyDataSetChanged();
            mChart.setVisibleXRangeMaximum(X_LENGTH);
        }

    }

    public void EcgAnalyze() {
        LineData ecgdata = mChart.getData();
        if (ecgdata == null) return;
        int maxX = (int) ecgdata.getXMax();
        int minX = (int) ecgdata.getXMin();
        if (maxX < 0 || minX < 0 || inCounter >= maxX) return;
        ILineDataSet set = ecgdata.getDataSetByIndex(0);

        int preCounter = inCounter - 1;
        preCounter = Math.max(0, preCounter);
        List<Entry> entrys = set.getEntriesForXValue(inCounter);
        List<Entry> preEntrys = set.getEntriesForXValue(preCounter);
        if (entrys.isEmpty() || preEntrys.isEmpty()) {
            return;
        }
        int curY = (int) entrys.get(0).getY();
        int preY = (int) preEntrys.get(0).getY();

        AlgoProcess(curY);
        inCounter++;
        inCounter = Math.max(minX, inCounter);
        inCounter = Math.min(maxX, inCounter);
    }

    int QRSCounter = 0;
    boolean prevbAF = false;
    int timeOfPrevAF = 0;
    int mAFNumber = 0;
    public void AlgoProcess(int data) {

        pipleline.add(data);
        if (pipleline.isDected()) {
            String strHeartRate = "" + pipleline.getHeartRate();

            setHRateOnView(strHeartRate);
            int peakPos = peakStart;
            LineData ecgdata = mChart.getData();
            Boolean bAF = false;
            if (pipleline.typeClassification.processwaveChars.type == 86 || pipleline.typeClassification.processwaveChars.type == 83 || pipleline.typeClassification.processwaveChars.type == 65)
                bAF = true;

            if (bAF) {
                if (!prevbAF) {
                    timeOfPrevAF = peakStart;
                }
            } else {
                if (prevbAF) {
//                    String AFStart = String.format("%d",timeOfPrevAF);
//                    String AFEnd = String.format("%d",peakStart);
//                    csvWrite(AFStart,AFEnd);
                }
            }
            prevbAF = bAF;

            if (ecgdata != null) {
                ILineDataSet set = ecgdata.getDataSetByIndex(0);
                List<Entry> entrys = set.getEntriesForXValue(peakPos);
                if (!entrys.isEmpty()) {
                    if (pipleline.typeClassification.processwaveChars.type == 86 || pipleline.typeClassification.processwaveChars.type == 83 || pipleline.typeClassification.processwaveChars.type == 65)
                    {
                        entrys.get(0).setData("AF");
                        mAFNumber++;
                        setAFNumberOnView();
                    }
                    else entrys.get(0).setData("N");
                }
            }

            peakStart = sampleCounter;
        }
        sampleCounter++;
    }

    private void feedMultiple() {
        if (feedThread != null)
            feedThread.interrupt();

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                EcgFlow();
                EcgAnalyze();
                MonitorEcgIO();
            }
        };

        feedThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (; ; ) {
                    if (IsGraphProgress) {
                        //Don't generate garbage runnables inside the loop.
                        if (!bStop) runOnUiThread(runnable);
                        try {
                            Thread.sleep(REFRESH_INTERVAL);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            break;
                        }
                    }
                }
            }
        });
        feedThread.start();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
    }

    @Override
    public void onNothingSelected() {
    }

    private void setHRateOnView(final String _strHrt) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTvHeartRate.setText(_strHrt);
            }
        });
    }
    private void setAFNumberOnView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTvAFNumber.setText(""+mAFNumber);
            }
        });
    }
}
