package com.ish.awtest2.bean;

import android.util.Log;

import org.litepal.crud.DataSupport;

/**
 *
 * @author ish
 * @date 2018/2/20
 */

public class MyAudioData extends DataSupport {
    private String audioData;

    public void iniData(Double[] audioArray) {
        audioData = "";
        for (int i = 0; i < audioArray.length-1; i++) {
            audioData = audioData + audioArray[i] + ",";
        }
        audioData = audioData + audioArray[audioArray.length-1];
    }

    //字符串转数组
    public Double[] getAudioArray(){
        String[] attrs = null;
        attrs = audioData.split(",");
        Double aduioArray[] = new Double[attrs.length];
        for(int i=0;i<attrs.length;i++){
            aduioArray[i] = Double.valueOf(attrs[i]);
        }
        return aduioArray;
    }

}
