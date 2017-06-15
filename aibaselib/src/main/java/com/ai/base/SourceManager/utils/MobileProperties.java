package com.ai.base.SourceManager.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Created by baggio on 2017/6/12.
 */

public class MobileProperties extends Properties {
    private static final long serialVersionUID = 5250040804288728227L;

    public MobileProperties() {
    }

    public MobileProperties(String path) throws IOException {
        this.load(path);
    }

    public MobileProperties(InputStream in) {
        try {
            super.load(in);
        } catch (IOException var11) {
            var11.printStackTrace();
        } finally {
            if(in != null) {
                try {
                    in.close();
                } catch (IOException var10) {
                    var10.printStackTrace();
                }

                in = null;
            }

        }

    }

    public void load(String path) throws IOException {
        FileInputStream stream = null;

        try {
            stream = new FileInputStream(path);
            super.load(stream);
        } catch (Exception var7) {
            var7.printStackTrace();
        } finally {
            if(stream != null) {
                stream.close();
                stream = null;
            }

        }

    }

    public Map<String, ?> getProMap() {
        HashMap map = new HashMap();
        Iterator it = this.entrySet().iterator();

        while(it.hasNext()) {
            Entry entry = (Entry)it.next();
            map.put(entry.getKey().toString(), entry.getValue());
        }

        return map;
    }
}
