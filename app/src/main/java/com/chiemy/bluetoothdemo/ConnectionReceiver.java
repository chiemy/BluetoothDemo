package com.chiemy.bluetoothdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created: chiemy
 * Date: 17/12/19
 * Description:
 */

public class ConnectionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("chiemy", "onReceive: " + intent.getAction());
    }
}
