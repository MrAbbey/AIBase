package com.ai.base.SourceManager.ui.progressDialog;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by baggio on 2017/6/13.
 */

public class SimpleProgressDialog extends ProgressDialogBuilder<SimpleProgressDialog> {
    protected ProgressDialog progressDialog;

    public SimpleProgressDialog(Context context) {
        super(context);
        this.progressDialog = new ProgressDialog(context);
    }

    public ProgressDialog getProgressDialog() {
        return this.progressDialog;
    }
}
