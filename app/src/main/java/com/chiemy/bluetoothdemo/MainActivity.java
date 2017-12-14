package com.chiemy.bluetoothdemo;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.tbruyelle.rxpermissions.RxPermissions;

import java.util.List;

import rx.Subscriber;

public class MainActivity extends AppCompatActivity {
    private BluetoothManager mBluetoothManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        mBluetoothManager = BluetoothManager.getInstance().init(getApplicationContext());
        DiscoveryRuleConfig config = new DiscoveryRuleConfig.Builder()
                .setTimeout(10_000)
                .setType(DiscoveryType.All)
                .build();
        mBluetoothManager.setDiscoveryRuleConfig(config);
        mBluetoothManager.setDeviceDiscoveryListener(new OnBluetoothDeviceDiscoveryListener() {

            @Override
            public void onBluetoothDeviceDiscoveryStarted() {
                Log.d("chiemy", "扫描开始");
            }

            @Override
            public void onBluetoothDeviceDiscoveryFound(BluetoothDeviceWrapper device,
                                                        List<BluetoothDeviceWrapper> devices) {
                Log.d("chiemy", "onBluetoothDeviceDiscoveryFound: " + device);
            }

            @Override
            public void onBluetoothDeviceDiscoveryFinished(List<BluetoothDeviceWrapper> devices) {
                Log.d("chiemy", "扫描结束");
            }

            @Override
            public void onBluetoothDeviceDiscoveryCancel() {
            }

            @Override
            public void onBluetoothDeviceDiscoveryFailed() {
            }
        });

        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions
                .request(Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {
                            mBluetoothManager.startDiscovery();
                        }
                    }
                });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothManager.release();
    }
}
