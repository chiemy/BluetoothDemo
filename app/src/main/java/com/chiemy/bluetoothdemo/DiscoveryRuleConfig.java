package com.chiemy.bluetoothdemo;

/**
 * Created: chiemy
 * Date: 17/12/13
 * Description: 扫描规则配置
 */
public class DiscoveryRuleConfig {
    private String[] mMacs;
    private long mTimeout;
    private DiscoveryType mType;

    private DiscoveryRuleConfig(Builder builder) {
        mMacs = builder.mMacs;
        mTimeout = builder.mTimeout;
        mType = builder.mType;
    }

    public String[] getMacFilter() {
        return mMacs;
    }

    public long getTimeout() {
        return mTimeout;
    }

    public DiscoveryType getType() {
        return mType;
    }

    public static class Builder {
        private String[] mMacs;
        private long mTimeout = 10_000;
        private DiscoveryType mType = DiscoveryType.Classic;

        /**
         * 扫描指定 Mac 地址的设备
         */
        public Builder setMacFilter(String...macs) {
            mMacs = macs;
            return this;
        }

        /**
         * 设置超时时间，只对 BLE 扫描有效
         * @param duration 毫秒
         * @return
         */
        public Builder setTimeout(long duration) {
            mTimeout = duration;
            return this;
        }

        public Builder setType(DiscoveryType type) {
            mType = type;
            return this;
        }

        public DiscoveryRuleConfig build() {
            return new DiscoveryRuleConfig(this);
        }
    }
}
