package com.ai.base.SourceManager.ui.progressDialog;

import android.content.Context;

/**
 * Created by baggio on 2017/6/13.
 */

public abstract class DialogBuilder<Type> {
    protected Context context;

    public DialogBuilder(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return this.context;
    }

    public abstract Type build();
}
