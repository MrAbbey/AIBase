package com.ai.base.loading;

import android.app.Application;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AILoadingViewBuilder {
    private Context context;
    private boolean backTouchable = true;
    private int outsideBackgroundColor = 0x00ffffff;//空白区域背景色
    private BackgroundelRelativeLayout backgroundelRelativeLayout;
    private ProgressBar progressBar;
    private TextView textView;
    private RoundFrameLayout contentBackground;

    public AILoadingViewListener loadingViewListener;
    public interface AILoadingViewListener {
        public void cancelLoading();
        public void startLoading();
        public void dismissLoading();
    }

    private static AILoadingViewBuilder instance;
    public static AILoadingViewBuilder getInstance() {
        if (instance == null) {
            synchronized (AILoadingViewBuilder.class) {
                instance = new AILoadingViewBuilder();
            }
        }
        return instance;
    }

    public void show(Context context, String text) {

        layoutLoadingView(context);

        this.backgroundelRelativeLayout = new BackgroundelRelativeLayout(context);
        this.backgroundelRelativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        this.backgroundelRelativeLayout.setGravity(Gravity.CENTER);
        this.backgroundelRelativeLayout.setBackgroundColor(this.outsideBackgroundColor);


        this.textView.setText(text);
        this.backgroundelRelativeLayout.addView(this.contentBackground);

        if (this.loadingViewListener != null) {
            this.loadingViewListener.startLoading();
        }

        //创建WindowManager
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams wlp = new WindowManager.LayoutParams();
        wlp.height = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.format = PixelFormat.RGBA_8888;
        if(!this.backTouchable) {
            wlp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;//不获得焦点
        }

        wm.addView(this.backgroundelRelativeLayout, wlp);
    }

    public void dismiss() {
        if (this.loadingViewListener != null) {
            this.loadingViewListener.dismissLoading();
        }

        try {
            if(this.backgroundelRelativeLayout == null)
                return ;
            WindowManager wm = (WindowManager) this.context.getSystemService(Context.WINDOW_SERVICE);
            wm.removeView(this.backgroundelRelativeLayout);
            this.backgroundelRelativeLayout = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void layoutLoadingView(Context context) {
        this.context = context;

        this.contentBackground = new RoundFrameLayout(context);
        FrameLayout.LayoutParams contentParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,120);
        this.contentBackground.setLayoutParams(contentParams);
        this.contentBackground.setBackgroundColor(0xCC000000);

        this.progressBar = new ProgressBar(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(100,100);
        params.setMargins(10,10,10,10);
        this.progressBar.setLayoutParams(params);
        this.contentBackground.addView(this.progressBar);

        this.textView = new TextView(context);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 120);
        layoutParams.setMarginStart(120);
        layoutParams.setMarginEnd(15);
        layoutParams.setMargins(120,0,10,0);
        this.textView.setTextColor(0xFFFFFFFF);
        this.textView.setTextSize(16);
        this.textView.setLayoutParams(layoutParams);
        this.textView.setGravity(Gravity.CENTER);
        this.textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        this.textView.setLines(1);
        this.contentBackground.addView(this.textView);
    }

    private class RoundFrameLayout extends FrameLayout {

        private float roundLayoutRadius = 5.0f;
        private Path roundPath;
        private RectF rectF;

        public RoundFrameLayout(Context context) {
            super(context);
            this.roundPath = new Path();
            this.rectF = new RectF();
        }

        private void setRoundPath() {
            //添加一个圆角矩形到path中, 如果要实现任意形状的View, 只需要手动添加path就行
            this.roundPath.addRoundRect(this.rectF, this.roundLayoutRadius, this.roundLayoutRadius, Path.Direction.CW);
        }

        public void setRoundLayoutRadius(float roundLayoutRadius) {
            this.roundLayoutRadius = roundLayoutRadius;
            setRoundPath();
            postInvalidate();
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
            this.rectF.set(0f, 0f, getMeasuredWidth(), getMeasuredHeight());
            setRoundPath();
        }

        @Override
        public void draw(Canvas canvas) {
            if (this.roundLayoutRadius > 0f) {
                canvas.clipPath(this.roundPath);
            }
            super.draw(canvas);
        }
    }


    private class BackgroundelRelativeLayout extends RelativeLayout {

        public BackgroundelRelativeLayout(Context context) {
            super(context);
        }
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
        }
        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            if(event.getKeyCode() == KeyEvent.KEYCODE_BACK )  {
                // 按硬件back取消
                dismiss();
                if (loadingViewListener != null) {
                    loadingViewListener.cancelLoading();
                }
            }
            return super.dispatchKeyEvent(event);
        }


//        // 点击外部也可以取消
//        @Override
//        public boolean onTouchEvent(MotionEvent event) {
//            if (event.getAction() == MotionEvent.ACTION_UP){
//                dismiss();
//            }
//            return true;
//        }
    }

}
