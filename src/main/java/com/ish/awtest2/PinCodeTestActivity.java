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
    LimitQueue<Double> xQueue = new LimitQueue<Double>(limit);
    LimitQueue<Double> yQueue = new LimitQueue<Double>(limit);
    LimitQueue<Double> zQueue = new LimitQueue<Double>(limit);

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
    private Double[] datax = null;
    private Double[] datay = null;
    private Double[] dataz = null;
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
    private int ampLength = 42;
    private int finalLength = 192;
    private Double[] firstKnockx = new Double[ampLength];
    private Double[] firstKnocky = new Double[ampLength];
    private Double[] firstKnockz = new Double[ampLength+2];
    private Double[] finalData = new Double[finalLength];

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
            double x = sensorEvent.values[0];
            double y = sensorEvent.values[1];
            double z = sensorEvent.values[2];
            double zChange = z - preValue;
            preValue = z;
            xQueue.offer(x);
            yQueue.offer(y);
            zQueue.offer(z);
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
                    datax = xQueue.toArray(new Double[limit]);
                    datay = yQueue.toArray(new Double[limit]);
                    dataz = zQueue.toArray(new Double[limit]);
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
                    datax = IIRFilter.highpass(datax, IIRFilter.TYPE_AMPITUDE);
                    datax = IIRFilter.lowpass(datax, IIRFilter.TYPE_AMPITUDE);

                    datay = IIRFilter.highpass(datay, IIRFilter.TYPE_AMPITUDE);
                    datay = IIRFilter.lowpass(datay, IIRFilter.TYPE_AMPITUDE);

                    dataz = IIRFilter.highpass(dataz, IIRFilter.TYPE_AMPITUDE);
                    dataz = IIRFilter.lowpass(dataz, IIRFilter.TYPE_AMPITUDE);
//                    for (int i = 0; i < data.length; i++) {
//                        s = s + "," + data[i];
//                    }
//                    Log.d(TAG, "onSensorChanged: Data" + s);
//                    s = "";
//                    Double[] cutData = Cut.cutMoutain(data, 50);

                    Double[] cutDatax = new Double[160];
                    Double[] cutDatay = new Double[160];
                    Double[] cutDataz = new Double[160];
                    System.arraycopy(datax, 40, cutDatax, 0, 160);
                    System.arraycopy(datay, 40, cutDatay, 0, 160);
                    System.arraycopy(dataz, 40, cutDataz, 0, 160);

                    //与第一个对齐
                    Double[] gccDatax = GCC.gcc(firstKnockx, cutDatax);
                    Double[] gccDatay = GCC.gcc(firstKnocky, cutDatay);
                    Double[] gccDataz = GCC.gcc(firstKnockz, cutDataz);
                    Double[] allAmpData = new Double[ampLength*3+2];
                    System.arraycopy(gccDatax,0,allAmpData,0,ampLength);
                    System.arraycopy(gccDatay,0,allAmpData,ampLength,ampLength);
                    System.arraycopy(gccDataz,0,allAmpData,ampLength*2,ampLength+2);

                    //加上fft的,得到最终数据
                    Double[] fftData = FFT.getHalfFFTData(allAmpData);
                    System.arraycopy(allAmpData, 0, finalData, 0, ampLength*3+2);
                    System.arraycopy(fftData, 0, finalData, ampLength*3+2, finalLength/3);
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
        System.arraycopy(trainData[0],0,firstKnockx,0,ampLength);
        System.arraycopy(trainData[0],ampLength,firstKnocky,0,ampLength);
        System.arraycopy(trainData[0],ampLength*2,firstKnockz,0,ampLength+2);

        btn = (Button) findViewById(R.id.pincode_test_btn);
        mTextViewCount = (TextView) findViewById(R.id.pincode_test_message);
        //初始化第一个来对齐

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
