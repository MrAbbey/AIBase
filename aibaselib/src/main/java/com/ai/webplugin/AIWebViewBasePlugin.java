package com.ai.webplugin;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.util.Base64;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.Toast;
import com.ai.base.AIBaseActivity;
import com.ai.base.certificateCamera.AICertificateCameraActivity;
import com.ai.base.document.AIOpenDocumentController;
import com.ai.base.fingerprint.FingerprintUtil;
import com.ai.base.loading.AILoadingViewBuilder;
import com.ai.base.util.LocalStorageManager;
import com.ai.base.util.PermissionUitls;
import com.ai.base.util.Utility;
import com.ai.webplugin.config.GlobalCfg;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Created by wuyoujian on 17/3/30.
 */

public class AIWebViewBasePlugin implements AIIPlugin {

    private AIBaseActivity mActivity;
    private WebView mWebView;
    private final int REQUSETCODE_SELECT_PHOTOS = 10000;
    private final int REQUSETCODE_PHOTOGRAPH = 10001;
    private final int REQUSETCODE_TAKECERTIFICATE = 10002;
    private Uri photographImageUri;
    private Handler mHandler = new Handler();

    public AIWebViewBasePlugin(AIBaseActivity activity, WebView webView) {
        this.mActivity = activity;
        this.mWebView = webView;
    }

    public AIBaseActivity getActivity() {
        return mActivity;
    }

    public void setActivity(AIBaseActivity activity) {
        this.mActivity = activity;
    }

    public WebView getWebView() {
        return mWebView;
    }

    public void setWebView(WebView webView) {
        mWebView = webView;
    }

