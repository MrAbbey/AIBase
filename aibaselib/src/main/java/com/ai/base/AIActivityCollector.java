package com.ai.base;

/**
 * Created by wuyoujian on 17/3/30.
 */

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

public class AIActivityCollector {

    private List<Activity> mActivities = new ArrayList<>();

    private static AIActivityCollector instance;
    public static AIActivityCollector getInstance(){
        if(instance == null){
            instance = new AIActivityCollector();
        }
        return instance;
    }

    public AIActivityCollector() {
        this.mActivities = new ArrayList<>();
    }

    public void addActivity(Activity activity) {
        mActivities.add(activity);
    }

    public void removeActivity(Activity activity) {
        mActivities.remove(activity);
    }

    public void finishAll() {
        for (Activity activity : mActivities) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
    }

    public  void backToRootActivity() {
        for (int i = 1; i < mActivities.size(); i ++) {
            Activity activity = mActivities.get(i);
            activity.finish();
        }
    }

    public Activity rootActivity() {
        return mActivities.get(0);
    }
}
