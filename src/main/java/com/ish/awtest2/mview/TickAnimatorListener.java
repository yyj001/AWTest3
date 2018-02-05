package com.ish.awtest2.mview;

import com.ish.awtest2.mview.TickView;

/**
 *
 * @author ish
 * @date 2018/2/3
 * <p>
 */

public interface TickAnimatorListener {
    void onAnimationStart(TickView tickView);

    void onAnimationEnd(TickView tickView);

    abstract class TickAnimatorListenerAdapter implements TickAnimatorListener {
        @Override
        public void onAnimationStart(TickView tickView) {

        }

        @Override
        public void onAnimationEnd(TickView tickView) {

        }
    }
}
