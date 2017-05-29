package com.ai.base.gesture;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuyoujian on 17/4/27.
 */

public class AIGesturePasswordLayout extends RelativeLayout {

    private static final String PWDLAYOUT_LOG_TAG = "AIGesturePasswordLayout";
    /**
     * 保存所有的AIGesturePasswordView
     */
    private AIGesturePasswordView[] mGesturePasswordViews;
    /**
     * 每个边上的AIGesturePasswordView的个数
     */
    public int mCount = 3;

    /**
     * 保存用户选中的AIGesturePasswordView的id, 编码从 0 到 mCount*mCount - 1
     */
    private List<Integer> mChoose = new ArrayList<Integer>();

    /**
     * 密码路径画笔
      */
    private Paint mPaint;

    /**
     * 画笔的宽度
     */
    public int mStrokeWidth = 5;

    // 圈/间距
    private int marginRate = 2;
    /**
     * 每个AIGesturePasswordView中间的间距 设置为：mGesturePasswordViewWidth * 25%
     */
    private int mMarginBetweenPasswordView = 30;
    /**
     * AIGesturePasswordView的边长 4 * mWidth / ( 5 * mCount + 1 )
     */
    private int mGesturePasswordViewWidth;

    /**
     * AIGesturePasswordView无手指触摸的状态下内圆的颜色
     */
    public int mNoFingerInnerCircleColor = 0x00000000;

    /**
     * AIGesturePasswordView无手指触摸的状态下外圆的颜色
     */
    public int mNoFingerOuterCircleColor = 0xff3595ff;
    /**
     * AIGesturePasswordView手指触摸的状态下内圆和外圆的颜色
     */
    public int mFingerOnColor = 0xFF378FC9;
    /**
     * AIGesturePasswordView手指抬起的状态下内圆和外圆的颜色
     */
    public int mFingerUpColor = 0xFFFF0000;
    /**
     * 错误状态下的颜色
     */
    public int mFingerWrongColor = 0xffff4444;


    /**
     * 宽度
     */
    private int mWidth;
    /**
     * 高度
     */
    private int mHeight;

    /**
     * 密码划过的路径
     */
    private Path mPath;
    /**
     * 指引线的开始位置x
     */
    private int mLastPathX;
    /**
     * 指引线的开始位置y
     */
    private int mLastPathY;
    /**
     * 指引线的结束位置
     */
    private Point mTmpTarget = new Point();

    /**
     * 回调接口
     */
    private OnGesturePasswordViewListener mOnGesturePasswordViewListener;

    public AIGesturePasswordLayout(Context context) {
        this(context,null,0);
    }

    public AIGesturePasswordLayout(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public AIGesturePasswordLayout(Context context, AttributeSet attrs, int defStyle)  {
        super(context, attrs, defStyle);

        // 初始化画笔
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(5);
        mPaint.setAlpha(255);
        mPath = new Path();
    }

    @Override
    // 主要画初始状态的UI
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)  {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);

        // 取小值者
        mHeight = mWidth = mWidth < mHeight ? mWidth : mHeight;

