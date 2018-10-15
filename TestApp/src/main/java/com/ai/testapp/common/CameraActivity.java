package com.ai.testapp.common;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ai.base.util.Utility;
import com.ai.testapp.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class CameraActivity extends AppCompatActivity {

    private Button btn;

    private CameraDevice cameraDevice;
    private CameraCaptureSession mPreviewSession;
    private CaptureRequest.Builder mCaptureRequestBuilder,captureRequestBuilder;
    private CaptureRequest mCaptureRequest;
    private ImageReader imageReader;
    private int height = 0,width= 0;
    private Size previewSize;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static  {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private TranslucencyView mMarkView;
    private TextureView mTextView;
    private FrameLayout mMainLayout;
    private ImageButton mCloseButton;
    private ImageButton mTakeButton;
    private ImageButton mFlashButton;
    private Boolean mIsFlashOn = false;
    private String mCameraId = "-1";

    private final int RESULT_CODE_CAMERA = 1;
    private final int WRITE_EXTERNAL_STORAGE = 2;

    private void layoutMainView() {
        mMainLayout = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        mMainLayout.setLayoutParams(params);
        mMainLayout.setBackgroundColor(Color.TRANSPARENT);

        mTextView = new TextureView(this);
        params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        mMainLayout.addView(mTextView);
        //设置TextureView监听
        mTextView.setSurfaceTextureListener(surfaceTextureListener);

        mMarkView = new TranslucencyView(this);
        mMarkView.setLayoutParams(params);
        mMainLayout.addView(mMarkView);

        mCloseButton = new ImageButton(this);
        mCloseButton.setImageBitmap(CameraICONData.getCloseBitmap());
        mCloseButton.setBackgroundColor(Color.TRANSPARENT);
        mMainLayout.addView(mCloseButton);
        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        mTakeButton = new ImageButton(this);
        mTakeButton.setImageBitmap(CameraICONData.getTakeBitmap());
        mTakeButton.setBackgroundColor(Color.TRANSPARENT);
        mMainLayout.addView(mTakeButton);
        mTakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        mFlashButton = new ImageButton(this);
        mFlashButton.setImageBitmap(CameraICONData.getFlashoffBitmap());
        mFlashButton.setBackgroundColor(Color.TRANSPARENT);
        mMainLayout.addView(mFlashButton);
        mFlashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsFlashOn = !mIsFlashOn;
                if (mIsFlashOn) {
                    mFlashButton.setImageBitmap(CameraICONData.getFlashonBitmap());

                } else {
                    mFlashButton.setImageBitmap(CameraICONData.getFlashoffBitmap());
                }

                try {
                    if (mPreviewSession != null) {
                        int flashMode = mIsFlashOn ? CaptureRequest.FLASH_MODE_TORCH : CaptureRequest.FLASH_MODE_OFF;
                        mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE, flashMode);
                    }
                    mPreviewSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, null);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        });

        setContentView(mMainLayout);
    }

    private void reLayoutViews() {
        int buttonWidth = Utility.dip2px(this,40);
        int buttonHeight = Utility.dip2px(this,40);
        int takeWidth = Utility.dip2px(this,72);
        int takeHeight = Utility.dip2px(this,72);
        int takeBottomMargin = Utility.dip2px(this,40);
        int buttonBottomMargin = (takeHeight-buttonHeight)/2 + takeBottomMargin;

        int space = Utility.dip2px(this,32);
        int mainWidth = mMainLayout.getWidth();
        int margin = (mainWidth - 2*space - 2*buttonWidth - takeWidth)/2;

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(buttonWidth, buttonHeight);
        params.topMargin = mMainLayout.getHeight() - buttonHeight - buttonBottomMargin;
        params.leftMargin = margin;
        mCloseButton.setLayoutParams(params);

        params = new FrameLayout.LayoutParams(takeWidth, takeHeight);
        params.topMargin = mMainLayout.getHeight() - takeHeight - takeBottomMargin;
        params.leftMargin = space + margin + buttonWidth;
        mTakeButton.setLayoutParams(params);

        params = new FrameLayout.LayoutParams(buttonWidth, buttonHeight);
        params.topMargin = mMainLayout.getHeight() - buttonHeight - buttonBottomMargin;
        params.leftMargin = space + margin + buttonWidth + takeWidth + space;
        mFlashButton.setLayoutParams(params);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        reLayoutViews();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        layoutMainView();
        checkPermisson();
    }

    private void checkPermisson() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //提示用户开户权限
            String[] perms = {"android.permission.WRITE_EXTERNAL_STORAGE"};
            ActivityCompat.requestPermissions(CameraActivity.this,perms, WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(cameraDevice!=null) {
            stopCamera();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cameraDevice !=null) {
            startCamera();
        }
    }

    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        //可用
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            CameraActivity.this.width=width;
            CameraActivity.this.height=height;
            openCamera();
        }


        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        //释放
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            stopCamera();
            return true;
        }

        //更新
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };


    /**打开摄像头*/
    private void openCamera() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //提示用户开户权限
                String[] perms = {"android.permission.CAMERA"};
                ActivityCompat.requestPermissions(CameraActivity.this,perms, RESULT_CODE_CAMERA);
            } else {
                CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                String cameraId = "-1";
                try {
                    //遍历所有摄像头
                    for (String id : manager.getCameraIdList()) {
                        CameraCharacteristics characteristics = manager.getCameraCharacteristics(id);
                        //默认打开后置摄像头
                        if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                            cameraId = id;
                            break;
                        }
                    }
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }

                if (cameraId != "-1") {
                    //设置摄像头特性
                    mCameraId = cameraId;
                    setCameraCharacteristics(manager,cameraId);
                    manager.openCamera(cameraId, stateCallback, null);
                }
            }
        } catch (CameraAccessException e){
            e.printStackTrace();
        }
    }

    /**设置摄像头的参数*/
    private void setCameraCharacteristics(CameraManager manager,String cameraId) {
        try {
            // 获取指定摄像头的特性
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            // 获取摄像头支持的配置属性
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            // 获取摄像头支持的最大尺寸
            Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),new CompareSizesByArea());
            // 创建一个ImageReader对象，用于获取摄像头的图像数据,2代表ImageReader中最多可以获取两帧图像流
            imageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(), ImageFormat.JPEG, 2);
            //设置获取图片的监听
            imageReader.setOnImageAvailableListener(imageAvailableListener,null);
            // 获取最佳的预览尺寸
            previewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height, largest);
        } catch (CameraAccessException e) {
            //e.printStackTrace();
        } catch (NullPointerException e) {
        }
    }

    private static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        // 收集摄像头支持的大过预览Surface的分辨率
        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            //不失真的图片，且大于预览窗口的大小
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }
        // 如果找到多个预览尺寸，获取其中面积最小的
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            //没有合适的预览尺寸
            return choices[0];
        }
    }

    // 为Size定义一个比较器Comparator
    static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            // 强转为long保证不会发生溢出
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    /**摄像头状态的监听*/
    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        // 摄像头被打开时触发该方法
        @Override
        public void onOpened(CameraDevice cameraDevice){
            CameraActivity.this.cameraDevice = cameraDevice;
            // 开始预览
            takePreview();
        }

        // 摄像头断开连接时触发该方法
        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            CameraActivity.this.cameraDevice.close();
            CameraActivity.this.cameraDevice = null;

        }
        // 打开摄像头出现错误时触发该方法
        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            cameraDevice.close();
        }
    };

    /**
     * 开始预览
     */
    private void takePreview() {
        SurfaceTexture mSurfaceTexture = mTextView.getSurfaceTexture();
        //设置TextureView的缓冲区大小
        mSurfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
        //获取Surface显示预览数据
        Surface mSurface = new Surface(mSurfaceTexture);
        try {
            //创建预览请求
            mCaptureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            // 设置自动对焦模式
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            //设置Surface作为预览数据的显示界面
            mCaptureRequestBuilder.addTarget(mSurface);
            //创建相机捕获会话，第一个参数是捕获数据的输出Surface列表，第二个参数是CameraCaptureSession的状态回调接口，当它创建好后会回调onConfigured方法，第三个参数用来确定Callback在哪个线程执行，为null的话就在当前线程执行
            cameraDevice.createCaptureSession(Arrays.asList(mSurface,imageReader.getSurface()),new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        //开始预览
                        mCaptureRequest = mCaptureRequestBuilder.build();
                        mPreviewSession = session;
                        //设置反复捕获数据的请求，这样预览界面就会一直有数据显示
                        mPreviewSession.setRepeatingRequest(mCaptureRequest, null, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**拍照*/
    private void takePicture()  {
        try {
            if (cameraDevice == null) {
                return;
            }
            // 创建拍照请求
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            // 设置自动对焦模式
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            // 将imageReader的surface设为目标
            captureRequestBuilder.addTarget(imageReader.getSurface());
            // 获取设备方向
            int rotation =  getWindowManager().getDefaultDisplay().getRotation();
            // 根据设备方向计算设置照片的方向
            captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            // 停止连续取景
            mPreviewSession.stopRepeating();
            //拍照
            CaptureRequest captureRequest = captureRequestBuilder.build();
            //设置拍照监听
            mPreviewSession.capture(captureRequest,captureCallback, null);
        } catch (CameraAccessException e) {
            //e.printStackTrace();
        }
    }

    /**监听拍照结果*/
    private CameraCaptureSession.CaptureCallback captureCallback= new CameraCaptureSession.CaptureCallback() {
        // 拍照成功
        @Override
        public void onCaptureCompleted(CameraCaptureSession session,CaptureRequest request,TotalCaptureResult result) {
            // 重设自动对焦模式
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            // 设置自动曝光模式
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            try {
                //重新进行预览
                mPreviewSession.setRepeatingRequest(mCaptureRequest, null, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
        }
    };

    /**监听拍照的图片*/
    private ImageReader.OnImageAvailableListener imageAvailableListener= new ImageReader.OnImageAvailableListener()
    {
        // 当照片数据可用时激发该方法
        @Override
        public void onImageAvailable(ImageReader reader) {

            //先验证手机是否有sdcard
            String status = Environment.getExternalStorageState();
            if (!status.equals(Environment.MEDIA_MOUNTED)) {
                Toast.makeText(getApplicationContext(), "你的sd卡不可用。", Toast.LENGTH_SHORT).show();
                return;
            }
            // 获取捕获的照片数据
            Image image = reader.acquireNextImage();
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);

            //手机拍照都是存到这个路径
            String filePath = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/";
            String picturePath = System.currentTimeMillis() + ".jpg";
            File file = new File(filePath, picturePath);
            try {
                //存到本地相册
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(data);
                fileOutputStream.close();
//                //显示图片
//                BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inSampleSize = 2;
//                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                //

            } catch (FileNotFoundException e) {
                //e.printStackTrace();
                setResult(0);
                finish();
            } catch (IOException e) {
                setResult(0);
                finish();
            } finally {
                image.close();
                Intent intent = new Intent();
                intent.putExtra("photoPath",file.getAbsolutePath());
                setResult(RESULT_OK,intent);
                finish();
            }
        }


    };

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults){
        switch(permsRequestCode){
            case RESULT_CODE_CAMERA:
                boolean cameraAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                if(cameraAccepted) {
                    //授权成功之后，调用系统相机进行拍照操作等
                    openCamera();
                }else{
                    //用户授权拒绝之后，友情提示一下就可以了
                    Toast.makeText(CameraActivity.this,"请开启应用拍照权限",Toast.LENGTH_SHORT).show();
                }
                break;

            case WRITE_EXTERNAL_STORAGE:

                break;
        }
    }

    /**启动拍照*/
    private void startCamera(){
        if (mTextView.isAvailable()) {
            if(cameraDevice==null) {
                openCamera();
            }
        } else {
            mTextView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    /**
     * 停止拍照释放资源*/
    private void stopCamera(){
        if(cameraDevice!=null){
            cameraDevice.close();
            cameraDevice=null;
        }
    }
}
