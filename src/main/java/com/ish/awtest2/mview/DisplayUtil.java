package com.ish.awtest2.mview;

import android.content.Context;

/**
 * Created by ish.
 * <p>
 */

class DisplayUtil {
    static int dp2px(Context context, float dpValue) {
        if (context == null) {
            return (int) (dpValue * 1.5f + 0.5f);
        }
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
