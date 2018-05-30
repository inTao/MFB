package com.example.user.magicstick.dataprocessor;

import java.util.ArrayList;

/**
 * Created by user on 2018/4/24.
 */

public class BackData {
    private CRC8CCIIT crc8CCIIT;
    private byte mNo;
    private byte[] mBackData, mData;
    private byte mType;
    private byte mCrcResult;
    private int result;
    private ArrayList<byte[]> packageList = new ArrayList<>();
    private ArrayList<Integer> errorList = new ArrayList<>();
    private byte[] ackByte;
    private boolean isError = false;
    private int size;
    private int errorSize;

    public final static int ACK_HASERROR = 1;
    public final static int RXD_SUCCEED = 2;
    public final static int ACK_ALL_RIGHT = 3;
    public final static int RXD_REPLENISH = 4;

    public BackData() {
        mNo = 0;
        size = 0;
        errorSize = 0;
    }

    public int Processor(byte[] data) {
        result = 0;
        crc8CCIIT = new CRC8CCIIT(data);
        mBackData = crc8CCIIT.getResult();
        mData = new byte[mBackData.length - 3];
        mType = (byte) ((data[0] & 0xff) >> 6);
        size = mNo;
        mNo = (byte) (mBackData[0] & 0x3f);
        if ((mNo != (size + 1) && mNo != 0 && mType != 2) || (ackByte == null && mNo != 0 && mType != 2)) {
            if (ackByte == null) {
                result = 0;
                errorSize = 0;
                ackByte = new byte[18];
                packageList.clear();
            }
            for (int i = size; i < mNo; i++) {
                packageList.add(new byte[18]);
                ackByte[size / 8] |= 1 << (size % 8);
                errorSize++;
                size++;
            }

            isError = true;
        }
        mCrcResult = mBackData[mBackData.length - 1];
        System.arraycopy(mBackData, 1, mData, 0, mData.length);

        if (mCrcResult == 0) {
            switch (mType) {
                case 3:
                case 0://接收到数据
                    if (mNo == 0) {
                        result = 0;
                        errorSize = 0;
                        ackByte = new byte[18];
                        packageList.clear();
                        packageList.add(mData);
                        isError = true;
                    } else {
                        if (ackByte == null) {
                            packageList.clear();
                            result = 0;
                            ackByte = new byte[18];
                            ackByte[0] |= 1 << (0);
                            errorSize ++;
                            packageList.add(new byte[18]);
                            isError = true;
                        }
                        packageList.add(mData);
                    }
                    if (mType == 0) {
                        break;
                    }
                    packageList.add(ackByte);
                    ackByte = null;
                    if (!isError) {
                        result = RXD_REPLENISH;
                    } else
                        result = RXD_SUCCEED;
                    size = 0;
                    mNo = 0;
                    break;
                case 1: //得到ACK
                    int z = 0;
                    for (byte b : mData
                            ) {
                        for (int i = 0; i <= 7; i++) {
                            if (((b & (1 << i)) >> i) == 1) {
                                result = ACK_HASERROR;
                                errorList.add(z);
                                System.out.println("第" + z + "帧有错误");
                            }
                            z++;
                        }
                    }
                    if (result == 0) {
                        result = ACK_ALL_RIGHT;
                    }
                    break;
                case 2://接收补发的数据
                    result = 0;
                    packageList.set(mNo, mData);
                    errorSize--;
                    if (errorSize == 0)
                        result = RXD_REPLENISH;
                    break;
            }
        } else {//错误信息
            isError = true;
            if (mNo == 0) {
                ackByte = new byte[18];
                packageList.clear();
            }
            if (ackByte == null) {
                ackByte = new byte[18];
                ackByte[0] |= 1 << (0);
            }
            ackByte[mNo / 8] |= 1 << (mNo % 8);

            if (mType == 3) {
                packageList.add(ackByte);
                result = RXD_SUCCEED;
            } else
                packageList.add(new byte[18]);
            errorSize++;
        }
        return result;
    }

    public ArrayList<Integer> getErrorList() {
        return errorList;
    }

    public ArrayList<byte[]> getPackageList() {
        return packageList;
    }
}
