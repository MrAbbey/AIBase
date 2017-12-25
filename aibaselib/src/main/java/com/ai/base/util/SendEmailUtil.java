package com.ai.base.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.UnsupportedEncodingException;

/**
 * Created by dusk on 2017/8/9.
 */

class SendEmailUtil {

    private volatile static SendEmailUtil sSendEmailUtil;
    private final String TAG = "SendEmailUtil";

    private SendEmailUtil() {
    }

    public static SendEmailUtil getInstance() {
        if (sSendEmailUtil == null) {
            synchronized (SendEmailUtil.class) {
                if (sSendEmailUtil == null) {
                    sSendEmailUtil = new SendEmailUtil();
                }
            }
        }
        return sSendEmailUtil;
    }

    /**
     * 发送email
     *
     * @param toAddr   要发送的地址
     * @param fromAddr 来自的地址
     * @param server   服务器
     * @param user     用户名
     * @param password 密码
     * @param subject  邮件主题
     * @param body     邮件内容
     * @param filePath 附件路径
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     */
    /*public void sendEmail(String toAddr, String fromAddr, String server, String user, String password, String subject, String body, String filePath) throws MessagingException, UnsupportedEncodingException {

        Properties properties = System.getProperties();
        properties.put("mail.smtp.host", server);
        properties.put("mail.smtp.auth", "true");
        MyAuthenticator myAuthenticator = new MyAuthenticator(user, password);
        Session session = Session.getDefaultInstance(properties, myAuthenticator);

        MimeMessage mimeMessage = new MimeMessage(session);
        mimeMessage.setFrom(new InternetAddress(fromAddr));
        mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(toAddr));
        mimeMessage.setSubject(subject, "UTF-8");

        MimeMultipart mimeMultipart = new MimeMultipart("mixed");


        MimeBodyPart mimeBodyPart = null;
        if (filePath != null) {
            mimeBodyPart = new MimeBodyPart();
            FileDataSource source = new FileDataSource(filePath);
            mimeBodyPart.setDataHandler(new DataHandler(source));
            mimeBodyPart.setFileName(MimeUtility.encodeWord(source.getName()));
        }

        MimeBodyPart textBodyPart = new MimeBodyPart();
        textBodyPart.setText(body, "UTF-8");

        mimeMultipart.addBodyPart(mimeBodyPart);
        mimeMultipart.addBodyPart(textBodyPart);

        mimeMessage.setContent(mimeMultipart);
        mimeMessage.saveChanges();
        Transport.send(mimeMessage);
    }*/

    /**
     * 发送不带附件的纯文本邮件
     *
     * @param context 上下文
     * @param toAddrs 送达的邮件地址
     * @param subject 邮件主题
     * @param text    邮件内容
     */
    public void sendEmailWithoutAttachment(Context context, String[] toAddrs, String subject, String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_EMAIL, toAddrs);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        context.startActivity(Intent.createChooser(intent, "请选择要发送邮件的APP"));
    }

    /**
     * 发送带附件的邮件
     *
     * @param context  上下文
     * @param toAddrs  送达地址
     * @param subject  邮件主题
     * @param text     邮件内容
     * @param filePath 附件路径,相对路径,根目录已封装
     */
    public void sendEmailWithAttachment(Context context, String[] toAddrs, String subject, String text, String filePath) {
        //附件路径
        String path = Environment.getExternalStorageDirectory().getPath() + File.separator + filePath;
        //Log.e(TAG, "sendEmailWithAttachment: path" + path);
        File file = new File(path);
        //Log.e(TAG, "sendEmailWithAttachment: file" + file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/octet-stream");
        intent.putExtra(Intent.EXTRA_EMAIL, toAddrs);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        context.startActivity(Intent.createChooser(intent, "请选择要发送邮件的APP"));
    }

    /*class MyAuthenticator extends Authenticator {
        private String mUser;
        private String mPassword;

        public MyAuthenticator(String user, String password) {
            super();
            mUser = user;
            mPassword = password;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(mUser, mPassword);//super.getPasswordAuthentication();
        }
    }*/
}
