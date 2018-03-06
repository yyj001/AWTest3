package com.ish.awtest2.bean;

import org.litepal.crud.DataSupport;

/**
 * Created by ish on 2018/1/25.
 */

public class KnockData extends DataSupport {
    private String allDataString;
    public void initData(Double[] array){
        allDataString = "";
        for (int i = 0; i < array.length-1; i++) {
            allDataString = allDataString + array[i] + ",";
        }
        allDataString = allDataString + array[array.length-1];
    }

    public Double[] getArray(){
        String[] attrs = null;
        attrs = allDataString.split(",");
        Double[] array = new Double[attrs.length];
        for(int i=0;i<attrs.length;i++){
            array[i] = Double.valueOf(attrs[i]);
        }
        return array;
    }
}
