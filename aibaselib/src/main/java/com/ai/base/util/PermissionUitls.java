package com.ai.base.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by song on 9/5/2016.
 * CALENDAR
 CAMERA
 CONTACTS
 LOCATION
 MICROPHONE
 PHONE
 SENSORS
 SMS
 STORAGE
 */
public class PermissionUitls {
    public static String CALENDAR = Manifest.permission.READ_CALENDAR;
    public static String CAMERA = Manifest.permission.CAMERA;
    public static String CONTACTS = Manifest.permission.READ_CONTACTS;
    public static String LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static String MICROPHONE = Manifest.permission.RECORD_AUDIO;
    public static String PHONE = Manifest.permission.READ_PHONE_STATE;
    public static String SENSORS = Manifest.permission.BODY_SENSORS;
    public static String SMS = Manifest.permission.READ_SMS;
    public static String STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;


    public static PermissionUitls permissionUitls;
    public static Context mContext;
    public static FragmentManager mFragmentManager;
    public static final int PERMISSION_ALL_CODE = 0;//
    public static final int PERMISSION_FIRST_PAGE_CODE = 100;// the permission of first page
    public static final int PERMISSION_CALENDAR_CODE = 1;
    public static final int PERMISSION_CAMERA_CODE = 2;
    public static final int PERMISSION_CONTACTS_CODE = 3;
    public static final int PERMISSION_LOCATION_CODE = 4;
    public static final int PERMISSION_MICROPHONE_CODE = 5;
    public static final int PERMISSION_PHONE_CODE = 6;
    public static final int PERMISSION_SENSORS_CODE = 7;
    public static final int PERMISSION_SMS_CODE = 8;
    public static final int PERMISSION_STORAGE_CODE = 9;
    private static PermissionListener mPermissionListener;
    private static int permissionCodeCheck = 0;
    public PermissionUitls() {

    }

    public static PermissionUitls getInstance() {
        if (permissionUitls == null) {
            permissionUitls = new PermissionUitls();
        }
        return permissionUitls;
    }

    public static PermissionUitls getInstance(FragmentManager fragmentManager, PermissionListener permissionListener) {
        mFragmentManager = fragmentManager;
        mPermissionListener = permissionListener;
        return getInstance();
    }


    public static void permssionCheck(int permissionCode) {
        if (Build.VERSION.SDK_INT >= 23) {
            permissionCodeCheck = permissionCode;
            List<String> unauthorizedPermissions = getUnauthorizedPermissonListByCode(permissionCode);
            if (unauthorizedPermissions != null && unauthorizedPermissions.size() !=0) {
                requestPermissions(permissionCode, unauthorizedPermissions);
            } else {
                mPermissionListener.permissionAgree();
            }
        }else {
            mPermissionListener.permissionAgree();
        }

    }

    public static boolean isAuthorized(String permission) {
        boolean isAuthorized = false;
        if (ContextCompat.checkSelfPermission(mContext, permission) == PackageManager.PERMISSION_GRANTED) {
            isAuthorized = true;
        }
        return isAuthorized;
    }