        // 初始化mGesturePasswordViews
        if (mGesturePasswordViews == null)  {
            mGesturePasswordViews = new AIGesturePasswordView[mCount * mCount];
            // 计算每个AIGesturePasswordView的宽度
            mGesturePasswordViewWidth = (int) (marginRate * mWidth * 1.0f / ((marginRate + 1) * mCount + 1));
            // 计算每个AIGesturePasswordView的间距
            mMarginBetweenPasswordView = mGesturePasswordViewWidth / marginRate;
            // 设置画笔的宽度为AIGesturePasswordView的内圆直径
            mPaint.setStrokeWidth(mStrokeWidth);

            for (int i = 0; i < mGesturePasswordViews.length; i++)  {
                //初始化每个AIGesturePasswordView
                mGesturePasswordViews[i] = new AIGesturePasswordView(getContext(),
                        mNoFingerInnerCircleColor, mNoFingerOuterCircleColor,
                        mFingerOnColor, mFingerUpColor);
                mGesturePasswordViews[i].setId(i + 1);
                //设置参数，主要是定位AIGesturePasswordView间的位置
                LayoutParams lockerParams = new LayoutParams(
                        mGesturePasswordViewWidth, mGesturePasswordViewWidth);

                // 不是每行的第一个，则设置位置为前一个的右边
                if (i % mCount != 0) {
                    lockerParams.addRule(RelativeLayout.RIGHT_OF,
                            mGesturePasswordViews[i - 1].getId());
                }
                // 从第二行开始，设置为上一行同一位置View的下面
                if (i > mCount - 1) {
                    lockerParams.addRule(RelativeLayout.BELOW,
                            mGesturePasswordViews[i - mCount].getId());
                }
                //设置右下左上的边距
                int rightMargin = mMarginBetweenPasswordView;
                int bottomMargin = mMarginBetweenPasswordView;
                int leftMagin = 0;
                int topMargin = 0;
                /**
                 * 每个View都有右外边距和底外边距 第一行的有上外边距 第一列的有左外边距
                 */
                if (i >= 0 && i < mCount) {
                    // 第一行
                    topMargin = mMarginBetweenPasswordView;
                }
                if (i % mCount == 0) {
                    // 第一列
                    leftMagin = mMarginBetweenPasswordView;
                }

                lockerParams.setMargins(leftMagin, topMargin, rightMargin,
                        bottomMargin);
                mGesturePasswordViews[i].setFingerStatus(AIGesturePasswordView.FingerStatus.STATUS_NO_FINGER, showPath);
                addView(mGesturePasswordViews[i], lockerParams);
            }

            Log.e(PWDLAYOUT_LOG_TAG, "mWidth = " + mWidth + " ,  mGestureViewWidth = "
                    + mGesturePasswordViewWidth + " , mMarginBetweenPasswordView = "
                    + mMarginBetweenPasswordView);

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)  {
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (action)
        {
            case MotionEvent.ACTION_DOWN:
                // 重置
                reset();
            case MotionEvent.ACTION_MOVE:
                // 初始化画笔为蓝色
                setViewColor(true);
                AIGesturePasswordView child = getChildIdByPos(x, y);

                if (child != null)
                {
                    int cId = child.getId();
                    if (!mChoose.contains(cId))
                    {
                        // 循环加入中间点
                        int subId = checkChoose(cId);
                        Log.e(PWDLAYOUT_LOG_TAG, "SubId:" +subId);
                        while (subId != -1) {
                            // 1、这部分代码和以下 2 部分基本一样，可以抽离出一个方法
                            mChoose.add(subId);
                            AIGesturePasswordView subChild = mGesturePasswordViews[subId - 1];
                            subChild.setFingerStatus(AIGesturePasswordView.FingerStatus.STATUS_FINGER_ON, showPath);
                            // 设置指引线的起点
                            mLastPathX = subChild.getLeft() / 2 + subChild.getRight() / 2;
                            mLastPathY = subChild.getTop() / 2 + subChild.getBottom() / 2;
                            // 非第一个，将两者使用线连上
                            mPath.lineTo(mLastPathX, mLastPathY);
                            // 继续循环
                            subId = checkChoose(cId);
                        }

                        // 2、中间点加入完成，继续加入当前选择的点
                        mChoose.add(cId);
                        child.setFingerStatus(AIGesturePasswordView.FingerStatus.STATUS_FINGER_ON, showPath);
                        // 设置指引线的起点
                        mLastPathX = child.getLeft() / 2 + child.getRight() / 2;
                        mLastPathY = child.getTop() / 2 + child.getBottom() / 2;

                        if (mChoose.size() == 1) {
                            // 当前添加为第一个
                            mPath.moveTo(mLastPathX, mLastPathY);
                        } else {
                            // 非第一个，将两者使用线连上
                            mPath.lineTo(mLastPathX, mLastPathY);
                        }
                    }
                }
                // 指引线的终点
                mTmpTarget.x = x;
                mTmpTarget.y = y;
                break;
            case MotionEvent.ACTION_UP:
                // 回调是否成功
                if (mOnGesturePasswordViewListener != null && mChoose.size() > 0) {
                    String answer = listToStr(mChoose);
                    mOnGesturePasswordViewListener.onGestureEvent(answer);
                }

                Log.e(PWDLAYOUT_LOG_TAG, "mChoose = " + mChoose);
                // 将终点设置位置为起点，即取消指引线
                mTmpTarget.x = mLastPathX;
                mTmpTarget.y = mLastPathY;

                // 改变子元素的状态为UP,默认,在未验证之前是作为对答案显示
                changeItemMode(true);

                // 计算每个元素中箭头需要旋转的角度
                for (int i = 0; i + 1 < mChoose.size(); i++)
                {
                    int childId = mChoose.get(i);
                    int nextChildId = mChoose.get(i + 1);

                    AIGesturePasswordView startChild = (AIGesturePasswordView) findViewById(childId);
                    AIGesturePasswordView nextChild = (AIGesturePasswordView) findViewById(nextChildId);

                    int dx = nextChild.getLeft() - startChild.getLeft();
                    int dy = nextChild.getTop() - startChild.getTop();
                    // 计算角度
                    int angle = (int) Math.toDegrees(Math.atan2(dy, dx)) + 90;
                    startChild.setArrowDegree(angle);
                }
                delayReset();// 调用
                break;

        }
        invalidate();
        return true;
    }

    private void changeItemMode(Boolean isRight)
    {
        for (AIGesturePasswordView gestureLockView : mGesturePasswordViews)
        {
            if (mChoose.contains(gestureLockView.getId()))
            {
                gestureLockView.setViewColor(mFingerUpColor);
                gestureLockView.setIsAnswerRight(isRight);
                gestureLockView.setFingerStatus(AIGesturePasswordView.FingerStatus.STATUS_FINGER_UP, showPath);
            }
        }
    }

