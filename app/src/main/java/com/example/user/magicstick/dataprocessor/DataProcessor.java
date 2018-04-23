package com.example.user.magicstick.dataprocessor;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.magicstick.ble.BtService;

import java.lang.reflect.Type;

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

    private byte[] frame() {
        byte[] f = new byte[19];
        byte[] data = mData;
        byte[] crc = {12};
        f[0] = (byte) (mNo | mType << 6);
        System.arraycopy(data, 0, f, 18-(data.length-1), data.length);
        CRC8CCIIT crc8CCIIT = new CRC8CCIIT(f);

        for (byte b : f
                ) {
            System.out.println(b);
        }
        return crc8CCIIT.getResult();
    }

    public void Write(byte type, int no, byte data[]){

        mType = type;
        mData = data;
        mNo = no;

        for (BluetoothGattService bg : mBtService.getSupportedGattServices()) {
            if (bg.getUuid().toString().equals("0000ffe5-0000-1000-8000-00805f9b34fb")) {
                for (BluetoothGattCharacteristic bc : bg.getCharacteristics()
                        ) {
                    if ((bc.getUuid().toString()).equals("0000ffe9-0000-1000-8000-00805f9b34fb")) {
                        BluetoothGattCharacteristic characteristic = bc;
                        final int charaProp = characteristic.getProperties();
                        //如果该char可写
                        if ((charaProp | BluetoothGattCharacteristic.PERMISSION_WRITE) > 0) {

                            characteristic.setValue(frame());
                            mBtService.writeCharacteristic(characteristic);
                        }
                    }
                }
            }
        }
    }
}
