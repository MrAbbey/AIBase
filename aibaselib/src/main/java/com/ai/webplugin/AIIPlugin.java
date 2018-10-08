package com.ai.webplugin;

import android.content.Intent;

public interface AIIPlugin {
    void startActivityForResult(Intent intent, int requestCode);
    void onActivityResult(int requestCode, int resultCode, Intent data);
}
