package com.ai.testapp.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;

public class TranslucencyView extends View {
    private  final Context mContext;

    public TranslucencyView(Context context){this(context,null);}
    public TranslucencyView(Context context, AttributeSet attributeSet){this(context,attributeSet,0);}
    public TranslucencyView(Context context, AttributeSet attributeSet, int defStyleAttr){
        super(context,attributeSet,defStyleAttr);
        this.mContext=context;
        initView();
    }
    private void initView(){
        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = canvas.getWidth();
        int height = canvas.getHeight();

        int margin = (int)(20);
        int imageWidth = (int)(width - margin*2);
        int imageHeight = (int)(imageWidth *(540/856f));
        int top = (height - imageHeight)/2;
        int left = margin;

        int sc = canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null, Canvas.ALL_SAVE_FLAG);
        // 创建画笔
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.GREEN);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(10.0f);
        p.setAntiAlias(true);
        canvas.drawRect(left,top,left + imageWidth,top +imageHeight,p);
        canvas.drawColor(Color.parseColor("#9f000000"));

        PorterDuffXfermode porterDuffXfermode=new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);
        p.setXfermode(porterDuffXfermode);
        p.setStyle(Paint.Style.FILL);
        canvas.drawRect(left,top,left + imageWidth,top +imageHeight,p);

        // 还原混合模式
        p.setXfermode(null);

        // 还原画布
        canvas.restoreToCount(sc);

    }
}
