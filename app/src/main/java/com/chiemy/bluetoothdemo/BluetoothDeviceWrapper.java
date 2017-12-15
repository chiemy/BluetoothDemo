package com.chiemy.bluetoothdemo;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

/**
 * Created: chiemy
 * Date: 17/12/14
 * Description:
 */

public class BluetoothDeviceWrapper implements Parcelable {
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

    protected BluetoothDeviceWrapper(Parcel in) {
        mDevice = in.readParcelable(BluetoothDevice.class.getClassLoader());
        mRssi = in.readInt();
        mScanRecord = in.createByteArray();
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

    public static final Creator<BluetoothDeviceWrapper> CREATOR = new
            Creator<BluetoothDeviceWrapper>() {
                @Override
                public BluetoothDeviceWrapper createFromParcel(Parcel in) {
                    return new BluetoothDeviceWrapper(in);
                }

                @Override
                public BluetoothDeviceWrapper[] newArray(int size) {
                    return new BluetoothDeviceWrapper[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mDevice, flags);
        dest.writeInt(mRssi);
        dest.writeByteArray(mScanRecord);
    }
}
