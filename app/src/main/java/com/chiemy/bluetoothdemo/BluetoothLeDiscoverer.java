package com.chiemy.bluetoothdemo;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created: chiemy
 * Date: 17/12/14
 * Description:
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class BluetoothLeDiscoverer implements BluetoothDiscoverer, BluetoothAdapter.LeScanCallback {
    private BluetoothAdapter mBluetoothAdapter;

    private OnBluetoothDeviceDiscoveryListener mDiscoveryListener;

    private DiscoveryRuleConfig mRuleConfig;

    private String[] mFilterMacs;

    private boolean mDiscovering;

    private List<BluetoothDeviceWrapper> mBluetoothDevices;

    private int mEntireMatchCount;

    private Handler mUIHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothConstants.MSG_CANCEL_DISCOVERY:
                    if (mDiscoveryListener != null) {
                        mDiscoveryListener.onBluetoothDeviceDiscoveryCancel();
                    }
                    break;
            }
        }
    };

    private static BluetoothLeDiscoverer instance = null;

    private BluetoothLeDiscoverer(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothDevices = new ArrayList<>();
    }

    public static BluetoothLeDiscoverer getInstance() {
        if (instance == null) {
            synchronized (BluetoothLeDiscoverer.class) {
                if (instance == null) {
                    instance = new BluetoothLeDiscoverer();
                }
            }
        }
        return instance;
    }

    @Override
    public void setDiscoveryRuleConfig(DiscoveryRuleConfig discoveryRuleConfig) {
        mRuleConfig = discoveryRuleConfig;
        if (mRuleConfig != null) {
            mFilterMacs = mRuleConfig.getMacFilter();
        }
    }

    @Override
    public void setDiscoveryListener(OnBluetoothDeviceDiscoveryListener listener) {
        mDiscoveryListener = listener;
    }

    @Override
    public boolean startDiscovery() {
        if (mDiscovering) {
            return true;
        }
        mDiscovering = true;
        UUID[] uuids = null;
        if (mRuleConfig != null) {
            uuids = mRuleConfig.mUUIDS;
        }
        boolean success = mBluetoothAdapter.startLeScan(uuids, this);
        if (!success) {
            mDiscovering = false;
            mUIHandler.sendEmptyMessage(BluetoothConstants.MSG_DISCOVERY_FAILED);
        } else {
            if (mRuleConfig != null
                    && mRuleConfig.getTimeout() > 0) {
                mUIHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        cancelDiscovery();
                    }
                }, mRuleConfig.getTimeout());
            }
            mEntireMatchCount = 0;
            mUIHandler.sendEmptyMessage(BluetoothConstants.MSG_START_DISCOVERY);
        }
        return success;
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (!mDiscovering) {
            return;
        }
        // Get the BluetoothDevice object from the Intent
        BluetoothDeviceWrapper deviceWrapper = new BluetoothDeviceWrapper(device, rssi, scanRecord);
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
                    if (mEntireMatchCount == macs.length) {
                        cancelDiscovery();
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
    }

    @Override
    public void cancelDiscovery() {
        if (mDiscovering) {
            mBluetoothAdapter.stopLeScan(this);
            mUIHandler.sendEmptyMessage(BluetoothConstants.MSG_CANCEL_DISCOVERY);
        }
    }

    @Override
    public boolean isDiscovering() {
        return mBluetoothAdapter.isDiscovering();
    }

    @Override
    public void release() {
        cancelDiscovery();
        mDiscoveryListener = null;
        instance = null;
    }

}
