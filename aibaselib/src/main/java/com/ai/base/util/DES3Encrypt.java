package com.ai.base.util;

import android.util.Base64;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;

/**
 * author: wuyoujian
 * Date: 2018/11/26
 *  * 可以直接编译成java平台使用的代码，只需要替换Base64，采用java平台的即可
 */
public class DES3Encrypt {

    public static byte[] encrypt(byte[] bytes ,String key,String ivParameter) throws Exception {
        DESedeKeySpec spec = new DESedeKeySpec(key.getBytes("utf-8"));
        SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("desede");

        Key deskey  = keyfactory.generateSecret(spec);
        Cipher cipher = Cipher.getInstance("desede/CBC/PKCS5Padding");

        IvParameterSpec ips = new IvParameterSpec(ivParameter.getBytes("utf-8"));
        cipher.init(Cipher.ENCRYPT_MODE, deskey, ips);

        byte[] encryptData = cipher.doFinal(bytes);
        return encryptData;
    }

    // 加密
    public static String encrypt(String content ,String key,String ivParameter) throws Exception {
        byte[] encrypted =  encrypt(content.getBytes("utf-8"),key,ivParameter);
        String encryString = Base64.encodeToString(encrypted,Base64.NO_WRAP);
        return encryString;
    }


    public static byte[] decrypt(byte[] bytes,String key,String ivParameter) throws Exception {
        DESedeKeySpec spec = new DESedeKeySpec(key.getBytes("utf-8"));

        SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("desede");
        Key deskey = keyfactory.generateSecret(spec);
        Cipher cipher = Cipher.getInstance("desede/CBC/PKCS5Padding");

        IvParameterSpec ips = new IvParameterSpec(ivParameter.getBytes("utf-8"));
        cipher.init(Cipher.DECRYPT_MODE, deskey, ips);

        byte[] decryptData = cipher.doFinal(bytes);
        return decryptData;
    }

    // 解密
    public static String decrypt(String encryptString,String key,String ivParameter) throws Exception {
        byte[] bytes = encryptString.getBytes("utf-8");
        // 先用base64解密
        byte[] encrypted = Base64.decode(bytes,Base64.NO_WRAP);
        byte[] decrptedBytes = decrypt(encrypted,key,ivParameter);
        String originalString = new String(decrptedBytes, "utf-8");
        return originalString;
    }

    public static String string2Hex(String theStr) {
        int tmp;
        String tmpStr;
        byte[] bytes = theStr.getBytes();
        StringBuffer result = new StringBuffer(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            tmp = bytes[i];
            if (tmp < 0) {
                tmp += 256;
            }
            tmpStr = Integer.toHexString(tmp);
            if (tmpStr.length() == 1) {
                result.append('0');
            }
            result.append(tmpStr);
        }
        return result.toString();
    }
}
