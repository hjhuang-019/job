package com.example.jobplatform.service;

import com.example.jobplatform.dto.AdminVerifyRequest;
import com.example.jobplatform.vo.AdminEnterprisePendingVO;
import com.example.jobplatform.vo.AdminJobSeekerPendingVO;
import com.example.jobplatform.vo.AdminStatisticsVO;

import java.util.List;

public interface AdminService {

    List<AdminJobSeekerPendingVO> listPendingJobSeekers();

    void verifyJobSeeker(Long id, AdminVerifyRequest request);

    List<AdminEnterprisePendingVO> listPendingEnterprises();

    void verifyEnterprise(Long id, AdminVerifyRequest request);

    AdminStatisticsVO getStatistics();
}
