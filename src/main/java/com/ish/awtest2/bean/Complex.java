package com.ish.awtest2.bean;

/**
 * Created by ish on 2018/1/29.
 */

public class Complex {
    public double r,i;
    public Complex(){
    }
    public Complex(double r,double i){
        this.r=r; //实部
        this.i=i; //虚部
    }
    public Complex(Double r){
        this.r = r;
        this.i =0.0;
    }

    public Double getAbs(){
        return Math.sqrt(r*r+i*i);
    }
}
