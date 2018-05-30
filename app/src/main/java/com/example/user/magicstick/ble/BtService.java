package com.example.user.magicstick.ble;


import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.companion.BluetoothDeviceFilter;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.example.user.magicstick.activite.MagicStickActivity;
import com.example.user.magicstick.sharedpreferences.PropertiesUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2018/4/16.
 */

public class BtService extends Service {

    private final static String TAG = BtService.class.getSimpleName();

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";


    private int no;
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {


        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            Log.d("StateChange---status", String.valueOf(status));
            Log.d("StateChange---new", String.valueOf(newState));
            //连接
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                startActivity(mIntent);
                mBluetoothGatt.discoverServices();
                PropertiesUtil propertiesUtil = PropertiesUtil.getInstance();
                propertiesUtil.setValue("BlueAddress", mDevice.getAddress());
                gatt.readRemoteRssi();

                //断开
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            System.out.println(rssi);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                for (BluetoothGattService bg : gatt.getServices()) {
                    Log.d(TAG, bg.getUuid().toString());
                    if (bg.getUuid().toString().equals("0000ffe0-0000-1000-8000-00805f9b34fb")) {
                        for (BluetoothGattCharacteristic bc : bg.getCharacteristics()
                                ) {
                            if ((bc.getUuid().toString()).equals("0000ffe4-0000-1000-8000-00805f9b34fb")) {
                                BluetoothGattCharacteristic characteristic = bc;
                                final int charaProp = characteristic.getProperties();
                                //如果该char可读
                                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                                    setCharacteristicNotification(characteristic, true);
                                }
                            }

                        }
                    }

                }


            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            } else
                Log.d(TAG, "is status------------->" + status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

            if (characteristic.getValue().length > 10) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            Log.d(TAG, String.valueOf(status));
            if (status == 1 && fram != null || fram.size() > 1 && no < fram.size()) {
                characteristic.setValue(fram.get(no));
                mBluetoothGatt.writeCharacteristic(characteristic);
                no++;
                System.out.println((System.nanoTime() - startTime));
            }
        }
    };

    private Intent mIntent;
    private BluetoothDevice mDevice;
    private ArrayList<byte[]> fram;
    private BluetoothGattCharacteristic characteristic;
    private long startTime;

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        Log.d(TAG, "onCharacteristicRead");
        sendBroadcast(intent);
    }

    //有数据接收
    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {

        final Intent intent = new Intent(action);
        StringBuffer stringBuffer = new StringBuffer();
        for (byte b : characteristic.getValue()
                ) {
            stringBuffer.append(Integer.toHexString(b));
        }
        System.out.println(stringBuffer.toString());
        intent.putExtra("data", characteristic.getValue());
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        public BtService getService() {
            return BtService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        close();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        mDevice = mBluetoothAdapter.getRemoteDevice(address);
        if (mDevice == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = mDevice.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mConnectionState = STATE_CONNECTING;
        mIntent = new Intent(this, MagicStickActivity.class);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic, ArrayList<byte[]> fram) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        System.out.println(startTime = System.nanoTime());
        no = 1;
        this.fram = fram;
        this.characteristic = characteristic;
        for (byte[] b : fram
                ) {
            StringBuffer stringBuffer = new StringBuffer();
            for (byte c : b
                    ) {
                stringBuffer.append(Integer.toHexString(c&0xff));
                stringBuffer.append(",");
            }
            System.out.println("aaaaa---------->"+stringBuffer.toString());
        }
        System.out.println(mBluetoothGatt.writeCharacteristic(characteristic));
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);


    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    public String getBtDevice() {
        return mDevice.getName();
    }
}