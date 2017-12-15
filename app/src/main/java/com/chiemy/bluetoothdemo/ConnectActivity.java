package com.chiemy.bluetoothdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

public class ConnectActivity extends AppCompatActivity implements BluetoothManager
        .OnConnectionListener {
    private static final String DEVICE = "device";

    public static void start(Context context, BluetoothDeviceWrapper device) {
        Intent intent = new Intent(context, ConnectActivity.class);
        intent.putExtra(DEVICE, device);
        context.startActivity(intent);
    }

    private BluetoothDeviceWrapper mDevice;
    private BluetoothManager mBluetoothManager;

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        mProgressBar = findViewById(R.id.progressBar);

        mDevice = getIntent().getParcelableExtra(DEVICE);
        mBluetoothManager = BluetoothManager.getInstance();
        mBluetoothManager.addConnectionListeners(this);
        mBluetoothManager.connectDevice(mDevice.getDevice());

        setTitle(mDevice.getDevice().getName());
    }

    @Override
    public void onConnectStateChanged(boolean connected) {
        if (connected) {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onConnectFailed(Throwable error) {
        Toast.makeText(this, "连接失败", Toast.LENGTH_SHORT).show();
        if (error != null) {
            error.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothManager.disconnected(mDevice.getDevice());
        mBluetoothManager.removeConnectionListeners(this);
    }
}
