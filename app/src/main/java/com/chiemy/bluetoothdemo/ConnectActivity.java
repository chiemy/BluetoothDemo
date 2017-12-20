package com.chiemy.bluetoothdemo;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProgressBar = findViewById(R.id.progressBar);

        mDevice = getIntent().getParcelableExtra(DEVICE);
        setTitle(mDevice.getDevice().getName());
        getSupportActionBar().setSubtitle("未连接");

        mBluetoothManager = BluetoothManager.getInstance();
        mBluetoothManager.addConnectionListeners(this);
        mBluetoothManager.connectDevice(mDevice.getDevice());

        BlueBikeSensor sensor = new BlueBikeSensor(this, mDevice.getDevice(), 10, new
                BlueBikeSensor
                .Callback() {
            @Override
            public void onConnectionStateChange(BlueBikeSensor sensor, BlueBikeSensor
                    .ConnectionState newState) {
                if (newState == BlueBikeSensor.ConnectionState.CONNECTED) {
                    sensor.setNotificationsEnabled(true);
                }
                Log.d("ConnectActivity", "onConnectionStateChange: " + newState);
            }

            @Override
            public void onSpeedUpdate(BlueBikeSensor sensor, double distance, double elapsedUs) {
                Log.d("ConnectActivity", "onSpeedUpdate: " + distance);
            }

            @Override
            public void onCadenceUpdate(BlueBikeSensor sensor, int rotations, double elapsedUs) {
                Log.d("ConnectActivity", "onCadenceUpdate: " + rotations);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.connect, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.connect:
                mBluetoothManager.connectDevice(mDevice.getDevice());
                break;
            case R.id.disconnect:
                mBluetoothManager.disconnected(mDevice.getDevice());
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    @Override
    public void onConnected(BluetoothDevice device) {
        if (isTargetDevice(device)) {
            getSupportActionBar().setSubtitle("已连接");
            mProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDisconnected(BluetoothDevice device, boolean initiative) {
        Toast.makeText(this, initiative ? "主动断开": "被动断开", Toast.LENGTH_SHORT).show();
        if (isTargetDevice(device)) {
            getSupportActionBar().setSubtitle("连接断开");
        }
    }

    private boolean isTargetDevice(BluetoothDevice device) {
        return TextUtils.equals(mDevice.getDevice().getAddress(), device.getAddress());
    }


    @Override
    public void onConnectFailed(Throwable error) {
        getSupportActionBar().setSubtitle("连接失败");
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
