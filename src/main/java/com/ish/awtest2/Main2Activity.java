package com.ish.awtest2;

import android.content.Context;
import android.content.res.Resources;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.wearable.activity.WearableActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.ish.awtest2.mview.TickView;

import static android.media.AudioRecord.READ_NON_BLOCKING;

public class Main2Activity extends WearableActivity {
    private String TAG = "audio";
    private Button errorBtn, sucessedBtn;
    private int frequency = 11025;
    private int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    private int audioEncoding = AudioFormat.ENCODING_PCM_FLOAT;
    private int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding) * 20;
    private float[] buffer = new float[bufferSize];
    private AudioRecord audioRecord;
    private boolean isStart = false;
    private String s = "";
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void run() {
        int bufferResultLength = audioRecord.read(buffer, 0, bufferSize,READ_NON_BLOCKING);
        Log.d(TAG, "run: " + buffer[0]);
        handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(Main2Activity.this, new String[]{android
                .Manifest.permission.RECORD_AUDIO}, 1);
        setContentView(R.layout.activity_main2);
        initData();
    }

    private void initData() {
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                frequency, channelConfiguration, audioEncoding, bufferSize);
        if (audioRecord == null) {
            Log.d(TAG, "initData: null");
        }
        errorBtn = (Button) findViewById(R.id.error_btn);
        sucessedBtn = (Button) findViewById(R.id.sucessed_btn);
        sucessedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStart = true;
                audioRecord.startRecording();
                handler.postDelayed(runnable, 1);
            }
        });
        errorBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                handler.removeCallbacks(runnable);
                isStart = false;
                int bufferResultLength = audioRecord.read(buffer, 0, bufferSize,READ_NON_BLOCKING);
                audioRecord.stop();
                Log.d(TAG, "onClick: " + bufferResultLength);
                int temp = bufferResultLength / 40;
                for (int i = 0; i < 40; i++) {
                    for (int j = 0; j < temp; j++) {
                        s = s + "," + buffer[j + temp* i];
                    }
                    Log.d(TAG, "onClick: " + s);
                    s = "";
                }
                Log.d(TAG, "onClick: " + buffer[1]);
            }
        });
    }
}