    /**
     *
     * 做一些必要的重置
     */
    private void reset()
    {
        if (mHandler != null && mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
        }
        mChoose.clear();
        mPath.reset();
        for (AIGesturePasswordView gestureLockView : mGesturePasswordViews)
        {
            gestureLockView.setFingerStatus(AIGesturePasswordView.FingerStatus.STATUS_NO_FINGER, showPath);
            gestureLockView.setArrowDegree(-1);
        }
    }

    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            reset();
            invalidate();
        }
    };
    private void delayReset() {
        mHandler.postDelayed(mRunnable, 1000);
    }

    /**
     * 检查当前左边是否在child中
     * @param child
     * @param x
     * @param y
     * @return
     */
    private boolean checkPositionInChild(View child, int x, int y)
    {

        //设置了内边距，即x,y必须落入下GestureLockView的内部中间的小区域中，可以通过调整padding使得x,y落入范围不变大，或者不设置padding
        int padding = (int) (mGesturePasswordViewWidth * 0.15);

        if (x >= child.getLeft() + padding && x <= child.getRight() - padding
                && y >= child.getTop() + padding
                && y <= child.getBottom() - padding)
        {
            return true;
        }
        return false;
    }

    /**
     * 通过x,y获得落入的GestureLockView
     * @param x
     * @param y
     * @return
     */
    private AIGesturePasswordView getChildIdByPos(int x, int y)
    {
        for (AIGesturePasswordView gestureLockView : mGesturePasswordViews)
        {
            if (checkPositionInChild(gestureLockView, x, y))
            {
                return gestureLockView;
            }
        }

        return null;

    }

    /**
     * 设置回调接口
     *
     * @param listener
     */
    public void setOnGestureLockViewListener(OnGesturePasswordViewListener listener)
    {
        this.mOnGesturePasswordViewListener = listener;
    }


    public String getChooseStr() {
        return listToStr(mChoose);
    }

    // 处理手势密码的字符串转换
    private String listToStr(List<Integer> list) {
        String str = "";
        if (list == null || list.size() <= 0) {
            return str;
        }
        for (int i = 0; i < list.size(); i++) {
            str += list.get(i) + "";
        }
        return str;
    }

    @Override
    public void dispatchDraw(Canvas canvas)
    {
        super.dispatchDraw(canvas);
        if (!showPath) {
            return;
        }
        //绘制GestureLockView间的连线
        if (mPath != null)
        {
            canvas.drawPath(mPath, mPaint);
        }
        //绘制指引线
        if (mChoose.size() > 0)
        {
            if (mLastPathX != 0 && mLastPathY != 0)
                canvas.drawLine(mLastPathX, mLastPathY, mTmpTarget.x,
                        mTmpTarget.y, mPaint);
        }

    }

    public interface OnGesturePasswordViewListener
    {
        /**
         * 手势密码值
         *
         * @param password
         */
        void onGestureEvent(String password);
    }

    // 传入true，设为蓝色，传入false，设为红色
    public void setViewColor(boolean isOk) {
        if (isOk) {
            mFingerUpColor = mFingerOnColor;
        } else {
            mFingerUpColor = mFingerWrongColor;
        }
        mPaint.setColor(mFingerUpColor);
        mPaint.setAlpha(255);
    }
    // 默认显示轨迹
    private boolean showPath = true;


    // 对外公开的set方法,在设置密码的时候一定要显示轨迹
    public void setShowPath(boolean showPath) {
        this.showPath = showPath;
    }


    // n * n的阵列，首位从0起算，计算公式：cId = x + n*y + 1
    private int checkChoose(int cId) {
        if (mChoose == null || mChoose.size() < 1) {
            return -1;
        }
        int lastX, lastY;
        int nowX, nowY;
        int lastId = mChoose.get(mChoose.size() - 1);

        lastX = (lastId - 1) % mCount;
        lastY = (lastId - 1) / mCount;

        nowX = (cId - 1) % mCount;
        nowY = (cId - 1) / mCount;

        int signX = compare(lastX, nowX);
        int signY = compare(lastY, nowY);
        // 比较x轴y轴间距
        int copiesX = (nowX - lastX) * signX;
        int copiesY = (nowY - lastY) * signY;
        int copies = copiesX > copiesY ? copiesY : copiesX;

        if (copiesX == 1 || copiesY == 1) {
            return -1;
        }

        if (signX == 0 || signY == 0) {
            return lastX + signX + (lastY + signY) * mCount + 1;
        }

        if (copies > 1 && copiesX % copies == 0 && copiesY % copies == 0) {
            return lastX + copiesX / copies * signX
                    + (lastY + copiesY / copies * signY) * mCount + 1;
        }

        return -1;
    }

    private int compare(int last, int now) {
        if (now > last) {
            return 1;
        } else if (now < last) {
            return -1;
        } else {
            return 0;
        }
    }

}
