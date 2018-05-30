package com.example.user.magicstick.activite;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.example.user.magicstick.LinearLayout;
import com.example.user.magicstick.MyAdapter;
import com.example.user.magicstick.ble.BtService;
import com.example.user.magicstick.R;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "MainActivity";
    private final static int REQUEST_ENABLE_BT = 1;
    //扫描时间
    private static final int SCAN_PERIOD = 5000;
    //蓝牙状态
    private static final int BLUETOOTH_ON = 10; //开启
    private static final int BLUETOOTH_SCANING = 11;  //扫描
    private static final int BLUETOOTH_OFF = 12; //关闭

    private static String[] permission = {
            "android.permission.BLUETOOTH_ADMIN",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION"};
    private static ArrayList<String> permission_denied = new ArrayList<>();

    private boolean isUnService;

    private Toolbar mToolbar;
    private BluetoothAdapter mBluetoothAdapter;
    private BtService mBtService;
    private Intent mIntent;
    private MyConn myConn;
    private IntentFilter intent;
    private MyBr myBr;

    private int mBlueToothState;
    private ArrayList<BlueDevice> mDeviceList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private MyAdapter myAdapter;
    private Handler mHandler;
    private TimerTask mTimerTask;
    private Timer mTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new Handler();
        //view初始化
        viewInit();
        isUnService = false;
        //蓝牙适配器 初始化
        blueToothInit();
        intent = new IntentFilter(mBtService.ACTION_GATT_CONNECTED);
        myBr = new MyBr();
        registerReceiver(myBr, intent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < permission.length; i++) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, permission[i]) != PackageManager.PERMISSION_GRANTED) {
                    permission_denied.add(permission[i]);
                }
            }
            if (permission_denied.isEmpty()) {//未授予的权限为空，表示都授予了
            } else {//请求权限方法
                String[] permissions = permission_denied.toArray(new String[permission_denied.size()]);//将List转为数组
                ActivityCompat.requestPermissions(MainActivity.this, permissions, 1001);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int grant : grantResults) {
            if (grant != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "aaaa", Toast.LENGTH_SHORT);
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        changedBlueToothEnable(mBluetoothAdapter.isEnabled());
    }

    private class MyBr extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == mBtService.ACTION_GATT_CONNECTED) {
                mDeviceList.clear();
                mTimer.cancel();
                mTimer = null;
                mTimerTask.cancel();
                mTimerTask = null;
                finish();
            }
        }
    }

    //ToolBar view生成menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        //判断状态 更新menu
        if (mBlueToothState == BLUETOOTH_ON) {
            menu.findItem(R.id.toolbar_search).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else if (mBlueToothState == BLUETOOTH_OFF) {
            menu.findItem(R.id.toolbar_search).setVisible(false);
        } else {
            menu.findItem(R.id.toolbar_search).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(R.layout.progressbar);
        }
        return super.onCreateOptionsMenu(menu);
    }

    //menu的点击反馈
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        scanLeDevice();
        return super.onOptionsItemSelected(item);
    }

    //点击处理
    private void scanLeDevice() {
        if (mBlueToothState == BLUETOOTH_ON) {
            mDeviceList.clear();
            myAdapter.notifyDataSetChanged();
            mBlueToothState = BLUETOOTH_SCANING;
            //开始扫描
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mBlueToothState = BLUETOOTH_ON;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        System.out.println("Thread name:------>" + Thread.currentThread().getName());
        invalidateOptionsMenu();
    }

    //蓝牙扫描的回调
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

            if (device.getName() != null) {
                Log.d("设备名称------->", device.getName() + "xxxx" + rssi);
                Boolean isRepetition = false;
                BlueDevice buleDevice = new BlueDevice();
                buleDevice.device = device;
                buleDevice.rssi = rssi;
                for (BlueDevice bd : mDeviceList
                        ) {
                    Log.d("设备名称------->", bd.device.getName() + "xxx1x" + rssi);
                    if (bd.device.getName().equals(device.getName())) {
                        bd.rssi = rssi;
                        isRepetition = true;
                    }
                }
                if (!isRepetition) {
                    mDeviceList.add(buleDevice);
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            myAdapter.notifyItemInserted(mDeviceList.size() - 1);
                        }
                    });
                }
            }
        }
    };

    //蓝牙需要的 方法 等  初始化
    private void blueToothInit() {
        //蓝牙适配器
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        //蓝牙服务
        mIntent = new Intent(this, BtService.class);
        myConn = new MyConn();
        bindService(mIntent, myConn, BIND_AUTO_CREATE);
    }

    //控件初始化
    private void viewInit() {
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.disable();
                    Log.d(TAG, String.valueOf(mBluetoothAdapter.isEnabled()));
                    changedBlueToothEnable(false);
                } else {
                    blueToothInit();
                }
            }
        });
        LinearLayout layoutManager = new LinearLayout(this);
        mRecyclerView = findViewById(R.id.rcv);
        mRecyclerView.setLayoutManager(layoutManager);
        myAdapter = new MyAdapter(getApplicationContext(), mDeviceList);
        mRecyclerView.setAdapter(myAdapter);
        myAdapter.setOnItemClick(new MyAdapter.OnItemClickLitener() {
            @Override
            public void OnItemCkickLitener(View v, int position) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                mBlueToothState = BLUETOOTH_ON;
                invalidateOptionsMenu();
                System.out.println("点击~~~~~~1");
                mBtService.connect(mDeviceList.get(position).device.getAddress());
            }
        });
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        myAdapter.notifyDataSetChanged();
                    }
                });
            }
        };
        mTimer = new Timer(true);
        mTimer.schedule(mTimerTask, 3000, 3000);
    }

    //改变蓝牙开关图标
    private void changedBlueToothEnable(boolean isEnable) {
        if (isEnable) {
            mToolbar.setNavigationIcon(R.drawable.bluetooth_state);
            mBlueToothState = BLUETOOTH_ON;
            invalidateOptionsMenu();
            scanLeDevice();
        } else {
            mToolbar.setNavigationIcon(R.drawable.bluetooth_state_off);
            mBlueToothState = BLUETOOTH_OFF;
            invalidateOptionsMenu();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDeviceList.clear();
                    myAdapter.notifyDataSetChanged();
                }
            }, 500);
        }
    }

    //activity 回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    changedBlueToothEnable(true);
                }
        }
    }


    private class MyConn implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBtService = ((BtService.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBtService = null;

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        unbindService(myConn);
        unregisterReceiver(myBr);
    }

    public class BlueDevice {
        public BluetoothDevice device;
        public int rssi;
    }
}
