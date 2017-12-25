package com.ai.base;


import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import com.ai.Interfaces.ActivityJumpListener;
import com.ai.base.util.LogUtil;
import com.ai.base.util.PermissionUitls;

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
                    &&!(getClassName().equals("com.ai.aiportal.gesture.AIGesturePasswordActivity"))
                    &&mEnbleGesturePwd) {
                ActivityJumpListener activityJumpListener =  ActivityConfig.getInstance().getActivityJumpListener();
                if (activityJumpListener != null) //宿主app进入
                {
                    activityJumpListener.jumpToAILocGesturePasswordActivity();
                }else{
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("com.ai.aiportal", "com.ai.aiportal.gesture.AIGesturePasswordActivity"));
                    startActivity(intent);
                }
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

        if (AIActivityLifecycleListener.getInstance().getRefCount() == 0) {
            return true;
        }

        return false;

//        ActivityManager activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningAppProcessInfo> appProcessInfos = activityManager.getRunningAppProcesses();
//        // 枚举进程
//        for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessInfos) {
//            if (appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
//                if (appProcessInfo.processName.equals(this.getApplicationInfo().processName)) {
//                    LogUtil.d("song","EntryActivity isRunningForeGround");
//                    return true;
//                }
//            }
//        }
//        LogUtil.d("song", "EntryActivity isRunningBackGround");
//        return false;
    }

    @NonNull
    private String getClassName() {
        String contextString = this.toString();
        return contextString.substring(0, contextString.indexOf("@"));
    }

//    private String getClassName(){
//        ActivityManager manager = (ActivityManager)   getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningTaskInfo> runningTasks = manager .getRunningTasks(1);
//        ActivityManager.RunningTaskInfo cinfo = runningTasks.get(0);
//        ComponentName component = cinfo.topActivity;
//        String className = component.getClassName();
//        return className;
//    }
}
