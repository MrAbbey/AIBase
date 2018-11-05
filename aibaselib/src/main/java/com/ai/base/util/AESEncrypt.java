package com.ai.base.util;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by wuyoujian on 2017/5/5.
 */

public class AESEncrypt {

    private static String ivParameter = "aiaiaiaiaiaiaiai";

    // 加密
    public static String encrypt(String content ,String key) throws Exception {
        byte[] encrypted =   encrypt(content.getBytes("utf-8"),key);
        String encryString = Base64.encodeToString(encrypted,Base64.NO_WRAP);
        return encryString;
    }

    // 加密
    public static byte[] encrypt(byte[] bytes ,String key) throws Exception {

        if(key == null) {
            return null;
        }
        if(key.length() != 16) {
            return null;
        }
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] raw = key.getBytes("utf-8");
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        // 使用CBC模式，需要一个向量iv，可增加加密算法的强度,向量长度为16
        IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes("utf-8"));
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        byte[] encrypted = cipher.doFinal(bytes);
        return encrypted;
    }

    // 解密
    public static String decrypt(String encryptString,String key) throws Exception {
        byte[] bytes = encryptString.getBytes("utf-8");
        // 先用base64解密
        byte[] encrypted = Base64.decode(bytes,Base64.NO_WRAP);
        byte[] decrptedBytes = decrypt(encrypted,key);
        String originalString = new String(decrptedBytes, "utf-8");
        return originalString;
    }

    // 解密
    public static byte[] decrypt(byte[] bytes,String key) throws Exception {
        try {
            byte[] raw = key.getBytes("utf-8");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes("utf-8"));
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(bytes);
            return original;
        } catch (Exception ex) {
            return null;
        }
    }
}
