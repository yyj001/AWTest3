package com.ish.awtest2.func;

import android.util.Log;

import com.ish.awtest2.bean.KnockData;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ish on 2018/1/26.
 */

public class Trainer {
    public static double dentID(){
        List<KnockData> allDatas = DataSupport.findAll(KnockData.class);
        Double[][] myData = new Double[allDatas.size()][32];
        int i = 0;
        for(KnockData row : allDatas) {
            myData[i] = row.getAllData();
            i++;
        }
        Double[] weight = calPower(myData);
        double threshold = getThreshold(myData, weight);
        return threshold;
    }

    public static double getNewDis(Double[][] trainData, Double[] testData) {
        int trainSize = trainData.length; //30
        int colNum = trainData[0].length; //35
        Double[] weight = calPower(trainData);

        double testProportion = 3; //分3份
        int testSize = (int) (trainSize / testProportion); //10
        int kflod = (int) testProportion;  //3
        //每一行都单独拿出来和其它20行比较，结果放在trainDis里面
        Double[] testDis = new Double[kflod]; //3

        for (int k = 0; k < kflod; k++) {
            int testStartPos = k * testSize;
            //训练20条
            Double[][] trainSet = new Double[trainSize - testSize][colNum];
            for (int i = 0, x = 0; i < trainSize - testSize; i++, x++) {
                if (x == testStartPos) {
                    x += testSize;
                }
                for (int j = 0; j < colNum; j++) {
                    trainSet[i][j] = trainData[x][j];
                }
            }
            testDis[k] = calDis(trainSet, testData, weight);
        }
        Double distance = mean(testDis);
        return distance;
    }

    public static Double getThreshold(Double[][] trainData, Double[] weight) {
        int trainSize = trainData.length; //30
        int colNum = trainData[0].length; //35
        double testProportion = 3; //分3份
        int testSize = (int) (trainSize / testProportion); //10
        int kflod = (int) testProportion;  //3
        //每一行都单独拿出来和其它20行比较，结果放在trainDis里面
        Double[][] trainDis = new Double[kflod][testSize]; //3*10

        for (int k = 0; k < kflod; k++) {
            int testStartPos = k * testSize;
            //测试10条
            Double[][] testSet = new Double[testSize][colNum];
            for (int i = 0; i < testSize; i++) {
                for (int j = 0; j < colNum; j++) {
                    testSet[i][j] = trainData[testStartPos + i][j];
                }
            }
            //训练20条
            Double[][] trainSet = new Double[trainSize - testSize][colNum];
            for (int i = 0, x = 0; i < trainSize - testSize; i++, x++) {
                if (x == testStartPos) {
                    x += testSize;
                }
                for (int j = 0; j < colNum; j++) {
                    trainSet[i][j] = trainData[x][j];
                }
            }
            // 0 to 9
            for (int t = 0; t < testSize; t++) {
                trainDis[k][t] = calDis(trainSet, testSet[t], weight);
            }
        }
        //平均列
        Double[] meanDis = new Double[testSize];
        for (int i = 0; i < testSize; i++) {
            Double s = 0.0;
            for (int j = 0; j < kflod; j++) {
                s += trainDis[j][i];
            }
            meanDis[i] = s / kflod;
        }
        Arrays.sort(meanDis);
        int pos = (int) (meanDis.length - Math.ceil(testSize / 10.0));
        //int pos = meanDis.length/2;
        return meanDis[pos];
    }

    public static Double calDis(Double[][] trainSet, Double[] testRow, Double[] weight) {
        int feature_dim = testRow.length;//35
        Double[] temp = new Double[feature_dim];
        //遍历每一列
        for (int i = 0; i < feature_dim; i++) {
            //把trainSet的一列加上testRow对应的值
            Double[] newData = new Double[trainSet.length + 1];
            int j = 0;
            //20行
            for (; j < trainSet.length; j++) {
                newData[j] = trainSet[j][i];
            }
            newData[j] = testRow[i];
            temp[i] = mean(pdis(newData));
        }
        //temp 35列乘权重
        for (int i = 0; i < feature_dim; i++) {
            temp[i] = temp[i] * weight[i];
        }
        return mean(temp);
    }

    public static Double mean(Double[] array) {
        return sum(array) / array.length;
    }

    public static Double[] calPower(Double[][] trainData) {
        int colSize = trainData[0].length;
        int rowSize = trainData.length;

        Double[] d = new Double[colSize];

        for (int i = 0; i < colSize; i++) {
            Double[] data = new Double[rowSize];
            for (int j = 0; j < rowSize; j++) {
                data[j] = trainData[j][i];
            }
            d[i] = sum(pdis(data));
        }
        //get max element
        Double max = 0.0;
        for (int i = 0; i < colSize; i++) {
            if (d[i] > max) {
                max = d[i];
            }
        }
        //max - d[i]
        Double sumAll = 0.0;
        for (int i = 0; i < colSize; i++) {
            d[i] = max - d[i];
            sumAll += d[i];
        }
        //d[i]/sum
        for (int i = 0; i < colSize; i++) {
            d[i] = d[i] / sumAll;
        }
        return d;
    }

    public static Double sum(Double[] array) {
        Double s = 0.0;
        for (int i = 0; i < array.length; i++) {
            s += array[i];
        }
        return s;
    }

    public static Double[] pdis(Double[] data) {
        int num = data.length * (data.length - 1) / 2;
        ArrayList<Double> outputArray = new ArrayList<Double>();
        for (int i = 0; i < data.length; i++) {
            for (int j = i + 1; j < data.length; j++) {
                outputArray.add(Math.abs(data[i] - data[j]));
            }
        }
        Double[] output = (Double[]) outputArray.toArray(new Double[num]);
        return output;
    }

    public static Double findAbsMax(Double[] array){
        Double max= 0.0;
        for(int i=0;i<array.length;i++){
            if(Math.abs(array[i])>max){
                max = array[i];
            }
        }
        return max;
    }
}
