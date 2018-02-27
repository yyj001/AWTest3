package com.ish.awtest2.func;

import android.util.Log;

/**
 * Created by chenlin on 16/01/2018.
 */
public class IIRFilter {
    private static Double[] ampha = {1.0, -0.982405793108396, 0.347665394851723};
    private static Double[] amphb = {0.582517796990030, -1.16503559398006, 0.582517796990030};
    private static Double[] ampla = {1.0, 7.67794020539283, 25.7972195281712, 49.5412256377875, 59.4761319700396, 45.7087344779166, 21.9601201321160, 6.03017223524430, 0.724600926221649};
    private static Double[] amplb = {0.851234941847226, 6.80987953477780, 23.8345783717223, 47.6691567434446, 59.5864459293058, 47.6691567434446, 23.8345783717223, 6.80987953477780, 0.851234941847226};

    private static double rate = 0.0000000000001;
    private static Double[] audha = {1.0, -1.98388104166084, 0.984009917549517};
    private static Double[] audhb = {0.991972739802589, -1.98394547960518, 0.991972739802589};
    private static Double[] audla = {1.0, -7.70788136310100, 25.9976696515944, -50.1165141444194, 60.3936379704043, -46.5869431821325, 22.4646074399259, -6.19121485178314, 0.746638479607965};
    private static Double[] audlb = {3.76448872074775 * rate, 3.01159097659820 * rate * 10, 1.05405684180937 * rate * 100, 2.10811368361874 * rate * 100,
            2.63514210452342 * rate * 100, 2.10811368361874 * rate * 100, 1.05405684180937 * rate * 100, 3.01159097659820 * rate * 10, 3.76448872074775 * rate};

    private static Double[] in;
    private static Double[] out;
    private static Double[] outData;
    public static final int TYPE_AMPITUDE = 1;
    public static final int TYPE_AUDIO = 2;


    public static Double[] highpass(Double[] signal, int type) {
        switch (type) {
            case TYPE_AMPITUDE:
                return filter(signal, ampha, amphb);
            case TYPE_AUDIO:
                return filter(signal, audha, audhb);
            default:
                break;
        }
        return filter(signal, audha, audhb);
    }

    public static Double[] lowpass(Double[] signal, int type) {
        switch (type) {
            case TYPE_AMPITUDE:
                return filter(signal, ampla, amplb);
            case TYPE_AUDIO:
                return filter(signal, audla, audlb);
            default:
                break;
        }
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
