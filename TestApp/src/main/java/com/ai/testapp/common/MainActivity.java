package com.ai.testapp.common;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ai.base.loading.AILoadingViewBuilder;
import com.ai.base.util.Utility;
import com.ai.testapp.R;
import com.ai.base.util.HttpUtil;
import com.ai.testapp.h5plugin.PortalActivity;
import com.qihoo360.replugin.RePlugin;
import com.qihoo360.replugin.model.PluginInfo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import okhttp3.Call;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private String pluginPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pluginPath = getFilesDir().getAbsolutePath() +"/plugins";

        Button button = (Button)findViewById(R.id.pluginButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //AILoadingViewBuilder.getInstance().show(MainActivity.this,"加载中...");
               // loadApk();

                Intent intent = new Intent(MainActivity.this, PortalActivity.class);
                startActivity(intent);
            }
        });

        String content = "mboss陕西移动订单中心&&&$$$$11144455";
        String md5 = Utility.md5(content);
        Log.d("md5",md5);
    }


    private void loadApk(){
        final String pluginName = "plugin.apk";
        String url = "http://10.174.61.149/" + pluginName;
        Toast.makeText(MainActivity.this,"开始下载插件...",Toast.LENGTH_SHORT).show();
        HttpUtil.sendOkHttpRequest(url,new okhttp3.Callback(){
            @Override
            public void onFailure(Call call, IOException e) {}

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                File file = new File(pluginPath);
                if (!file.exists()) {
                    file.mkdir();
                }


                final String pluginFile = pluginPath + File.separator  + "plugin.apk";
                File filePlugin = new File(pluginFile);
                if (filePlugin.exists()) {
                    RePlugin.uninstall(pluginFile);
                    filePlugin.delete();
                }
                byte[] data = response.body().bytes();

                final RandomAccessFile savedFile = new RandomAccessFile(filePlugin,"rw");
                savedFile.write(data);
                response.body().close();
                savedFile.close();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Toast.makeText(MainActivity.this,"开始加载插件...",Toast.LENGTH_SHORT).show();
                        PluginInfo inf =  RePlugin.install(pluginFile);
                        if (inf != null ) {
                            RePlugin.preload(inf);
                        }

                        String activityName = "com.ai.aibasetest.MainActivity";
                        RePlugin.startActivity(MainActivity.this, RePlugin.createIntent("crmapp",
                                activityName));
                    }
                });
            }
        });
    }
}
