package com.ai.base.gesture;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

/**
 * Created by wuyoujian on 17/4/27.
 */

public class AIGesturePasswordView  extends View {

    private static final String PWDVIEW_LOG_TAG = "AIGesturePasswordView";

    /**
     * AIGesturePasswordView的三种状态
     */
    enum FingerStatus {
        STATUS_NO_FINGER, STATUS_FINGER_ON, STATUS_FINGER_UP
    }

    /**
     * AIGesturePasswordView的当前状态
     */
    private FingerStatus mCurrentStatus = FingerStatus.STATUS_NO_FINGER;

    /**
     * 宽度
     */
    private int mWidth;
    /**
     * 高度
     */
    private int mHeight;
    /**
     * 外圆半径
     */
    private int mRadius;
    /**
     * 画笔的宽度
     */
    public int mStrokeWidth = 5;

    /**
     * 圆心坐标
     */
    private int mCenterX;
    private int mCenterY;
    private Paint mPaint;

    /**
     * 箭头（小三角最长边的一半长度 = mArrawRate * mWidth / 2 ）
     */
    private float mArrowRate = 0.2f;
    private int mArrowDegree = -1;
    private Path mArrowPath;
    /**
     * 内圆的半径 = mInnerCircleRadiusRate * mRadus
     *
     */
    private float mInnerCircleRadiusRate = 0.2F;

    /**
     * 四个颜色，可由用户自定义，初始化时由AIGesturePasswordLayout传入
     */
    private int mColorNoFingerInner;
    private int mColorNoFingerOutter;
    private int mColorFingerOn;
    private int mColorFingerUp;

    public AIGesturePasswordView(Context context , int colorNoFingerInner , int colorNoFingerOutter , int colorFingerOn , int colorFingerUp )
    {
        super(context);
        this.mColorNoFingerInner = colorNoFingerInner;
        this.mColorNoFingerOutter = colorNoFingerOutter;
        this.mColorFingerOn = colorFingerOn;
        this.mColorFingerUp = colorFingerUp;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mArrowPath = new Path();

    }

    @Override
    //当控件的父元素正要放置该控件时调用.
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);

        // 取长和宽中的小值
        mWidth = mWidth < mHeight ? mWidth : mHeight;
        mRadius = mCenterX = mCenterY = mWidth / 2;
        mRadius -= mStrokeWidth / 2;

        // 绘制三角形，初始时是个默认箭头朝上的一个等腰三角形
        // 用户绘制结束后，根据由相连两个AIGesturePasswordView决定需要旋转多少度
        float mArrowLength = mWidth / 2 * mArrowRate;
        mArrowPath.moveTo(mWidth / 2, mStrokeWidth + 2);
        mArrowPath.lineTo(mWidth / 2 - mArrowLength, mStrokeWidth + 2
                + mArrowLength);
        mArrowPath.lineTo(mWidth / 2 + mArrowLength, mStrokeWidth + 2
                + mArrowLength);
        mArrowPath.close();
        mArrowPath.setFillType(Path.FillType.WINDING);

    }

    @Override
    protected void onDraw(Canvas canvas)
    {

        switch (mCurrentStatus)
        {
            case STATUS_FINGER_UP:
                // 在手指抬起后，答案错误一定会进行绘制，如果答案正确那么再判断是否显示路径
                if (!isAnswerRight || showPath) {
                    // 绘制外圆
                    mPaint.setColor(mColorFingerUp);
                    mPaint.setStyle(Paint.Style.STROKE);
                    mPaint.setStrokeWidth(mStrokeWidth);
                    canvas.drawCircle(mCenterX, mCenterY, mRadius, mPaint);
                    // 绘制内圆
                    mPaint.setStyle(Paint.Style.FILL);
                    canvas.drawCircle(mCenterX, mCenterY, mRadius * mInnerCircleRadiusRate, mPaint);
                    // 绘制箭头
                    drawArrow(canvas);

                    break;
                }
            case STATUS_FINGER_ON:
                // 在画手势密码时判断是否显示路径
                if (showPath) {
                    // 绘制外圆
                    mPaint.setStyle(Paint.Style.STROKE);
                    mPaint.setColor(mColorFingerOn);
                    mPaint.setStrokeWidth(mStrokeWidth);
                    canvas.drawCircle(mCenterX, mCenterY, mRadius, mPaint);
                    // 绘制内圆
                    mPaint.setStyle(Paint.Style.FILL);
                    canvas.drawCircle(mCenterX, mCenterY, mRadius * mInnerCircleRadiusRate, mPaint);

                    break;
                }
            case STATUS_NO_FINGER:
                // 绘制外圆
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setColor(mColorNoFingerOutter);
                mPaint.setStrokeWidth(mStrokeWidth);
                canvas.drawCircle(mCenterX, mCenterY, mRadius, mPaint);
                // 绘制内圆
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setColor(mColorNoFingerInner);
                canvas.drawCircle(mCenterX, mCenterY, mRadius * mInnerCircleRadiusRate, mPaint);

                break;
        }

    }

    /**
     * 绘制箭头
     * @param canvas
     */
    private void drawArrow(Canvas canvas)
    {
        if (mArrowDegree != -1)
        {
            mPaint.setStyle(Paint.Style.FILL);

            canvas.save();
            canvas.rotate(mArrowDegree, mCenterX, mCenterY);
            canvas.drawPath(mArrowPath, mPaint);

            canvas.restore();
        }

    }

    /**
     * 设置当前模式并重绘界面
     *
     * @param fingerStatus
     */
    public void setFingerStatus(FingerStatus fingerStatus, boolean showPath)
    {
        this.mCurrentStatus = fingerStatus;
        this.showPath = showPath;
        invalidate();
    }

    //用于改变颜色
    public void setViewColor(int color) {
        mColorFingerUp = color;
    }

    public void setArrowDegree(int degree)
    {
        this.mArrowDegree = degree;
    }

    public int getArrowDegree()
    {
        return this.mArrowDegree;
    }

    // 用于设置是否绘制轨迹（箭头和内圆）
    private boolean showPath = true;// 默认显示

    // 用于标记答案是否正确
    private boolean isAnswerRight;
    public void setIsAnswerRight(boolean isRight) {
        isAnswerRight = isRight;
    }

}
