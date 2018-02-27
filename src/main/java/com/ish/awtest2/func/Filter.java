package com.ish.awtest2.func;

import android.util.Log;

/**
 * Created by chenlin on 16/01/2018.
 */
public class Filter {
    private static double rate = 0.000000001;
    private static Double[] audha = {1.0, -1.98388104166084, 0.984009917549517};
    private static Double[] audhb = {0.991972739802589, -1.98394547960518, 0.991972739802589};
    private static Double[] audla = {1.0,-7.12374475999005,22.2456381652698,-39.7699187284106,44.5164561572107,-31.9455283104170,14.3513710384153,-3.68998752786006,0.415714445797744};
    private static Double[] audlb = {1.87506157822703*rate,1.50004926258163*rate*10,5.25017241903569*rate*10,
            1.05003448380714*rate*100,1.31254310475892*rate*100,1.05003448380714*rate*100,5.25017241903569*rate*10,
            1.50004926258163*rate*10,1.87506157822703*rate};

    private static Double[] in;
    private static Double[] out;
    private static Double[] outData;

    public static Double[] highpass(Double[] signal) {
        return filter(signal, audha, audhb);
    }

    public static Double[] lowpass(Double[] signal) {
        return filter(signal, audla, audlb);
    }

    private static Double[] filter(Double[] signal, Double[] a, Double[] b) {
        in = new Double[b.length];
        out = new Double[a.length - 1];
        outData = new Double[signal.length];
        for (int i = 0; i < signal.length; i++) {

            System.arraycopy(in, 0, in, 1, in.length - 1);  //in[1]=in[0],in[2]=in[1]...
            in[0] = signal[i];

            //calculate y based on a and b coefficients
            //and in and out.
            Double y = 0.0;
            for (int j = 0; j < b.length; j++) {
                if (in[j] != null) {
                    y += b[j] * in[j];
                }
            }


            for (int j = 0; j < a.length - 1; j++) {
                if (out[j] != null) {
                    y -= a[j + 1] * out[j];
                }
            }

            //shift the out array
            System.arraycopy(out, 0, out, 1, out.length - 1);
            out[0] = y;
            //Log.d("radio", "i" + i + "length" + outData.length);
            outData[i] = y;
        }
        return outData;
    }

}
