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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.magicstick.dataprocessor.BackData;
import com.example.user.magicstick.dataprocessor.CRC8CCIIT;
import com.example.user.magicstick.dataprocessor.DataProcessor;
import com.example.user.magicstick.R;
import com.example.user.magicstick.ble.BtService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.util.ArrayList;

/**
 * Created by user on 2018/4/16.
 */

public class MagicStickActivity extends AppCompatActivity implements View.OnClickListener {

    private String TAG = MainActivity.class.getSimpleName();

    private BluetoothAdapter mBtAdaper;
    private MyConn myConn;
    private BtService mBtService;
    private MyBr myBr;

    private Toolbar mToolbar;
    private DataProcessor mProcessor;
    private Button mRButton, mSButton;
    private int packageSum;

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
        mRButton = findViewById(R.id.button);
        mSButton = findViewById(R.id.button2);
        mRButton.setOnClickListener(this);
        mSButton.setOnClickListener(this);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                Intent intent = new Intent(this, ReadActivity.class);
                startActivity(intent);
                break;
            case R.id.button2:
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            InputStream i = getAssets().open("pwn.bin");
                            int len = i.available();
                            byte[] buffer = new byte[len];
                            i.read(buffer);
                            getAddress(buffer);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
                break;
        }
    }

    private Object upDataObject = new Object();

    //处理包地址
    private void getAddress(byte[] buffer) {
        //包地址
        int[] address = {0x7000, 0x747D, 0x78FA, 0x7D77, 0x81F4, 0x8671, 0x8AEE, 0x8F6B, 0x93E8, 0x9865};
        //包的个数
        double p = buffer.length / 1150;
        packageSum = (int) p;
        if (p * 1150 != buffer.length) {
            packageSum++;
        }
        //处理数据
        for (int i = 0; i < packageSum; i++) {
            //得到每个地址 转化为俩个byte
            String s = Integer.toBinaryString(address[i]);
            char[] c = s.toCharArray();
            String b = "";
            String b1 = "";
            int minus = 16 - c.length;
            if (minus != 0) {
                for (int a = 0; a < minus; a++) {
                    b = b + "0";
                }
            }
            for (int a = 0; a < c.length; a++) {
                if (a < 8 - minus) {
                    b = b + c[a];
                } else {
                    b1 = b1 + c[a];
                }
            }
            byte[] bytes = new byte[1152];
            if (i == packageSum - 1) {
                int len = buffer.length - i * 1149;
                bytes = new byte[len+3];
                System.arraycopy(buffer, i * 1149, bytes, 3, len);
            } else {
                System.arraycopy(buffer, i * 1149, bytes, 3, 1149);

            }
            bytes[0] = 0x01;
            bytes[1] = (byte) ((int) (Integer.valueOf(b, 2)));
            bytes[2] = (byte) ((int) (Integer.valueOf(b1, 2)));
            mProcessor.Write((byte) 0, bytes);
            synchronized (upDataObject) {
                try {
                    upDataObject.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class MyConn implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBtService = ((BtService.LocalBinder) service).getService();
            mProcessor = new DataProcessor(mBtService);
            String s = mBtService.getBtDevice();
            mToolbar.setTitle("名称： " + s);
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

    private BackData mBackData = new BackData();

    private class MyBr extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //接收数据
            if (action.equals(BtService.ACTION_DATA_AVAILABLE)) {
                //获取crc验证后的数据
                int dataResult = mBackData.Processor(intent.getByteArrayExtra("data"));
                StringBuffer stringBuffer1 = new StringBuffer();
                for (byte c : intent.getByteArrayExtra("data")) {
                    stringBuffer1.append(Integer.toHexString(c & 0xff) + ",");
                }
                TextView textView = findViewById(R.id.textView3);
                textView.append(stringBuffer1.toString() + "\n");
                if (dataResult == mBackData.ACK_HASERROR) {
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            for (int e : mBackData.getErrorList()) {
                                byte[] bytes = new byte[19];
                                System.arraycopy((mProcessor.getFrame().get(e)), 0, bytes, 0, 19);
                                bytes[0] = (byte) ((bytes[0] & 0x3f) | 0x80);
                                CRC8CCIIT crc8CCIIT = new CRC8CCIIT(bytes);
                                mProcessor.Write((byte) 2, crc8CCIIT.getResult());
                                StringBuffer stringBuffer = new StringBuffer();
                                for (byte b : bytes
                                        ) {
                                    stringBuffer.append(Integer.toHexString(b & 0xff));
                                }
                                System.out.println(stringBuffer.toString());
                            }
                        }
                    }.start();

                } else if (dataResult == mBackData.ACK_ALL_RIGHT) {
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            synchronized (upDataObject) {
                                upDataObject.notify();
                            }
                        }
                    }.start();

                } else if (dataResult == mBackData.RXD_SUCCEED) {
                    ArrayList<byte[]> pList = mBackData.getPackageList();
                    byte[] ackByte = pList.get(pList.size() - 1);
                    pList.remove(pList.size() - 1);
                    mProcessor.Write((byte) 1, ackByte);
                    for (byte[] b : pList
                            ) {
                        StringBuffer buffer = new StringBuffer();
                        for (byte b1 : b
                                ) {
                            buffer.append(Integer.toHexString(b1 & 0xff));
                        }
                        System.out.println(buffer.toString());
                    }
                } else if (dataResult == mBackData.RXD_REPLENISH) {
                    System.out.println("补发接收完成");
                    ArrayList<byte[]> pList = mBackData.getPackageList();
                    mProcessor.Write((byte) 1, new byte[18]);
                    for (byte[] b : pList
                            ) {
                        StringBuffer buffer = new StringBuffer();
                        for (byte b1 : b
                                ) {
                            buffer.append(Integer.toHexString(b1 & 0xff));
                        }
                        System.out.println(buffer.toString());
                    }
                }
            }
        }
    }
}
