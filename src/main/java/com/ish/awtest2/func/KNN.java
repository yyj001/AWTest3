package com.ish.awtest2.func;

public class KNN{
	public static int judgeDis(Double[][] trainData,Double[] testData){
		double sum =0;
		int testNum = 20;
		int dataRow = trainData.length;
		int dataCol = trainData[0].length;
		Double[][] diffMat = new Double[dataRow][dataCol];
		Double[] distanceMat = new Double[dataRow];
		for(int i=0;i<dataRow;i++){
			System.arraycopy(testData, 0, diffMat[i], 0, dataCol);
			sum = 0;
			for(int j=0;j<dataCol;j++){
				diffMat[i][j] = diffMat[i][j]-trainData[i][j];
				sum += Math.abs(diffMat[i][j]);
			}
			distanceMat[i] = sum;
		}
		//
		
		double min=distanceMat[0];
		int flag = 0;
		for(int i=0;i<dataRow;i++){
			if (distanceMat[i] <= min){
				min = distanceMat[i];
				flag = i;
			}
		}
		//int relustLabel = flag/testNum;
		int relustLabel = flag%testNum;
		return relustLabel+1;
//
	}
}