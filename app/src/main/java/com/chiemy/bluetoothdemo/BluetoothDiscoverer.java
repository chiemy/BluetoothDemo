package com.chiemy.bluetoothdemo;

/**
 * Created: chiemy
 * Date: 17/12/14
 * Description: 蓝牙扫描接口
 */
public interface BluetoothDiscoverer {
    /**
     * 设置扫描监听
     */
    void setDiscoveryListener(OnBluetoothDeviceDiscoveryListener listener);

    void setDiscoveryRuleConfig(DiscoveryRuleConfig discoveryRuleConfig);

    /**
     * 开始扫描设备
     */
    boolean startDiscovery();

    /**
     * 取消扫描设备
     */
    void cancelDiscovery();

    /**
     * 是否在扫描
     */
    boolean isDiscovering();

    /**
     * 释放资源
     */
    void release();

}
