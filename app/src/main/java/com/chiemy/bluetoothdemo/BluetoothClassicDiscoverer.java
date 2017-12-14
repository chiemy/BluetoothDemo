package com.chiemy.bluetoothdemo;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * Created: chiemy
 * Date: 17/12/14
 * Description:
 */
class BluetoothClassicDiscoverer implements BluetoothDiscoverer {
    private BluetoothAdapter mBluetoothAdapter;

    private OnBluetoothDeviceDiscoveryListener mDeviceDiscoveryListener;

    private BluetoothDiscovererReceiver mDiscovererReceiver;

    private Handler mUIHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothConstants.MSG_CANCEL_DISCOVERY:
                    if (mDeviceDiscoveryListener != null) {
                        mDeviceDiscoveryListener.onBluetoothDeviceDiscoveryCancel();
                    }
                    break;
            }
        }
    };

    private static BluetoothClassicDiscoverer instance = null;

    private BluetoothClassicDiscoverer(Context context) {
        Context appContext = context.getApplicationContext();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mDiscovererReceiver = new BluetoothDiscovererReceiver(appContext);
        mDiscovererReceiver.setFilterMatchListener(new BluetoothDiscovererReceiver.FilterMatchListener() {
            @Override
            public void onAllMatched() {
                cancelDiscovery();
            }
        });
    }

    public static BluetoothClassicDiscoverer getInstance(Context context) {
        if (instance == null) {
            synchronized (BluetoothClassicDiscoverer.class) {
                if (instance == null) {
                    instance = new BluetoothClassicDiscoverer(context);
                }
            }
        }
        return instance;
    }

    @Override
    public void setDiscoveryRuleConfig(DiscoveryRuleConfig discoveryRuleConfig) {
        if (discoveryRuleConfig != null) {
            mDiscovererReceiver.setFilter(discoveryRuleConfig.getMacFilter());
        }
    }

    @Override
    public void setDiscoveryListener(OnBluetoothDeviceDiscoveryListener listener) {
        mDeviceDiscoveryListener = listener;
    }

    @Override
    public boolean startDiscovery() {
        if (mBluetoothAdapter.isDiscovering()) {
            return true;
        }
        mDiscovererReceiver.register();
        mDiscovererReceiver.setDiscoveryListener(mDeviceDiscoveryListener);
        boolean success = mBluetoothAdapter.startDiscovery();
        if (!success) {
            mUIHandler.sendEmptyMessage(BluetoothConstants.MSG_DISCOVERY_FAILED);
        }
        return success;
    }

    @Override
    public void cancelDiscovery() {
        mDiscovererReceiver.unregiser();
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
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
        mDeviceDiscoveryListener = null;
        instance = null;
    }
}
