package com.ai.base.certificateCamera;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.ai.base.AIBaseActivity;
import com.ai.base.util.Utility;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;


//  参考google官网demo
//  专业拍身份证和银行卡照片
//  by wuyj
public class AICertificateCameraActivity extends AIBaseActivity {

    // 相机的状态
    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAITING_LOCK = 1;
    private static final int STATE_WAITING_PRECAPTURE = 2;
    private static final int STATE_WAITING_NON_PRECAPTURE = 3;
    private static final int STATE_PICTURE_TAKEN = 4;

    private String mCameraId;
    private CameraCaptureSession mCaptureSession;
    private static CameraDevice mCameraDevice;
    private Size mPreviewSize;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private ImageReader mImageReader;
    //以防止在关闭相机之前应用程序退出
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private int mSensorOrientation;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CaptureRequest mPreviewRequest;
    private int mState = STATE_PREVIEW;
    private static CameraManager mManager;

    //从屏幕旋转转换为JPEG方向
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private AITranslucencyView mMarkView;
    private TextureView mTextureView;
    private FrameLayout mMainLayout;
    private ImageButton mCloseButton;
    private ImageButton mTakeButton;
    private ImageButton mFlashButton;
    private Boolean mIsFlashOn = false;

    private final int RESULT_CODE_CAMERA = 1;
    private final int WRITE_EXTERNAL_STORAGE = 2;
    public static final String kImageSavePathKey = "photoPath";

    private void layoutMainView() {
        mManager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);

        mMainLayout = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        mMainLayout.setLayoutParams(params);
        mMainLayout.setBackgroundColor(Color.TRANSPARENT);

