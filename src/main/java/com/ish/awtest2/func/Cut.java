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
     * @param size 返回长度
     * @return 返回50个长度
     */
    public static Double[] cutMoutain(Double[] signal,int size){
        result = new Double[size];
        for(int i=startPoint;i<signal.length-size;i++){
            if(Math.abs(signal[i])>deviation2 && Math.abs(signal[i]-signal[i-1])>deviation){
                startPoint = i;
                break;
            }
        }
        System.arraycopy(signal, startPoint-spaceNumber, result, 0, size);
        return result;
    }
}
