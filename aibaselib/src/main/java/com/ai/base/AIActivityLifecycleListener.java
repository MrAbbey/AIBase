package com.ai.base;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * Created by wuyoujian on 2017/12/6.
 */

public class AIActivityLifecycleListener implements Application.ActivityLifecycleCallbacks {

    private static AIActivityLifecycleListener instance;
    private int refCount = 0;

    public static AIActivityLifecycleListener getInstance() {
        if (instance == null) {
            synchronized (AIActivityLifecycleListener.class) {
                instance = new AIActivityLifecycleListener();
            }
        }
        return instance;
    }

    public int getRefCount() {
        return refCount;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        refCount++;
    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        refCount--;
        if(refCount == 0){
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }
}
