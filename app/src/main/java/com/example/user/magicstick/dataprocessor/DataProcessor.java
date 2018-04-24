package com.example.user.magicstick.dataprocessor;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.magicstick.ble.BtService;

import java.lang.reflect.Type;
import java.util.concurrent.SynchronousQueue;

/**
 * Created by user on 2018/4/19.
 */

public class DataProcessor {
    byte mType;
    int mNo;
    byte[] mData;
    BtService mBtService;

    public DataProcessor(BtService mBtService) {

        this.mBtService = mBtService;
    }

    private byte[] frame(byte mType, int mNo, byte[] mData) {
        byte[] f = new byte[19];
        byte[] data = mData;
        f[0] = (byte) (mNo | mType << 6);

        System.arraycopy(data, 0, f, 18 - (data.length - 1), data.length);
        CRC8CCIIT crc8CCIIT = new CRC8CCIIT(f);

        return crc8CCIIT.getResult();
    }

    public void Write(byte type, byte data[]) {

        mType = type;
        mData = data;

        for (BluetoothGattService bg : mBtService.getSupportedGattServices()) {
            if (bg.getUuid().toString().equals("0000ffe5-0000-1000-8000-00805f9b34fb")) {
                for (BluetoothGattCharacteristic bc : bg.getCharacteristics()
                        ) {
                    if ((bc.getUuid().toString()).equals("0000ffe9-0000-1000-8000-00805f9b34fb")) {
                        BluetoothGattCharacteristic characteristic = bc;
                        final int charaProp = characteristic.getProperties();
                        //如果该char可写
                        if ((charaProp | BluetoothGattCharacteristic.PERMISSION_WRITE) > 0) {
                            if (mData.length < 18 && type == 0) {
                                characteristic.setValue(frame((byte) 3, 0, mData));
                                synchronized (this) {
                                    mBtService.writeCharacteristic(characteristic);
                                }
                            } else {
                                Writes(characteristic);
                            }


                        }
                    }
                }
            }
        }
    }

    private void Writes(final BluetoothGattCharacteristic characteristic) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                final int len = mData.length;
                int sum = len / 18;
                if (sum * 18 < mData.length) {
                    sum += 1;
                }
                for (int i = 0; i < sum; i++) {
                    System.out.println("第" + i + "帧");
                    byte[] bytes;
                    if (i == sum - 1) {
                        bytes = new byte[len - i * 18];
                        mType = 3;
                        System.arraycopy(mData, (i * 18), bytes, 0, bytes.length);
                    } else {
                        bytes = new byte[18];
                        System.arraycopy(mData, (i * 18), bytes, 0, 18);
                    }

                    characteristic.setValue(frame(mType, i, bytes));
                    synchronized (this) {
                        mBtService.writeCharacteristic(characteristic);
                    }
                    try {
                        Thread.sleep(45);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }.start();
    }
}
