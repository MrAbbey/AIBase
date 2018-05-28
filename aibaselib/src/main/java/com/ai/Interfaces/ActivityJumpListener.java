package com.ai.Interfaces;

/**
 * Created by song on 2017/6/7.
 * 用于插件页面回调跳转宿主工程的activity
 */

public interface ActivityJumpListener {
    public void jumpToAILocGesturePasswordActivity();//跳转到手势密码输入页面。
    public void jumpToAILocFingerprintActivity();//跳转到指纹验证页面。
}