    public void excuteJavascript(String js, final ValueCallback<String> callback) {
        final String javascript = "javascript:" + js;
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                excuteJSInWebView(javascript, callback);
            }
        });
    }

    public void callback(String actionName, String param, final ValueCallback<String> callback) {
        param = Utility.encodeForJs(param);
        final String javascript = "window.WadeNAObj.callback(\'" + actionName + "\',\'" + param + "\')";
        excuteJavascript(javascript,callback);
    }

    private void excuteJSInWebView(final String javascript, final ValueCallback<String> callback) {
        if (mWebView != null) {
            int currentapiVersion = android.os.Build.VERSION.SDK_INT;
            if (currentapiVersion < android.os.Build.VERSION_CODES.LOLLIPOP) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadUrl(javascript);
                        callback.onReceiveValue("success");
                    }
                });
            } else {
                mWebView.evaluateJavascript(javascript, callback);
            }
        }
    }

    public void startActivityForResult(Intent intent, int requestCode) {
        getActivity().startActivityForResult(this,intent,requestCode);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            // 处理相册选择图片和拍照图片
            if (requestCode == REQUSETCODE_SELECT_PHOTOS || requestCode == REQUSETCODE_PHOTOGRAPH){
                handlePhoto(requestCode,data);
            } else if(requestCode == REQUSETCODE_TAKECERTIFICATE) {
                String path = data.getStringExtra(AICertificateCameraActivity.kImageSavePathKey);
                File file = new File(path);
                if (file.exists()){
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(file);
                        // 设置一个，每次装载信息的容器
                        byte[] buffer = new byte[1024];
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        // 开始读取数据
                        int len = 0;// 每次读取到的数据的长度
                        // len值为-1时，表示没有数据了
                        while ((len = fis.read(buffer)) != -1) {
                            // append方法往sb对象里面添加数据
                            outputStream.write(buffer, 0, len);
                        }
                        // 输出字符串
                        byte[] byteArray = outputStream.toByteArray();
                        if (byteArray.length <= 0) return;
                        final String base64Str = Base64.encodeToString(byteArray, Base64.NO_WRAP);
                        if (base64Str.length() <= 0 ) return;

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback("JN_TakeCertificate",base64Str,null);
                            }
                        });

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // private API BEGIN
    private void handlePhoto(int requestCode,Intent data) {
        Bitmap bitmap = null;
        String callbackName_tmp = "JN_SelectPhoto";
        switch (requestCode) {
            case REQUSETCODE_SELECT_PHOTOS:
                callbackName_tmp = "JN_SelectPhoto";
                // 判断手机系统版本号
                if (Build.VERSION.SDK_INT >= 19) {
                    // 4.4及以上系统使用这个方法处理图片
                    bitmap = handleImageOnKitKat(data);

                } else {
                    // 4.4以下系统使用这个方法处理图片
                    bitmap = handleImageBeforeKitKat(data);
                }
                break;
            case REQUSETCODE_PHOTOGRAPH:
                callbackName_tmp = "JN_Photograph";
                try {
                    // 将拍摄的照片显示出来
                    bitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(photographImageUri));
                } catch (Exception e) {
                }
                break;
            default:
        }

        // 缩放图片到30%
        Bitmap zoomBitmap = Utility.zoomImage(bitmap,bitmap.getWidth()*0.3,bitmap.getHeight()*0.3);
        if (zoomBitmap.getByteCount() <= 0) return;

        // 压缩图片到20%
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        zoomBitmap.compress(Bitmap.CompressFormat.PNG, 20, baos);
        // 二进制编码
        byte[] datas = baos.toByteArray();
        if (datas.length <= 0) return;
        final String base64Str = Base64.encodeToString(datas, Base64.NO_WRAP);
        if (base64Str.length() <= 0 ) return;


        final String callbackName = callbackName_tmp;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callback(callbackName,base64Str,null);
            }
        });
    }

    @TargetApi(19)
    private Bitmap handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(getActivity(), uri)) {
            // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())) {
                // 解析出数字格式的id
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是content类型的Uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // 如果是file类型的Uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        return bitmap;
    }

    private Bitmap handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        return bitmap;
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = getActivity().getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    // private API END

    ////////////////////////////////////////////////////////////////////////
    //
    // 扩展原生能力接口
    //
    ///////////////////////////////////////////////////////////////////////

    public void JN_Test(final String obj) {
        Log.d("JSONObject", obj);
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callback("JN_Test", obj, null);
            }
        });

    }

    public void JN_JSONObj(final JSONObject obj) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                try {
                    Toast.makeText(getActivity(), obj.toString(), Toast.LENGTH_LONG).show();
                } catch (Exception e) {

                }
            }
        });
    }

    // 退出程序
    public void JN_Quit(final String param) {
        // 创建构建器
        String msg = String.format("您确定要退出%s", param);
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        // 设置参数
        builder.setTitle("提示")
                .setMessage(msg)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //
                        //callback("JN_Quit","0",null);
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback("JN_Quit", "1", null);
                        System.exit(0);
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    // 分享链接到系统剪切板
    public void JN_Sharing(final String url) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // 从API11开始android推荐使用android.content.ClipboardManager
                ClipboardManager cm = (ClipboardManager) mActivity.getSystemService(mActivity.CLIPBOARD_SERVICE);
                // 将文本内容放到系统剪贴板里。
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB) {
                    cm.setText(url);
                } else {
                    ClipData data = ClipData.newPlainText("JN_Sharing", url);
                    cm.setPrimaryClip(data);
                }

                Toast.makeText(mActivity, "已帮您复制分享内容到剪切板中", Toast.LENGTH_LONG).show();
            }
        });

        callback("JN_Sharing", "0", null);
    }

    // 调用系统中可以打开对应文档的应用
    public void JN_OpenDocument(final String url) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GlobalCfg globalCfg = GlobalCfg.getInstance();
                String fileProvider = (String)globalCfg.attr(GlobalCfg.CONFIG_FIELD_FILEPROVIDER);
                if (fileProvider == null || fileProvider.length()== 0) {
                    try {
                        InputStream is = getActivity().getResources().getAssets().open("global.properties");
                        globalCfg.parseConfig(is);
                    } catch (Exception e) {

                    }

                    fileProvider =  (String)globalCfg.attr(GlobalCfg.CONFIG_FIELD_FILEPROVIDER);
                }
                AIOpenDocumentController.getInstance().openOnlineFileInContext(getActivity(), url, fileProvider);
            }
        });
    }

    // 自动更新
    public void JN_CheckVersion(final String versionConfigUrl) {
        AIWebViewPluginEngine.getInstance().checkUpdate(versionConfigUrl);
    }

    // 获取版本号
    public void JN_VersionNumber() {
        GlobalCfg globalCfg = GlobalCfg.getInstance();
        String versionNumber =  (String)globalCfg.attr(GlobalCfg.CONFIG_FIELD_VERSION);
        if (versionNumber == null || versionNumber.length()== 0) {
            try {
                InputStream is = getActivity().getResources().getAssets().open("global.properties");
                globalCfg.parseConfig(is);
            } catch (Exception e) {

            }

            versionNumber =  (String)globalCfg.attr(GlobalCfg.CONFIG_FIELD_VERSION);
        }

        callback("JN_VersionNumber",versionNumber,null);
    }

    // 启动loading
    public void JN_ShowLoading(final String text) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AILoadingViewBuilder.getInstance().show(getActivity(),text);
            }
        });
    }

    // 退出loading
    public void JN_DismissLoading() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AILoadingViewBuilder.getInstance().dismiss();
            }
        });
    }

    // 自消失提示语
    public void JN_ShowMessage(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(),message,Toast.LENGTH_LONG).show();
            }
        });
    }

    // 保存数据
    public void JN_SetValueWithKey(final JSONArray array) {
        if (array != null && array.length() >= 2) {
            try {
                LocalStorageManager.getInstance().setContext(getActivity());
                LocalStorageManager.getInstance().setString(array.getString(0),array.getString(1));
            } catch (JSONException e) {

            }
        }
    }

    // 获取已存储的数据
    public void JN_GetValueWithKey(final String key) {
        LocalStorageManager.getInstance().setContext(getActivity());
        String value = LocalStorageManager.getInstance().getString(key);
        callback("JN_GetValueWithKey",value,null);
    }

    // 指纹验证
    public void JN_Fingerprint() {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FingerprintUtil.callFingerPrint(getActivity(), new FingerprintUtil.OnCallBackListenr() {
                    @Override
                    public void onHardwareUnSupport() {
                        callback("JN_Fingerprint","手机不支持指纹",null);
                    }

                    @Override
                    public void onInsecurity() {
                        callback("JN_Fingerprint","未给应用开放指纹",null);
                    }

                    @Override
                    public void onEnrollFailed() {
                        callback("JN_Fingerprint","手机未录制指纹",null);
                    }

                    @Override
                    public void onCancel() {
                        callback("JN_Fingerprint","cancel",null);
                    }

                    @Override
                    public void onAuthenticationStart() {

                    }

                    @Override
                    public void onAuthenticationError(int errMsgId, CharSequence errString) {
                        callback("JN_Fingerprint","failed",null);
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        callback("JN_Fingerprint","failed",null);
                    }

                    @Override
                    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {

                    }

                    @Override
                    public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                        callback("JN_Fingerprint","success",null);
                    }
                });
            }
        });

    }

    // 调动系统短信应用发送短信 -- 不需要权限
    public void JN_SMS(final JSONArray array) {
        //第一个参数短信接收号码，第二参数是一个可选参数,发送的内容
        if (array != null && array.length() >= 1) {
            try {
                final String phoneNumber = array.getString(0);
                String temp = "";
                if (array.length() >= 2) {
                    temp = array.getString(1);
                }

                final String content = temp;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Uri uri = Uri.parse("smsto:"+phoneNumber);
                        Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                        intent.putExtra("sms_body", content);
                        getActivity().startActivity(intent);
                    }
                });
            } catch (Exception e) {}
        }
    }

    // 调动系统打电话应用，拨打电话 -- 不需要权限
    public void JN_Telephone(final String phoneNumber) {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:"+phoneNumber));
                    getActivity().startActivity(intent);
                }
            });
        } catch (Exception e) {}
    }

    // 调动系统里的可以用于发邮件的邮箱应用列表 -- 不需要权限
    public void JN_Email(final JSONArray array) {
        //第一个参数收件人邮箱号
        //第二个参数邮件主题，可选参数
        //第三个邮件正文，可选参数
        if (array != null && array.length() >= 1) {
            try {
                final String mailAdress = array.getString(0);

                String temp = "";
                if (array.length() >= 2) {
                    temp = array.getString(1);
                }
                final String subject = temp;

                if (array.length() >= 3) {
                    temp = array.getString(2);
                }
                final String content = temp;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Uri uri = Uri.parse("mailto:"+mailAdress);
                        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                        //intent.putExtra(Intent.EXTRA_CC, copyto); // 抄送人
                        intent.putExtra(Intent.EXTRA_SUBJECT, subject); // 主题
                        intent.putExtra(Intent.EXTRA_TEXT, content); // 正文
                        getActivity().startActivity(Intent.createChooser(intent, "请选择邮件类应用"));
                    }
                });
            } catch (Exception e) {}
        }
    }

    // 打开系统的浏览器应用 -- 不需要权限
    public void JN_Brower(final String urlString) {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(urlString));
                    getActivity().startActivity(intent);
                }
            });
        } catch (Exception e) {}
    }

    // 打开系统的相册选择图片- 需要存储权限
    public void JN_SelectPhoto() {

        final int permissionCode = PermissionUitls.PERMISSION_STORAGE_CODE;
        PermissionUitls.mContext = getActivity() ;
        final String checkPermissinos [] = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE};

        PermissionUitls.PermissionListener permissionListener = new PermissionUitls.PermissionListener() {
            @Override
            public void permissionAgree() {
                switch (permissionCode) {
                    case PermissionUitls.PERMISSION_STORAGE_CODE : {
                        try {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(Intent.ACTION_PICK);
                                    intent.setType("image/*");//相片类型
                                    startActivityForResult(intent,REQUSETCODE_SELECT_PHOTOS);
                                }
                            });
                        } catch (Exception e) {

                        }
                        break;
                    }
                }
            }

            @Override
            public void permissionReject() {
                Toast.makeText(getActivity(),"请授予操作手机存储的权限！",Toast.LENGTH_SHORT).show();
            }
        };
        PermissionUitls permissionUitls = PermissionUitls.getInstance(null, permissionListener);
        permissionUitls.permssionCheck(permissionCode,checkPermissinos);
    }

    // 打开系统相机拍照 -- 需要相机权限
    public void JN_Photograph() {

        final int permissionCode = PermissionUitls.PERMISSION_CAMERA_CODE;
        PermissionUitls.mContext = getActivity() ;
        final String checkPermissinos [] = {
                Manifest.permission.CAMERA};

        PermissionUitls.PermissionListener permissionListener = new PermissionUitls.PermissionListener() {
            @Override
            public void permissionAgree() {
                switch (permissionCode) {
                    case PermissionUitls.PERMISSION_CAMERA_CODE : {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 创建File对象，用于存储拍照后的图片
                                File outputImage = new File(getActivity().getExternalCacheDir(), "JN_Photograph.png");
                                try {
                                    if (outputImage.exists()) {
                                        outputImage.delete();
                                    }
                                    outputImage.createNewFile();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                if (Build.VERSION.SDK_INT < 24) {
                                    photographImageUri = Uri.fromFile(outputImage);
                                } else {
                                    GlobalCfg globalCfg = GlobalCfg.getInstance();
                                    String fileprovider =  (String)globalCfg.attr(GlobalCfg.CONFIG_FIELD_FILEPROVIDER);
                                    if (fileprovider == null) {
                                        Toast.makeText(mActivity,"请在manifest中配置FileProvider",Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                    photographImageUri = FileProvider.getUriForFile(getActivity(), fileprovider, outputImage);
                                }
                                // 启动相机程序
                                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, photographImageUri);
                                startActivityForResult(intent, REQUSETCODE_PHOTOGRAPH);
                            }
                        });
                    }
                }
            }

            @Override
            public void permissionReject() {
                Toast.makeText(getActivity(),"请授予操作手机存储的权限！",Toast.LENGTH_SHORT).show();
            }
        };
        PermissionUitls permissionUitls = PermissionUitls.getInstance(null, permissionListener);
        permissionUitls.permssionCheck(permissionCode,checkPermissinos);
    }

    // 打开系统相机拍照 -- 需要相机权限
    public void JN_TakeCertificate() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 创建File对象，用于存储拍照后的图片
                Intent intent = new Intent(getActivity(), AICertificateCameraActivity.class);
                startActivityForResult(intent, REQUSETCODE_TAKECERTIFICATE);
            }
        });
    }
}



