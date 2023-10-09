package com.specknet.pdiotapp.doctor;

/**
 * 自定义的一个实体类
 * 这个类封装了 时间 心率数值 血氧数值
 */
public class Spo2AndHeartRateBean {

    // 下面4个成员变量
    private long timestamp;
    private int heartRate;
    private double spo2;

    // 构造方法  空参
    public Spo2AndHeartRateBean() {
    }

    public Spo2AndHeartRateBean(long timestamp, int heartRate, double spo2) {
        this.timestamp = timestamp;
        this.heartRate = heartRate;
        this.spo2 = spo2;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


    public double getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }

    public double getSpo2() {
        return spo2;
    }

    public void setSpo2(double spo2) {
        this.spo2 = spo2;
    }

}
