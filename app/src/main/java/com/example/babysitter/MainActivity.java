package com.example.babysitter;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Group;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.github.mikephil.charting.animation.Easing;
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
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.utils.FileUtils;
import com.github.mikephil.charting.utils.MPPointD;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity   {

    private MyMediaRecorder audioRecorder;
    private int RECORD_AUDIO_WRITE_EXTTERNAL_STORAGE_CODE = 1;
    private Thread thread;
    private boolean isThreadRun =true;// для проверки потока
    int soundLevel = 0;
    TextView tv_cur_value_chg;
    private boolean bListener = true; // для проверки записи звука
    boolean stoped=false;  // для кнопки stop

    private BarChart mChart;
    private LimitLine ll;
    private YAxis leftAxis;

    long savedTime=0;
    private boolean isChart;
    ArrayList<BarEntry> yVals;
    Button btn_stop;
    Button btn_start;
    TextView tv_status;
    View v_limit_line_big;
    View v_limit_line;
    View v_limit_line_chgble;
    TextView tv_limit_value_chgble;
    TextView tv_limit_value;
    float dY, dX;
    float  Y0=0, Yc ;
    double YValue;
    List<Float> Ys;
    List<Integer> yy = new ArrayList<>();

    int alarmSoundLevel= 0;
    List<Long> alarmTime= new ArrayList<>();
    long alarmTimeDiff;
    RelativeLayout gr_Button;
    ConstraintLayout parent;


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
                tv_cur_value_chg.setText(String.valueOf(soundLevel));
                if(alarmSoundLevel >4 ){
                    if(alarmTimeDiff<20) {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+"87087152129"));
                    startActivity(intent);
                        tv_status.setText(String.valueOf(alarmTimeDiff));
                        Log.d("alarm", "Alarm time is  - "+alarmTimeDiff);
                        alarmSoundLevel = 0;
                        alarmTimeDiff = 0;
                        alarmTime.clear();

                        isThreadRun = false;
                        bListener = false;
                        audioRecorder.delete();
                        Log.d(TAG, "stopClick is called");
                        thread = null;
                        isChart = false;
                        stoped = true;


                        yy.clear();
                    }else{
                        alarmTimeDiff = 0;
                        alarmTime.clear();
                    }
                }
                yy.add(soundLevel);
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
        v_limit_line = findViewById(R.id.limit_line_view);
        v_limit_line_big = findViewById(R.id.limit_line_big);
        tv_limit_value = findViewById(R.id.limit_value);
        tv_limit_value_chgble = findViewById(R.id.limit_value_chgble);
        tv_cur_value_chg = findViewById(R.id.curt_value_chgble);
        v_limit_line_chgble = findViewById(R.id.limit_line_chgble);
        gr_Button=  findViewById(R.id.group_of_btn);
        parent = findViewById(R.id.parentView);

        Log.d(TAG,"onCreate is called");
        audioRecorder = new MyMediaRecorder();
        Ys= new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)==PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(MainActivity.this,"You have already granted permissions",Toast.LENGTH_SHORT).show();
        }else{
            requestRecordAudioPermission();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        initChart();


        v_limit_line_big.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        v_limit_line_chgble.setVisibility(View.VISIBLE);
                        tv_limit_value_chgble.setVisibility(View.VISIBLE);
                        dY = v.getY() - event.getRawY()+v.getHeight()/2;
                        float Y = v_limit_line_chgble.getY();
                        Log.d("ACTION_DOWN", "v getY() = " + v.getHeight()/2);
                        Log.d("ACTION_DOWN", "rawY = " + event.getRawY());
                        Log.d("ACTION_DOWN", "dY() = " + dY);
                        Display display = getWindowManager().getDefaultDisplay();
                        Point size = new Point();
                        display.getSize(size);
                        int height = size.y;
//                        Log.d("ACTION_DOWN", "screenHeight() = " +  height);
                        Ys.add(v_limit_line_chgble.getY());

                        break;
                    case MotionEvent.ACTION_MOVE:
                        if(v_limit_line_chgble.getY() > 0+gr_Button.getHeight() + tv_status.getHeight()
                                && v_limit_line_chgble.getY() < parent.getHeight() - 40){
                            v_limit_line_chgble.setVisibility(View.VISIBLE);
                            float ChartY = event.getRawY()+dY -gr_Button.getHeight()-72 -tv_status.getHeight();
                            MPPointD point = mChart.getTransformer(YAxis.AxisDependency.LEFT).getValuesByTouchPoint(event.getX(),ChartY);
                            YValue = point.y;
                            v_limit_line_chgble.animate()
                                    .y(event.getRawY() + dY)
                                    .setDuration(0)
                                    .start();
                            tv_limit_value_chgble.animate()
                                    .y(event.getRawY() + dY - 50)
                                    .setDuration(0)
                                    .start();
                            Log.d("ACTION_MOVE",  ChartY+"(ChartY)"+" = "+ v.getY()+"(event.getY()) + "+gr_Button.getHeight()+"(gr_Button.getHeight()) + "
                                    + tv_status.getHeight()+"(tv_status.getHeight())"+52);
                            Log.d("ACTION_MOVE", ".y("+(event.getRawY()+dY)+"); "+event.getRawY()+"(event.getRawY())"+dY+"(dY)");
                            Ys.add(event.getRawY() + dY);
                            Log.d("Yvalue", "yValue = " + YValue);
                            tv_limit_value_chgble.setText(String.valueOf(Math.round(YValue)));
                        }else{
                            v_limit_line_chgble.setVisibility(View.INVISIBLE);
                            v_limit_line_chgble.animate()
                                    .y(event.getRawY() + dY)
                                    .setDuration(0)
                                    .start();
                            tv_limit_value_chgble.animate()
                                    .y(event.getRawY() + dY - 50)
                                    .setDuration(0)
                                    .start();
                        }

                        break;
                    case MotionEvent.ACTION_UP:
                        v_limit_line_chgble.animate()
                                .y(Ys.get(0))
                                .setDuration(0)
                                .start();
                        v_limit_line_chgble.setVisibility(View.INVISIBLE);
                        tv_limit_value_chgble.setVisibility(View.INVISIBLE);
                        tv_limit_value.setText(tv_limit_value_chgble.getText());
                        if(YValue>10 && YValue<32500){
                        leftAxis.removeAllLimitLines();
                        LimitLine limitLineNew = new LimitLine((int)YValue);
                        limitLineNew.setLineColor(Color.RED);
                        leftAxis.setAxisMaximum(limitLineNew.getLimit() * 1.43f);
                        leftAxis.addLimitLine(limitLineNew);
                        limitLineNew.setLineWidth(25f);
                        tv_limit_value_chgble.setText(String.valueOf((int)YValue));
                        mChart.getData().notifyDataChanged();
                        mChart.notifyDataSetChanged();
                        mChart.invalidate();
                        }

                        Log.d("haha" , "Y view = " + (event.getRawY() + dY ));
                        Ys.clear();
                    default:
                        return false;

                }


                return true;
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_activity_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
                Intent settings_intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settings_intent);
        }
        return super.onOptionsItemSelected(item);
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

    private void requestRecordAudioPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.RECORD_AUDIO)
        && ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
        && ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CALL_PHONE)){
            new AlertDialog.Builder(this)
                    .setTitle("Permissions needed")
                    .setMessage(R.string.permissions_explanations)
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]
                                            {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CALL_PHONE}
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
                    {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CALL_PHONE}
                    , RECORD_AUDIO_WRITE_EXTTERNAL_STORAGE_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == RECORD_AUDIO_WRITE_EXTTERNAL_STORAGE_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED){
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
                            if(soundLevel>leftAxis.getLimitLines().get(0).getLimit()){
                                alarmSoundLevel++;
                                alarmTime.add(savedTime);
                                alarmTimeDiff = alarmTime.get(alarmTime.size()-1)-alarmTime.get(0);
                            }

                            Log.d(TAG,"started Listening audio");
                            Message message = new Message();
                            message.what = 1;
                            handler.sendMessage(message);
                        }
                        if(stoped){
                            stoped=false;
                            throw new InterruptedException();
                        }else{
                            Thread.sleep(100);
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

//            mChart.setOnChartGestureListener(this);
            Log.d("haha", "sdfsdfs");
            setupChart();
            setupAxes();
            setupData();

//            mChart.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    switch(event.getAction()){
//                        case MotionEvent.ACTION_DOWN:
//                            float posY = (float) mChart.getPixelForValues(0, leftAxis.getLimitLines().get(0).getLimit(),mChart.getAxisLeft().getAxisDependency()).y;
//                            MPPointD pointDown = mChart.getTransformer(YAxis.AxisDependency.LEFT).getValuesByTouchPoint(event.getX(),event.getY());
//                            YValue = pointDown.y;
//                            if(event.getY() >= posY - 35 && event.getY()<= posY + 35){
//                                dY = v_limit_line_chgble.getY() - event.getRawY();
//                                v_limit_line_chgble.setVisibility(View.VISIBLE);
////                                v_limit_line_chgble.animate().y(posY).start();
//                                tv_limit_value_chgble.setVisibility(View.VISIBLE);
//                                Ys.add(event.getRawY());
//                            Log.d("mChart", "ACTION_DOWN getY() = " + event.getY() + " yValue = " + YValue);}
//                            else{
//                                v_limit_line_chgble.setVisibility(View.INVISIBLE);
//                                tv_limit_value_chgble.setVisibility(View.INVISIBLE);
//                                Log.d("mChart", "ACTION_DOWN ElsE getY() = " + event.getY());
//                            }
//                            break;
//                        case MotionEvent.ACTION_MOVE:
//                            MPPointD point = mChart.getTransformer(YAxis.AxisDependency.LEFT).getValuesByTouchPoint(event.getX(),event.getY());
//                            YValue = point.y;
//                            v_limit_line_chgble.animate()
//                                    .y(event.getRawY() + dY)
//                                    .setDuration(0)
//                                    .start();
//                            tv_limit_value_chgble.animate()
//                                    .y(event.getRawY() + dY- 50)
//                                    .setDuration(0)
//                                    .start();
////                            Log.d("yoyo", "animation Y = " + (event.getRawY() + dY));
////                            Log.d("yoyo", "animation dY = " + (v_limit_line_chgble.getY()));
//                            Ys.add(event.getY());
////                            Log.d("Yvalue", "yValue = " + YValue);
//                            tv_limit_value_chgble.setText(String.valueOf(YValue));
//                            break;
//                        case MotionEvent.ACTION_UP:
//                            if(Ys.size() !=0){
//                                v_limit_line_chgble.animate()
//                                        .y(Ys.get(0))
//                                        .setDuration(4000)
//                                        .start();
//                                v_limit_line_chgble.setVisibility(View.INVISIBLE);
//                                tv_limit_value_chgble.setVisibility(View.INVISIBLE);
//                                tv_limit_value.setText(tv_limit_value_chgble.getText());
//
////                        Log.d("haha" , "Y view = " + (event.getRawY() + dY ));
//                                Ys.clear();}
//                            break;
//                        default:
//                            return false;
//                    }
//                    return true;
//
//                }
//            });
        }
    }

    private void setupChart(){
        mChart.setViewPortOffsets(100, 0, 5, 20);
        // disable description text
        mChart.getDescription().setEnabled(false);
        // enable touch gestures
        mChart.setTouchEnabled(false);
        mChart.setDragEnabled(false);
        mChart.setDragDecelerationEnabled(false);
        mChart.setHighlightPerDragEnabled(false);
        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(false);
        // enable scaling
        mChart.setScaleEnabled(false);
        mChart.setDrawGridBackground(false);
        // set an alternative background color
//        mChart.setBackgroundColor(Color.DKGRAY);
    }



    private void setupAxes() {
        ValueFormatter xAxisFormatter = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return super.getFormattedValue(value);
            }
        };

        XAxis xAxis = mChart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);

