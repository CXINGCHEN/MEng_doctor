package com.specknet.pdiotapp.doctor;

import androidx.annotation.NonNull;

import org.json.JSONObject;

/**
 * 自定义的一个实体类
 * 这个类封装了消息的内容 时间 数值
 */
public class AlertMsg {

    // 下面4个成员变量
    private long timestamp;
    private String msg;
    private double heartRateAverage;
    private double spo2Average;

    // 构造方法  空参
    public AlertMsg() {
    }

    // 构造方法  需要传进来全参数
    public AlertMsg(long timestamp, String msg, double heartRateAverage, double spo2Average) {
        this.timestamp = timestamp;
        this.msg = msg;
        this.heartRateAverage = heartRateAverage;
        this.spo2Average = spo2Average;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public double getHeartRateAverage() {
        return heartRateAverage;
    }

    public void setHeartRateAverage(double heartRateAverage) {
        this.heartRateAverage = heartRateAverage;
    }

    public double getSpo2Average() {
        return spo2Average;
    }

    public void setSpo2Average(double spo2Average) {
        this.spo2Average = spo2Average;
    }


    @NonNull
    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject(); // {}
        try {
            jsonObject.put("timestamp",timestamp);
            jsonObject.put("msg",msg);
            jsonObject.put("heartRateAverage",heartRateAverage);
            jsonObject.put("spo2Average",spo2Average);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }


    public JSONObject toJsonObject() {
        JSONObject jsonObject = new JSONObject(); // {}
        try {
            jsonObject.put("timestamp",timestamp);
            jsonObject.put("msg",msg);
            jsonObject.put("heartRateAverage",heartRateAverage);
            jsonObject.put("spo2Average",spo2Average);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

}
