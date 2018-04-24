package com.example.user.magicstick.dataprocessor;

import android.content.Intent;

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
    private boolean hasError;
    private ArrayList<Integer> errorList = new ArrayList<>();

    public BackData(byte[] data) {
        crc8CCIIT = new CRC8CCIIT(data);
    }

    public boolean Processor() {
        hasError = false;
        mBackData = crc8CCIIT.getResult();
        mData = new byte[mBackData.length - 3];
        mType = (byte) ((mBackData[0] & 0xff) >> 6);
        mNo = (byte) (mBackData[0] & 0x3f);
        mCrcResult = mBackData[mBackData.length - 1];
        System.arraycopy(mBackData, 1, mData, 0, mData.length);
        if (mCrcResult == 0) {
            switch (mType) {
                case 0:
                    break;
                case 1:
                    int z = 0;
                    for (byte b : mData
                            ) {
                        for (int i = 7; i >= 0; i--) {
                            if (((b & (1 << i)) >> i) == 1) {
                                hasError = false;
                                errorList.add(z);
                                System.out.println("第二帧有错误");
                            }
                            z++;
                        }
                    }
                    break;
                case 2:
                    break;
                case 3:
                    break;
            }
        }
        return hasError;
    }

    public ArrayList<Integer> getErrorList() {
        return errorList;
    }

}
