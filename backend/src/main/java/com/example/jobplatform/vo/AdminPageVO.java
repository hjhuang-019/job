package com.example.jobplatform.vo;

import java.util.List;

public class AdminPageVO<T> {

    private List<T> records;
    private long total;

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
