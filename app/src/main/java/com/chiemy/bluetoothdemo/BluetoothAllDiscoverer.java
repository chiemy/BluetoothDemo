package com.chiemy.bluetoothdemo;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

/**
 * Created: chiemy
 * Date: 17/12/14
 * Description:
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class BluetoothAllDiscoverer implements BluetoothDiscoverer {

    private static BluetoothAllDiscoverer instance = null;

    private BluetoothDiscoverer mClassicDiscoverer;
    private BluetoothDiscoverer mLeDiscoverer;

    private OnBluetoothDeviceDiscoveryListener mRemoteDiscoveryListener;

    private List<BluetoothDeviceWrapper> mBluetoothDevices;

    private boolean mClassicStart;
    private boolean mClassicFinish;
    private boolean mClassicCancel;
    private boolean mClassicFailed;

    private boolean mLeStart;
    private boolean mLeFinish;
    private boolean mLeCancel;
    private boolean mLeFailed;

    private BluetoothAllDiscoverer(Context context) {
        mClassicDiscoverer = BluetoothClassicDiscoverer.getInstance(context);
        mClassicDiscoverer.setDiscoveryListener(mClassicDiscoveryListener);
        if (BluetoothUtil.isBleSupported(context)) {
            mLeDiscoverer = BluetoothLeDiscoverer.getInstance();
            mLeDiscoverer.setDiscoveryListener(mLeDiscoveryListener);
        }

        mBluetoothDevices = new ArrayList<>();
    }

    public static BluetoothAllDiscoverer getInstance(Context context) {
        if (instance == null) {
            synchronized (BluetoothAllDiscoverer.class) {
                if (instance == null) {
                    instance = new BluetoothAllDiscoverer(context);
                }
            }
        }
        return instance;
    }

    @Override
    public void setDiscoveryListener(OnBluetoothDeviceDiscoveryListener listener) {
        mRemoteDiscoveryListener = listener;
    }

    @Override
    public void setDiscoveryRuleConfig(DiscoveryRuleConfig discoveryRuleConfig) {
        if (mLeDiscoverer != null) {
            mLeDiscoverer.setDiscoveryRuleConfig(discoveryRuleConfig);
        }
    }

    @Override
    public boolean startDiscovery() {
        reset();
        mBluetoothDevices.clear();
        boolean success = mClassicDiscoverer.startDiscovery();
        if (mLeDiscoverer != null) {
            success |= mLeDiscoverer.startDiscovery();
        }
        return success;
    }

    @Override
    public void cancelDiscovery() {
        reset();
        mClassicDiscoverer.cancelDiscovery();
        if (mLeDiscoverer != null) {
            mLeDiscoverer.cancelDiscovery();
        }
    }

    private void reset() {
        mClassicStart = false;
        mClassicCancel = false;
        mClassicFinish = false;
        mClassicFailed = false;

        mLeStart = false;
        mLeCancel = false;
        mLeFinish = false;
        mLeFailed = false;
    }

    @Override
    public boolean isDiscovering() {
        boolean discovering = mClassicDiscoverer.isDiscovering();
        if (mLeDiscoverer != null) {
            discovering |= mLeDiscoverer.isDiscovering();
        }
        return discovering;
    }

    @Override
    public void release() {
        mClassicDiscoverer.release();
        if (mLeDiscoverer != null) {
            mLeDiscoverer.release();
        }
        instance = null;
    }

    private OnBluetoothDeviceDiscoveryListener mClassicDiscoveryListener = new
            OnBluetoothDeviceDiscoveryListener() {

        @Override
        public void onBluetoothDeviceDiscoveryStarted() {
            mClassicStart = true;
            mClassicFinish = false;
            mClassicCancel = false;
        }

        @Override
        public void onBluetoothDeviceDiscoveryFound(BluetoothDeviceWrapper device,
                                                    List<BluetoothDeviceWrapper> devices) {
            mBluetoothDevices.add(device);
            if (mRemoteDiscoveryListener != null) {
                mRemoteDiscoveryListener.onBluetoothDeviceDiscoveryFound(
                        device,
                        mBluetoothDevices
                );
            }
        }

        @Override
        public void onBluetoothDeviceDiscoveryFinished(List<BluetoothDeviceWrapper> devices) {
            mClassicFinish = true;
            onDiscoveryFinished(mBluetoothDevices);
        }

        @Override
        public void onBluetoothDeviceDiscoveryCancel() {
            mClassicCancel = true;
            onDiscoveryCancel();
        }

        @Override
        public void onBluetoothDeviceDiscoveryFailed() {
            mClassicFailed = true;
            onDiscoveryFailed();
        }
    };

    private OnBluetoothDeviceDiscoveryListener mLeDiscoveryListener = new
            OnBluetoothDeviceDiscoveryListener() {

                @Override
                public void onBluetoothDeviceDiscoveryStarted() {
                    mLeStart = true;
                    mLeFinish = false;
                    mLeCancel = false;
                }

                @Override
                public void onBluetoothDeviceDiscoveryFound(BluetoothDeviceWrapper device,
                                                            List<BluetoothDeviceWrapper> devices) {
                    mBluetoothDevices.add(device);
                    if (mRemoteDiscoveryListener != null) {
                        mRemoteDiscoveryListener.onBluetoothDeviceDiscoveryFound(
                                device,
                                mBluetoothDevices
                        );
                    }
                }

                @Override
                public void onBluetoothDeviceDiscoveryFinished(List<BluetoothDeviceWrapper> devices) {
                    mLeFinish = true;
                    onDiscoveryFinished(mBluetoothDevices);
                }

                @Override
                public void onBluetoothDeviceDiscoveryCancel() {
                    mLeCancel = true;
                    onDiscoveryCancel();
                }

                @Override
                public void onBluetoothDeviceDiscoveryFailed() {
                    mLeFailed = true;
                    onDiscoveryFailed();
                }
            };

    private void onDiscoveryFinished(List<BluetoothDeviceWrapper> devices) {
        if (mRemoteDiscoveryListener != null) {
            boolean callback;
            if (mClassicStart) {
                if (mLeStart) {
                    callback = mClassicFinish && mLeFinish;
                } else {
                    callback = mClassicFinish;
                }
            } else {
                callback = mLeStart && mLeFinish;
            }
            if (callback) {
                mRemoteDiscoveryListener.onBluetoothDeviceDiscoveryFinished(devices);
            }
        }
    }

    private void onDiscoveryCancel() {
        if (mRemoteDiscoveryListener != null) {
            boolean callback;
            if (mClassicStart) {
                if (mLeStart) {
                    callback = mClassicCancel && mLeCancel;
                } else {
                    callback = mClassicCancel;
                }
            } else {
                callback = mLeStart && mLeCancel;
            }
            if (callback) {
                mRemoteDiscoveryListener.onBluetoothDeviceDiscoveryCancel();
            }
        }
    }

    private void onDiscoveryFailed() {
        if (mRemoteDiscoveryListener != null) {
            boolean callback;
            if (mClassicStart) {
                if (mLeStart) {
                    callback = mClassicFailed && mLeFailed;
                } else {
                    callback = mClassicFailed;
                }
            } else {
                callback = mLeStart && mLeFailed;
            }
            if (callback) {
                mRemoteDiscoveryListener.onBluetoothDeviceDiscoveryFailed();
            }
        }
    }
}