    public static boolean isAuthorizedByCode(int permissionCode) {
        boolean isAuthorized = false;
        List<String> unauthorizedPermissonList = getUnauthorizedPermissonListByCode(permissionCode);
        if (unauthorizedPermissonList.size() == 0) {
            isAuthorized = true;
        }
        return isAuthorized;
    }
    public static List<String> getUnauthorizedPermissonListByCode(int permissionCode) {
        List<String> permissionList = new ArrayList<>();
        switch (permissionCode) {
            case PERMISSION_ALL_CODE:
                if (!isAuthorized(CALENDAR)) {
                    permissionList.add(CALENDAR);
                }
                if (!isAuthorized(CAMERA)) {
                    permissionList.add(CAMERA);
                }
                if (!isAuthorized(CONTACTS)) {
                    permissionList.add(CONTACTS);
                }
                if (!isAuthorized(LOCATION)) {
                    permissionList.add(LOCATION);
                }
                if (!isAuthorized(MICROPHONE)) {
                    permissionList.add(MICROPHONE);
                }
                if (!isAuthorized(PHONE)) {
                    permissionList.add(PHONE);
                }
                if (!isAuthorized(SENSORS)) {
                    permissionList.add(SENSORS);
                }
                if (!isAuthorized(SMS)) {
                    permissionList.add(SMS);
                }
                if (!isAuthorized(STORAGE)) {
                    permissionList.add(STORAGE);
                }
                break;
            case PERMISSION_CALENDAR_CODE:

                if (!isAuthorized(CALENDAR)) {
                    permissionList.add(CALENDAR);
                }
                break;
            case PERMISSION_CAMERA_CODE:

                if (!isAuthorized(CAMERA)) {
                    permissionList.add(CAMERA);
                }
                break;
            case PERMISSION_CONTACTS_CODE:
                if (!isAuthorized(CONTACTS)) {
                    permissionList.add(CONTACTS);
                }
                break;

            case PERMISSION_LOCATION_CODE:
                if (!isAuthorized(LOCATION)) {
                    permissionList.add(LOCATION);
                }
                break;
            case PERMISSION_MICROPHONE_CODE:
                if (!isAuthorized(MICROPHONE)) {
                    permissionList.add(MICROPHONE);
                }
                break;
            case PERMISSION_PHONE_CODE:
                if (!isAuthorized(PHONE)) {
                    permissionList.add(PHONE);
                }
                break;
            case PERMISSION_SENSORS_CODE:
                if (!isAuthorized(SENSORS)) {
                    permissionList.add(SENSORS);
                }
                break;
            case PERMISSION_SMS_CODE:
                if (!isAuthorized(SMS)) {
                    permissionList.add(SMS);
                }
                break;
            case PERMISSION_STORAGE_CODE:
                if (!isAuthorized(STORAGE)) {
                    permissionList.add(STORAGE);
                }
                break;

            case PERMISSION_FIRST_PAGE_CODE:
                if (!isAuthorized(PHONE)) {
                    permissionList.add(PHONE);
                }
                if (!isAuthorized(STORAGE)) {
                    permissionList.add(STORAGE);
                }
                if (!isAuthorized(LOCATION)) {
                    permissionList.add(LOCATION);
                }
                break;
        }
        return permissionList;
    }

    public static boolean isGetAllPermissionsByList(List<String> permissions) {
        boolean getAllPermissoins = true;
        if (permissions == null) return true;
        for (String permissionCode : permissions) {
            if (ContextCompat.checkSelfPermission(mContext, permissionCode)
                    != PackageManager.PERMISSION_GRANTED) {
                getAllPermissoins = false;
                break;
            }
        }
        return getAllPermissoins;
    }

    public static boolean isGetAllPermissionsByList(int[] grantResults) {
        boolean getAllPermissoins = true;
        if (grantResults == null) return true;
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                getAllPermissoins = false;
                break;
            }
        }
        return getAllPermissoins;
    }

    public static boolean shouldShowRequestPermissionRationaleByList(String[] permissions) {
        boolean shouldShow = false;
        if (permissions == null) return true;
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext, permission)) {
                shouldShow = true;
                break;
            }
        }
        return shouldShow;
    }

    /**
     * Requests the CAMERA permission.
     * If the permission has been denied previously, a SnackBar will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    private static void requestPermissions(int permissionCode, List<String> permissionList) {
        int REQUEST_CODE = permissionCode;
        if (permissionList != null) {
            int size = permissionList.size();
            if (size != 0) {
                String[] permissions = permissionList.toArray(new String[size]);
                ActivityCompat.requestPermissions((Activity) mContext, permissions, REQUEST_CODE);
            }

        }

    }

    private static void showPermissionWarn(String content) {
        Toast.makeText(mContext,content,Toast.LENGTH_SHORT).show();
    }

    public static void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                                  @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_FIRST_PAGE_CODE :
                if (!isAuthorized(STORAGE)) {
                    String content;
                    if (!ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext, STORAGE)) {

                        content = mContext.getString(R.string.PermissionDialog_setting);
                    }else {
                        content = mContext.getString(R.string.PermissionDialog_Needful);
                    }
                    showPermissionWarn(content);
                }else{
                    // the other permission is not important. agree or not will go on;
                    mPermissionListener.permissionAgree();
                }
                break;
            default:
                int i = grantResults[0];
                if (i == PackageManager.PERMISSION_GRANTED){
                    mPermissionListener.permissionAgree();
                }else{
                    mPermissionListener.permissionReject();
                }
                break;
        }

    }

    public interface PermissionListener {
        void permissionAgree();
        void permissionReject();
    }
}
