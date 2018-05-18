package com.ai.base.location;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import java.util.List;

/**
 * Created by wuyoujian on 2018/3/13.
 */

public abstract class LocationSystemAPIUtil {

    private static LocationManager locationManager;
    private static Location myLocation;
    private static int locationNumber = 0;

    /**
     * 判断是否有可用的内容提供器
     * @return 不存在返回null
     */
    private static String judgeProvider(LocationManager locationManager) {
        List<String> prodiverlist = locationManager.getProviders(true);
        if(prodiverlist.contains(LocationManager.NETWORK_PROVIDER)){
            return LocationManager.NETWORK_PROVIDER;
        } else if(prodiverlist.contains(LocationManager.GPS_PROVIDER)) {
            return LocationManager.GPS_PROVIDER;
        } else{
            return null;
        }
    }

    /**
     * 判断GPS导航是否打开.
     */
    private static boolean isOpenGPS(final Activity context){

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            return false;
        }

        return true;
    }

    public static void callLocation(final Activity context, final OnCallBackListener listener) {

        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                        (context, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                //获得位置服务
                locationManager = (LocationManager)context.getSystemService(context.LOCATION_SERVICE);
                String provider = judgeProvider(locationManager);
                if (provider == null) {
                    if (listener != null) {
                        listener.onLocationUnSupport();
                    }
                    return;
                }

                if (!isOpenGPS(context)) {
                    if (listener != null) {
                        listener.onUnopenGPS();
                    }
                    return;
                }

                if (listener != null) {
                    listener.onLocationStart();

                    myLocation = locationManager.getLastKnownLocation(provider);
                    locationNumber = 0;
                    while (locationNumber <= 4) {
                        locationManager.requestLocationUpdates(provider, 1000, 0, new LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {
                                if (location != null) {
                                    myLocation = location;
                                    listener.onLocationChanged(location);
                                    return;
                                }

                                locationNumber ++;
                            }

                            @Override
                            public void onStatusChanged(String provider, int status, Bundle extras) {
                                listener.onStatusChanged(provider,status,extras);
                            }

                            @Override
                            public void onProviderEnabled(String provider) {
                                listener.onProviderEnabled(provider);
                            }

                            @Override
                            public void onProviderDisabled(String provider) {
                                listener.onProviderDisabled(provider);
                            }
                        });

                        if (myLocation !=null) return;
                    }

                    if (myLocation != null) {
                        listener.onLocationChanged(myLocation);
                    } else {
                        listener.onLocationFail();
                    }
                }
            }
        });
    }


    public interface OnCallBackListener {
        void onLocationStart();
        void onLocationUnSupport();
        void onLocationFail();
        void onUnopenGPS();
        void onLocationChanged(Location location);
        void onStatusChanged(String provider, int status, Bundle extras);
        void onProviderEnabled(String provider);
        void onProviderDisabled(String provider);
    }
}
