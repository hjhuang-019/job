package com.example.jobplatform.service;

import com.example.jobplatform.dto.ApplicationCreateRequest;
import com.example.jobplatform.dto.ApplicationStatusUpdateRequest;
import com.example.jobplatform.vo.ApplicationVO;

import java.util.List;

public interface ApplicationService {

    ApplicationVO create(ApplicationCreateRequest request);

    List<ApplicationVO> listMyApplications();

    List<ApplicationVO> listByJobId(Long jobId);

    ApplicationVO updateStatus(Long id, ApplicationStatusUpdateRequest request);
}
