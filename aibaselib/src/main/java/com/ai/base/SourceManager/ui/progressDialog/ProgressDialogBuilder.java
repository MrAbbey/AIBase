package com.ai.base.SourceManager.ui.progressDialog;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;

/**
 * Created by baggio on 2017/6/13.
 */

public abstract class ProgressDialogBuilder<Type> extends DialogBuilder<ProgressDialog> {
    protected final int STYLE_SPINNER = 0;
    protected final int STYLE_HORIZONTAL = 1;
    protected int progressDialogStyle = 0;
    protected boolean cancelable = true;
    protected boolean canceledOnTouchOutside = false;
    protected Drawable icon;
    protected String title;
    protected String message;

    public ProgressDialogBuilder(Context context) {
        super(context);
    }

    public ProgressDialogBuilder<Type> setProgressStyle(int progressDialogStyle) {
        this.progressDialogStyle = progressDialogStyle;
        return this;
    }

    public ProgressDialogBuilder<Type> setCancelable(boolean cancelable) {
        this.cancelable = cancelable;
        return this;
    }

    public ProgressDialogBuilder<Type> setCanceledOnTouchOutside(boolean canceledOnTouchOutside) {
        this.canceledOnTouchOutside = canceledOnTouchOutside;
        return this;
    }

    public ProgressDialogBuilder<Type> setIcon(Drawable icon) {
        this.icon = icon;
        return this;
    }

    public ProgressDialogBuilder<Type> setTitle(String title) {
        this.title = title;
        return this;
    }

    public ProgressDialogBuilder<Type> setMessage(String message) {
        this.message = message;
        return this;
    }

    public abstract ProgressDialog getProgressDialog();

    public ProgressDialog build() {
        ProgressDialog progressDialog = this.getProgressDialog();
        progressDialog.setProgressStyle(this.progressDialogStyle);
        progressDialog.getWindow().setGravity(17);
        progressDialog.setCancelable(this.cancelable);
        progressDialog.setCanceledOnTouchOutside(this.canceledOnTouchOutside);
        if(this.icon != null) {
            progressDialog.setIcon(this.icon);
        }

        if(this.title != null) {
            progressDialog.setTitle(this.title);
        }

        if(this.message != null) {
            progressDialog.setMessage(this.message);
        }

        return progressDialog;
    }
}
