package com.example.jobplatform.service;

import com.example.jobplatform.dto.JobSaveRequest;
import com.example.jobplatform.vo.JobPageVO;
import com.example.jobplatform.vo.JobVO;

import java.util.List;

public interface JobService {

    JobVO create(JobSaveRequest request);

    JobPageVO listPublishedJobs(Integer pageNum, Integer pageSize, String keyword, String city, String workMode, String disabilitySupportType);

    JobVO update(Long id, JobSaveRequest request);

    void delete(Long id);

    List<JobVO> listMyJobs();

    JobVO updateStatus(Long id, String status);

    JobVO detail(Long id);
}
