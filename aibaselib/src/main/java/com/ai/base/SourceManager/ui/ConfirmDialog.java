package com.ai.base.SourceManager.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by baggio on 2017/6/12.
 */

public class ConfirmDialog extends AlertDialog.Builder {
    public ConfirmDialog(Context context, String title, String message, String okLabel, String cancelLabel, String midLabel) {
        super(context,AlertDialog.THEME_HOLO_DARK);
        if(message != null) {
            this.setMessage(message);
        }

        if(title != null) {
            this.setTitle(title);
        }

        this.setCancelable(false);
        if(okLabel == null) {
            okLabel ="确定";
        }
        this.setPositiveButton(okLabel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ConfirmDialog.this.okEvent();
                dialog.dismiss();
            }
        });
        if(cancelLabel == null) {
            cancelLabel ="取消";
        }

        this.setNegativeButton(cancelLabel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ConfirmDialog.this.cancelEvent();
                dialog.dismiss();
            }
        });
        if(midLabel != null) {
            this.setNeutralButton(midLabel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ConfirmDialog.this.midEvent();
                    dialog.dismiss();
                }
            });
        }

    }

    public ConfirmDialog(Context context, String title, String message, String okLabel, String cancelLabel) {
        this(context, title, message, okLabel, cancelLabel, (String)null);
    }

    public ConfirmDialog(Context context, String title, String message) {
        this(context, title, message, (String)null, (String)null);
    }

    protected void okEvent() {
    }

    protected void cancelEvent() {
    }

    protected void midEvent() {
    }
}
