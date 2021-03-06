package com.ai.base.certificateCamera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class AITranslucencyView extends View {
    private  final Context mContext;

    public AITranslucencyView(Context context){this(context,null);}
    public AITranslucencyView(Context context, AttributeSet attributeSet){this(context,attributeSet,0);}
    public AITranslucencyView(Context context, AttributeSet attributeSet, int defStyleAttr){
        super(context,attributeSet,defStyleAttr);
        this.mContext=context;
        setWillNotDraw(false);
    }

    public Rect getTransparencyRect() {
        int width = this.getWidth();
        int height = this.getHeight();

        int margin = (int)(20);
        int transparencyWidth = (int)(width - margin*2);
        int transparencyHeight = (int)(transparencyWidth *(540/856f));
        int top = (height - transparencyHeight)/2;
        int left = margin;


        Rect rect = new Rect();
        rect.left = left;
        rect.top = top;
        rect.right = left + transparencyWidth;
        rect.bottom = top + transparencyHeight;

        return rect;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Rect rect = getTransparencyRect();
        // 存储画布
        int sc = canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null, Canvas.ALL_SAVE_FLAG);
        // 创建画笔
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.GREEN);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(10.0f);
        p.setAntiAlias(true);
        canvas.drawRect(rect.left,rect.top,rect.right,rect.bottom,p);
        canvas.drawColor(Color.parseColor("#9f000000"));
        p.setColor(Color.WHITE);
        p.setStrokeWidth (2);//设置画笔宽度
        p.setAntiAlias(true); //指定是否使用抗锯齿功能，如果使用，会使绘图速度变慢
        p.setStyle(Paint.Style.FILL);//绘图样式，对于设文字和几何图形都有效
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(70);
        canvas.drawText("请把证件置于方框中",rect.centerX(),rect.top - 30,p);
        //canvas.drawText("点击屏幕对焦",rect.getCenterX(),rect.y+ 10 + rect.height + 80,p);

        PorterDuffXfermode porterDuffXfermode=new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);
        p.setXfermode(porterDuffXfermode);
        p.setStyle(Paint.Style.FILL);
        canvas.drawRect(rect.left,rect.top,rect.right,rect.bottom,p);

        // 还原混合模式
        p.setXfermode(null);
        // 还原画布
        canvas.restoreToCount(sc);
    }
}
