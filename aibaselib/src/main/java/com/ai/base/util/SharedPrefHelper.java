package com.ai.base.util;

import android.content.ContextWrapper;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by song on 2017/6/12.
 */

public class SharedPrefHelper {
    private static SharedPrefHelper instance;
    private ContextWrapper context;
    public static SharedPrefHelper getInstance(ContextWrapper context) {
        if(instance == null) {
            Class var0 = LocalStorageManager.class;
            synchronized(LocalStorageManager.class) {
                instance = new SharedPrefHelper(context);
            }
        }

        return instance;
    }
    public SharedPrefHelper(ContextWrapper context) {
        this.context = context;
    }

    public final String get(String sharedName, String key) {
        SharedPreferences sharedata = this.context.getSharedPreferences(sharedName, 0);
        String data = sharedata.getString(key, "");
        return data;
    }

    public final String get(String sharedName, String key, String defValue) {
        SharedPreferences sharedata = this.context.getSharedPreferences(sharedName, 0);
        String data = sharedata.getString(key, defValue);
        return data;
    }

    public final Map<String, String> get(String sharedName, String[] keys) {
        SharedPreferences sharedata = this.context.getSharedPreferences(sharedName, 0);
        HashMap datas = new HashMap();
        String[] arr$ = keys;
        int len$ = keys.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            String key = arr$[i$];
            datas.put(key, sharedata.getString(key, ""));
        }

        return datas;
    }

    public final Map<?, ?> getAll(String sharedName) {
        SharedPreferences sharedata = this.context.getSharedPreferences(sharedName, 0);
        return sharedata.getAll();
    }

    public final void put(String sharedName, String key, Object value) {
        SharedPreferences.Editor shareEditor = this.context.getSharedPreferences(sharedName, 0).edit();
        this.put(shareEditor, key, value);
        shareEditor.commit();
    }

    public final void put(String sharedName, Map<?, ?> map) {
        SharedPreferences.Editor shareEditor = this.context.getSharedPreferences(sharedName, 0).edit();
        Iterator it = map.keySet().iterator();

        while(it.hasNext()) {
            String key = it.next().toString();
            this.put(shareEditor, key, map.get(key), false);
        }

        shareEditor.commit();
    }

    private final void put(SharedPreferences.Editor shareEditor, String key, Object value) {
        this.put(shareEditor, key, value, true);
    }

    private final void put(SharedPreferences.Editor shareEditor, String key, Object value, boolean bo) {
        if(value instanceof String) {
            shareEditor.putString(key, (String)value);
        } else if(value instanceof Integer) {
            shareEditor.putInt(key, ((Integer)value).intValue());
        } else if(value instanceof Boolean) {
            shareEditor.putBoolean(key, ((Boolean)value).booleanValue());
        } else if(value instanceof Long) {
            shareEditor.putLong(key, ((Long)value).longValue());
        } else if(value instanceof Float) {
            shareEditor.putFloat(key, ((Float)value).floatValue());
        }
// else if(bo) {
//            throw new MobileException("SharedPrefHelper not support stored object");
//        }

    }

    public final void remove(String sharedName, String key) {
        SharedPreferences.Editor shareEditor = this.context.getSharedPreferences(sharedName, 0).edit();
        shareEditor.remove(key);
        shareEditor.commit();
    }

    public final void remove(String sharedName, String[] keys) {
        SharedPreferences.Editor shareEditor = this.context.getSharedPreferences(sharedName, 0).edit();
        String[] arr$ = keys;
        int len$ = keys.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            String key = arr$[i$];
            shareEditor.remove(key);
        }

        shareEditor.commit();
    }

    public final void remove(String sharedName, JSONArray keys) throws JSONException {
        SharedPreferences.Editor shareEditor = this.context.getSharedPreferences(sharedName, 0).edit();
        int i = 0;

        for(int len = keys.length(); i < len; ++i) {
            String key = keys.getString(i);
            shareEditor.remove(key);
        }

        shareEditor.commit();
    }

    public final void clear(String sharedName) {
        SharedPreferences.Editor shareEditor = this.context.getSharedPreferences(sharedName, 0).edit();
        shareEditor.clear();
        shareEditor.commit();
    }
}
