package com.chiemy.bluetoothdemo;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.WorkerThread;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created: chiemy
 * Date: 17/12/13
 * Description:
 */

public class BluetoothSocketProxy {
    private final BluetoothSocket mBluetoothSocket;
    private final InputStream mInStream;
    private final OutputStream mOutStream;
    private boolean mCanceled;
    private Handler mHandler;

    private String mMacAddress;

    public BluetoothSocketProxy(BluetoothSocket socket, Handler uiHandler) {
        mBluetoothSocket = socket;
        mMacAddress = socket.getRemoteDevice().getAddress();
        InputStream mTmpIs = null;
        OutputStream mTmpOs = null;

        try {
            mTmpIs = socket.getInputStream();
            mTmpOs = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mInStream = mTmpIs;
        mOutStream = mTmpOs;
        mHandler = uiHandler;
    }

    public BluetoothSocket getBluetoothSocket() {
        return mBluetoothSocket;
    }

    @WorkerThread
    public void connect() throws IOException {
        mBluetoothSocket.connect();
    }

    public void readInNewThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                read();
            }
        }).start();
    }

    @WorkerThread
    public void read() {
        byte[] buffer = new byte[1024];
        int bytes;

        while (!mCanceled) {
            try {
                // Read from the InputStream
                bytes = mInStream.read(buffer);

                // Send the obtained bytes to the UI activity
                Message msg
                        = mHandler.obtainMessage(BluetoothConstants.MSG_READ, bytes, -1, buffer);
                Bundle bundle = new Bundle(1);
                bundle.putString(BluetoothConstants.KEY_MAC_ADDRES, mMacAddress);
                msg.setData(bundle);
                msg.sendToTarget();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @WorkerThread
    public void write(byte[] bytes) {
        if (mBluetoothSocket.isConnected()) {
            try {
                mOutStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void close() throws IOException {
        mCanceled = true;
        mBluetoothSocket.close();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BluetoothSocketProxy that = (BluetoothSocketProxy) o;

        return mMacAddress != null ? mMacAddress.equals(that.mMacAddress) : that.mMacAddress ==
                null;
    }

    @Override
    public int hashCode() {
        return mMacAddress != null ? mMacAddress.hashCode() : 0;
    }
}
