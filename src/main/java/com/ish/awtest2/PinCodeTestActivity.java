package com.ish.awtest2;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ish.awtest2.bean.PinCodeKnockData;
import com.ish.awtest2.bean.MyAudioData;
import com.ish.awtest2.func.FFT;
import com.ish.awtest2.func.GCC;
import com.ish.awtest2.func.IIRFilter;
import com.ish.awtest2.func.KNN;
import com.ish.awtest2.func.LimitQueue;
import com.ish.awtest2.mview.TickView;

import org.litepal.crud.DataSupport;

import java.util.List;

public class PinCodeTestActivity extends WearableActivity implements SensorEventListener {

    private TextView mTextViewCount;
    private Button btn;

    private SensorManager sm;
    private double preValue = 0;
    /**
     * count记录初始队列点的数目
     * count2记录移位点的数目
     */
    private long count = 0;
    private long count2 = 0;

    /**
     * 倒计时，从-1开始，接受两秒空白
     */
    private int recLen = -1;

    /**
     * 设置队列长度
     */
    private int limit = 200;

    /**
     * 设置队列缓存
     */
    LimitQueue<Double> queue = new LimitQueue<Double>(limit);

    /**
     * flag 按钮判断开始
     * ifStart 录完200个点，可以开始敲击
     * ifStart2 敲击开始
     */
    private boolean flag = false;
    private boolean ifStart = false;
    private boolean ifStart2 = false;
    /**
     * []data 队列转数组
     */
    private Double[] data = null;
    /**
     * deviation 振动改变阈值
     */
    private double deviation = 0.5;
    /**
     * knockCount 记录敲击次数
     */
    private int knockCount = 0;
    /**
     * firstKnock 记录敲击次数
     */
    private int ampLength = 32;
    private int finalLength = 48;
    private int audioLength = 1024;
    private int finalAudioLength = 1024;
    private Double[] firstKnock = new Double[ampLength];
    private Double[] finalData = new Double[finalLength];
    private Double[] firstAudioData = new Double[audioLength];
    private Double[] finalAudioData = new Double[finalAudioLength];

    /**
     * 训练距离
     */

    /**
     * 训练数据
     */
    Double[][] trainData;
    Double[][] audioTrainData;
    private static final String TAG = "sensorTest";
    private String s = "";
    //
    private ImageView fingerImage;

    private double newDis1, newDis2;

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            recLen++;
            if (recLen == 1) {
                mTextViewCount.setText("READY");
            } else if (recLen > 2) {
                mTextViewCount.setText("TAP YOUR HAND");
            }
            handler.postDelayed(this, 2000);
        }
    };


    private int key= 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_code_test);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Enables Always-on
        setAmbientEnabled();
        initData();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER && flag) {
            double z = sensorEvent.values[2];
            double zChange = z - preValue;
            preValue = z;
            queue.offer(z);
            count++;
            //mTextView.setText(z + "");
            //判断是否存了200个点
            if (!ifStart) {
                if (count == limit) {
                    ifStart = true;
                }
            }
            //等待敲击
            else {
                //遇到敲击
                if (zChange > deviation && !ifStart2 && count > 210) {
                    ifStart2 = true;
                    count = 0;
                }
            }
            //开始左移100个点
            if (ifStart2) {
                //左移了100个点
                if (count == limit / 2) {
                    //停止接收直到处理完
                    flag = false;
                    ifStart2 = false;
                    //队列转数组
                    data = queue.toArray(new Double[limit]);
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
                    data = IIRFilter.highpass(data, IIRFilter.TYPE_AMPITUDE);
                    data = IIRFilter.lowpass(data, IIRFilter.TYPE_AMPITUDE);
//                    for (int i = 0; i < data.length; i++) {
//                        s = s + "," + data[i];
//                    }
//                    Log.d(TAG, "onSensorChanged: Data" + s);
//                    s = "";
//                    Double[] cutData = Cut.cutMoutain(data, 50);

                    Double[] cutData = new Double[160];
                    System.arraycopy(data, 40, cutData, 0, 160);

                    //与第一个对齐
                    Double[] gccData = GCC.gcc(firstKnock, cutData);
                    //加上fft的,得到最终数据
                    Double[] fftData = FFT.getHalfFFTData(gccData);
                    System.arraycopy(gccData, 0, finalData, 0, ampLength);
                    System.arraycopy(fftData, 0, finalData, ampLength, finalLength - ampLength);
                    flag = true;
                    key = KNN.judgeDis(trainData,finalData);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(PinCodeTestActivity.this, key+"", Toast.LENGTH_SHORT).show();
                        }
                    });
                    Log.d(TAG, "key: " + key);
                }
            }
        }
    }

    private void initData() {
        mTextViewCount = (TextView) findViewById(R.id.pincode_test_message);
        //取出训练数据
        List<PinCodeKnockData> allDatas = DataSupport.findAll(PinCodeKnockData.class);
        trainData = new Double[allDatas.size()][finalLength];
        int r = 0;
        for (PinCodeKnockData row : allDatas) {
            trainData[r] = row.getArray();
            r++;
        }

        btn = (Button) findViewById(R.id.pincode_test_btn);
        mTextViewCount = (TextView) findViewById(R.id.pincode_test_message);
        //初始化第一个来对齐
        System.arraycopy(trainData[0],0,firstKnock,0,ampLength);
        firstAudioData = DataSupport.findFirst(MyAudioData.class).getAudioArray();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //停止
                if (flag) {
                    ifStart = false;
                    flag = false;
                    count = 0;
                    s = "";
                    handler.removeCallbacks(runnable);
                    mTextViewCount.setText("0");
                    btn.setText("START");
                } else {
                    //fingerImage.setVisibility(View.VISIBLE);
                    handler.postDelayed(runnable, 1000);
                    recLen = 0;
                    flag = true;
                    btn.setText("STOP");

                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        //创建一个SensorManager来获取系统的传感器服务
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                , 10000);

    }

    @Override
    public void onPause() {
        sm.unregisterListener(this);
        super.onPause();
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
