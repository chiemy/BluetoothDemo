package com.chiemy.bluetoothdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created: chiemy
 * Date: 17/12/15
 * Description:
 */

public class BluetoothServer implements Runnable {
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothServerSocket mmServerSocket;
    private List<BluetoothSocketProxy> mBluetoothSocketList;
    private Handler mUIHandler;

    private boolean mAccepting;

    private UUID mUUID;

    public BluetoothServer(UUID uuid, Handler uiHandler) {
        mUIHandler = uiHandler;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mUUID = uuid;
        mBluetoothSocketList = new ArrayList<>(2);
    }

    private void createSocket() {
        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("hotbody", mUUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mmServerSocket = tmp;
    }

    public void accept() {
        if (mAccepting) {
            return;
        }
        createSocket();
        new Thread(this).start();
    }

    public void cancelAccept() {
        mAccepting = false;
    }

    public void release() {
        cancelAccept();
        close();
    }

    /**
     * Will cancel the listening socket, and cause the thread to finish
     */
    public void close() {
        if (mmServerSocket != null) {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        mAccepting = true;
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned
        while (mAccepting) {
            try {
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // If a connection was accepted
            if (socket != null) {
                // Do work to manage the connection (in a separate thread)
                BluetoothSocketProxy bluetoothSocketProxy = new BluetoothSocketProxy(
                        socket,
                        mUIHandler
                );
                bluetoothSocketProxy.readInNewThread();
                bluetoothSocketProxy.write("hello".getBytes());
                if (mBluetoothSocketList.contains(bluetoothSocketProxy)) {
                    mBluetoothSocketList.remove(bluetoothSocketProxy);
                }
                mBluetoothSocketList.add(bluetoothSocketProxy);
            }
        }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        int size = mBluetoothSocketList.size();
        for (int i = 0; i < size; i++) {
            mBluetoothSocketList.get(i).write(bytes);
        }
    }
}
