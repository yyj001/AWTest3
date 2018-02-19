package com.ish.awtest2.func;

/**
 * Created by ish on 2018/2/19.
 * 数学工具类
 */

public class MyMath {
    /**
     *
     * @param array
     * @return 最大绝对值
     */
    public static Double findAbsMax(Double[] array){
        Double max= 0.0;
        for(int i=0;i<array.length;i++){
            if(Math.abs(array[i])>max){
                max = array[i];
            }
        }
        return max;
    }

    /**
     *
     * @param array
     * @return 数组平均
     */
    public static Double mean(Double[] array) {
        return sum(array) / array.length;
    }

    /**
     *
     * @param array
     * @return 数组求和
     */
    public static Double sum(Double[] array) {
        Double s = 0.0;
        for (int i = 0; i < array.length; i++) {
            s += array[i];
        }
        return s;
    }

    /**
     * 格式化浮点数，防止指数表示
     * @param value
     * @return
     */
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
