package com.chiemy.bluetoothdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created: chiemy
 * Date: 17/12/13
 * Description:
 */

public class BluetoothManager {

    private static BluetoothManager instance = null;

    private Context mContext;

    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothDiscoverer mBluetoothDiscoverer;

    private BluetoothDevice mConnectedDevice;

    private Status mStatus = Status.None;

    /**
     * 连接状态监听集合
     */
    private List<OnConnectionListener> mConnectionListeners;

    private OnBluetoothDeviceDiscoveryListener mDeviceDiscoveryListener;

    private List<BluetoothDevice> mBluetoothDevices;

    private boolean mRegisterStateReceiver;

    /**
     * 开启蓝牙后是否开始扫描
     */
    private boolean mDiscoveryAfterStateOn;

    private boolean mConnecting;

    private DiscoveryRuleConfig mDiscoveryRuleConfig;

    private UUID mUUID;

    private Map<String, BluetoothDeviceConnector> mConnectorMap;

    private BluetoothServer mServer;

    private Handler mUIHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            switch (msg.what) {
                case BluetoothConstants.MSG_READ:
                    byte[] data = (byte[]) msg.obj;
                    String readMessage = new String(data, 0, msg.arg1);
                    String address = null;
                    if (bundle != null) {
                        address = bundle.getString(BluetoothConstants.KEY_MAC_ADDRES);
                    }
                    Toast.makeText(mContext, address + " : " + readMessage, Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothConstants.MSG_CANCEL_DISCOVERY:
                    if (mDeviceDiscoveryListener != null) {
                        mDeviceDiscoveryListener.onBluetoothDeviceDiscoveryCancel();
                    }
                    break;
                case BluetoothConstants.MSG_CONNECTED:
                    if (mConnectionListeners != null) {
                        int size = mConnectionListeners.size();
                        for (int i = 0; i < size; i++) {
                            mConnectionListeners.get(i).onConnectStateChanged(true);
                        }
                    }
                    break;
                case BluetoothConstants.MSG_CONNECT_FAILED:
                    Throwable error = null;
                    if (bundle != null) {
                        error = (Throwable) bundle.getSerializable(BluetoothConstants.KEY_ERROR);
                    }
                    if (mConnectionListeners != null) {
                        int size = mConnectionListeners.size();
                        for (int i = 0; i < size; i++) {
                            mConnectionListeners.get(i).onConnectFailed(error);
                        }
                    }
                    break;
            }
        }
    };

    private BluetoothManager() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            throw new IllegalArgumentException("Device does not support Bluetooth");
        }
        mConnectionListeners = new ArrayList<>(2);
        mBluetoothDevices = new ArrayList<>(2);

        mUUID = UUID.fromString(BluetoothConstants.UUID_NAME);
        mConnectorMap = new HashMap<>(2);

        mServer = new BluetoothServer(mUUID, mUIHandler);
        mServer.accept();
    }

    public static BluetoothManager getInstance() throws IllegalArgumentException {
        if (instance == null) {
            synchronized (BluetoothManager.class) {
                if (instance == null) {
                    instance = new BluetoothManager();
                }
            }
        }
        return instance;
    }

    public BluetoothManager init(Context context) {
        mContext = context.getApplicationContext();
        registerStateReceiver();
        return this;
    }

    private void registerStateReceiver() {
        if (!mRegisterStateReceiver) {
            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            mContext.registerReceiver(mStateReceiver, filter);
            mRegisterStateReceiver = true;
        }
    }

    public void setDiscoveryRuleConfig(DiscoveryRuleConfig discoveryRuleConfig) {
        mDiscoveryRuleConfig = discoveryRuleConfig;
    }

    /**
     * 蓝牙扫描监听
     *
     * @param listener
     */
    public void setDeviceDiscoveryListener(OnBluetoothDeviceDiscoveryListener listener) {
        mDeviceDiscoveryListener = listener;
    }

    /**
     * 添加蓝牙连接状态监听
     *
     * @param listener
     */
    public void addConnectionListeners(OnConnectionListener listener) {
        if (!mConnectionListeners.contains(listener)) {
            mConnectionListeners.add(listener);
        }
    }

    public void removeConnectionListeners(OnConnectionListener listener) {
        if (mConnectionListeners.contains(listener)) {
            mConnectionListeners.remove(listener);
        }
    }

    public Status getState() {
        return mStatus;
    }

    public BluetoothDevice getConnectedDevice() {
        return mConnectedDevice;
    }

    /**
     * 已配对的设备列表
     */
    public List<BluetoothDevice> getBondedDevices() {
        return null;
    }

    public boolean startDiscovery() {
        boolean success = false;
        if (BluetoothUtil.isBluetoothEnable()) {
            getDiscoverer();
            success = mBluetoothDiscoverer.startDiscovery();
        } else {
            mDiscoveryAfterStateOn = true;
            BluetoothUtil.enableBluetooth();
        }
        return success;
    }

    private void getDiscoverer() {
        if (mDiscoveryRuleConfig != null) {
            if (mDiscoveryRuleConfig.getType() == DiscoveryType.Classic) {
                mBluetoothDiscoverer = BluetoothClassicDiscoverer.getInstance(mContext);
            } else if (mDiscoveryRuleConfig.getType() == DiscoveryType.BLE) {
                mBluetoothDiscoverer = BluetoothLeDiscoverer.getInstance();
            } else if (mDiscoveryRuleConfig.getType() == DiscoveryType.All){
                mBluetoothDiscoverer = BluetoothAllDiscoverer.getInstance(mContext);
            }
        } else {
            mBluetoothDiscoverer = BluetoothClassicDiscoverer.getInstance(mContext);
        }
        mBluetoothDiscoverer.setDiscoveryListener(mDeviceDiscoveryListener);
        mBluetoothDiscoverer.setDiscoveryRuleConfig(mDiscoveryRuleConfig);
    }

    public void cancelDiscovery() {
        if (mBluetoothDiscoverer != null) {
            mBluetoothDiscoverer.cancelDiscovery();
        }
    }

    public void connectDevice(BluetoothDevice device) {
        // Cancel discovery because it will slow down the connection
        cancelDiscovery();
        String macAddress = device.getAddress();
        BluetoothDeviceConnector connector = mConnectorMap.get(macAddress);
        if (connector == null) {
            connector = new BluetoothDeviceConnector(mUIHandler);
            mConnectorMap.put(macAddress, connector);
        }
        connector.connect(macAddress, mUUID);
    }

    public void disconnected(BluetoothDevice device) {
        BluetoothDeviceConnector connector = mConnectorMap.get(device.getAddress());
        if (connector != null) {
            connector.cancel();
        }
    }

    private void setConnected() {

    }

    public void sendMessage(byte[] data) {

    }

    public void release() {
        mDeviceDiscoveryListener = null;
        mConnectionListeners.clear();
        if (mRegisterStateReceiver) {
            mContext.unregisterReceiver(mStateReceiver);
        }
        if (mBluetoothDiscoverer != null) {
            mBluetoothDiscoverer.release();
        }
        mServer.release();

        if (mConnectorMap != null) {
            for(BluetoothDeviceConnector connector : mConnectorMap.values()) {
                if (connector != null) {
                    connector.cancel();
                }
            }
        }

        instance = null;
    }

    private BroadcastReceiver mStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            switch (state) {
                case BluetoothAdapter.STATE_ON:
                    if (mDiscoveryAfterStateOn) {
                        mDiscoveryAfterStateOn = false;
                        startDiscovery();
                    }
                    break;
                case BluetoothAdapter.STATE_DISCONNECTED:

                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 连接状态监听
     */
    public interface OnConnectionListener {
        void onConnectStateChanged(boolean connected);

        void onConnectFailed(Throwable error);
    }

    public enum Status {
        /**
         * 无状态
         */
        None,
        /**
         * 扫描中
         */
        Discovering,
        /**
         * 连接中
         */
        Connecting,
        /**
         * 已连接
         */
        Connected
    }

}
