package com.specknet.pdiotapp.doctor;

public class RecognitionResultBean {

    private Long timestamp;// 日期
    private String label; // 动作类型
    private int labelIndex; // 动作类型index
    private int count; // 次数
    private String dataSource; // 数据来源


    public RecognitionResultBean() {
    }

    public RecognitionResultBean(Long timestamp, String label, int count, String dataSource) {
        this.timestamp = timestamp;
        this.label = label;
        this.count = count;
        this.dataSource = dataSource;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getLabelIndex() {
        return labelIndex;
    }

    public void setLabelIndex(int labelIndex) {
        this.labelIndex = labelIndex;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }
}
