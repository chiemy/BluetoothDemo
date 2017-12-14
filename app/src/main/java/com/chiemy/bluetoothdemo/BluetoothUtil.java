package com.chiemy.bluetoothdemo;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

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
}
