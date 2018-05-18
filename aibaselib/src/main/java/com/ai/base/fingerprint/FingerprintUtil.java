package com.ai.base.fingerprint;

import android.app.KeyguardManager;
import android.content.Context;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;

/**
 * Created by wuyoujian on 2018/3/12.
 */

public class FingerprintUtil extends Object {
    public static CancellationSignal cancellationSignal;

    public static void callFingerPrint(Context context, final OnCallBackListenr listener) {
        FingerprintManagerCompat managerCompat = FingerprintManagerCompat.from(context);
        //判断硬件设备是否支持指纹
        if (!managerCompat.isHardwareDetected()) {
            if (listener != null) {
                listener.onHardwareUnSupport();
            }
            return;
        }


        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(context.KEYGUARD_SERVICE);
        //判断设备是否开启了指纹解锁能力
        if (!keyguardManager.isKeyguardSecure()) {
            if (listener != null) {
                listener.onInsecurity();
            }
            return;
        }

        //判断设备是否已经录制了指纹
        if (!managerCompat.hasEnrolledFingerprints()) {
            if (listener != null) {
                listener.onEnrollFailed(); //未注册
            }
            return;
        }
        if (listener != null) {
            //开始指纹识别
            listener.onAuthenticationStart();
            //必须重新实例化，否则cancel 过一次就不能再使用了
            cancellationSignal = new CancellationSignal();

            managerCompat.authenticate(null, 0, cancellationSignal, new FingerprintManagerCompat.AuthenticationCallback() {
                // 当出现错误的时候回调此函数
                @Override
                public void onAuthenticationError(int errMsgId, CharSequence errString) {
                    if (listener != null) {
                        listener.onAuthenticationError(errMsgId, errString);
                    }
                }

                // 当指纹验证失败的时候会回调此函数，
                // 失败之后允许多次尝试，失败次数过多会停止响应一段时间然后再停止sensor的工作
                @Override
                public void onAuthenticationFailed() {
                    if (listener != null) {
                        listener.onAuthenticationFailed();
                    }
                }

                @Override
                public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                    if (listener != null) {
                        listener.onAuthenticationHelp(helpMsgId, helpString);
                    }
                }

                // 当验证的指纹成功时会回调此函数
                @Override
                public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                    if (listener != null) {
                        listener.onAuthenticationSucceeded(result);
                    }
                }
            }, null);
        }
    }

    public static void cancel() {
        if (cancellationSignal != null)
            cancellationSignal.cancel();
    }

    public  interface OnCallBackListenr {
        void onHardwareUnSupport();
        void onInsecurity();
        void onEnrollFailed();
        void onAuthenticationStart();
        void onAuthenticationError(int errMsgId, CharSequence errString);
        void onAuthenticationFailed();
        void onAuthenticationHelp(int helpMsgId, CharSequence helpString);
        void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result);
    }
}
