package com.chiemy.bluetoothdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.util.UUID;

/**
 * Created: chiemy
 * Date: 17/12/13
 * Description: 蓝牙连接器
 */

public class BluetoothDeviceConnector implements Runnable {
    private BluetoothSocketProxy mSocket;
    private BluetoothSocket mBluetoothSocket;
    private BluetoothDevice mmDevice;

    private boolean mIsConnecting;
    private boolean mIsConnected;

    private Handler mUIHandler;


    public BluetoothDeviceConnector(Handler uiHandler) {
        mUIHandler = uiHandler;
    }

    public void connect(String address, UUID uuid) {
        if (!mIsConnected
                && !mIsConnecting) {
            mIsConnecting = true;
            createSocket(address, uuid);

            new Thread(this).start();
        }
    }

    private void createSocket(String address, UUID uuid) {
        mmDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
        // Use a temporary object that is later assigned to mSocket,
        // because mSocket is final
        BluetoothSocket tmp = null;
        try {
            tmp = mmDevice.createInsecureRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mBluetoothSocket = tmp;
        mSocket = new BluetoothSocketProxy(mBluetoothSocket, mUIHandler);
    }

    public BluetoothDevice getDevice() {
        return mmDevice;
    }

    public boolean isConnected() {
        return mIsConnected;
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        mSocket.write(bytes);
    }

    /**
     * Will cancel an in-progress connection, and close the socket
     */
    public void cancel() {
        mIsConnected = false;
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mSocket.connect();
            mIsConnected = true;
            mUIHandler.sendEmptyMessage(BluetoothConstants.MSG_CONNECTED);
            write("hello".getBytes());
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            try {
                mSocket.close();
            } catch (IOException io) {
            }

            Message msg = mUIHandler.obtainMessage(BluetoothConstants.MSG_CONNECT_FAILED);
            Bundle bundle = new Bundle(1);
            bundle.putSerializable(BluetoothConstants.KEY_ERROR, connectException);
            msg.setData(bundle);
            msg.sendToTarget();
            return;
        } finally {
            mIsConnecting = false;
        }

        // Do work to manage the connection (in a separate thread)
        mSocket.read();
    }
}
