package com.chiemy.bluetoothdemo;

/**
 * Created: chiemy
 * Date: 17/12/13
 * Description:
 */

public final class BluetoothConstants {
    // Hint: If you are connecting to a Bluetooth serial board
    // then try using the well-known SPP UUID 00001101-0000-1000-8000-00805F9B34FB.
    // However if you are connecting to an Android peer then please generate your own unique UUID.
    public static final String UUID_NAME = "00001101-0000-1000-8000-00805F9B34FB";

    public static final int MSG_READ = 1;
    public static final int MSG_START_DISCOVERY = 2;
    public static final int MSG_FINISH_DISCOVERY = 3;
    public static final int MSG_CANCEL_DISCOVERY = 4;
    public static final int MSG_DISCOVERY_FAILED = 5;
}