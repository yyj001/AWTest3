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
import android.widget.LinearLayout;

import com.ish.awtest2.mview.TickView;

import static android.media.AudioRecord.READ_NON_BLOCKING;
import static com.ish.awtest2.R.color.black;
import static com.ish.awtest2.R.drawable.circle2;

public class Main2Activity extends WearableActivity {

    private Button btn;
    private LinearLayout circle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(Main2Activity.this, new String[]{android
                .Manifest.permission.RECORD_AUDIO}, 1);
        setContentView(R.layout.activity_main2);
        circle = (LinearLayout)findViewById(R.id.circle);
        btn = (Button)findViewById(R.id.full_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                circle.setBackground(getResources().getDrawable(R.drawable.circle2));
            }
        });
    }


}
