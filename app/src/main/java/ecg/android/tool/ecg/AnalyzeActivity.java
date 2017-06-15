package ecg.android.tool.ecg;

import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Stack;

import ecg.android.tool.parser.DataPoint;
import no.nordicsemi.android.nrftoolbox.R;

public class AnalyzeActivity extends AppCompatActivity implements OnChartValueSelectedListener,AdapterView.OnItemSelectedListener,View.OnTouchListener {

    public int nXBufferSize = 0;
    private LineChart mChart;
    public Button loadBtn;
    private EditText StartField;
    private EditText EndField;
    private TextView ProgressField;
    private Spinner FileSpinner;
    File file;
    InputStreamReader reader;
    BufferedReader in;
    public Stack<String> AFs;
    public ECGFile loadFile = new ECGFile();
    public int     ecgDataLen = 0;
    private float MAX = 4000;
    private float MIN = 0;
    private final static int X_LENGTH = 2000;
    public Stack<Integer> AFSpawnTime = new Stack<Integer>();
    private Thread thread;
    private final static int REFRESH_INTERVAL = 50; // 1 second interval
    private boolean isGraphInProgress = false;
    public boolean bLoading = false;
    public ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyze);
        loadBtn = (Button)findViewById(R.id.loadButton);
        loadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bLoading) return;
                progressBar.setVisibility(View.VISIBLE);
                ProgressField.setVisibility(View.VISIBLE);
                OpenEcgData();
            }
        });
        loadAF("AF.csv");
        StartField = (EditText)findViewById(R.id.editTextStart);
        EndField = (EditText)findViewById(R.id.editTextEnd);
        FileSpinner = (Spinner)findViewById(R.id.editTextAF);
        ProgressField = (TextView)findViewById(R.id.PressLabel);
        ProgressField.setVisibility(View.INVISIBLE);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        setupChart();
        isGraphInProgress = true;
        charMoniterRun();

    }
    @Override
    protected void onDestroy() {
        isGraphInProgress = false;
        super.onDestroy();
    }
    private void charMoniterRun() {
        if (thread != null)
            thread.interrupt();

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(bLoading)
                {
                    loadEcgData();
                }
                else SetTimeRange();
            }
        };

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (;;) {
                    if ( isGraphInProgress) {
                        //Don't generate garbage runnables inside the loop.
                        runOnUiThread(runnable);
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
        thread.start();
    }

    public void SelectAF(int index)
    {
        Integer time = AFSpawnTime.get(index);
        mChart.moveViewTo((float)time, ( MIN + MAX ) / 2 ,mChart.getAxisLeft().getAxisDependency());
    }
    public void loadAF(String filename)
    {
        AFs = new Stack<String>();
        final File root = new File(Environment.getExternalStorageDirectory(), MitBitActivity.FOLDER_NAME);
        if (!root.exists()) return;
        File f = new File(root,filename);

        if(!f.exists()) return;
        String str;
        try {
            FileInputStream fin = new FileInputStream(f);
            reader = new InputStreamReader(fin);
            in = new BufferedReader(reader);
            while( (str = in.readLine()) !=null) {
                String parts[] = str.split(",");
                int min,sec,mili;
                min = Integer.parseInt(parts[0].substring(1,parts[0].length()-1));
                sec = Integer.parseInt(parts[1].substring(1,parts[1].length()-1));
                mili = Integer.parseInt(parts[2].substring(1,parts[2].length()-1));
                AFSpawnTime.push(min*60000+sec*1000+mili);
                String afMoment = String.format("%02d:%02d:%03d",min,sec,mili);
                AFs.push(afMoment);
            }
            in.close();
            reader.close();
            fin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void setupChart()
    {
        mChart = (LineChart) findViewById(R.id.lineChart);
        mChart.setOnChartValueSelectedListener(this);
           // disable description text
        mChart.getDescription().setEnabled(false);

        mChart.setTouchEnabled(false);
        mChart.setOnTouchListener(this);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleXEnabled(true);
        mChart.setScaleYEnabled(true);

        mChart.setDrawGridBackground(false);
        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(false);

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
        leftAxis.setAxisMaximum(4000);
        leftAxis.setAxisMinimum(0);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);
    }
    public void OpenEcgData()
    {

        loadFile.Open("data.ecg",true);
        ecgDataLen = (int)loadFile.GetLength();
        if(ecgDataLen!=0)
        {
            nXBufferSize = 0;
            LineData ecgdata = mChart.getData();

            if (ecgdata != null) {

                ILineDataSet set = ecgdata.getDataSetByIndex(0);
                if (set == null) {
                    set = createSet();
                    ecgdata.addDataSet(set);
                }
            }
            bLoading = true;
            loadBtn.setEnabled(false);
        }
    }
    public void CloseEcgData()
    {
        loadFile.Close();
    }
    public void loadEcgData()
    {

        int pos = loadFile.getPosition()/4;
        if(pos==-1) return;
        LineData data = mChart.getData();
        if(data==null) return;
        int percent = pos*100/ecgDataLen;
        percent = Math.min(100,percent);
        String str = String.format("Loading...%d",percent);
        ProgressField.setText(str+"%");
        if(pos>=ecgDataLen) {
            MIN = data.getYMin();
            MAX = data.getYMax();
            data.notifyDataChanged();
            mChart.notifyDataSetChanged();
            mChart.setVisibleXRangeMaximum(X_LENGTH);
            mChart.moveViewTo(nXBufferSize, ( MIN + MAX ) / 2 ,mChart.getAxisLeft().getAxisDependency());
            bLoading = false;
            progressBar.setVisibility(View.INVISIBLE);
            ProgressField.setVisibility(View.INVISIBLE);

            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, android.R.id.text1);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            FileSpinner.setAdapter(spinnerAdapter);
            if(AFs.size()!=0)
            {
                for(int i=0;i<AFs.size();i++)
                {
                    spinnerAdapter.add(AFs.get(i));
                }
                spinnerAdapter.notifyDataSetChanged();
            }
            else
            {
                spinnerAdapter.add("No AFs to display");
            }
            FileSpinner.setOnItemSelectedListener(this);
            mChart.setTouchEnabled(true);
            bLoading = false;

            return;
        }

        int readCount=ecgDataLen-pos;
        readCount = Math.min(1000,readCount);
        for(int i=0;i<readCount;i++)
        {
            int val = loadFile.Read();
            String mark="";
            for(int j=0;j<AFSpawnTime.size();j++)
            {
                if((int)(AFSpawnTime.get(j))==nXBufferSize)
                {
                    mark = "AF";
                }
            }
            data.addEntry(new Entry(nXBufferSize,val,mark ), 0);
            nXBufferSize++;
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
        set.setValueTextSize(15f);
        set.setDrawValues(true);

        set.setDrawHighlightIndicators(false);
        set.setDrawFilled(false);
        set.setFillAlpha(20);
        set.setHighlightLineWidth(50f);
        return set;
    }
    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

   @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
       if(AFs.size()==0) return;
       SelectAF(position);
       SetTimeRange();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    public void SetTimeRange()
    {
        LineData data = mChart.getData();

        if (data == null) return;

        ILineDataSet set = data.getDataSetByIndex(0);
        if (set == null) return;

        int maxVisibleX = (int)mChart.getHighestVisibleX();
        int minVisibleX = (int) mChart.getLowestVisibleX();

//        if(!AFSpawnTime.empty())
//        {
//            int selIndex = FileSpinner.getSelectedItemPosition();
//            int time = (int)AFSpawnTime.get(selIndex);
//            if(time<minVisibleX || time>maxVisibleX)
//            {
//                SelectAF(selIndex);
//                return;
//            }
//        }

        int min,sec,mili;
        mili = maxVisibleX%1000;
        maxVisibleX = maxVisibleX/1000;
        sec = maxVisibleX%60;
        maxVisibleX = maxVisibleX/60;
        min = maxVisibleX;
        String end = String.format("%02d:%02d:%03d",min,sec,mili);
        EndField.setText(end);

        mili = minVisibleX%1000;
        minVisibleX = minVisibleX/1000;
        sec = minVisibleX%60;
        minVisibleX = minVisibleX/60;
        min = minVisibleX;
        String start = String.format("%02d:%02d:%03d",min,sec,mili);
        StartField.setText(start);
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if(!bLoading) SetTimeRange();
        return false;
    }
}
