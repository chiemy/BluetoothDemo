package com.chiemy.bluetoothdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import java.lang.reflect.Method;

/**
 * Created: chiemy
 * Date: 17/12/13
 * Description:
 */

public final class BluetoothUtil {
    /**
     * 蓝牙是否开启
     */
    public static boolean isBluetoothEnable() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null){
            return false;
        }
        return mBluetoothAdapter.isEnabled();
    }

    /**
     * 开启蓝牙
     */
    public static boolean enableBluetooth() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null){
            return false;
        }
        if(!mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.enable();
        }
        return true;
    }

    /**
     * 关闭蓝牙
     */
    public static void disableBluetooth(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null){
            return;
        }
        if(mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.disable();
        }
    }

    public static boolean isBleSupported(Context context) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                && context != null
                && context.getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public static void ensureDiscoverable(Context context) {
        if (BluetoothAdapter.getDefaultAdapter().getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            context.startActivity(discoverableIntent);
        }
    }

    public static boolean unpair(BluetoothDevice pairedDevice) {
        boolean success = false;
        try {
            Method method = pairedDevice.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(pairedDevice, (Object[]) null);
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }
}
