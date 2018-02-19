package com.ish.awtest2;

import android.app.Service;
import android.bluetooth.BluetoothClass;
import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
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

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.time.chrono.MinguoChronology;
import java.util.List;

import static android.media.AudioRecord.READ_NON_BLOCKING;


public class MainActivity extends WearableActivity implements SensorEventListener {

    //view
    private TextView mTextView;
    private TextView mTextViewCount;
    private Button btn;
    private Button btn2;
    private Button btn3;
    private Button btn4;
    private Button btn5;
    private Button btn6;

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

    private static final String TAG = "sensor";
    private int colNum = 3400;
    private String s = "";
    /**
     * 音频数据
     */
    private int frequency = 11025;
    private int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding) * 20;
    private short[] rawAudioData = new short[bufferSize];
    private AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
            frequency, channelConfiguration, audioEncoding, bufferSize);
    //如果不每隔一秒就把音频保存一下就会有问题
    private Runnable audioRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void run() {
            int bufferResultLength = audioRecord.read(rawAudioData, 0, bufferSize, READ_NON_BLOCKING);
            handler.postDelayed(this, 1000);
        }
    };

    //按钮判断开始

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (recLen < 30) {
                recLen++;
                mTextViewCount.setText("" + recLen);
                handler.postDelayed(this, 2000);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{android
                .Manifest.permission.RECORD_AUDIO}, 1);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        iniView();
        SQLiteDatabase db = Connector.getDatabase();
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER && flag) {
            double z = sensorEvent.values[2];
            double zChange = z - preValue;
            preValue = z;
            queue.offer(z);
            count++;
            mTextView.setText(z + "");
            //判断是否存了200个点
            if (!ifStart) {
                if (count == limit) {
                    ifStart = true;
                }
                //Log.d(TAG, "count: "+ count);
            }
            //等待敲击
            else {
                //遇到敲击
                if (zChange > deviation && !ifStart2) {
                    ifStart2 = true;
                    count = 0;
                    knockCount++;
//                    saveRawAudioData();
//                    Log.d(TAG, "click click click");
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
                    data = queue.toArray(new Double[limit]);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
//                            for(int i=0;i<limit;i++){
//                               s+=","+data[i];
//                            }
//                            Log.d(TAG, ","+s);
//                            s = "";
                            data = IIRFilter.highpass(data);
                            data = IIRFilter.lowpass(data);
//                            for(int i=0;i<limit;i++){
//                                s+=","+data[i];
//                            }
//                            Log.d(TAG, "after filter,"+s);
//                            s = "";
                            Double[] cutData = Cut.cutMoutain(data, 50, 40, 18,0.08);
                            //如果是第一个敲击，记录下来，后面的敲击gcc以它对齐
                            if (knockCount == 1) {
                                System.arraycopy(cutData, 10, firstKnock, 0, ampLength);
                                Double[] fftData = FFT.getHalfFFTData(firstKnock);
                                System.arraycopy(firstKnock, 0, finalData, 0, ampLength);
                                System.arraycopy(fftData, 0, finalData, ampLength, finalLength - ampLength);
                            } else {
                                Double[] gccData = GCC.gcc(firstKnock, cutData);
                                Double[] fftData = FFT.getHalfFFTData(gccData);
                                System.arraycopy(gccData, 0, finalData, 0, ampLength);
                                System.arraycopy(fftData, 0, finalData, ampLength, finalLength - ampLength);
                            }
                            KnockData knockData = new KnockData();
                            knockData.initialData(finalData);
                            knockData.saveThrows();

                        }
                    }).start();
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

    public void showDatabase() {
        List<KnockData> allDatas = DataSupport.findAll(KnockData.class);
        Log.d(TAG, "行数: " + allDatas.size());
        for (KnockData row : allDatas) {
            Double[] rowData = row.getAllData();
            String tempStr = "";
            for (int i = 0; i < rowData.length; i++) {
                tempStr += "," + rowData[i];
            }
            Log.d(TAG, "showDatabase: " + tempStr);
        }
    }

    public void deleteOneRow() {
        DataSupport.deleteAll(KnockData.class);
    }

    public void iniView() {
        btn = (Button) findViewById(R.id.btn);
        btn2 = (Button) findViewById(R.id.btn2);
        btn3 = (Button) findViewById(R.id.btn3);
        btn4 = (Button) findViewById(R.id.btn4);
        btn5 = (Button) findViewById(R.id.btn5);
        btn6 = (Button) findViewById(R.id.btn6);
        mTextView = (TextView) findViewById(R.id.text);
        mTextViewCount = (TextView) findViewById(R.id.text_count);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //停止
                if (flag) {
                    //关闭录音
                    handler.removeCallbacks(audioRunnable);
                    audioRecord.stop();

                    ifStart = false;
                    flag = false;
                    count = 0;
                    s = "";
                    handler.removeCallbacks(runnable);
                    mTextViewCount.setText("0");
                    btn.setText("开始");
                } else {
                    //开启录音
                    audioRecord.startRecording();
                    handler.postDelayed(audioRunnable, 1);
                    //倒计时
                    handler.postDelayed(runnable, 200);
                    recLen = -1;
                    flag = true;
                    btn.setText("停止");
                    knockCount = 0;
                }
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatabase();
                Toast.makeText(MainActivity.this, "输出成功", Toast.LENGTH_SHORT).show();
            }
        });
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteOneRow();
                Toast.makeText(MainActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
            }
        });
        //训练
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double threshold = Trainer.dentID();
                // get it
                Log.d(TAG, "onClick: threshold" + threshold);
                SharedPreferences p = getApplicationContext().getSharedPreferences("Myprefs",
                        Context.MODE_PRIVATE);
                p.edit().putFloat("threshold", (float) threshold).apply();
            }
        });
        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TestActivity.class));
            }
        });
        btn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Main2Activity.class));
            }
        });
    }

    public void saveRawAudioData() {
        int bufferResultLength = audioRecord.read(rawAudioData, 0, bufferSize);
        Log.d(TAG, "onClick: " + bufferResultLength);
        int temp = bufferResultLength / 40;
        for (int i = 0; i < 40; i++) {
            for (int j = 0; j < temp; j++) {
                s = s + "," + rawAudioData[j + temp * i];
            }
            Log.d(TAG, "radio: " + s);
            s = "";
        }
    }

    @Override
    public void onPause() {
        sm.unregisterListener(this);
        handler.removeCallbacks(audioRunnable);
        handler.removeCallbacks(runnable);
        audioRecord.stop();
        super.onPause();
    }
}