//        xAxis.setValueFormatter(new IndexAxisValueFormatter());

//        xl.setTextColor(Color.WHITE);
//        xl.setDrawGridLines(false);
//        xl.setAvoidFirstLastClipping(true);
//        xl.setEnabled(true);

        leftAxis = mChart.getAxisLeft();
//        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMinimum(2f);
        leftAxis.setEnabled(true);
//        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

//         Add a limit line
        ll = new LimitLine(16000, "Upper Limit");
        leftAxis.setAxisMaximum(ll.getLimit()*1.43f);
        ll.setLineWidth(25f);
        ll.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
        ll.setTextSize(10f);
        ll.setLineColor(Color.GREEN);
        ll.setTextColor(Color.BLUE);

        // reset all limit lines to avoid overlapping lines

        leftAxis.addLimitLine(ll);
        // limit lines are drawn behind data (and not on top)
//        leftAxis.setDrawLimitLinesBehindData(true);
//        ll.setL
    }
//    public YAxis.AxisDependency getAxisDependency(){
//
//    }

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
            set1.setAxisDependency(YAxis.AxisDependency.LEFT);
            if(set1.getEntryCount()>50){
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
            data.setBarWidth(1f);
        }

//        data.setValueTextSize(20f);
        data.setDrawValues(false);
        mChart.setData(data);
//        mChart.setVisibleXRange(1,2);
//        mChart.setVisibleYRange(1,10, );
        mChart.getLegend().setEnabled(false);
        mChart.animateXY(1000, 1000);
        // dont forget to refresh the drawing
        mChart.invalidate();
        isChart=true;
    }

//    public class MyValueFormatter implements IValueFormatter

    public void stopClick(View v){
        isThreadRun = false;
        bListener= false;
        audioRecorder.delete();
        Log.d(TAG,"stopClick is called");
        thread = null;
        isChart = false;
        stoped = true;
        tv_status.setText(String.valueOf(Collections.max(yy)));
        yy.clear();
    }
    public void startClick(View v){
        File file = FileUtil.createFile("temp.amr");
        if(file !=null) {
            Log.d(TAG,"onResume is called");
            startRecord(file);
        }else{
            Toast.makeText(getApplicationContext(), getString(R.string.activity_recFileErr), Toast.LENGTH_LONG).show();
        }
        bListener= true;
        stoped = false;
        isThreadRun = true;

    }



}
