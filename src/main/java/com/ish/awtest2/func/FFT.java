package com.ish.awtest2.func;

import com.ish.awtest2.bean.Complex;

/**
 * Created by ish on 2018/1/29.
 */

public class FFT {
    public static Complex[] changedLow(Complex[] a, int length){
        int mr=0;

        for(int m=1;m<length;++m){
            int l=length/2;
            while(mr+l>=length){
                l=l>>1;    //右移相当于,l除以2
            }
            mr=mr%l+l;
            if(mr>m){
                Complex t=new Complex();
                t=a[m];
                a[m]=a[mr];
                a[mr]=t;
            }
        }

        return a;
    }
    /**
    *乘积因子
    **/
    public static Complex complex_exp(Complex z){
        Complex r=new Complex();
        double expx=Math.exp(z.r);
        r.r=expx*Math.cos(z.i);
        r.i=expx*Math.sin(z.i);
        return r;
    }

    /**
    *基-2 fft蝶形变换
    *fft_tepy=1正变换, -1反变换
    **/
    public static Complex[] fft_2(Complex[] a,int length,int fft_tepy){
        double pisign=fft_tepy*Math.PI;
        // System.out.print(" pisign:"+pisign+"\n");
        Complex t=new Complex();
        int l=1;

        while(l<length){
            for(int m=0;m<l;++m){
                int temp_int=l*2; //左移相当于,l乘以2
                for(int i=m;temp_int<0?i>=(length-1):i<length;i+=temp_int){
                    Complex temp=new Complex(0.0,m*pisign/l);

                    Complex temp_exp=complex_exp(temp);
                    t.r=a[i+l].r*temp_exp.r-a[i+l].i*temp_exp.i;
                    t.i=a[i+l].r*temp_exp.i+a[i+l].i*temp_exp.r;

                    a[i+l].r=a[i].r-t.r;
                    a[i+l].i=a[i].i-t.i;
                    a[i].r=a[i].r+t.r;
                    a[i].i=a[i].i+t.i;

                } // end for i

            } // end for m
//            System.out.print("\n now is the loop and l="+l+"\n");
//            for(int c=0;c<length;c++){
//                System.out.print(a[c].r+"+j"+a[c].i+"\n");
//            }
            l=l*2;
        }//end while
        //左移相当于,l乘以2
        return a;
    }

    /**
     * Double数组直接得到fft结果，返回Double数组，长度为2的幂
     * @param signal
     * @return
     */
    public static Double[] getHalfFFTData(Double[] signal){
        int column = signal.length/2;
        Double[] output = new Double[column];
        //转复数
        Complex[] temp = new Complex[column*2];
        for(int i=0;i<column*2;i++){
            temp[i] = new Complex(signal[i]);
        }
        //fft
        temp = changedLow(temp,temp.length);
        temp = fft_2(temp,temp.length,1);
        //求模，转double
        for(int i=0;i<column;i++){
            output[i] = temp[i].getAbs();
        }
        //找最大归一化
        Double max = MyMath.findAbsMax(output);
        for(int i=0;i<column;i++){
            output[i] = output[i]/max;
        }
        return output;
    }
}

