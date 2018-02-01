package com.ish.awtest2.func;

/**
 * Created by ish on 2018/1/24.
 */

public class GCC {
    private static Double[] outputData ;
    private static Double[] moveSum ;
    private static int mountainSize ;
    private static int spaceSize ;
    //signal1的峰位置
    private static int s  ;
    /**
     * @param signal1  长度为32
     * @param signal2  长度为50
     * @return
     */
    public static Double[] gcc(Double[] signal1 , Double[] signal2){
        mountainSize = signal1.length;
        spaceSize = signal2.length - mountainSize;
        moveSum = new Double[spaceSize];
        outputData = new Double[mountainSize];
        //右移
        for(int i=0;i<spaceSize;i++){
            Double sum = 0.0;
            for(int j=i,k=0;k<mountainSize;k++,j++){
                sum+=signal1[k] * signal2[j];
            }
            moveSum[i] = sum;
        }
        //找出最大值
        Double max = 0.0;
        int move = 0;
        for(int i=0;i<spaceSize;i++){
            if(moveSum[i]>max){
                max = moveSum[i];
                move = i;
            }
        }
        System.arraycopy(signal2, move, outputData, 0, mountainSize);
        return outputData;
    }
}

