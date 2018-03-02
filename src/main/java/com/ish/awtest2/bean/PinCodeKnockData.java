package com.ish.awtest2.bean;

import android.util.Log;

import org.litepal.crud.DataSupport;

/**
 * Created by ish on 2018/2/28.
 */

public class PinCodeKnockData extends DataSupport{
    private String xDataString;
    private String yDataString;
    private String zDataString;

    public void initData(Double[] xArray,Double[] yArray,Double[] zArray){
        xDataString = "";
        for (int i = 0; i < xArray.length-1; i++) {
            xDataString = xDataString + xArray[i] + ",";
        }
        xDataString = xDataString + xArray[xArray.length-1];

        yDataString = "";
        for (int i = 0; i < yArray.length-1; i++) {
            yDataString = yDataString + yArray[i] + ",";
        }
        yDataString = yDataString + yArray[yArray.length-1];

        zDataString = "";
        for (int i = 0; i < zArray.length-1; i++) {
            zDataString = zDataString + zArray[i] + ",";
        }
        zDataString = zDataString + zArray[zArray.length-1];
    }

    public Double[] getXArray(){
        String[] attrs = null;
        attrs = xDataString.split(",");
        Double array[] = new Double[attrs.length];
        for(int i=0;i<attrs.length;i++){
            array[i] = Double.valueOf(attrs[i]);
        }
        return array;
    }

    public Double[] getYArray(){
        String[] attrs = null;
        attrs = yDataString.split(",");
        Double array[] = new Double[attrs.length];
        for(int i=0;i<attrs.length;i++){
            array[i] = Double.valueOf(attrs[i]);
        }
        return array;
    }

    public Double[] getZArray(){
        String[] attrs = null;
        attrs = zDataString.split(",");
        Double array[] = new Double[attrs.length];
        for(int i=0;i<attrs.length;i++){
            array[i] = Double.valueOf(attrs[i]);
        }
        return array;
    }
}
