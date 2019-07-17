package com.example.babysitter;

import android.graphics.Color;
import android.media.MediaRecorder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Toast;


import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;

import java.io.File;

public class MainActivity extends AppCompatActivity implements OnChartGestureListener {

    private MyMediaRecorder audioRecorder;
    private Thread thread;
    private boolean isThreadRun =true;// для проверки потока
    private int soundLevel;
    private boolean bListener = true; // для проверки записи звука
    boolean stoped=false;  // для кнопки stop
    private BarChart mChart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mChart = findViewById(R.id.bar_chart);
        setupChart();
        audioRecorder = new MyMediaRecorder();
//        audioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//        audioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);


    }



    private void startListeningAudio(){
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(isThreadRun){
                    try{
                        if(bListener){
                            soundLevel = audioRecorder.getMaxAmplitude();
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
        thread.run();
    }

    /**
     * Start recording
     * @param fFile
     */
    public void startRecord(File fFile){
        try{
            audioRecorder.setMyRecAudioFilel(fFile);
            if (audioRecorder.startRecorder()) {
                startListeningAudio();
            }else{
                Toast.makeText(this, getString(R.string.activity_recStartErr), Toast.LENGTH_SHORT).show();
            }
        }catch(Exception e){
            Toast.makeText(this, getString(R.string.activity_recBusyErr), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void setupChart(){
        // disable description text
        mChart.getDescription().setEnabled(false);
        // enable touch gestures
        mChart.setTouchEnabled(false);
        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);
        // enable scaling
        mChart.setScaleEnabled(false);
        mChart.setDrawGridBackground(false);
        // set an alternative background color
        mChart.setBackgroundColor(Color.DKGRAY);
    }

    private void setupAxes() {
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
