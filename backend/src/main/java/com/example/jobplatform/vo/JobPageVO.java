package com.example.jobplatform.vo;

import java.util.List;

public class JobPageVO {

    private List<JobVO> records;
    private Long total;
    private Integer pageNum;
    private Integer pageSize;

    public List<JobVO> getRecords() {
        return records;
    }

    public void setRecords(List<JobVO> records) {
        this.records = records;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
