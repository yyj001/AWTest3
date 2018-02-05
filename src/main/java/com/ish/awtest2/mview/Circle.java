package com.ish.awtest2.mview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.ish.awtest2.R;

import java.util.jar.Attributes;

/**
 * Created by ish on 2018/2/2.
 */

public class Circle extends View {
    private Paint mPaint;
    private Drawable mDrawable;
    int[] mPoints = new int[8];

    public Circle(Context context) {
        this(context,null,0);
    }

    public Circle(Context context, AttributeSet attrs,int defStyleAttr){
        super(context,attrs,defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        //设置圆圈的外切矩形,radius是圆的半径，centerX，centerY是控件中心的坐标
//        mRectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
//
//        //设置打钩的几个点坐标（具体坐标点的位置不用怎么理会，自己定一个就好，没有统一的标准）
//        //画一个√，需要确定3个坐标点的位置
//        //所以这里我先用一个float数组来记录3个坐标点的位置，
//        //最后在onDraw()的时候使用canvas.drawLines(mPoints, mPaintTick)来画出来
//        //其中这里mPoint[0]~mPoint[3]是确定第一条线"\"的两个坐标点位置
//        //mPoint[4]~mPoint[7]是确定第二条线"/"的两个坐标点位置
//
//        mPoints[0] = centerX - tickRadius + tickRadiusOffset;
//        mPoints[1] = (float) centerY;
//        mPoints[2] = centerX - tickRadius / 2 + tickRadiusOffset;
//        mPoints[3] = centerY + tickRadius / 2;
//        mPoints[4] = centerX - tickRadius / 2 + tickRadiusOffset;
//        mPoints[5] = centerY + tickRadius / 2;
//        mPoints[6] = centerX + tickRadius * 2 / 4 + tickRadiusOffset;
//        mPoints[7] = centerY - tickRadius * 2 / 4;
    }
}