        mTextureView = new TextureView(this);
        params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        mMainLayout.addView(mTextureView);
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);

        mMarkView = new AITranslucencyView(this);
        mMarkView.setLayoutParams(params);
        mMainLayout.addView(mMarkView);

        mCloseButton = new ImageButton(this);
        mCloseButton.setImageBitmap(AICameraICONData.getCloseBitmap());
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
        mTakeButton.setImageBitmap(AICameraICONData.getTakeBitmap());
        mTakeButton.setBackgroundColor(Color.TRANSPARENT);
        mMainLayout.addView(mTakeButton);
        mTakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        mFlashButton = new ImageButton(this);
        mFlashButton.setImageBitmap(AICameraICONData.getFlashoffBitmap());
        mFlashButton.setBackgroundColor(Color.TRANSPARENT);
        mMainLayout.addView(mFlashButton);
        mFlashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsFlashOn = !mIsFlashOn;
                setFlashStatus(mIsFlashOn);
            }
        });

        setContentView(mMainLayout);
    }

    private void setFlashStatus(Boolean on) {
        try {
            if (on) {
                mFlashButton.setImageBitmap(AICameraICONData.getFlashonBitmap());

            } else {
                mFlashButton.setImageBitmap(AICameraICONData.getFlashoffBitmap());
            }
            if (mCaptureSession != null) {
                int flashMode = on ? CaptureRequest.FLASH_MODE_TORCH : CaptureRequest.FLASH_MODE_OFF;
                mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, flashMode);
            }
            mPreviewRequest = mPreviewRequestBuilder.build();
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            //e.printStackTrace();
        }
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
        setPortraitFullscreen();
        super.onCreate(savedInstanceState);
        layoutMainView();
        checkPermisson();
    }

    private void setPortraitFullscreen() {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    private void setNavigationBarTransparent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Android 5.0 以上 全透明
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            // 状态栏（以上几行代码必须，参考setStatusBarColor|setNavigationBarColor方法源码）
            window.setStatusBarColor(Color.TRANSPARENT);
            // 虚拟导航键
            window.setNavigationBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Android 4.4 以上 半透明
            Window window = getWindow();
            // 状态栏
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 虚拟导航键
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    private void checkPermisson() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //提示用户开户权限
            String[] perms = {"android.permission.WRITE_EXTERNAL_STORAGE"};
            ActivityCompat.requestPermissions(AICertificateCameraActivity.this,perms, WRITE_EXTERNAL_STORAGE);
        }
    }

    //拍照之后，图片可用保存时候调用
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            mBackgroundHandler.post(new AICertificateCameraActivity.ImageSaver(AICertificateCameraActivity.this,
                    reader.acquireNextImage(),
                    mMarkView.getTransparencyRect(),
                    new Size(mMarkView.getWidth(), mMarkView.getHeight())));
        }
    };

    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            //在TextureView可用的时候尝试打开摄像头
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }
    };

    //实现监听CameraDevice状态回调
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            //要想预览、拍照等操作都是需要通过会话来实现，所以创建会话用于预览
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }
    };


    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {
            switch (mState) {
                case STATE_PREVIEW: {
                    break;
                }
                case STATE_WAITING_LOCK: {
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    if (afState == null) {
                        captureStillPicture();
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                            CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            mState = STATE_PICTURE_TAKEN;
                            captureStillPicture();
                        } else {
                            runPrecaptureSequence();
                        }
                    }
                    break;
                }
                case STATE_WAITING_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        mState = STATE_WAITING_NON_PRECAPTURE;
                    }
                    break;
                }
                case STATE_WAITING_NON_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        mState = STATE_PICTURE_TAKEN;
                        captureStillPicture();
                    }
                    break;
                }
                default:
                    break;
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            process(result);
        }
    };

    // 开启相机预览界面
    public void startCameraPreView() {
        startBackgroundThread();
        //1、如果TextureView 可用则直接打开相机
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mTextureView != null) {
                    if (mTextureView.isAvailable()) {
                        openCamera(mTextureView.getWidth(), mTextureView.getHeight());
                    } else {
                        //设置TextureView 的回调后，当满足之后自动回调到
                        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
                    }
                }
            }
        },300);//建议加上尤其是你需要在多个界面都要开启预览界面时候

    }

    //开启HandlerThread
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        if (mBackgroundThread == null) {
            return;
        }
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 拍照
    public void takePicture() {
        lockFocus();
    }

    // 通过会话提交捕获图像的请求，通常在捕获回调回应后调用
    private void captureStillPicture() {
        try {
            if (null == mCameraDevice) {
                return;
            }
            //创建用于拍照的CaptureRequest.Builder
            final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());

            // 使用和预览一样的模式 AE and AF
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            // 设置拍照图片的方向
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));

            CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    unlockFocus();
                }
            };
            mCaptureSession.stopRepeating();
            mCaptureSession.capture(captureBuilder.build(), CaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void lockFocus() {
        try {
            if (mCaptureSession == null) {
                return;
            }
            // This is how to tell the camera to lock focus.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the lock.
            mState = STATE_WAITING_LOCK;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void unlockFocus() {
        try {
            // Reset the auto-focus trigger
            if (mCaptureSession == null) {
                return;
            }
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
            // After this, the camera will go back to the normal state of preview，重新预览
            mState = STATE_PREVIEW;
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // 从指定的屏幕旋转中检索JPEG方向。
    private int getOrientation(int rotation) {
        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
    }

    private void runPrecaptureSequence() {
        try {
            // This is how to tell the camera to trigger.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the precapture sequence to be set.
            mState = STATE_WAITING_PRECAPTURE;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;
            // 将默认缓冲区的大小配置为我们想要的相机预览的大小。
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            // This is the output Surface we need to start preview.
            Surface surface = new Surface(texture);
            // set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            // 把显示预览界面的TextureView添加到到CaptureRequest.Builder中
            mPreviewRequestBuilder.addTarget(surface);
            // create a CameraCaptureSession for camera preview.
            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    // The camera is already closed
                    if (null == mCameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    mCaptureSession = cameraCaptureSession;
                    try {
                        // 设置自动对焦参数并把参数设置到CaptureRequest.Builder中
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        // 封装好CaptureRequest.Builder后，调用build 创建封装好CaptureRequest 并发送请求
                        mPreviewRequest = mPreviewRequestBuilder.build();
                        mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // 获取虚拟导航条高度
    public static int getVirtualBarHeight(Context context) {
        int vh = 0;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        try {
            @SuppressWarnings("rawtypes")
            Class c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);
            vh = dm.heightPixels - display.getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vh;
    }

    private void setUpCameraOutputs(int width, int height) {

        try {
            for (String cameraId : mManager.getCameraIdList()) {
                CameraCharacteristics characteristics = mManager.getCameraCharacteristics(cameraId);

                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                //前置摄像头
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }

                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }

                // 在未透明化虚拟导航条时，需要加上虚拟导航条的高度
                height += getVirtualBarHeight(this);
                //处理图片方向相关
                int displayRotation = getWindowManager().getDefaultDisplay().getRotation();
                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                boolean swappedDimensions = false;
                switch (displayRotation) {
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                        if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                            swappedDimensions = true;
                        }
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                            swappedDimensions = true;
                        }
                        break;
                    default:
                        break;
                }
                // 选一个与mTextureView比例一致的尺寸
                // 一般手机都支持一个与屏幕比例一致的尺寸
                Size largest;
                if (swappedDimensions) {
                    largest = chooseOptimalSize(map.getOutputSizes(ImageFormat.JPEG),height,width);
                } else {
                    largest = chooseOptimalSize(map.getOutputSizes(ImageFormat.JPEG),width,height);
                }

                // 创建一个ImageReader对象，用于获取摄像头的图像数据,2代表ImageReader中最多可以获取两帧图像流
                mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(), ImageFormat.JPEG, 2);
                // 设置ImageReader监听
                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);

                mPreviewSize = largest;
                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
        }
    }

    // 选择一个与mTextureView比例一致的尺寸
    private static Size chooseOptimalSize(Size[] choices, int width, int height)  {
        // 收集摄像头支持的大过预览Surface的分辨率
        List<Size> bigEnough = new ArrayList<>();
        for (Size option : choices) {
            int optionHeight = option.getHeight();
            int optionWidth = option.getWidth();
            //不失真的图片，且大于等于预览窗口的大小
            if (optionHeight == optionWidth * height / width &&
                    optionWidth >= width && optionHeight >= height) {
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

    // 打开指定摄像头
    private void openCamera(int width, int height) {
        //4、设置参数
        setUpCameraOutputs(width, height);
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //提示用户开户权限
                String[] perms = {"android.permission.CAMERA"};
                ActivityCompat.requestPermissions(AICertificateCameraActivity.this,perms, RESULT_CODE_CAMERA);
            } else {
                mManager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
            }
        } catch (CameraAccessException e){
            //e.printStackTrace();
        }
    }

    // 关闭摄像头
    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    // 释放Act和View
    public void onDestroyHelper() {
        stopBackgroundThread();
        closeCamera();
        mTextureView = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCameraPreView();
    }


    @Override
    protected void onPause() {
        super.onPause();
        mIsFlashOn = false;
        setFlashStatus(mIsFlashOn);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onDestroyHelper();
    }

    private static class CompareSizesByArea implements Comparator<Size> {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    private static class ImageSaver implements Runnable {

        private final Image mImage;
        private final Rect mRect;
        private final Size mSize;
        private final AppCompatActivity mActivityCompat;
        public ImageSaver(AppCompatActivity activityCompat, Image image,Rect rect,Size size) {
            mActivityCompat = activityCompat;
            mImage = image;
            mRect = rect;
            mSize = size;
        }

        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);

            String filePath = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/";
            String picturePath = System.currentTimeMillis() + ".jpg";
            File file = new File(filePath, picturePath);
            BufferedOutputStream bos = null;
            try {
                bos = new BufferedOutputStream(new FileOutputStream(file));
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

                float left, top, w, h;
                left = (float) mRect.left / (float) mSize.getWidth() * bitmap.getWidth();
                top = (float)mRect.top / (float)mSize.getHeight()*bitmap.getHeight();
                w = mRect.width()/(float)mSize.getWidth() *bitmap.getWidth();
                h = mRect.height()/(float)mSize.getHeight() *bitmap.getHeight();
                Bitmap cropBitmap = Bitmap.createBitmap(bitmap,
                        (int)left,
                        (int)top,
                        (int)w,
                        (int)h);
                cropBitmap.compress(Bitmap.CompressFormat.JPEG, 30, bos);
                bos.flush();
                bos.close();
            } catch (IOException e) {
               // e.printStackTrace();
            } finally {
                mImage.close();
                if (null != bos) {
                    try {
                        bos.close();
                        Intent intent = new Intent();
                        intent.putExtra(kImageSavePathKey,file.getAbsolutePath());
                        mActivityCompat.setResult(RESULT_OK,intent);
                        mActivityCompat.finish();
                    } catch (IOException e) {
                        //e.printStackTrace();
                    }
                }
            }
        }
    }
}
