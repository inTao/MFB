package com.example.user.magicstick.activite;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;
import com.example.user.magicstick.dataprocessor.DataProcessor;
import com.example.user.magicstick.R;
import com.example.user.magicstick.ble.BtService;


/**
 * Created by user on 2018/4/16.
 */

public class MagicStickActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();

    private BluetoothAdapter mBtAdaper;
    private MyConn myConn;
    private BtService mBtService;
    private MyBr myBr;

    private Toolbar mToolbar;
    private DataProcessor mProcessor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_magicstick);
        init();
        register();

    }

    private void init() {
        mBtAdaper = BluetoothAdapter.getDefaultAdapter();
        mToolbar = findViewById(R.id.magicToolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MagicStickActivity.this, MainActivity.class);
                startActivity(intent);
                mBtService.disconnect();
                finish();
            }
        });
    }


    //绑定服务和注册广播
    private void register() {
        //绑定服务部分
        Intent btIntent = new Intent(this, BtService.class);
        myConn = new MyConn();
        bindService(btIntent, myConn, BIND_AUTO_CREATE);

        //注册广播部分
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BtService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BtService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BtService.ACTION_DATA_AVAILABLE);
        myBr = new MyBr();
        registerReceiver(myBr, intentFilter);
    }

    public void onClick(View v) {
        Intent intent = new Intent(this,ReadActivity.class);
        startActivity(intent);
    }

    private class MyConn implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBtService = ((BtService.LocalBinder) service).getService();
            mProcessor = new DataProcessor(mBtService);
            String s = mBtService.getBtDevice();
            mToolbar.setTitle("名称： " + s);
            //mProcessor.Write((byte) 3,0,new byte[]{0x01});
            Toast.makeText(MagicStickActivity.this, s, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (mBtService != null)
                mBtService = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(myConn);
        unregisterReceiver(myBr);
        mBtService = null;
        System.out.println("我消失了了");
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!mBtAdaper.isEnabled()) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();

        }
    }

    private class MyBr extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //接收数据
            if (action.equals(BtService.ACTION_DATA_AVAILABLE)) {
                byte[] bytes = intent.getByteArrayExtra("data");
                StringBuffer stringBuffer = new StringBuffer();

                for (byte b : bytes
                        ) {
                    stringBuffer.append(Integer.toBinaryString(b) + ",");
                }
                System.out.println(stringBuffer.toString());
            }
        }
    }
}
