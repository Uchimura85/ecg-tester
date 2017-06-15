package ecg.android.tool.ecg;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
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
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ecg.android.tool.ECGLib.PipeLine;
import ecg.android.tool.FeaturesActivity;
import ecg.android.tool.parser.CSVParser;
import ecg.android.tool.parser.Frame;
import ecg.android.tool.profile.BleManager;
import ecg.android.tool.profile.BleProfileActivity;
import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.utility.DebugLogger;

public class ECGActivity extends BleProfileActivity implements ECGManagerCallbacks, OnChartValueSelectedListener {

    private final static String GRAPH_STATUS = "graph_status";
    private final static String HR_VALUE = "hr_value";
    private final static String IS_CONNECT = "hr_value";

    private final static int REFRESH_INTERVAL = 1; // 1 refresh interval

    private boolean isGraphInProgress = false;
    private TextView mHRSValue, mHRSPosition, mTV_battery_level;
    private TextView mTV_accX, mTV_accY, mTV_accZ, mTV_debug;
    private ImageView mImgHrsConnect; // hand connect state

    private final static int QueueSize = 1000;
    private int[] mHrmQueue = new int[QueueSize];

    private int mHrmQueueCounter = 0;

    private int mHrmValue = 0;
    private int mAFValue = 0;
    private LineChart mChart;

    private final static int X_LENGTH = 1000;
    public PipeLine pipeline;
    private float MAX = 3000;
    private float MIN = 0;
    private boolean isConnect = false;

    public CSVParser parser = new CSVParser("AFIB Sample.csv");
    public Frame frame = null;
    public CSVWriter writer = null;
    public FileWriter csvFile = null;
    public ECGFile captureFile = null;

    @Override
    protected void onCreateView(final Bundle savedInstanceState) {
        setContentView(R.layout.activity_feature_ecg);

        setGUI();
        timerHandler = new Handler();
        pipeline = new PipeLine();
        pipeline.init(250, 5);

        for (int i = 0; i < 4; i++)
            enQueue(0);

        frame = parser.getFrame();
        _checkPermission();
    }

    private final static int REQUEST_PERMISSION_REQ_CODE = 34; // any 8-bit number

    private boolean _checkPermission() {
        int nLog = ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.BLUETOOTH);
        Log.d("permisiontest", "nLog: " + nLog);
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            // When user pressed Deny and still wants to use this functionality, show the rationale
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH)) {
                Log.d("permisiontest", "1 BLUETOOTH if");
            } else {
                Log.d("permisiontest", "1 BLUETOOTH else");
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH}, REQUEST_PERMISSION_REQ_CODE);
            }
        }
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            // When user pressed Deny and still wants to use this functionality, show the rationale
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH_ADMIN)) {
                Log.d("permisiontest", "2 BLUETOOTH_ADMIN if");
            } else {
                Log.d("permisiontest", "2 BLUETOOTH_ADMIN else");
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_ADMIN}, REQUEST_PERMISSION_REQ_CODE);
            }
        }
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
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            // When user pressed Deny and still wants to use this functionality, show the rationale
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH)) {
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH}, REQUEST_PERMISSION_REQ_CODE);
            }
        }
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // When user pressed Deny and still wants to use this functionality, show the rationale
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Log.d("permisiontest", "3 ACCESS_COARSE_LOCATION if");
            } else {
                Log.d("permisiontest", "3 ACCESS_COARSE_LOCATION else");
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_REQ_CODE);
            }
        }
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.NFC) != PackageManager.PERMISSION_GRANTED) {
            // When user pressed Deny and still wants to use this functionality, show the rationale
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.NFC)) {
                Log.d("permisiontest", "4 NFC if");
            } else {
                Log.d("permisiontest", "4 NFC else");
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.NFC}, REQUEST_PERMISSION_REQ_CODE);
            }
        }

        return false;
    }

    public void createCSV(String filename) {
        final File root = new File(Environment.getExternalStorageDirectory(), MitBitActivity.FOLDER_NAME);
        if (!root.exists()) {
            root.mkdir();
        }

        String filePath = Environment.getExternalStorageDirectory() + File.separator + MitBitActivity.FOLDER_NAME + File.separator + filename;
        File f = new File(filePath);
        try {
            if (f.exists() && !f.isDirectory()) {
                csvFile = new FileWriter(filePath, false);
                writer = new CSVWriter(csvFile);
            } else {
                writer = new CSVWriter(new FileWriter(filePath));
            }
        } catch (final IOException e) {
            DebugLogger.e("csv writing fail", e.toString());
        }
    }

