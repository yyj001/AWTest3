package com.ish.awtest2.func;

/**
 * Created by chenlin on 16/01/2018.
 */
public class IIRFilter {
    private static Double[] ha = {1.0, -0.982405793108396, 0.347665394851723};
    private static Double[] hb = {0.582517796990030, -1.16503559398006, 0.582517796990030};
    private static Double[] la = {1.0, 7.67794020539283, 25.7972195281712, 49.5412256377875, 59.4761319700396, 45.7087344779166, 21.9601201321160, 6.03017223524430, 0.724600926221649};
    private static Double[] lb = {0.851234941847226, 6.80987953477780, 23.8345783717223, 47.6691567434446, 59.5864459293058, 47.6691567434446, 23.8345783717223, 6.80987953477780, 0.851234941847226};

    private static Double[] in;
    private static Double[] out;
    private static Double[] outData;

    public static Double[] highpass(Double[] signal) {
        return filter(signal, ha, hb);
    }

    public static Double[] lowpass(Double[] signal) {
        return filter(signal, la, lb);
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
            double y = 0.0;
            for (int j = 0; j < b.length; j++) {
                if (in[j] != null) {
                    y += b[j] * in[j];
                }
            }


            for (int j = 0; j < a.length - 1; j++) {
                if(out[j]!=null){
                    y -= a[j + 1] * out[j];
                }
            }

            //shift the out array
            System.arraycopy(out, 0, out, 1, out.length - 1);
            out[0] = y;
            outData[i] = y;
        }
        return outData;
    }

}
