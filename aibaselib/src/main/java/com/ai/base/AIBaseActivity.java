package com.ai.base;


import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ai.base.gesture.AILocGesturePasswordActivity;
import com.ai.base.util.LogUtil;
import com.ai.base.util.PermissionUitls;

import java.util.List;

/**
 * Created by wuyoujian on 17/3/29.
 */

public abstract class AIBaseActivity extends AppCompatActivity {

    protected  boolean mEnbleGesturePwd = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //AIStatusBarCompat.compat(this, 0xFF000000);
        AIActivityCollector.getInstance().addActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AIActivityCollector.getInstance().removeActivity(this);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PermissionUitls.getInstance();
        PermissionUitls.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //用来控制应用前后台切换的逻辑
    private boolean isCurrentRunningForeground = true;
    @Override
    public void onStart() {
        super.onStart();
        if (!isCurrentRunningForeground) {
            if (ActivityConfig.getInstance().isShowGesturePasswordActivity()
                    &&!(this instanceof AILocGesturePasswordActivity)
                    &&mEnbleGesturePwd) {
                startActivity(new Intent(this, AILocGesturePasswordActivity.class));

            }
            LogUtil.d("song", ">>>>>>>>>>>>>>>>>>>切到前台 activity process");
        }
    }
    @Override
    public void onStop() {
        super.onStop();
        isCurrentRunningForeground = isRunningForeground();
        if (!isCurrentRunningForeground) {
            ActivityConfig.getInstance().saveLockTime();
            LogUtil.d("song",">>>>>>>>>>>>>>>>>>>切到后台 activity process");
        }
    }

    public boolean isRunningForeground() {
        ActivityManager activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcessInfos = activityManager.getRunningAppProcesses();
        // 枚举进程
        for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessInfos) {
            if (appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                if (appProcessInfo.processName.equals(this.getApplicationInfo().processName)) {
                    LogUtil.d("song","EntryActivity isRunningForeGround");
                    return true;
                }
            }
        }
        LogUtil.d("song", "EntryActivity isRunningBackGround");
        return false;
    }

}
