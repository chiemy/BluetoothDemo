package com.chiemy.bluetoothdemo;

import android.bluetooth.BluetoothDevice;

import java.util.Arrays;

/**
 * Created: chiemy
 * Date: 17/12/14
 * Description:
 */

public class BluetoothDeviceWrapper {
    private BluetoothDevice mDevice;
    private int mRssi;
    private byte[] mScanRecord;

    public BluetoothDeviceWrapper(BluetoothDevice device) {
        mDevice = device;
    }

    public BluetoothDeviceWrapper(BluetoothDevice device, int rssi, byte[] scanRecord) {
        mDevice = device;
        mRssi = rssi;
        mScanRecord = scanRecord;
    }

    public BluetoothDevice getDevice() {
        return mDevice;
    }

    public int getRssi() {
        return mRssi;
    }

    public byte[] getScanRecord() {
        return mScanRecord;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BluetoothDeviceWrapper that = (BluetoothDeviceWrapper) o;

        return mDevice != null ? mDevice.equals(that.mDevice) : that.mDevice == null;
    }

    @Override
    public int hashCode() {
        return mDevice != null ? mDevice.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "BluetoothDeviceWrapper{" +
                "mDevice=" + mDevice +
                ", mRssi=" + mRssi +
                ", mScanRecord=" + Arrays.toString(mScanRecord) +
                '}';
    }
}
