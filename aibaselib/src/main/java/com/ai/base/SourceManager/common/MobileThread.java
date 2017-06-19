package com.ai.base.SourceManager.common;

import android.webkit.WebResourceResponse;

/**
 * Created by baggio on 2017/6/12.
 */

public abstract class MobileThread  extends Thread {
    private long waitoutTime;
    private String threadName;
    private Thread thread;
    private boolean flag;
    private static MobileThreadManage manage = MobileThreadManage.getInstance();

    public MobileThread(String threadName) {
        this.waitoutTime = -1L;
        this.flag = true;
        this.threadName = threadName;
    }

    public MobileThread(String threadName, long waitoutTime) {
        this(threadName);
        this.waitoutTime = waitoutTime;
    }

    public final void run() {
        String threadKey = null;

        try {
            if(this.waitoutTime > 0L) {
                this.thread = new Thread(new Runnable() {
                    public void run() {
                        try {
                            synchronized(this) {
                                this.wait(MobileThread.this.waitoutTime);
                            }

                            if(MobileThread.this.isAlive()) {
                                MobileThread.this.interrupt();
                                MobileThread.this.flag = false;
                            }
                        } catch (InterruptedException var4) {
                            ;
                        }

                    }
                });
            }

            threadKey = manage.addThread(this.threadName, this);
            if(this.thread != null) {
                this.thread.start();
            }

            this.execute();
            if(this.thread != null) {
                this.thread.interrupt();
            }
        } catch (Exception var6) {
            this.error(var6);
        } finally {
            manage.remove(this.threadName, threadKey);
            this.callback(this.flag);
        }

    }

    protected abstract WebResourceResponse execute() throws Exception;

    protected void callback(boolean flag) {
    }

    protected void error(Exception e) {
        e.printStackTrace();
    }

    public static void main(String[] args) {
    }
}
