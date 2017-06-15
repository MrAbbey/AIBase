package com.ai.base.SourceManager.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by baggio on 2017/6/12.
 */

public class MobileThreadManage {
    private HashMap<String, MobileThread> cache = new HashMap();
    private int cacheCount = 0;
    private static MobileThreadManage manage;

    private MobileThreadManage() {
    }

    public static MobileThreadManage getInstance() {
        if(manage == null) {
            manage = new MobileThreadManage();
        }

        return manage;
    }

    public synchronized String addThread(String threadName, MobileThread thread) {
        if(this.cache.get(threadName) != null) {
            threadName = threadName + this.cacheCount++;
        }

        this.cache.put(threadName, thread);
        return threadName;
    }

    public synchronized Thread getThread(String threadKey) {
        return (Thread)this.cache.get(threadKey);
    }

    public synchronized void remove(String threadName, String threadKey) {
        this.cache.remove(threadKey == null?threadName:threadKey);
    }

    public synchronized void destroy() {
        Iterator it = this.cache.entrySet().iterator();

        while(it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            if(((MobileThread)entry.getValue()).isAlive()) {
                ((MobileThread)entry.getValue()).interrupt();
            }
        }

        this.cache.clear();
    }
}
