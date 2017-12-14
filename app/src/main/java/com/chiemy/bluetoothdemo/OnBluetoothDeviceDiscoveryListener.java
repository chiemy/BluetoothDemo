package com.chiemy.bluetoothdemo;

import java.util.List;

/**
 * Created: chiemy
 * Date: 17/12/14
 * Description:
 */

public interface OnBluetoothDeviceDiscoveryListener {
    /**
     * 扫描开始
     */
    void onBluetoothDeviceDiscoveryStarted();

    /**
     * 扫描到设备
     *
     * @param device  最新扫描到的设备
     * @param devices 扫描到的设备列表
     */
    void onBluetoothDeviceDiscoveryFound(BluetoothDeviceWrapper device, List<BluetoothDeviceWrapper> devices);

    /**
     * 扫描结束
     *
     * @param devices 扫描到的设备列表
     */
    void onBluetoothDeviceDiscoveryFinished(List<BluetoothDeviceWrapper> devices);

    /**
     * 取消扫描
     */
    void onBluetoothDeviceDiscoveryCancel();

    /**
     * 扫描失败
     */
    void onBluetoothDeviceDiscoveryFailed();
}
