package com.ish.awtest2;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ish.awtest2.bean.KnockData;
import com.ish.awtest2.func.Cut;
import com.ish.awtest2.func.FFT;
import com.ish.awtest2.func.GCC;
import com.ish.awtest2.func.IIRFilter;
import com.ish.awtest2.func.LimitQueue;
import com.ish.awtest2.func.Trainer;
import com.ish.awtest2.mview.TickView;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.util.List;

/**
 * @author ish
 */
public class TestActivity extends WearableActivity implements SensorEventListener {

    //private TextView mTextView;
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
    private Double[] firstKnock = new Double[ampLength];
    private Double[] finalData = new Double[finalLength];

    /**
     * 训练距离
     */
    float threshold = 0;
    /**
     * 训练数据
     */
    Double[][] trainData;
    private static final String TAG = "sensorTest";
    private String s = "";
    FFT fft = new FFT();

    //
    TickView tickView;
    //动画
    Animation disappearAnimation;

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            recLen++;
            if(recLen==0){
                mTextViewCount.setText("READY");
            }else if(recLen==1){
                mTextViewCount.setText("GO");

            }else if(recLen>2){
                mTextViewCount.setText("TAP YOUR HAND");
            }
            handler.postDelayed(this, 2000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        iniView();
        SQLiteDatabase db = Connector.getDatabase();
    }

    public void iniView() {
        tickView = (TickView)findViewById(R.id.tick_view_test);

        //慢慢消失动画
        disappearAnimation = new AlphaAnimation(1, 0);
        disappearAnimation.setDuration(500);

        disappearAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                //tickView.setVisibility(View.GONE);
                tickView.setChecked(false);
            }
        });


        btn = (Button) findViewById(R.id.test_btn);
        //mTextView = (TextView) findViewById(R.id.test_text);
        mTextViewCount = (TextView) findViewById(R.id.test_text_count);
        //初始化第一个来对齐
        Double[] firstData = DataSupport.findFirst(KnockData.class).getAllData();
        System.arraycopy(firstData,0,firstKnock,0,ampLength);
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
                    handler.postDelayed(runnable, 1000);
                    recLen = -1;
                    flag = true;
                    btn.setText("STOP");
                    btn.setAlpha(0);
                    tickView.setType(TickView.TYPE_ERROR);
                    tickView.setChecked(true);
                }
            }
        });

        //取阈值
        SharedPreferences p = getApplicationContext().getSharedPreferences("Myprefs",
                Context.MODE_PRIVATE);
        threshold = p.getFloat("threshold", threshold);
        //取出训练数据
        List<KnockData> allDatas = DataSupport.findAll(KnockData.class);
        trainData = new Double[allDatas.size()][finalLength];
        int r = 0;
        for (KnockData row : allDatas) {
            trainData[r] = row.getAllData();
            r++;
        }
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
                if (zChange > deviation && !ifStart2&&count>210) {
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
                    data = IIRFilter.highpass(data);
                    data = IIRFilter.lowpass(data);
//                    for (int i = 0; i < data.length; i++) {
//                        s = s + "," + data[i];
//                    }
//                    Log.d(TAG, "onSensorChanged: Data" + s);
//                    s = "";
//                    Double[] cutData = Cut.cutMoutain(data, 50);
                    Double[] cutData  = new Double[160];
                    System.arraycopy(data,40,cutData,0,160);
                    Double[] gccData = GCC.gcc(firstKnock, cutData);

                    //加上fft的
                    Double[] fftData = fft.getHalfFFTData(gccData);
                    System.arraycopy(gccData, 0, finalData, 0, ampLength);
                    System.arraycopy(fftData, 0, finalData, ampLength, finalLength - ampLength);
                    for (int i = 0; i < finalData.length; i++) {
                        s = s + "," + finalData[i];
                    }
                    Log.d(TAG, "onSensorChanged: finalData" + s);
                    s = "";
                    //将新的敲击数据加入对比
                    double newDis = Trainer.getNewDis(trainData, finalData);
                    Log.d(TAG, "onSensorChanged: " + newDis);
                    if (threshold >= newDis) {
                        tickView.setType(TickView.TYPE_SUCCESS);
                        tickView.setChecked(true);
                        mTextViewCount.setText("SUCESSED");
                        recLen = 2;
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                tickView.startAnimation(disappearAnimation);
                            }
                        },1500);
                        //Toast.makeText(TestActivity.this, "解锁成功", Toast.LENGTH_SHORT).show();
                        //Log.d(TAG, "onSensorChanged: 解锁成功");
                    } else {
                        tickView.setType(TickView.TYPE_ERROR);
                        tickView.setChecked(true);
                        mTextViewCount.setText("TRY AGAIN");
                        recLen = 2;
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                tickView.startAnimation(disappearAnimation);
                            }
                        },1500);
                    }

//                        }
//                    }).start();
                    flag = true;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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

    public static String formatFloatNumber(Double value) {
        if (value != null) {
            if (value.doubleValue() != 0.00) {
                java.text.DecimalFormat df = new java.text.DecimalFormat("#######0.0000000000000");
                return df.format(value.doubleValue());
            } else {
                return "0.00";
            }
        }
        return "";
    }

}
