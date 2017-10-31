package com.ai.base.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by Dusk on 2017/8/9.
 */

public class SendSMSUtil {

    private static SendSMSUtil sSendSMSUtil;

    private SendSMSUtil() {
    }

    public static synchronized SendSMSUtil getInstance(){
        if (sSendSMSUtil == null) {
            sSendSMSUtil = new SendSMSUtil();
        }
        return sSendSMSUtil;
    }

    /**
     * 调用系统短信界面发送短信
     * @param context 上下文
     * @param phomeNumber 电话号码
     * @param text 发送信息内容
     */
    public void sendSMS(Context context, String phomeNumber, String text){

        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        smsIntent.setData(Uri.parse("smsto:"));
        smsIntent.setType("vnd.android-dir/mms-sms");//必须指定type
        smsIntent.putExtra("address", phomeNumber);//address字段不能改
        smsIntent.putExtra("sms_body", text);//sms_body 不能改
        context.startActivity(smsIntent);
    }
}
