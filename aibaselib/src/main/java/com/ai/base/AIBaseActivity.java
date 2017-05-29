package com.ai.base;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by wuyoujian on 17/3/29.
 */

public abstract class AIBaseActivity extends AppCompatActivity {

    protected  boolean mEnbleGesturePwd = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //AIStatusBarCompat.compat(this, 0xFF000000);
        AIActivityCollector.getInstance().addActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ActivityConfig.getInstance().isShowGesturePasswordActivity()
                &&!this.getClass().getSimpleName().equalsIgnoreCase("AILocGesturePasswordActivity")
                &&mEnbleGesturePwd) {
            //startActivity(new Intent(this, AILocGesturePasswordActivity.class));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        ActivityConfig.getInstance().saveLockTime();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AIActivityCollector.getInstance().removeActivity(this);
    }


}
