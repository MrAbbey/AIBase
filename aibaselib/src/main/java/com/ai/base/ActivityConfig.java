package com.ai.base;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.ai.Interfaces.AIGesturePasswordListener;
import com.ai.Interfaces.ActivityJumpListener;

/**
 * Created by wuyoujian on 17/5/1.
 */

public class ActivityConfig {
    private Context mContext;
    private String kSharedPreferencesKey_AlreadyGesturePWD = "kSharedPreferencesKey_DeviceId";
    private String kSharedPreferencesKey_AlreadyFingerprint = "alreadyFingerprint";
    //private long kDurTime = 1*1000;
    private long kDurTime = 3*60*1000;
    private static ActivityConfig instance;
    // 保存最后一次调用onPause()的系统时间戳
    private long lockTime = 0;

    private String gestureAnswer;//手势密码
    private boolean testModelOn = false;
    private ActivityJumpListener activityJumpListener;// 在子类中实现，并且是宿主app中
    private AIGesturePasswordListener aiGesturePasswordListener;// 在子类中实现

    public AIGesturePasswordListener getAiGesturePasswordListener() {
        return aiGesturePasswordListener;
    }

    public ActivityJumpListener getActivityJumpListener() {
        return activityJumpListener;
    }

    public void setActivityJumpListener(ActivityJumpListener activityJumpListener) {
        this.activityJumpListener = activityJumpListener;
    }

    public void setAiGesturePasswordListener(AIGesturePasswordListener aiGesturePasswordListener) {
        this.aiGesturePasswordListener = aiGesturePasswordListener;
    }
    public String getGestureAnswer() {
        return gestureAnswer;
    }
    public void setTestModelOn(boolean testModelOn) {
        this.testModelOn = testModelOn;
    }

    public void setGestureAnswer(String gestureAnswer) {
        this.gestureAnswer = gestureAnswer;
    }

    public void setDurTime(long kDurTime) {
        this.kDurTime = kDurTime;
    }

    public void setLockTime(long lockTime) {
        this.lockTime = lockTime;
    }

    public long getLockTime() {
        return lockTime;
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    public void setAlreadyGesturePassword() {
        // 从本地读取
        SharedPreferences sharedPreferences= mContext.getSharedPreferences("ActivityConfig",
                Activity.MODE_PRIVATE);
        //实例化SharedPreferences.Editor对象
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String key = kSharedPreferencesKey_AlreadyGesturePWD;
        // 保存到本地
        editor.putBoolean(key,true);
        editor.commit();
    }

    public boolean isAlreadyGesturePassword() {
        SharedPreferences sharedPreferences= mContext.getSharedPreferences("ActivityConfig",
                Activity.MODE_PRIVATE);
        //实例化SharedPreferences.Editor对象
        boolean isAlready = sharedPreferences.getBoolean(kSharedPreferencesKey_AlreadyGesturePWD,false);
        if (testModelOn) isAlready = true;
        return isAlready;
    }

    public void clearAlreadyGesturePassword() {
        SharedPreferences sharedPreferences= mContext.getSharedPreferences("ActivityConfig",
                Activity.MODE_PRIVATE);
        //实例化SharedPreferences.Editor对象
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String key = kSharedPreferencesKey_AlreadyGesturePWD;
        editor.putBoolean(key,false);
        editor.commit();
    }


    public static ActivityConfig getInstance() {
        if (instance == null) {
            synchronized (ActivityConfig.class) {
                instance = new ActivityConfig();
            }
        }
        return instance;
    }

    public void saveLockTime() {
        lockTime = System.currentTimeMillis();
    }

    public boolean isShowGesturePasswordActivity() {
        long durTime = System.currentTimeMillis() - lockTime;

        // 需要结合是否有设置手势密码
        if (durTime <= kDurTime || !isAlreadyGesturePassword()) {
            return false;
        }
        return true;
    }


    /**
     * 设置指纹快捷登录
     */
    public void setAlreadyFingeprint() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("ActivityConfig", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String key = this.kSharedPreferencesKey_AlreadyFingerprint;
        editor.putBoolean(key, true);
        editor.commit();
    }

    /**
     * 判断是否已经设置了指纹快捷登录
     * @return
     */
    public boolean isAlreadyFingeprint() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("ActivityConfig", 0);
        boolean isAlready = sharedPreferences.getBoolean(this.kSharedPreferencesKey_AlreadyFingerprint, false);
        return isAlready;
    }

    /**
     * 清除已经设置的指纹登录开关
     */
    public void clearAlreadyFingeprint() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("ActivityConfig", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String key = this.kSharedPreferencesKey_AlreadyFingerprint;
        editor.putBoolean(key, false);
        editor.commit();
    }

    /**
     * APP打开后是否已经用指纹登录过
     */
    private boolean isAlreadyLogin = false;

    public boolean isAlreadyLogin() {
        return isAlreadyLogin;
    }

    public void setAlreadyLogin(boolean alreadyLogin) {
        isAlreadyLogin = alreadyLogin;
    }
    /**
     * 判断是否显示指纹验证页
     * @return
     */
    public boolean isShowFingerprintActivity() {
        long durTime = System.currentTimeMillis() - lockTime;

        // 需要结合是否有设置手势密码
        if (durTime <= kDurTime || !isAlreadyFingeprint()) {
            return false;
        }
        return true;
    }
}
