package com.example.jobplatform.service;

import com.example.jobplatform.dto.AdminBlacklistRequest;
import com.example.jobplatform.dto.AdminVerifyRequest;
import com.example.jobplatform.vo.AdminAuditLogVO;
import com.example.jobplatform.vo.AdminEnterprisePendingVO;
import com.example.jobplatform.vo.AdminEnterpriseUserRowVO;
import com.example.jobplatform.vo.AdminJobSeekerPendingVO;
import com.example.jobplatform.vo.AdminJobSeekerUserRowVO;
import com.example.jobplatform.vo.AdminPageVO;
import com.example.jobplatform.vo.AdminStatisticsVO;

import java.util.List;

public interface AdminService {

    AdminPageVO<AdminJobSeekerUserRowVO> pageJobSeekerUsers(int page, int size, String keyword);

    AdminPageVO<AdminEnterpriseUserRowVO> pageEnterpriseUsers(int page, int size, String keyword);

    List<AdminAuditLogVO> listAuditLogs(String businessType, Long businessId);

    void blacklistUser(Long userId, AdminBlacklistRequest request);

    void unblacklistUser(Long userId);

    List<AdminJobSeekerPendingVO> listPendingJobSeekers();

    void verifyJobSeeker(Long id, AdminVerifyRequest request);

    List<AdminEnterprisePendingVO> listPendingEnterprises();

    void verifyEnterprise(Long id, AdminVerifyRequest request);

    AdminStatisticsVO getStatistics();
}