//    public void csvWrite(int time) {
//        int millisecond = time % 1000;
//        time /= 1000;
//        int second = time % 60;
//        time /= 60;
//        int minute = time;
////        String str;
////        str = String.format("%d:%2d:%3d", minute, second, millisecond);
//        writer.writeNext(new String[]{String.valueOf(minute), String.valueOf(second), String.valueOf(millisecond)});
//    }

    public void csvClose() {
        try {
            if (writer != null) writer.close();
            if (csvFile != null) csvFile.close();
        } catch (final IOException e) {
            DebugLogger.e("csv writing fail", e.toString());
        }
        writer = null;
        csvFile = null;

    }

    private void setBatteryLevelOnView(final int value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTV_battery_level.setText(("Battery: " + value + "mV"));

            }
        });
    }


    @Override
    public void onBatteryReceived(int value) {
        setBatteryLevelOnView(value);
    }


    private void setGUI() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mTV_accX = (TextView) findViewById(R.id.textView_accx);
        mTV_accY = (TextView) findViewById(R.id.textView_accY);
        mTV_accZ = (TextView) findViewById(R.id.textView_accZ);
        mTV_battery_level = (TextView) findViewById(R.id.text_battery_level);
        mHRSValue = (TextView) findViewById(R.id.text_hrs_value);
        mHRSPosition = (TextView) findViewById(R.id.text_hrs_position);
        mTV_debug = (TextView) findViewById(R.id.tv_debug);
        mImgHrsConnect = (ImageView) findViewById(R.id.image_hrs_position);
        mChart = (LineChart) findViewById(R.id.chart1);
        mChart.setOnChartValueSelectedListener(this);

        // disable description text
        mChart.getDescription().setEnabled(false);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleXEnabled(true);
        mChart.setScaleYEnabled(true);

        mChart.setDrawGridBackground(false);
        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        // set an alternative background color
        mChart.setBackgroundColor(Color.TRANSPARENT);

        //Begin multi line

        LineData data = new LineData();
        data.setValueTextColor(Color.BLACK);
        //

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
//        setData(1000,2000);
    }

    private void setData(int count, float range) {

        ArrayList<Entry> yVals1 = new ArrayList<Entry>();

        for (int i = 0; i < count; i++) {
            float mult = range / 2f;
            float val = (float) (Math.random() * mult) + 50;
            yVals1.add(new Entry(i, val));
        }

        ArrayList<Entry> yVals2 = new ArrayList<Entry>();

        for (int i = 0; i < count - 1; i++) {
            float mult = range;
            float val = (float) (Math.random() * mult) + 450;
            yVals2.add(new Entry(i, val));
//            if(i == 10) {
//                yVals2.add(new Entry(i, val + 50));
//            }
        }

        ArrayList<Entry> yVals3 = new ArrayList<Entry>();

        for (int i = 0; i < count; i++) {
            float mult = range;
            float val = (float) (Math.random() * mult) + 500;
            yVals3.add(new Entry(i, val));
        }

        LineDataSet set1, set2, set3;

        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) mChart.getData().getDataSetByIndex(0);
            set2 = (LineDataSet) mChart.getData().getDataSetByIndex(1);
            set3 = (LineDataSet) mChart.getData().getDataSetByIndex(2);
            set1.setValues(yVals1);
            set2.setValues(yVals2);
            set3.setValues(yVals3);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(yVals1, "DataSet 1");

            set1.setAxisDependency(YAxis.AxisDependency.LEFT);
            set1.setColor(ColorTemplate.getHoloBlue());
            set1.setCircleColor(Color.WHITE);
            set1.setLineWidth(2f);
            set1.setCircleRadius(1f);
            set1.setFillAlpha(65);
            set1.setFillColor(ColorTemplate.getHoloBlue());
            set1.setHighLightColor(Color.rgb(244, 117, 117));
            set1.setDrawCircleHole(false);
            //set1.setFillFormatter(new MyFillFormatter(0f));
            //set1.setDrawHorizontalHighlightIndicator(false);
            //set1.setVisible(false);
            //set1.setCircleHoleColor(Color.WHITE);

            // create a dataset and give it a type
            set2 = new LineDataSet(yVals2, "DataSet 2");
            set2.setAxisDependency(YAxis.AxisDependency.RIGHT);
            set2.setColor(Color.RED);
            set2.setCircleColor(Color.WHITE);
            set2.setLineWidth(2f);
            set2.setCircleRadius(2f);
            set2.setFillAlpha(65);
            set2.setFillColor(Color.RED);
            set2.setDrawCircleHole(false);
            set2.setHighLightColor(Color.rgb(244, 117, 117));
            //set2.setFillFormatter(new MyFillFormatter(900f));

            set3 = new LineDataSet(yVals3, "DataSet 3");
            set3.setAxisDependency(YAxis.AxisDependency.RIGHT);
            set3.setColor(Color.YELLOW);
            set3.setCircleColor(Color.WHITE);
            set3.setLineWidth(2f);
            set3.setCircleRadius(3f);
            set3.setFillAlpha(65);
            set3.setFillColor(ColorTemplate.colorWithAlpha(Color.YELLOW, 200));
            set3.setDrawCircleHole(false);
            set3.setHighLightColor(Color.rgb(244, 117, 117));

            // create a data object with the datasets
            LineData data = new LineData(set1, set2, set3);
            data.setValueTextColor(Color.WHITE);
            data.setValueTextSize(9f);

            // set data
            mChart.setData(data);
        }
    }

    //    ---------------BEGIN Calculate Average Ecg Count ---------------------------------
    int hrMAX = 10;
    int[] hrQueue = new int[hrMAX];
    int hrIndex = 0;

    int drawCount = 0;
    long drawTime = -1;
    int firstHRSize = 0;

    public int averageHRCount() {
        if (firstHRSize == 0) return 1;
        int total = 0;
        for (int i = 0; i < firstHRSize; i++) {
            total += hrQueue[i];
        }
        total = total / firstHRSize;
        return total;
    }

    //    ---------------END Calculate Average Ecg Count ---------------------------------

    //    ----------------BEGIN Calculate Average Fetch ( Drawing ) Count --------------------------------
    int drMAX = 10;
    int[] drQueue = new int[drMAX];
    int drIndex = 0;
    int firstDRSize = 0;

    public int averageDrawingCount() {
        if (firstDRSize == 0) return 1;
        int total = 0;
        for (int i = 0; i < firstDRSize; i++) {
            total += drQueue[i];
        }
        total = total / firstDRSize;
        return total;
    }
    //    ----------------END Calculate Average Fetch ( Drawing ) Count --------------------------------

    int fetchCount = 0;
    Handler timerHandler;

    final Runnable runnableFroDraw = new Runnable() {
        @Override
        public void run() {
            if (drawTime == -1) {
                drawTime = System.currentTimeMillis();
            }
            drawCount++;
            long elapseTime = System.currentTimeMillis() - drawTime;
            if (elapseTime > 1000) {
                drawTime = System.currentTimeMillis();
                if (mHrmQueueCounter > 800) {
                    fetchCount = Math.round(averageHRCount() / averageDrawingCount());
                } else if (mHrmQueueCounter < 100) {
                    fetchCount = averageHRCount() / averageDrawingCount() - 3;
                } else {
                    fetchCount = averageHRCount() / averageDrawingCount();
                }

                Log.d("avgDrawTime", "DataFreq: " + averageHRCount() + " FSP: " + averageDrawingCount() + " fetchCount: " + fetchCount + " queue: " + mHrmQueueCounter);
                setDebugStringOnView("DataFreq: " + averageHRCount() + " FPS: " + averageDrawingCount() + " fetchCount: " + fetchCount + " queue: " + mHrmQueueCounter);
                if (firstDRSize < drMAX) firstDRSize++;
                drQueue[drIndex] = drawCount;
                drIndex++;
                drIndex = drIndex % drMAX;
                drawCount = 0;
            }

            addEntry();
            timerHandler.postDelayed(this, REFRESH_INTERVAL);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
    }

    int nXBufferSize = 0;

    private void addEntry() {

        if (isEmptyQueue()) return;

        LineData data = mChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }
//            ILineDataSet setX = data.getDataSetByIndex(1);
//            if (setX == null) {
//                setX = createSetX();
//                data.addDataSet(setX);
//            }
//            ILineDataSet setY = data.getDataSetByIndex(2);
//            if (setY == null) {
//                setY = createSetY();
//                data.addDataSet(setY);
//            }
//            ILineDataSet setZ = data.getDataSetByIndex(3);
//            if (setZ == null) {
//                setZ = createSetZ();
//                data.addDataSet(setZ);
//            }

            //
//            if (fetchCount == 0) return;
            for (int i = 0; i < 35; i++) {
                if (isEmptyQueue()) break;
                int val = deQueue();
                if (val == -1) break;

//                data.addEntry(new Entry(nXBufferSize, val), 0);
                set.addEntry(new Entry(nXBufferSize, val));
//                setX.addEntry(new Entry(nXBufferSize, val.x));
//                setY.addEntry(new Entry(nXBufferSize, val.y));
//                setZ.addEntry(new Entry(nXBufferSize, val.z));


                // analysis af while hand connected.

                AlgoProcess(val);
//                if(captureFile != null){
//                    captureFile.Write(val.e);
//                }

                nXBufferSize++;
            }

            // n page show
            if (set.getEntryCount() > X_LENGTH * 300) {
                for (int j = 0; j < 100; j++) {
                    set.removeFirst();
//                    setX.removeFirst();
//                    setY.removeFirst();
//                    setZ.removeFirst();

                }
            }

            MIN = data.getYMin();
            MAX = data.getYMax();
            data.notifyDataChanged();
            mChart.notifyDataSetChanged();
            mChart.setVisibleXRangeMaximum(X_LENGTH);
            mChart.moveViewTo(nXBufferSize, (MIN + MAX) / 2, mChart.getAxisLeft().getAxisDependency());
        }
    }

    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "ECG");
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
        set.setValueTextSize(9f);
        set.setDrawValues(true);
        return set;
    }

    private LineDataSet createSetX() {
        LineDataSet set = new LineDataSet(null, "accx");
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.RED);
        set.setCircleColor(Color.RED);
        set.setLineWidth(2f);
        set.setCircleRadius(1f);
        set.setFillAlpha(65);
        set.setFillColor(Color.RED);
        set.setHighLightColor(Color.BLACK);
        set.setValueTextColor(Color.RED);
        set.setValueTextSize(9f);
        set.setDrawValues(true);
        return set;
    }

    private LineDataSet createSetY() {
        LineDataSet set = new LineDataSet(null, "accy");
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.BLACK);
        set.setCircleColor(Color.BLACK);
        set.setLineWidth(2f);
        set.setCircleRadius(1f);
        set.setFillAlpha(65);
        set.setFillColor(Color.BLACK);
        set.setHighLightColor(Color.BLACK);
        set.setValueTextColor(Color.RED);
        set.setValueTextSize(9f);
        set.setDrawValues(true);
        return set;
    }

    private LineDataSet createSetZ() {
        LineDataSet set = new LineDataSet(null, "accz");
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.GREEN);
        set.setCircleColor(Color.GREEN);
        set.setLineWidth(2f);
        set.setCircleRadius(1f);
        set.setFillAlpha(65);
        set.setFillColor(Color.GREEN);
        set.setHighLightColor(Color.BLACK);
        set.setValueTextColor(Color.RED);
        set.setValueTextSize(9f);
        set.setDrawValues(true);
        return set;
    }


    @Override
    protected void onStart() {
        super.onStart();
        final Intent intent = getIntent();
        if (!isDeviceConnected() && intent.hasExtra(FeaturesActivity.EXTRA_ADDRESS)) {
            final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(getIntent().getByteArrayExtra(FeaturesActivity.EXTRA_ADDRESS));
            onDeviceSelected(device, device.getName());

            intent.removeExtra(FeaturesActivity.EXTRA_APP);
            intent.removeExtra(FeaturesActivity.EXTRA_ADDRESS);
        }
        //getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        isGraphInProgress = savedInstanceState.getBoolean(GRAPH_STATUS);
        mHrmValue = savedInstanceState.getInt(HR_VALUE);
        isConnect = savedInstanceState.getBoolean(IS_CONNECT);
        if (isGraphInProgress) {
            startShowGraph();
            setDeviceConnectStateOnView();
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(GRAPH_STATUS, isGraphInProgress);
        outState.putInt(HR_VALUE, mHrmValue);
        outState.putBoolean(IS_CONNECT, isConnect);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopShowGraph();
    }

    @Override
    protected int getLoggerProfileTitle() {
        return R.string.hrs_feature_title;
    }

    @Override
    protected int getAboutTextId() {
        return R.string.hrs_about_text;
    }

    @Override
    protected int getDefaultDeviceName() {
        return R.string.hrs_default_name;
    }

    @Override
    protected UUID getFilterUUID() {
        return ECGManager.HR_SERVICE_UUID;
    }

    void startShowGraph() {
        isGraphInProgress = true;
        timerHandler.postDelayed(runnableFroDraw, 10);
    }

    void stopShowGraph() {
        isGraphInProgress = false;
        setDefaultUI();

    }

    @Override
    protected BleManager<ECGManagerCallbacks> initializeManager() {
        final ECGManager manager = ECGManager.getInstance(getApplicationContext());
        manager.setGattCallbacks(this);
        return manager;
    }


    @Override
    public void onServicesDiscovered(final BluetoothDevice device, final boolean optionalServicesFound) {
        // this may notify user or show some views
    }

    @Override
    public void onDeviceReady(final BluetoothDevice device) {
        startShowGraph();
    }

    @Override
    public void onHRSensorPositionFound(final BluetoothDevice device, final String position) {
        setHRSPositionOnView(position);
    }

    int peakStart = 0;

    long hrReceive = -1;
    int hrNumber = 0;

    @Override
    public void onHRValueReceived(final BluetoothDevice device, int ecgVal, boolean isSensorDetected) {
        hrNumber++;
        if (hrReceive == -1) hrReceive = System.currentTimeMillis();
        long ellipse = System.currentTimeMillis() - hrReceive;
        if (ellipse > 1000) {
            hrReceive = System.currentTimeMillis();
            Log.d("hrRec", "" + hrNumber);

            hrQueue[hrIndex] = hrNumber;
            hrIndex++;
            hrIndex = hrIndex % hrMAX;
            if (firstHRSize < hrMAX) firstHRSize++;

            hrNumber = 0;
        }
        isConnect = isSensorDetected;
        setDeviceConnectStateOnView();
        if (ecgVal >= Math.pow(2, 15)) {
            return;
        }
        enQueue(ecgVal);
    }

    public int inCounter = 300;

    boolean prevbAF = false;
    int timeOfPrevAF = 0;
    public int sampleCounter = 0;

    public void AlgoProcess(int data) {

        pipeline.add(data);
        if (pipeline.isDected()) {
            String str = "" + pipeline.getHeartRate();
            setHRSValueOnView(pipeline.getHeartRate());

            Log.d("Heartrate", str);
//			bpmView.setText(str);
            int peakPos = peakStart;
            Log.d("peakpos", "" + peakPos);
            LineData LineData = mChart.getData();
            Boolean bAF = false;
            if (pipeline.typeClassification.processwaveChars.type == 86 || pipeline.typeClassification.processwaveChars.type == 83 || pipeline.typeClassification.processwaveChars.type == 65)
                bAF = true;

            if (bAF) {
                if (!prevbAF) {
                    timeOfPrevAF = peakStart;
                }
            } else {
                if (prevbAF) {
                    String AFStart = "" + timeOfPrevAF;
                    String AFEnd = "" + peakStart;
                    Log.d("afresult", AFStart + ":" + AFEnd);
                }
            }
            prevbAF = bAF;

            if (LineData != null) {
                ILineDataSet set = LineData.getDataSetByIndex(0);
                List<Entry> entrys = set.getEntriesForXValue(peakPos);
                if (!entrys.isEmpty()) {
                    String strAFandNormal;
                    if (pipeline.typeClassification.processwaveChars.type == 86 || pipeline.typeClassification.processwaveChars.type == 83 || pipeline.typeClassification.processwaveChars.type == 65) {
                        strAFandNormal = "AF";
                    } else {
                        strAFandNormal = "N";
                    }
                    if (isConnect) {
                        entrys.get(0).setData(strAFandNormal);
                    } else {
                        setHRSValueOnView(0);
                    }
                    Log.d("Heartrate", "entrys is no empty ---" + strAFandNormal);
                }
            }

            peakStart = sampleCounter;
        }
        sampleCounter++;
    }

    @Override
    public void onAccDataReceived(AccData data, boolean isSensorDeteted) {
        setAccDataOnView(data);
//        hrNumber++;
//        if (hrReceive == -1) hrReceive = System.currentTimeMillis();
//        long ellipse = System.currentTimeMillis() - hrReceive;
//        if (ellipse > 1000) {
//            hrReceive = System.currentTimeMillis();
//            Log.d("hrRec", "" + hrNumber);
//
//            hrQueue[hrIndex] = hrNumber;
//            hrIndex++;
//            hrIndex = hrIndex % hrMAX;
//            if (firstHRSize < hrMAX) firstHRSize++;
//
//            hrNumber = 0;
//        }
//        isConnect = isSensorDeteted;
//        setDeviceConnectStateOnView();
//        if (data.e >= Math.pow(2, 15)) {
//            return;
//        }
////        enQueue(data);
    }

    private void setAccDataOnView(final AccData data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String xSignal = "";
                String ySignal = "";
                String zSignal = "";
//                if ((data.x & 0x8000) == 0x8000) {
//                    xSignal = "-";
//                    data.x = data.x & 0x7fff;
//                }
//                if ((data.y & 0x8000) == 0x8000) {
//                    ySignal = "-";
//                    data.y = data.y & 0x7fff;
//                }
//                if ((data.z & 0x8000) == 0x8000) {
//                    zSignal = "-";
//                    data.z = data.z & 0x7fff;
//                }

                mTV_accX.setText(("x: " + xSignal + data.x));
                mTV_accY.setText(("y: " + ySignal + data.y));
                mTV_accZ.setText(("z: " + zSignal + data.z));

            }
        });
    }

    @Override
    public void onDeviceDisconnected(final BluetoothDevice device) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mHRSValue.setText(R.string.not_available_value);
                mHRSPosition.setText(R.string.not_available);
                stopShowGraph();
            }
        });
        csvClose();
        if (captureFile != null) {
            captureFile.Write(nXBufferSize);
            captureFile.Close();
            captureFile = null;
        }
        super.onDeviceDisconnected(device);
    }

    public void onDeviceConnected(final BluetoothDevice device) {
        super.onDeviceConnected(device);

        mAFValue = 0;
        createCSV("AF.csv");
        captureFile = new ECGFile();
        captureFile.Open("data.ecg", false);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    private int filter(int Y1, int Y2, int Y3, int Y4, int X5, double fk) {
        double x = fk;
        double A0 = Math.pow((1 - x), 4);
        double B1 = 4 * x;
        double B2 = -6 * x * x;
        double B3 = 4 * x * x * x;
        double B4 = -x * x * x * x;

        double Y5;
        Y5 = (X5 * A0 + Y4 * B1 + Y3 * B2 + Y2 * B3 + Y1 * B4) * 1.005;
        return (int) Y5;
    }

    // BEGIN management Queue
    private int enQueue(int value) {
        if (isFullQueue()) {
            System.arraycopy(mHrmQueue, 1, mHrmQueue, 0, mHrmQueueCounter - 1);
            mHrmQueueCounter--;
        }
//        if (mHrmQueueCounter > 3) {
//            //value = filter(mHrmQueue[mHrmQueueCounter-4],mHrmQueue[mHrmQueueCounter-3],mHrmQueue[mHrmQueueCounter-2],mHrmQueue[mHrmQueueCounter-1],value,0.1);
//            //value = Math.min(value,3000);
//        }
        mHrmQueue[mHrmQueueCounter] = value;
        mHrmQueueCounter++;
        return value;
    }


    private int deQueue() {
        if (isEmptyQueue()) return -1;
        int result = mHrmQueue[4];
        System.arraycopy(mHrmQueue, 1, mHrmQueue, 0, mHrmQueueCounter - 1);
        mHrmQueueCounter--;
        return result;
    }
//
//    private AccData deQueue(int count) {
//        if (count == 0 || this.mHrmQueue.length < count) {
//            return null;
//        }
//
//        int derivationE[] = new int[count];
//        int derivationX[] = new int[count];
//        int derivationY[] = new int[count];
//        int derivationZ[] = new int[count];
//
//        for (int i = 0; i < count; i++) {
//            if (i == 0) {
//                derivationE[i] = this.mHrmQueue[i + 1].e - this.mHrmQueue[i].e;
//
//                derivationX[i] = this.mHrmQueue[i + 1].x - this.mHrmQueue[i].x;
//                derivationY[i] = this.mHrmQueue[i + 1].y - this.mHrmQueue[i].y;
//                derivationZ[i] = this.mHrmQueue[i + 1].z - this.mHrmQueue[i].z;
//            } else if (i == count - 1) {
//                if (this.mHrmQueue.length > count) {
//                    derivationE[i] = this.mHrmQueue[i + 1].e - 2 * this.mHrmQueue[i].e + this.mHrmQueue[i - 1].e;
//
//                    derivationX[i] = this.mHrmQueue[i + 1].x - 2 * this.mHrmQueue[i].x + this.mHrmQueue[i - 1].x;
//                    derivationY[i] = this.mHrmQueue[i + 1].y - 2 * this.mHrmQueue[i].y + this.mHrmQueue[i - 1].y;
//                    derivationZ[i] = this.mHrmQueue[i + 1].z - 2 * this.mHrmQueue[i].z + this.mHrmQueue[i - 1].z;
//                } else {
//                    derivationE[i] = -this.mHrmQueue[i].e + this.mHrmQueue[i - 1].e;
//
//                    derivationX[i] = -this.mHrmQueue[i].x + this.mHrmQueue[i - 1].x;
//                    derivationY[i] = -this.mHrmQueue[i].y + this.mHrmQueue[i - 1].y;
//                    derivationZ[i] = -this.mHrmQueue[i].z + this.mHrmQueue[i - 1].z;
//
//                }
//            } else {
//                derivationE[i] = this.mHrmQueue[i + 1].e - 2 * this.mHrmQueue[i].e + this.mHrmQueue[i - 1].e;
//
//                derivationX[i] = this.mHrmQueue[i + 1].x - 2 * this.mHrmQueue[i].x + this.mHrmQueue[i - 1].x;
//                derivationY[i] = this.mHrmQueue[i + 1].y - 2 * this.mHrmQueue[i].y + this.mHrmQueue[i - 1].y;
//                derivationZ[i] = this.mHrmQueue[i + 1].z - 2 * this.mHrmQueue[i].z + this.mHrmQueue[i - 1].z;
//            }
//            derivationE[i] = Math.abs(derivationE[i]);
//            derivationX[i] = Math.abs(derivationX[i]);
//            derivationY[i] = Math.abs(derivationY[i]);
//            derivationZ[i] = Math.abs(derivationZ[i]);
//        }
//        int peakIndexE = 0;
//        int peakValE = derivationE[0];
//
//        int peakIndexX = 0;
//        int peakValX = derivationX[0];
//
//        int peakIndexY = 0;
//        int peakValY = derivationY[0];
//
//        int peakIndexZ = 0;
//        int peakValZ = derivationZ[0];
//
//        for (int i = 1; i < count; i++) {
//            if (derivationE[i] > peakValE) {
//                peakValE = derivationE[i];
//                peakIndexE = i;
//            }
//        }
//        for (int i = 1; i < count; i++) {
//            if (derivationX[i] > peakValX) {
//                peakValX = derivationX[i];
//                peakIndexX = i;
//            }
//        }
//
//        for (int i = 1; i < count; i++) {
//            if (derivationY[i] > peakValY) {
//                peakValY = derivationY[i];
//                peakIndexY = i;
//            }
//        }
//        for (int i = 1; i < count; i++) {
//            if (derivationZ[i] > peakValZ) {
//                peakValZ = derivationZ[i];
//                peakIndexZ = i;
//            }
//        }
//
//        int rstE = this.mHrmQueue[peakIndexE].e;
//        int rstX = this.mHrmQueue[peakIndexX].x;
//        int rstY = this.mHrmQueue[peakIndexY].y;
//        int rstZ = this.mHrmQueue[peakIndexZ].z;
//
//        System.arraycopy(mHrmQueue, count, mHrmQueue, 0, mHrmQueueCounter - count);
//        mHrmQueueCounter = mHrmQueueCounter - count;
//
//        return new AccData(rstE, rstX, rstY, rstZ);
//    }

    private boolean isFullQueue() {
        return mHrmQueueCounter == QueueSize;
    }

    private boolean isEmptyQueue() {
        return mHrmQueueCounter < 5;
    }
    // END management Queue

    @Override
    protected void setDefaultUI() {
        mHRSValue.setText(R.string.not_available_value);
        mHRSPosition.setText(R.string.not_available);
        mImgHrsConnect.setImageResource(R.drawable.disconnection);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
    }

    @Override
    public void onNothingSelected() {
    }

    public void onDebugStop(View v) {
    }

    // set ui value
    private void setDebugStringOnView(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTV_debug.setText(str);
            }
        });
    }

    private void setDeviceConnectStateOnView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isConnect) mImgHrsConnect.setImageResource(R.drawable.connection);
                else mImgHrsConnect.setImageResource(R.drawable.disconnection);
            }
        });
    }

    private void setHRSValueOnView(final int value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mHRSValue.setText(("" + value));
            }
        });
    }

    private void setHRSPositionOnView(final String position) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (position != null) {
                    mHRSPosition.setText(position);
                }
            }
        });
    }
}
