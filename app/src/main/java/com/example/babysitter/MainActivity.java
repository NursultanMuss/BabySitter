package com.example.babysitter;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnChartGestureListener {

    private MyMediaRecorder audioRecorder;
    private int RECORD_AUDIO_WRITE_EXTTERNAL_STORAGE_CODE = 1;
    private Thread thread;
    private boolean isThreadRun =true;// для проверки потока
    int soundLevel = 0;
    private boolean bListener = true; // для проверки записи звука
    boolean stoped=false;  // для кнопки stop
    private BarChart mChart;
    long savedTime=0;
    private boolean isChart;
    ArrayList<BarEntry> yVals;
    Button btn_stop;
    Button btn_start;
    TextView tv_status;

    public static final String TAG = MainActivity.class.getSimpleName();


    final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 1){
                if(!isChart){
                    Log.d(TAG,"handler is initChart");
                    initChart();
                    return;
                }
                Log.d(TAG,"handler is updateData");
                updateData(soundLevel,0);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_start = findViewById(R.id.btn_start);
        btn_stop = findViewById(R.id.btn_stop);
        tv_status = findViewById(R.id.tv_status);
        Log.d(TAG,"onCreate is called");
        audioRecorder = new MyMediaRecorder();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(MainActivity.this,"You have already granted permissions",Toast.LENGTH_SHORT).show();
        }else{
            requestRecordAudioPermission();
        }
    }

    private void requestRecordAudioPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.RECORD_AUDIO)
        && ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            new AlertDialog.Builder(this)
                    .setTitle("Permissions needed")
                    .setMessage(R.string.permissions_explanations)
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]
                                            {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}
                                    , RECORD_AUDIO_WRITE_EXTTERNAL_STORAGE_CODE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        }else{
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}
                    , RECORD_AUDIO_WRITE_EXTTERNAL_STORAGE_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == RECORD_AUDIO_WRITE_EXTTERNAL_STORAGE_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, R.string.permissions_granted, Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, R.string.permissions_denied, Toast.LENGTH_SHORT);
            }
        }
    }

    private void startListeningAudio(){
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(isThreadRun){
                    try{
                        if(bListener){
                            soundLevel = audioRecorder.getMaxAmplitude();
                            Log.d(TAG,"started Listening audio");
                            Message message = new Message();
                            message.what = 1;
                            handler.sendMessage(message);
                        }
                        if(stoped){
                            stoped=false;
                            throw new InterruptedException();

                        }
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                        bListener = false;
                    }
                }
            }
        });
        thread.start();
    }

    /**
     * Start recording
     * @param fFile
     */
    public void startRecord(File fFile){
        try{
            audioRecorder.setMyRecAudioFilel(fFile);
            if (audioRecorder.startRecorder()) {
                Log.d(TAG,"startRecord is called");
                startListeningAudio();
            }else{
                Toast.makeText(this, getString(R.string.activity_recStartErr), Toast.LENGTH_SHORT).show();
            }
        }catch(Exception e){
            Toast.makeText(this, getString(R.string.activity_recBusyErr), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void initChart(){
        if(mChart != null){
            if(mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0){
                savedTime++;
                isChart = true;
                Log.d(TAG,"initChart is not null and have Data");
            }
        }
        else{
            Log.d(TAG,"initChart is null init chart");
            mChart = findViewById(R.id.bar_chart);
            setupChart();
            setupAxes();

            setupData();
        }
    }

    private void setupChart(){
        mChart.setViewPortOffsets(50, 20, 5, 60);
        // disable description text
        mChart.getDescription().setEnabled(false);
        // enable touch gestures
        mChart.setTouchEnabled(true);
        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);
        // enable scaling
        mChart.setScaleEnabled(false);
        mChart.setDrawGridBackground(false);
        // set an alternative background color
        mChart.setBackgroundColor(Color.DKGRAY);
    }

    private void setupAxes() {
        ValueFormatter xAxisFormatter = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return super.getFormattedValue(value);
            }
        };
        XAxis xl = mChart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
//        leftAxis.setAxisMaximum(TOTAL_MEMORY);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

        // Add a limit line
        LimitLine ll = new LimitLine(590, "Upper Limit");
        ll.setLineWidth(2f);
        ll.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
        ll.setTextSize(10f);
        ll.setTextColor(Color.WHITE);
        // reset all limit lines to avoid overlapping lines
        leftAxis.removeAllLimitLines();
        leftAxis.addLimitLine(ll);
        // limit lines are drawn behind data (and not on top)
        leftAxis.setDrawLimitLinesBehindData(true);
    }

    private void updateData(int val, long time) {
        if(mChart==null){
            return;
        }
        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            BarDataSet set1 = (BarDataSet)mChart.getData().getDataSetByIndex(0);
            set1.setValues(yVals);
            BarEntry entry=new BarEntry(savedTime,val);
            set1.addEntry(entry);
            if(set1.getEntryCount()>200){
                set1.removeFirst();
            }
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
            mChart.invalidate();
            savedTime++;
        }
    }
    private void setupData() {
        yVals = new ArrayList<>();
        yVals.add(new BarEntry(0,0));
        BarData data = new BarData();
        data.setValueTextColor(Color.WHITE);

        BarDataSet set1 = new BarDataSet(yVals, "DataSet 1");
//            set1.setValueTypeface(tf);
//        set1.setMode(BarDataSet.Mode.CUBIC_BEZIER);
//        set1.setCubicIntensity(0.02f);
//        set1.setDrawFilled(true);
//        set1.setDrawCircles(false);
//        set1.setCircleColor(Color.GREEN);
//        set1.setHighLightColor(Color.rgb(244, 117, 117));
//        set1.setColor(Color.GREEN);
//        set1.setFillColor(Color.GREEN);
//        set1.setFillAlpha(100);
//        set1.setDrawHorizontalHighlightIndicator(false);
//            set1.setFillFormatter(new FillFormatter() {
//                @Override
//                public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
//                    return -10;
//                }
//            });



        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            data =  mChart.getBarData();
            data.clearValues();
            data.removeDataSet(0);
            data.addDataSet(set1);
        }else {
            data = new BarData(set1);
            data.setBarWidth(0.9f);
        }

        data.setValueTextSize(9f);
        data.setDrawValues(false);
        mChart.setData(data);
        mChart.getLegend().setEnabled(false);
        mChart.animateXY(2000, 2000);
        // dont forget to refresh the drawing
        mChart.invalidate();
        isChart=true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        File file = FileUtil.createFile("temp.amr");
        if(file !=null) {
            Log.d(TAG,"onResume is called");
            startRecord(file);
        }else{
            Toast.makeText(getApplicationContext(), getString(R.string.activity_recFileErr), Toast.LENGTH_LONG).show();
        }
        bListener= true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        bListener=false;
        audioRecorder.delete();
        Log.d(TAG,"onPause is called");
        thread = null;
        isChart= false;
    }

    @Override
    protected void onDestroy() {
        if(thread !=null){
            isThreadRun = false;
            thread = null;
        }
        Log.d(TAG,"onDestroy is called");
        audioRecorder.delete();
        super.onDestroy();
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartLongPressed(MotionEvent me) {

    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {

    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {

    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {

    }
}
