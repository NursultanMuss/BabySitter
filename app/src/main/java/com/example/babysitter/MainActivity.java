package com.example.babysitter;

import android.media.MediaRecorder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    MediaRecorder audioRecorder;
    private Thread thread;
    private boolean isThreadRun =true;// для проверки потока
    private int soundLevel;
    private boolean bListener = true; // для проверки записи звука
    boolean stoped=false;  // для кнопки stop

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audioRecorder = new MediaRecorder();
        audioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        audioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);


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
                            Thread.interrupted();
                            stoped=false;
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
}
