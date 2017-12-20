package com.chiemy.bluetoothdemo;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.UUID;

/**
 * Created: chiemy
 * Date: 17/12/20
 * Description:
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BlueBikeSensor {

    // Cycling Speed and Cadence
    // Assigned Number: 0x1816
    // https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.service.cycling_speed_and_cadence.xml&u=org.bluetooth.service.cycling_speed_and_cadence.xml
    public static final UUID CSC_SERVICE_UUID = UUID.fromString
            ("00001816-0000-1000-8000-00805f9b34fb");

    public enum ConnectionState {
        INIT,
        CONNECTED,
        ERROR,
    }

    ;

    public interface Callback {
        void onConnectionStateChange(BlueBikeSensor sensor,
                                     BlueBikeSensor.ConnectionState newState);

        void onSpeedUpdate(BlueBikeSensor sensor, double distance, double elapsedUs);

        void onCadenceUpdate(BlueBikeSensor sensor, int rotations, double elapsedUs);
    }

    // CSC Measurement
    // Assigned Number: 0x2a5b
    // https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.characteristic.csc_measurement.xml
    private static final UUID CSC_MEASUREMENT_UUID = UUID.fromString
            ("00002a5b-0000-1000-8000-00805f9b34fb");

    // CSC Feature
    // Assigned Number: 0x2a5c
    // https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.characteristic.csc_feature.xml
    private static final UUID CSC_FEATURE_UUID = UUID.fromString
            ("00002a5c-0000-1000-8000-00805f9b34fb");

    // Client Characteristic Configuration
    // Assigned Number: 0x2902
    // https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.descriptor.gatt.client_characteristic_configuration.xml
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString
            ("00002902-0000-1000-8000-00805f9b34fb");

    private static final String TAG = BlueBikeSensor.class.getSimpleName();

    private ConnectionState mState;
    private double mCircumference;
    private boolean mEnabled;
    private String mError;

    private Context mContext;
    private Callback mCallback;

    private BluetoothDevice mBluetoothDevice;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic mFeatureChar;
    private BluetoothGattCharacteristic mMeasurementChar;

    private boolean mHasWheel, mHasCrank;

    private boolean mWheelStopped, mCrankStopped;
    private long mLastWheelReading;
    private int mLastCrankReading;
    private int mLastWheelTime, mLastCrankTime;

    private BluetoothGattCallback mBluetoothGattCb = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            BlueBikeSensor parent = BlueBikeSensor.this;

            if (status != BluetoothGatt.GATT_SUCCESS) {
                doError("Error connecting to device");
                return;
            }

            if (parent.mState == ConnectionState.INIT
                    && newState == BluetoothGatt.STATE_CONNECTED) {
                Log.d(TAG, "Connected to device");

                if (!parent.mBluetoothGatt.discoverServices()) {
                    doError("Error trying to discover services");
                    return;
                }
            }

            // FIXME: We probably need to handle connection / disconnection events after init
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                doError("Error while discovering services");
                return;
            }

            Log.d(TAG, "Services discovered");

            BluetoothGattService service = mBluetoothGatt.getService(CSC_SERVICE_UUID);

            mFeatureChar = service.getCharacteristic(CSC_FEATURE_UUID);
            mMeasurementChar = service.getCharacteristic(CSC_MEASUREMENT_UUID);

            gatt.readCharacteristic(mFeatureChar);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic
                characteristic, int status) {
            BlueBikeSensor parent = BlueBikeSensor.this;

            if (status != BluetoothGatt.GATT_SUCCESS) {
                doError("Error reading characteristic " + characteristic);
                return;
            }

            if (characteristic == parent.mFeatureChar) {
                int flags = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16,
                        0);

                parent.mHasWheel = (flags & 0x1) != 0;
                parent.mHasCrank = (flags & 0x2) != 0;

                // Now we've got all the information we need to start collecting data
                parent.mState = ConnectionState.CONNECTED;

                parent.mCallback.onConnectionStateChange(parent, mState);
            }
        }

        // Once notifications are enabled for a characteristic,
        // onCharacteristicChanged() callback is triggered if the characteristic changes on the remote device
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            BlueBikeSensor parent = BlueBikeSensor.this;
            boolean hasWheel, hasCrank;
            // 轮子转的圈数
            long wheelRotations;
            // 曲轴转的圈数
            int crankRotations;
            int time;

            // We'll only ever be notified on the measurement characteristic

            byte[] value = parent.mMeasurementChar.getValue();

            if (value.length < 1) {
                Log.w(TAG, "Bad measurement size " + value.length);
                return;
            }

            // 轮子
            hasWheel = (value[0] & 0x1) != 0;
            // 曲柄（脚蹬着转的轴）
            hasCrank = (value[0] & 0x2) != 0;

            if ((hasWheel && hasCrank && value.length < 11) || (hasWheel && value.length < 7) ||
                    (hasCrank && value.length < 5)) {
                Log.w(TAG, "Bad measurement size " + value.length);
                return;
            }

            int i = 1;

            // Note: We only send out a delta update when we have a meaningful
            // delta. If the user was coasting or stopped, the last update will
            // be from a long ago, making the delta meaningless for both
            // instantaneous and average calculations.

            if (hasWheel) {
                wheelRotations = readU32(value, i);
                time = readU16(value, i + 4);

                if (wheelRotations == 0) {
                    // 轮子停止运动
                    mWheelStopped = true;

                } else if (mWheelStopped) {
                    // 轮子又开始动了
                    mWheelStopped = false;
                    mLastWheelReading = wheelRotations;
                    mLastWheelTime = time;

                    parent.mCallback.onSpeedUpdate(parent, 0, 0.0);

                } else {
                    // 距离上次更新的时长
                    int timeDiff;

                    if (wheelRotations < mLastWheelReading) {
                        // Can happen if bicycle reverses
                        // 逆向
                        wheelRotations = 0;
                    }

                    timeDiff = do16BitDiff(time, mLastWheelTime);

                    parent.mCallback.onSpeedUpdate(
                            parent,
                            (wheelRotations - mLastWheelReading) * mCircumference,
                            (timeDiff * 1000000.0) / 1024.0);

                    mLastWheelReading = wheelRotations;
                    mLastWheelTime = time;
                }

                i += 6;
            }

            if (hasCrank) {
                crankRotations = readU16(value, i);
                time = readU16(value, i + 2);

                if (crankRotations == 0) {
                    // Coasting or stopped
                    // 惯性或停止
                    mCrankStopped = true;

                } else if (mCrankStopped) {
                    // Crank's started up again
                    // 曲轴开始动
                    mCrankStopped = false;
                    mLastCrankReading = crankRotations;
                    mLastCrankTime = time;

                    parent.mCallback.onCadenceUpdate(parent, 0, 0.0);

                } else {
                    // Delta over last update
                    int rotDiff, timeDiff;

                    rotDiff = do16BitDiff(crankRotations, mLastCrankReading);
                    timeDiff = do16BitDiff(time, mLastCrankTime);

                    parent.mCallback.onCadenceUpdate(parent, rotDiff, (timeDiff * 1000000.0) / 1024.0);

                    mLastCrankReading = crankRotations;
                    mLastCrankTime = time;
                }
            }
        }
    };

    private void doError(String error) {
        Log.w(TAG, error);

        mError = error;
        mState = ConnectionState.ERROR;

        mCallback.onConnectionStateChange(this, mState);

        return;
    }

    private int do16BitDiff(int a, int b) {
        if (a >= b)
            return a - b;
        else
            return (a + 65536) - b;
    }

    private int readU32(byte[] bytes, int offset) {
        // Does not perform bounds checking
        return ((bytes[offset + 3] << 24) & 0xff000000) +
                ((bytes[offset + 2] << 16) & 0xff0000) +
                ((bytes[offset + 1] << 8) & 0xff00) +
                (bytes[offset] & 0xff);
    }

    private int readU16(byte[] bytes, int offset) {
        return ((bytes[offset + 1] << 8) & 0xff00) + (bytes[offset] & 0xff);
    }

    /**
     *
     * @param context
     * @param device 蓝牙设备
     * @param diameter 轮胎半径
     * @param callback 回调
     */
    public BlueBikeSensor(Context context,
                          BluetoothDevice device,
                          double diameter,
                          BlueBikeSensor.Callback callback) {
        mState = ConnectionState.INIT;
        mContext = context;
        mBluetoothDevice = device;
        // 周长
        mCircumference = diameter * Math.PI;
        mCallback = callback;
        // Connecting to a GATT Server
        mBluetoothGatt = device.connectGatt(mContext, false, mBluetoothGattCb);

        mWheelStopped = mCrankStopped = true;
    }

    public boolean hasSpeed() {
        if (mState != ConnectionState.CONNECTED)
            throw new IllegalStateException("Not connected");

        return mHasWheel;
    }

    public boolean hasCadence() {
        if (mState != ConnectionState.CONNECTED)
            throw new IllegalStateException("Not connected");

        return mHasCrank;
    }

    public String getError() {
        return mError;
    }

    /**
     * Should notified when a particular characteristic changes on the device
     */
    public void setNotificationsEnabled(boolean enable) {
        if (mState != ConnectionState.CONNECTED)
            throw new IllegalStateException("Not connected");

        if (enable == mEnabled)
            return;

        mEnabled = enable;

        mBluetoothGatt.setCharacteristicNotification(mMeasurementChar, mEnabled);
        BluetoothGattDescriptor descriptor = mMeasurementChar.getDescriptor
                (CLIENT_CHARACTERISTIC_CONFIG);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
    }
}