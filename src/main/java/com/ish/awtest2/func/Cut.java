package com.ish.awtest2.func;

import android.util.Log;

/**
 * Created by ish on 2018/1/24.
 */

public class Cut {
    //判断峰开始
    private static double deviation = 0.08;
    private static double deviation2 = 0.25;
    //从30开始遍历
    private static int startPoint = 40;
    //前面补18个点
    private static int spaceNumber = 18;
    //输出结果
    private static Double[] result = null;
    /**
     *
     * @param signal 信号
     * @param finalSize 返回长度
     * @param sPos 开始遍历的位置
     * @param sNumber 找到峰后在前面补的长度
     * @param d 变化量阈值，用来判断峰
     * @param d2 峰值，用来判断峰
     * @return 返回50个长度
     */
    public static Double[] cutMoutain(Double[] signal,int finalSize,int sPos,int sNumber,double d,double d2){
        startPoint = sPos;
        spaceNumber = sNumber;
        deviation = d;
        deviation2 = d2;
        result = new Double[finalSize];
        for(int i=startPoint;i<signal.length-finalSize;i++){
            if(Math.abs(signal[i])>deviation2 && Math.abs(signal[i]-signal[i-1])>deviation){
//            if(Math.abs(signal[i]-signal[i-1])>deviation){
                startPoint = i;
                break;
            }
        }
        System.arraycopy(signal, startPoint-spaceNumber, result, 0, finalSize);
        return result;
    }

    public static Double[] cutMoutain2(Double[] signal,int finalSize,int sPos,int sNumber){
        startPoint = sPos;
        spaceNumber = sNumber;
        result = new Double[finalSize];
        int maxStartPos=startPoint;
        double maxValue = 0,sumValue = 0;
        for(int i=startPoint;i<startPoint+finalSize-spaceNumber;i++){
            maxValue += Math.abs(signal[i]);
        }
        for(int i=startPoint+1;i<signal.length-(finalSize-spaceNumber);i++){
            sumValue = 0;
            //计算当前32个点的能量；
            for(int j=i;j<i+finalSize-spaceNumber;j++){
                sumValue+=Math.abs(signal[j]);
            }
            if(sumValue>maxValue){
                maxValue = sumValue;
                maxStartPos = i;
            }
        }
        Log.d("pso", "cutMoutain2: " + maxStartPos);
        System.arraycopy(signal,maxStartPos-spaceNumber,result,0,finalSize);
        return result;
    }
}
