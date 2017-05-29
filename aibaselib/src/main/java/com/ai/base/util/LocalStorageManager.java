package com.ai.base.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by wuyoujian on 2017/5/5.
 */

public class LocalStorageManager {

    private final static String sharedPreferencesName = "LocalStorageManager";
    private String mEncryptKey = "www.asiainfo.com";
    private Context mContext;
    private static LocalStorageManager instance;
    public static LocalStorageManager getInstance() {
        if (instance == null) {
            synchronized (LocalStorageManager.class) {
                instance = new LocalStorageManager();
            }
        }
        return instance;
    }

    public void setContext(Context context){
        mContext = context;
    }

    public void setEncryptKey(String encryptKey) {
        mEncryptKey = encryptKey;
    }

    public void setString(String key, String value) {

        SharedPreferences sharedPreferences= mContext.getSharedPreferences(sharedPreferencesName,
                mContext.MODE_PRIVATE);
        //实例化SharedPreferences.Editor对象
        SharedPreferences.Editor editor = sharedPreferences.edit();

        try {
            String encryptValue = AESEncrypt.encrypt(value, mEncryptKey);
            editor.putString(key,encryptValue);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getString(String key){
        SharedPreferences sharedPreferences= mContext.getSharedPreferences(sharedPreferencesName,
                mContext.MODE_PRIVATE);

        String encryptValue = sharedPreferences.getString(key,null);
        try {
            if (encryptValue != null) {
                String value = AESEncrypt.decrypt(encryptValue,mEncryptKey);
                return value;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void clear(){
        SharedPreferences sharedPreferences= mContext.getSharedPreferences(sharedPreferencesName,
                mContext.MODE_PRIVATE);
        //实例化SharedPreferences.Editor对象
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
    }

    public void remove(String key){
        SharedPreferences sharedPreferences= mContext.getSharedPreferences(sharedPreferencesName,
                mContext.MODE_PRIVATE);
        //实例化SharedPreferences.Editor对象
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
    }
}
