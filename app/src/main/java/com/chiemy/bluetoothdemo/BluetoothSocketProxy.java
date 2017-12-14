package com.chiemy.bluetoothdemo;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.support.annotation.WorkerThread;

import java.io.ByteArrayOutputStream;
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

    public BluetoothSocketProxy(BluetoothSocket socket, Handler uiHandler) {
        mBluetoothSocket = socket;
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
        while (!mCanceled) {
            try {
                // Read from the InputStream
                int read;
                byte[] buffer = new byte[1024];
                ByteArrayOutputStream data = new ByteArrayOutputStream();
                while ((read = mInStream.read(buffer)) > 0) {
                    data.write(buffer, 0, read);
                }
                // Send the obtained bytes to the UI activity
                mHandler
                        .obtainMessage(BluetoothConstants.MSG_READ, data.toByteArray())
                        .sendToTarget();
            } catch (IOException e) {
                break;
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
}
