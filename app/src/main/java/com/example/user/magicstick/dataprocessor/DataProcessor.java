package com.example.user.magicstick.dataprocessor;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import com.example.user.magicstick.ble.BtService;

import java.util.ArrayList;

/**
 * Created by user on 2018/4/19.
 */

public class DataProcessor {
    private byte mType;//0:发送正常帧 1：ack 2：补发帧 3：结束帧
    private byte[] mData;
    private BtService mBtService;
    private ArrayList<byte[]> frameList = new ArrayList<>();

    public DataProcessor(BtService mBtService) {

        this.mBtService = mBtService;
    }

    private byte[] frame(int mNo, byte[] mData) {
        byte[] f = new byte[19];
        byte[] data = mData;
        f[0] = (byte) (mNo | mType << 6);
        System.arraycopy(data, 0, f, 1, data.length);
        CRC8CCIIT crc8CCIIT = new CRC8CCIIT(f);
        return crc8CCIIT.getResult();
    }

    public ArrayList<byte[]> getFrame() {
        return frameList;
    }

    public void Write(byte type, byte data[]) {

        mType = type;
        mData = data;

        for (BluetoothGattService bg : mBtService.getSupportedGattServices()) {
            if (bg.getUuid().toString().equals("0000ffe5-0000-1000-8000-00805f9b34fb")) {
                for (BluetoothGattCharacteristic bc : bg.getCharacteristics()
                        ) {
                    if ((bc.getUuid().toString()).equals("0000ffe9-0000-1000-8000-00805f9b34fb")) {
                        final BluetoothGattCharacteristic characteristic = bc;
                        final int charaProp = characteristic.getProperties();
                        //如果该char可写
                        if ((charaProp | BluetoothGattCharacteristic.PERMISSION_WRITE) > 0) {
                            //发送数据
                            if (mData.length <= 18 && mType == 0) {
                                frameList.clear();
                                mType = 3;
                                frameList.add(frame(0, mData));

                                characteristic.setValue(frameList.get(0));
                                System.out.println("开始时间:" + System.nanoTime());
                                mBtService.writeCharacteristic(characteristic, null);
                                //发送
                            } else if (mData.length > 18 && mType == 0) {
                                frameList.clear();
                                Writes(characteristic);
                            } else if (mType == 2) { //补发数据
                                characteristic.setValue(mData);
                                try {
                                    Thread.sleep(20);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                mBtService.writeCharacteristic(characteristic, null);
                                //发送ack
                            } else if (mType == 1) {
                                characteristic.setValue(frame(0, mData));
                                try {
                                    Thread.sleep(40);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                mBtService.writeCharacteristic(characteristic, null);
                            }
                        }
                    }
                }
            }
        }
    }


    private void Writes(final BluetoothGattCharacteristic characteristic) {

        final int len = mData.length;
        int sum = len / 18;
        if (sum * 18 < mData.length) {
            sum += 1;
        }
        for (int i = 0; i < sum; i++) {
            System.out.println("第" + i + "帧");
            byte[] bytes;
            if (i == sum - 1 || i % 64 == 63) {
                if (i == sum - 1)
                    bytes = new byte[len - i * 18];
                else
                    bytes = new byte[18];
                mType = 3;
                System.arraycopy(mData, (i * 18), bytes, 0, bytes.length);
            } else {
                if (i % 64 == 0)
                    mType = 0;
                bytes = new byte[18];
                System.arraycopy(mData, (i * 18), bytes, 0, 18);
            }
            frameList.add(frame(i % 64, bytes));
        }
        byte[] b = new byte[80];
//        ArrayList<byte[]> frame = new ArrayList<>();
//        for (int i = 0; i<frameList.size();i++){
//            System.arraycopy(frameList.get(i),0,b,(i%4)*20,20);
//            if (i==3||(i%4==3)||i==frameList.size()-1){
//                frame.add(b);
//            }
//        }
        characteristic.setValue(frameList.get(0));
        mBtService.writeCharacteristic(characteristic, frameList);
    }
}
