package com.chiemy.bluetoothdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created: chiemy
 * Date: 17/12/14
 * Description:
 */

public class BluetoothDiscovererReceiver extends BroadcastReceiver {
    private IntentFilter mDiscoveryFilter;
    private Context mContext;
    private OnBluetoothDeviceDiscoveryListener mDiscoveryListener;

    private List<BluetoothDeviceWrapper> mBluetoothDevices;

    private boolean mIsRegister;

    private String[] mFilterMacs;

    private int mEntireMatchCount;

    private FilterMatchListener mFilterMatchListener;

    public BluetoothDiscovererReceiver(Context context) {
        mContext = context.getApplicationContext();
        mDiscoveryFilter = new IntentFilter(android.bluetooth.BluetoothDevice.ACTION_FOUND);
        mDiscoveryFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        mDiscoveryFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        mBluetoothDevices = new ArrayList<>();
    }

    public void setDiscoveryListener(OnBluetoothDeviceDiscoveryListener listener) {
        mDiscoveryListener = listener;
    }

    public void register() {
        if (!mIsRegister) {
            mIsRegister = true;
            mContext.registerReceiver(this, mDiscoveryFilter);
        }
    }

    public void unregiser() {
        if (mIsRegister) {
            mIsRegister = false;
            mContext.unregisterReceiver(this);
        }
    }

    public void setFilter(String[] macs) {
        mFilterMacs = macs;
    }

    public void setFilterMatchListener(FilterMatchListener filterMatchListener) {
        mFilterMatchListener = filterMatchListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!mIsRegister) {
            return;
        }
        String action = intent.getAction();
        // When discovery finds a device
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            // Get the BluetoothDevice object from the Intent
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            BluetoothDeviceWrapper deviceWrapper = new BluetoothDeviceWrapper(device);
            if (mBluetoothDevices.contains(deviceWrapper)) {
                return;
            }
            // Mac 地址是否匹配
            boolean macMatched = true;
            if (mFilterMacs != null && mFilterMacs.length > 0) {
                macMatched = false;
                String[] macs = mFilterMacs;
                for (int i = 0; i < macs.length; i++) {
                    if (TextUtils.equals(macs[i], device.getAddress())) {
                        mEntireMatchCount++;
                        // 所有特定设备都找到了
                        if (mEntireMatchCount == macs.length
                                && mFilterMatchListener != null) {
                            mFilterMatchListener.onAllMatched();
                        }
                        macMatched = true;
                        break;
                    } else if (device.getAddress().contains(macs[i])) {
                        macMatched = true;
                        break;
                    }
                }
            }
            if (macMatched) {
                mBluetoothDevices.add(deviceWrapper);
                if (mDiscoveryListener != null) {
                    mDiscoveryListener.onBluetoothDeviceDiscoveryFound(deviceWrapper, mBluetoothDevices);
                }
            }
        } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
            mEntireMatchCount = 0;
            mBluetoothDevices.clear();
            if (mDiscoveryListener != null) {
                mDiscoveryListener.onBluetoothDeviceDiscoveryStarted();
            }
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            if (mDiscoveryListener != null) {
                mDiscoveryListener.onBluetoothDeviceDiscoveryFinished(mBluetoothDevices);
            }
        }
    }

    public interface FilterMatchListener {
        void onAllMatched();
    }
}
