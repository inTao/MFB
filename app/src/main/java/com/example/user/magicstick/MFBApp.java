package com.example.user.magicstick;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.example.user.magicstick.activite.MainActivity;
import com.example.user.magicstick.ble.BtService;
import com.example.user.magicstick.sharedpreferences.PropertiesUtil;

import java.util.ArrayList;


/**
 * Created by user on 2018/4/14.
 */

public class MFBApp extends Application {


    //    private MySCo mConn;
    private String TAG = "MFBApp";
    private Intent mIntent;
    private static MFBApp sInstance;
    private BtService mService;
    private MyConn myConn;
    private Boolean isBind;


    public static MFBApp getApp() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        //开启服务
        mIntent = new Intent(MFBApp.this, BtService.class);
        myConn = new MyConn();
        startService(mIntent);
        isBind = bindService(mIntent, myConn, BIND_AUTO_CREATE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isBind){
                    unbindService(myConn);
                    isBind = false;
                }

            }
        }, 2000);

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        //关闭服务
        stopService(mIntent);
    }


    private class MyConn implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((BtService.LocalBinder) service).getService();
            PropertiesUtil propertiesUtil = PropertiesUtil.getInstance();
            String blueAddress = propertiesUtil.getString("BlueAddress", "");
            if (!blueAddress.equals("")) {
                mService.connect(blueAddress);
                System.out.println(blueAddress);
            } else {
                if (isBind){
                    unbindService(myConn);
                    isBind = false;
                }

            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }
}
