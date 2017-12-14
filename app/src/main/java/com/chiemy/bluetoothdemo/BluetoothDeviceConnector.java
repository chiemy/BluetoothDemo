package com.chiemy.bluetoothdemo;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.util.UUID;

/**
 * Created: chiemy
 * Date: 17/12/13
 * Description: 蓝牙连接器
 */

public class BluetoothDeviceConnector implements Runnable {
    private final BluetoothSocketProxy mSocket;
    private final BluetoothDevice mmDevice;

    private boolean mIsConnecting;

    public BluetoothDeviceConnector(BluetoothDevice device,
                                    UUID uuid,
                                    Handler uiHandler) {
        // Use a temporary object that is later assigned to mSocket,
        // because mSocket is final
        BluetoothSocket tmp = null;
        try {
            tmp = device.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mmDevice = device;
        mSocket = new BluetoothSocketProxy(tmp, uiHandler);
    }

    public BluetoothDevice getDevice() {
        return mmDevice;
    }

    public void connect() {
        if (!isConnected()
                && !mIsConnecting) {
            mIsConnecting = true;
            new Thread(this).start();
        }
    }

    public boolean isConnected() {
        return mSocket.getBluetoothSocket().isConnected();
    }

    @Override
    public void run() {
        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        } finally {
            mIsConnecting = false;
        }

        // Do work to manage the connection (in a separate thread)
        startRead();
    }

    private void startRead() {
        mSocket.read();
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        mSocket.write(bytes);
    }

    /**
     * Will cancel an in-progress connection, and close the socket
     */
    public void cancel() {
        try {
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
