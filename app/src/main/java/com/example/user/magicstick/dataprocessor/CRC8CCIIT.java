package com.example.user.magicstick.dataprocessor;

/**
 * Created by user on 2018/4/21.
 */

public class CRC8CCIIT {
    private final byte[] mResult;
    private int crc;

    CRC8CCIIT(byte[] b) {
        this.crc = 0x0;
        mResult = new byte[b.length + 1];
        int j = 0;
        for (byte i : b) {
            CRC8(i);
            mResult[j++] = i;
        }
        Ref();
        mResult[j++] = (byte) (this.crc & 0xff);

    }

    public byte[] getResult() {
        return mResult;
    }

    private void CRC8(byte data) {//CRC-8/CCITT        x8+x6+x4+x3+x2+x1
        int G = 0x5E;//x8+x6+x4+x3+x2+x1
        int i = 1;
        for (int j = 0; j < 8; j++) {
            if ((this.crc & 0x80) != 0) {
                this.crc <<= 1;
                this.crc ^= G;
            } else {
                this.crc <<= 1;
            }
            if ((data & 0xff & i) != 0) {
                this.crc ^= G;
            }
            i <<= 1;

        }
    }
    private void Ref() {
        int c = 0;
        for (int i = 0; i < 8; i++) {
            c <<= 1;
            c |= this.crc & 1;
            this.crc >>= 1;
        }
        this.crc = c;
    }

}
