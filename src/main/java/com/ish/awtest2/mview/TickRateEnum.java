package com.ish.awtest2.mview;


/**
 * Created by ish on 2018/2/3.
 * <p>
 * 动画执行速率枚举配置
 */

enum TickRateEnum {

    SLOW(800, 480, 720),
    NORMAL(500, 300, 450),
    FAST(300, 180, 270);

    public static final int RATE_MODE_SLOW = 0;
    public static final int RATE_MODE_NORMAL = 1;
    public static final int RATE_MODE_FAST = 2;

    private int mRingAnimatorDuration;
    private int mCircleAnimatorDuration;
    private int mScaleAnimatorDuration;

    TickRateEnum(int mRingAnimatorDuration, int mCircleAnimatorDuration, int mScaleAnimatorDuration) {
        this.mRingAnimatorDuration = mRingAnimatorDuration;
        this.mCircleAnimatorDuration = mCircleAnimatorDuration;
        this.mScaleAnimatorDuration = mScaleAnimatorDuration;
    }

    public int getmRingAnimatorDuration() {
        return mRingAnimatorDuration;
    }

    public com.ish.awtest2.mview.TickRateEnum setmRingAnimatorDuration(int mRingAnimatorDuration) {
        this.mRingAnimatorDuration = mRingAnimatorDuration;
        return this;
    }

    public int getmCircleAnimatorDuration() {
        return mCircleAnimatorDuration;
    }

    public com.ish.awtest2.mview.TickRateEnum setmCircleAnimatorDuration(int mCircleAnimatorDuration) {
        this.mCircleAnimatorDuration = mCircleAnimatorDuration;
        return this;
    }

    public int getmScaleAnimatorDuration() {
        return mScaleAnimatorDuration;
    }

    public com.ish.awtest2.mview.TickRateEnum setmScaleAnimatorDuration(int mScaleAnimatorDuration) {
        this.mScaleAnimatorDuration = mScaleAnimatorDuration;
        return this;
    }

    public static com.ish.awtest2.mview.TickRateEnum getRateEnum(int rateMode) {
        com.ish.awtest2.mview.TickRateEnum tickRateEnum;
        switch (rateMode) {
            case RATE_MODE_SLOW:
                tickRateEnum = com.ish.awtest2.mview.TickRateEnum.SLOW;
                break;
            case RATE_MODE_NORMAL:
                tickRateEnum = com.ish.awtest2.mview.TickRateEnum.NORMAL;
                break;
            case RATE_MODE_FAST:
                tickRateEnum = com.ish.awtest2.mview.TickRateEnum.FAST;
                break;
            default:
                tickRateEnum = com.ish.awtest2.mview.TickRateEnum.NORMAL;
                break;
        }
        return tickRateEnum;
    }
}
