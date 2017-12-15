package com.chiemy.bluetoothdemo;

import android.Manifest;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions.RxPermissions;

import java.util.List;

import rx.Subscriber;

public class MainActivity extends AppCompatActivity {
    private BluetoothManager mBluetoothManager;

    private BluzDeviceAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter = new BluzDeviceAdapter());
        recyclerView.addItemDecoration(new DividerDecoration());

        mBluetoothManager = BluetoothManager.getInstance().init(getApplicationContext());
        DiscoveryRuleConfig config = new DiscoveryRuleConfig.Builder()
                .setTimeout(10_000)
                .setType(DiscoveryType.Classic)
                .build();
        mBluetoothManager.setDiscoveryRuleConfig(config);
        mBluetoothManager.setDeviceDiscoveryListener(new OnBluetoothDeviceDiscoveryListener() {

            @Override
            public void onBluetoothDeviceDiscoveryStarted() {
                Toast.makeText(MainActivity.this, "扫描开始", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBluetoothDeviceDiscoveryFound(BluetoothDeviceWrapper device,
                                                        List<BluetoothDeviceWrapper> devices) {
                mAdapter.setData(devices);
            }

            @Override
            public void onBluetoothDeviceDiscoveryFinished(List<BluetoothDeviceWrapper> devices) {
                Toast.makeText(MainActivity.this, "扫描结束", Toast.LENGTH_SHORT).show();
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

    private class BluzDeviceAdapter extends RecyclerView.Adapter<BluzDeviceViewHolder> {
        private List<BluetoothDeviceWrapper> mBluetoothDeviceWrappers;

        public void setData(List<BluetoothDeviceWrapper> bluetoothDeviceWrappers) {
            mBluetoothDeviceWrappers = bluetoothDeviceWrappers;
            notifyDataSetChanged();
        }

        @Override
        public BluzDeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return BluzDeviceViewHolder.create(parent);
        }

        @Override
        public void onBindViewHolder(BluzDeviceViewHolder holder, int position) {
            holder.bind(mBluetoothDeviceWrappers.get(position));
        }

        @Override
        public int getItemCount() {
            return mBluetoothDeviceWrappers != null ? mBluetoothDeviceWrappers.size() : 0;
        }
    }

    private static class BluzDeviceViewHolder extends RecyclerView.ViewHolder {

        public static BluzDeviceViewHolder create(ViewGroup parent) {
            return new BluzDeviceViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bluz_device,
                            parent, false)
            );
        }

        private TextView mTvName;
        private TextView mTvMac;
        private TextView mTvRssi;
        private TextView mTvType;

        private BluetoothDeviceWrapper mDevice;

        public BluzDeviceViewHolder(View itemView) {
            super(itemView);
            mTvName = itemView.findViewById(R.id.tv_name);
            mTvMac = itemView.findViewById(R.id.tv_mac);
            mTvRssi = itemView.findViewById(R.id.tv_rssi);
            mTvType = itemView.findViewById(R.id.tv_type);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConnectActivity.start(v.getContext(), mDevice);
                }
            });
        }

        public void bind(BluetoothDeviceWrapper deviceWrapper) {
            mDevice = deviceWrapper;
            mTvName.setText(deviceWrapper.getDevice().getName());
            mTvMac.setText(deviceWrapper.getDevice().getAddress());
            mTvRssi.setText(String.valueOf(deviceWrapper.getRssi()));
            mTvType.setText("");
        }
    }

    private class DividerDecoration extends RecyclerView.ItemDecoration {
        private Paint mPaint;

        DividerDecoration() {
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setColor(Color.BLACK);
            mPaint.setStyle(Paint.Style.FILL);
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            final int left = parent.getPaddingLeft();
            final int right = parent.getWidth() - parent.getPaddingRight();

            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                final int top = child.getBottom() + params.bottomMargin;

                final int bottom = top + 1;
                c.drawRect(left, top, right, bottom, mPaint);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.enable_discovery:
                BluetoothUtil.ensureDiscoverable(this);
                return true;
            case R.id.rediscovery:
                mBluetoothManager.startDiscovery();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothManager.release();
    }
}
