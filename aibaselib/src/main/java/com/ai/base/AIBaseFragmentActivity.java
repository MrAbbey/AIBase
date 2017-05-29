package com.ai.base;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * Created by wuyoujian on 17/4/24.
 */

public class AIBaseFragmentActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AIActivityCollector.getInstance().addActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AIActivityCollector.getInstance().removeActivity(this);
    }
}
