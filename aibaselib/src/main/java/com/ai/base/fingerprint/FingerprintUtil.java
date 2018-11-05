package com.ai.base.fingerprint;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by wuyoujian on 2018/3/12.
 */

public class FingerprintUtil extends Object {
    public static CancellationSignal cancellationSignal;
    private static int dialogImageId = 1000;
    private static int dialogTextViewId = 1001;
    private static int tryNumber = 3;
    private static boolean isCancel = false;

    public static void callFingerPrint(final Context context, final OnCallBackListenr listener) {

        tryNumber = 3;
        isCancel = false;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);

        RelativeLayout relativeLayout = new RelativeLayout(context);
        RelativeLayout.LayoutParams params0 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        relativeLayout.setLayoutParams(params0);
        relativeLayout.setBackgroundColor(Color.WHITE);

        ImageView imageView = new ImageView(context);
        imageView.setImageBitmap(FingerprintImage.getFingerprintBitmap());
        imageView.setId(dialogImageId);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
        params.setMargins(0,20,0,20);
        imageView.setLayoutParams(params);
        relativeLayout.addView(imageView);

        final TextView textView = new TextView(context);
        textView.setTextSize(16);
        textView.setText("请使用指纹验证");
        textView.setId(dialogTextViewId);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        params1.addRule(RelativeLayout.BELOW, dialogImageId);
        params1.setMargins(0,0,0,60);
        textView.setLayoutParams(params1);
        relativeLayout.addView(textView);


        Button cancelButton = new Button(context);
        cancelButton.setText("取消");
        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        params2.addRule(RelativeLayout.BELOW, dialogTextViewId);
        cancelButton.setLayoutParams(params2);
        cancelButton.setBackgroundColor(Color.WHITE);
        relativeLayout.addView(cancelButton);

        builder.setView(relativeLayout);
        final Dialog dialog = builder.create();
        dialog.show();

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                tryNumber = 3;
                isCancel = true;
                cancel();

                if (listener !=null) {
                    listener.onCancel();
                }
            }
        });


        FingerprintManagerCompat managerCompat = FingerprintManagerCompat.from(context);
        //判断硬件设备是否支持指纹
        if (!managerCompat.isHardwareDetected()) {
            if (listener != null) {
                listener.onHardwareUnSupport();
                textView.setText("手机不支持指纹");
                textView.setTextColor(Color.RED);
            }
            return;
        }


        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(context.KEYGUARD_SERVICE);
        //判断设备是否开启了指纹解锁能力
        if (!keyguardManager.isKeyguardSecure()) {
            if (listener != null) {
                listener.onInsecurity();
                textView.setText("手机未开放指纹解锁能力");
                textView.setTextColor(Color.RED);
            }
            return;
        }

        //判断设备是否已经录制了指纹
        if (!managerCompat.hasEnrolledFingerprints()) {
            if (listener != null) {
                listener.onEnrollFailed(); //未注册
                textView.setText("手机未录制指纹");
                textView.setTextColor(Color.RED);
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
                        if(!isCancel) {
                            Toast.makeText(context,"指纹验证异常，请等1-2分钟后重试",Toast.LENGTH_LONG).show();
                        }

                        cancel();
                        dialog.cancel();
                    }
                }

                // 当指纹验证失败的时候会回调此函数，
                // 失败之后允许多次尝试，失败次数过多会停止响应一段时间然后再停止sensor的工作
                @Override
                public void onAuthenticationFailed() {
                    if (listener != null) {

                        tryNumber --;
                        if (tryNumber <= 0) {
                            cancel();
                            dialog.cancel();
                            Toast.makeText(context,"指纹验证异常，请重新验证",Toast.LENGTH_LONG).show();
                            tryNumber = 3;
                        } else {
                            listener.onAuthenticationFailed();
                            textView.setText("指纹验证失败，请重试");
                            textView.setTextColor(Color.RED);
                        }
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
                        tryNumber = 3;
                        dialog.cancel();
                        cancel();

                        listener.onAuthenticationSucceeded(result);
                    }
                }
            }, null);
        }
    }

    public static void cancel() {
        if (cancellationSignal != null) {
            cancellationSignal.cancel();
        }
    }

    public  interface OnCallBackListenr {
        //判断硬件设备是否支持指纹
        void onHardwareUnSupport();
        //判断设备是否开启了指纹解锁能力
        void onInsecurity();
        //判断设备是否已经录制了指纹
        void onEnrollFailed();
        // 取消验证
        void onCancel();

        void onAuthenticationStart();
        void onAuthenticationError(int errMsgId, CharSequence errString);
        void onAuthenticationFailed();
        void onAuthenticationHelp(int helpMsgId, CharSequence helpString);
        void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result);
    }
}
