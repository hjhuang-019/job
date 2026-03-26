package com.example.jobplatform.service.impl;

import com.example.jobplatform.common.BusinessException;
import com.example.jobplatform.dto.AdminVerifyRequest;
import com.example.jobplatform.entity.EnterpriseProfile;
import com.example.jobplatform.entity.JobSeekerProfile;
import com.example.jobplatform.entity.SysUser;
import com.example.jobplatform.mapper.EnterpriseProfileMapper;
import com.example.jobplatform.mapper.JobApplicationMapper;
import com.example.jobplatform.mapper.JobMapper;
import com.example.jobplatform.mapper.JobSeekerProfileMapper;
import com.example.jobplatform.mapper.SysUserMapper;
import com.example.jobplatform.security.UserContext;
import com.example.jobplatform.service.AdminService;
import com.example.jobplatform.service.MessageService;
import com.example.jobplatform.vo.AdminEnterprisePendingVO;
import com.example.jobplatform.vo.AdminJobSeekerPendingVO;
import com.example.jobplatform.vo.AdminStatisticsVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class AdminServiceImpl implements AdminService {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_JOB_SEEKER = "JOB_SEEKER";
    private static final String ROLE_ENTERPRISE = "ENTERPRISE";
    private static final String VERIFY_APPROVED = "APPROVED";
    private static final String VERIFY_REJECTED = "REJECTED";

    private final JobSeekerProfileMapper jobSeekerProfileMapper;
    private final EnterpriseProfileMapper enterpriseProfileMapper;
    private final SysUserMapper sysUserMapper;
    private final JobMapper jobMapper;
    private final JobApplicationMapper jobApplicationMapper;
    private final MessageService messageService;

    public AdminServiceImpl(JobSeekerProfileMapper jobSeekerProfileMapper,
                            EnterpriseProfileMapper enterpriseProfileMapper,
                            SysUserMapper sysUserMapper,
                            JobMapper jobMapper,
                            JobApplicationMapper jobApplicationMapper,
                            MessageService messageService) {
        this.jobSeekerProfileMapper = jobSeekerProfileMapper;
        this.enterpriseProfileMapper = enterpriseProfileMapper;
        this.sysUserMapper = sysUserMapper;
        this.jobMapper = jobMapper;
        this.jobApplicationMapper = jobApplicationMapper;
        this.messageService = messageService;
    }

    @Override
    public List<AdminJobSeekerPendingVO> listPendingJobSeekers() {
        requireAdminUserId();
        return jobSeekerProfileMapper.selectPendingForAdmin();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifyJobSeeker(Long id, AdminVerifyRequest request) {
        Long adminUserId = requireAdminUserId();
        JobSeekerProfile profile = jobSeekerProfileMapper.selectById(id);
        if (profile == null) {
            throw new BusinessException(404, "求职者认证资料不存在");
        }
        String verifyStatus = normalizeVerifyStatus(request.getStatus());
        int updated = jobSeekerProfileMapper.updateVerifyByAdmin(id, verifyStatus, adminUserId, LocalDateTime.now());
        if (updated <= 0) {
            throw new BusinessException("求职者认证审核失败");
        }
        sendAuditNotice(profile.getUserId(), adminUserId, "求职者认证审核结果", verifyStatus, request.getComment(), "JOB_SEEKER_PROFILE", id);
    }

    @Override
    public List<AdminEnterprisePendingVO> listPendingEnterprises() {
        requireAdminUserId();
        return enterpriseProfileMapper.selectPendingForAdmin();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifyEnterprise(Long id, AdminVerifyRequest request) {
        Long adminUserId = requireAdminUserId();
        EnterpriseProfile profile = enterpriseProfileMapper.selectById(id);
        if (profile == null) {
            throw new BusinessException(404, "企业认证资料不存在");
        }
        String verifyStatus = normalizeVerifyStatus(request.getStatus());
        int updated = enterpriseProfileMapper.updateVerifyByAdmin(id, verifyStatus, adminUserId, LocalDateTime.now());
        if (updated <= 0) {
            throw new BusinessException("企业认证审核失败");
        }
        sendAuditNotice(profile.getUserId(), adminUserId, "企业认证审核结果", verifyStatus, request.getComment(), "ENTERPRISE_PROFILE", id);
    }

    @Override
    public AdminStatisticsVO getStatistics() {
        requireAdminUserId();
        AdminStatisticsVO vo = new AdminStatisticsVO();
        vo.setUserCount(zeroIfNull(sysUserMapper.countByRole(ROLE_JOB_SEEKER)));
        vo.setEnterpriseCount(zeroIfNull(sysUserMapper.countByRole(ROLE_ENTERPRISE)));
        vo.setJobCount(zeroIfNull(jobMapper.countAll()));
        vo.setApplicationCount(zeroIfNull(jobApplicationMapper.countAll()));
        return vo;
    }

    private Long requireAdminUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        SysUser sysUser = sysUserMapper.selectById(userId);
        if (sysUser == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (!ROLE_ADMIN.equals(sysUser.getRole())) {
            throw new BusinessException(403, "仅管理员可操作该接口");
        }
        return userId;
    }

    private String normalizeVerifyStatus(String status) {
        if (!StringUtils.hasText(status)) {
            throw new BusinessException(400, "审核结果不能为空");
        }
        String value = status.trim().toUpperCase(Locale.ROOT);
        if ("PASS".equals(value) || "APPROVED".equals(value)) {
            return VERIFY_APPROVED;
        }
        if ("REJECT".equals(value) || "REJECTED".equals(value)) {
            return VERIFY_REJECTED;
        }
        throw new BusinessException(400, "审核结果仅支持 PASS 或 REJECT");
    }

    private void sendAuditNotice(Long receiverUserId,
                                 Long adminUserId,
                                 String title,
                                 String verifyStatus,
                                 String comment,
                                 String businessType,
                                 Long businessId) {
        String statusLabel = VERIFY_APPROVED.equals(verifyStatus) ? "通过" : "驳回";
        String content = title + "已" + statusLabel + "。";
        if (StringUtils.hasText(comment)) {
            content += " 备注：" + comment.trim();
        }
        messageService.sendMessage(
                receiverUserId,
                adminUserId,
                "AUDIT_NOTICE",
                title,
                content,
                businessType,
                businessId
        );
    }

    private Long zeroIfNull(Long value) {
        return value == null ? 0L : value;
    }
}
