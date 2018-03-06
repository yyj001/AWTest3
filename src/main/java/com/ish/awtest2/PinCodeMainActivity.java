package com.ish.awtest2;

import android.content.Context;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ish.awtest2.bean.PinCodeKnockData;
import com.ish.awtest2.bean.MyAudioData;
import com.ish.awtest2.bean.PinCodeKnockData;
import com.ish.awtest2.func.Cut;
import com.ish.awtest2.func.FFT;
import com.ish.awtest2.func.GCC;
import com.ish.awtest2.func.IIRFilter;
import com.ish.awtest2.func.LimitQueue;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.util.List;

public class PinCodeMainActivity extends WearableActivity implements SensorEventListener {

    private TextView countTextView;
    private TextView valueTextView;
    private TextView messageTextView;
    private Button startBtn;
    private Button testBtn;
    private Button showBtn;
    private Button deleteBtn;

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
    private Double[] dataX = null;
    private Double[] dataY = null;
    private Double[] dataZ = null;
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
    //private int finalLength = 48;
    private int finalLength = 192;
    private Double[] firstKnockX = new Double[ampLength];
    private Double[] firstKnockY = new Double[ampLength];
    private Double[] firstKnockZ = new Double[ampLength+2];
    private Double[] finalData = new Double[finalLength];

    private static final String TAG = "sensor";
    private String s = "";
    Handler handler = new Handler();

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (recLen < 80) {
                recLen++;
                countTextView.setText("" + recLen);
                handler.postDelayed(this, 2000);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_code_main);
        // Enables Always-on
        setAmbientEnabled();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        iniView();
        SQLiteDatabase db = Connector.getDatabase();
    }

    public void iniView() {
        countTextView = (TextView) findViewById(R.id.pincode_text_count);
        valueTextView = (TextView) findViewById(R.id.pincode_text_value);
        messageTextView = (TextView) findViewById(R.id.pincode_message);
        startBtn = (Button) findViewById(R.id.pincode_start);
        testBtn = (Button) findViewById(R.id.pincode_test);
        showBtn = (Button) findViewById(R.id.pincode_show_database);
        deleteBtn = (Button) findViewById(R.id.pincode_delete);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //停止
                if (flag) {
                    ifStart = false;
                    flag = false;
                    count = 0;
                    s = "";
                    handler.removeCallbacks(runnable);
                    countTextView.setText("0");
                    startBtn.setText("start");
                } else {
                    //倒计时
                    handler.postDelayed(runnable, 200);
                    recLen = -1;
                    flag = true;
                    startBtn.setText("stop");
                    knockCount = 0;
                }
            }
        });

        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PinCodeMainActivity.this, PinCodeTestActivity.class));
            }
        });

        showBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatabase();
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDatabase();
            }
        });

    }

    private void showDatabase() {
        List<PinCodeKnockData> allDatas = DataSupport.findAll(PinCodeKnockData.class);
        Log.d(TAG, "行数: " + allDatas.size());
        for (PinCodeKnockData row : allDatas) {
            Double[] rowData = row.getArray();
            String tempStr = "";
            for (int i = 0; i < rowData.length; i++) {
                tempStr += "," + rowData[i];
            }
            Log.d(TAG, "showDatabase: " + tempStr);
        }
    }

    private void deleteDatabase() {
        DataSupport.deleteAll(PinCodeKnockData.class);
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
            valueTextView.setText(z + "");
            //判断是否存了200个点
            if (!ifStart) {
                if (count == limit) {
                    ifStart = true;
                }
            }
            //等待敲击
            else {
                //遇到敲击
                if (zChange > deviation && !ifStart2) {
                    ifStart2 = true;
                    count = 0;
                    knockCount++;
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
                    Toast.makeText(this, "" + knockCount, Toast.LENGTH_SHORT).show();
                    dataX = xQueue.toArray(new Double[limit]);
                    dataY = yQueue.toArray(new Double[limit]);
                    dataZ = zQueue.toArray(new Double[limit]);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
//                            for(int i=0;i<limit;i++){
//                               s+=","+data[i];
//                            }
//                            Log.d(TAG, ","+s);
//                            s = "";
                            dataX = IIRFilter.highpass(dataX, IIRFilter.TYPE_AMPITUDE);
                            dataX = IIRFilter.lowpass(dataX, IIRFilter.TYPE_AMPITUDE);

                            dataY = IIRFilter.highpass(dataY, IIRFilter.TYPE_AMPITUDE);
                            dataY = IIRFilter.lowpass(dataY, IIRFilter.TYPE_AMPITUDE);

                            dataZ = IIRFilter.highpass(dataZ, IIRFilter.TYPE_AMPITUDE);
                            dataZ = IIRFilter.lowpass(dataZ, IIRFilter.TYPE_AMPITUDE);
//                            for(int i=0;i<limit;i++){
//                                s+=","+dataZ[i];
//                            }
//                            Log.d(TAG, "after filter,"+s);
//                            s = "";
                            //3轴之后会很不稳定，spacenumber得设置多一点
                            Double[] cutDatax = Cut.cutMoutain2(dataX, 70, 35, 40);
                            Double[] cutDatay = Cut.cutMoutain2(dataY, 70, 35, 40);
                            Double[] cutDataz = Cut.cutMoutain2(dataZ, 70, 35, 40);
//                            for(int i=0;i<70;i++){
//                                s+=","+cutDataz[i];
//                            }
//                            Log.d(TAG, "after cut,"+s);
//                            s = "";
                            //如果是第一个敲击，记录下来，后面的敲击gcc以它对齐
                            Double[] allAmpData = new Double[ampLength*3+2];
                            if (knockCount == 1) {
                                System.arraycopy(cutDatax, 15, firstKnockX, 0, ampLength);
                                System.arraycopy(cutDatay, 15, firstKnockY, 0, ampLength);
                                System.arraycopy(cutDataz, 15, firstKnockZ, 0, ampLength+2);
                                //拼接三轴振动
                                System.arraycopy(firstKnockX,0,allAmpData,0,ampLength);
                                System.arraycopy(firstKnockY,0,allAmpData,ampLength,ampLength);
                                System.arraycopy(firstKnockZ,0,allAmpData,ampLength*2,ampLength+2);
                                } else {
                                Double[] gccDatax = GCC.gcc(firstKnockX, cutDatax);
                                Double[] gccDatay = GCC.gcc(firstKnockY, cutDatay);
                                Double[] gccDataz = GCC.gcc(firstKnockZ, cutDataz);
                                //拼接
                                System.arraycopy(gccDatax,0,allAmpData,0,ampLength);
                                System.arraycopy(gccDatay,0,allAmpData,ampLength,ampLength);
                                System.arraycopy(gccDataz,0,allAmpData,ampLength*2,ampLength+2);
                            }
                            //一起fft
                            Double[] fftData = FFT.getHalfFFTData(allAmpData);
                            System.arraycopy(allAmpData, 0, finalData, 0, ampLength*3+2);
                            System.arraycopy(fftData, 0, finalData, ampLength*3+2, finalLength/3);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    messageTextView.setText("please tap key" + (knockCount / 20 + 1));
                                }
                            });
                            PinCodeKnockData PinCodeKnockData = new PinCodeKnockData();
                            PinCodeKnockData.initData(finalData);
                            PinCodeKnockData.saveThrows();

                        }
                    }).start();
                    flag = true;
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //创建一个SensorManager来获取系统的传感器服务
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                , 10000);
    }

    @Override
    public void onPause() {
        sm.unregisterListener(this);
        handler.removeCallbacks(runnable);
        super.onPause();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
