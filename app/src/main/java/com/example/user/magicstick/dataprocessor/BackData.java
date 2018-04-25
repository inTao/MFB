package com.example.user.magicstick.dataprocessor;

import java.util.ArrayList;

/**
 * Created by user on 2018/4/24.
 */

public class BackData {
    private final CRC8CCIIT crc8CCIIT;
    private byte mNo = 0;
    private byte[] mBackData, mData;
    private byte mType;
    private byte mCrcResult;
    private int result;
    private ArrayList<byte[]> packageList = new ArrayList<>();
    private ArrayList<Integer> errorList = new ArrayList<>();
    private byte[] ackByte;

    public final static int ACK_HASERROR = 1;
    public final static int RXD_SUCCEED = 2;

    public BackData(byte[] data) {
        crc8CCIIT = new CRC8CCIIT(data);
    }

    public int Processor() {
        mBackData = crc8CCIIT.getResult();
        mData = new byte[mBackData.length - 3];
        mType = (byte) ((mBackData[0] & 0xff) >> 6);
        mNo = (byte) (mBackData[0] & 0x3f);
        mCrcResult = mBackData[mBackData.length - 1];
        System.arraycopy(mBackData, 1, mData, 0, mData.length);
        if (mCrcResult == 0) {
            switch (mType) {
                case 3:
                case 0://接收到数据
                    if (mNo == 0) {
                        ackByte = new byte[18];
                        packageList.clear();
                        packageList.add(mData);
                    } else {
                        packageList.add(mData);
                    }
                    if (mType == 0) {
                        break;
                    }
                    packageList.add(ackByte);
                    result = RXD_SUCCEED;
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
                    break;
                case 2:
                    break;
            }
        } else {
            ackByte[(mNo / 8) + 1] |= 1 << (mNo % 8);
            if (mType == 3) {
                packageList.add(ackByte);
                result = RXD_SUCCEED;
            }
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
