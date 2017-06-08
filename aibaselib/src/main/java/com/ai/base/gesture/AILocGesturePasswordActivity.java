package com.ai.base.gesture;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ai.Interfaces.AIGesturePasswordListener;
import com.ai.base.ActivityConfig;
import com.ai.base.AIBaseActivity;
import com.ai.base.util.Utility;

import static android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM;
import static android.widget.RelativeLayout.ALIGN_PARENT_RIGHT;

/**
 * Created by wuyoujian on 17/4/27.
 */

public class AILocGesturePasswordActivity extends AIBaseActivity {

    private RelativeLayout mRelativeLayout;
    private TextView mTextView;
    private AIGesturePasswordLayout mGesturePasswordLayout;
    /**
     * 发送密码错误结果广播
     */
    public static final String kPasswordErrorBroadcast = "com.ai.base.passwordError.LOCAL_BROADCAST";

    private String mAnswer;
    /**
     * 最大尝试次数
     */
    private int mTryTimes = 3;
    private int mTryCount = mTryTimes;
    private Button mChangeButton;
    public void setTryTimes(int tryTimes) {
        mTryTimes = tryTimes;
        mTryCount = mTryTimes;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("手势密码");

        try {
            String answer = ActivityConfig.getInstance().getGestureAnswer();
            this.mAnswer = answer;
        } catch (Exception e) {
        }

        initView();
    }

    private void initView() {

        mRelativeLayout = new RelativeLayout(this);
        mRelativeLayout.setBackgroundColor(Color.WHITE);
        mRelativeLayout.setPadding(Utility.dip2px(this,16),Utility.dip2px(this,16),
                Utility.dip2px(this,16),Utility.dip2px(this,16));

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        mRelativeLayout.setLayoutParams(params);


        RelativeLayout.LayoutParams tvParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        tvParams.setMargins(0,Utility.dip2px(this,16),0,0);

        mTextView = new TextView(this);
        mTextView.setText("请绘制手势密码");
        mTextView.setTextColor(Color.BLACK);
        mTextView.setTextSize(16);
        mTextView.setGravity(Gravity.CENTER);
        mTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        mRelativeLayout.addView(mTextView,tvParams);

        mGesturePasswordLayout = new AIGesturePasswordLayout(this);
        mGesturePasswordLayout.setGravity(Gravity.CENTER_VERTICAL);
        mGesturePasswordLayout.setBackgroundColor(0x00ffffff);
        mGesturePasswordLayout.setOnGestureLockViewListener(mListener);
        //
        tvParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        mRelativeLayout.addView(mGesturePasswordLayout,tvParams);
        mChangeButton = new Button(this);
        tvParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        tvParams.addRule(ALIGN_PARENT_RIGHT);
        tvParams.addRule(ALIGN_PARENT_BOTTOM);
        mChangeButton.setBackgroundColor(Color.TRANSPARENT);
        mChangeButton.setText("切换到密码登录");
        mChangeButton.setTextColor(0xff3595ff);
        mChangeButton.setTextSize(16);
        mRelativeLayout.addView(mChangeButton,tvParams);
        mChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button btn = (Button)v;
                btn.setTextColor(Color.GRAY);
                AIGesturePasswordListener aiGesturePasswordListener = ActivityConfig.getInstance().getAiGesturePasswordListener();
                aiGesturePasswordListener.backLogin();
            }
        });
        setContentView(mRelativeLayout);
    }

    @Override
    public void onBackPressed() {
        // 阻止Lock页面的返回事件
        moveTaskToBack(true);
    }

    private void setCheckStatus(Boolean bSuc) {
        if(bSuc) {
            mTextView.setText("输入正确");
            mGesturePasswordLayout.setViewColor(true);
            finish();
        } else {

            mTextView.setText("输入不正确");
            mGesturePasswordLayout.setViewColor(false);
            mTryCount --;
        }
    }

    /**
     * 处理手势图案的输入结果
     * @param password
     */
    private void gestureEvent(String password) {

        if (password != null) {
            if (mTryCount >= 0 && mTryCount <=mTryTimes ) {
                if (password.length() < 4) {
                    mTextView.setText("需要四个点以上");
                    mGesturePasswordLayout.setViewColor(false);
                    return;
                } else {
                    boolean isPass = password.equalsIgnoreCase(mAnswer);
                    setCheckStatus(isPass);
                }
            } else {
                unmatchedExceedBoundary();
            }
        }
    }

    private LocalBroadcastManager localBroadcastManager;
    private void sendPasswordErrorBroadcast() {
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        Intent intent = new Intent(kPasswordErrorBroadcast);
        localBroadcastManager.sendBroadcast(intent);
    }


    /**
     * 处理输错次数超限的情况
     */
    private void unmatchedExceedBoundary() {
        // 正常情况这里需要做处理（如退出或重登）
        ActivityConfig.getInstance().clearAlreadyGesturePassword();
        Toast.makeText(this, "错误次数太多，请重新用密码登录", Toast.LENGTH_SHORT).show();

        sendPasswordErrorBroadcast();
        finish();
    }

    // 手势操作的回调监听
    private AIGesturePasswordLayout.OnGesturePasswordViewListener mListener = new
            AIGesturePasswordLayout.OnGesturePasswordViewListener() {
                @Override
                public void onGestureEvent(String password) {
                    gestureEvent(password);
                }
            };


}
