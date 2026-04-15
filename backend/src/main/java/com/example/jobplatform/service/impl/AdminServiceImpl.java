package com.example.jobplatform.service.impl;

import com.example.jobplatform.common.BusinessException;
import com.example.jobplatform.dto.AdminBlacklistRequest;
import com.example.jobplatform.dto.AdminVerifyRequest;
import com.example.jobplatform.entity.EnterpriseProfile;
import com.example.jobplatform.entity.JobSeekerProfile;
import com.example.jobplatform.entity.SysAuditLog;
import com.example.jobplatform.entity.SysUser;
import com.example.jobplatform.mapper.EnterpriseProfileMapper;
import com.example.jobplatform.mapper.JobApplicationMapper;
import com.example.jobplatform.mapper.JobMapper;
import com.example.jobplatform.mapper.JobSeekerProfileMapper;
import com.example.jobplatform.mapper.SysAuditLogMapper;
import com.example.jobplatform.mapper.SysUserMapper;
import com.example.jobplatform.security.UserContext;
import com.example.jobplatform.service.AdminService;
import com.example.jobplatform.service.MessageService;
import com.example.jobplatform.vo.AdminAuditLogVO;
import com.example.jobplatform.vo.AdminEnterprisePendingVO;
import com.example.jobplatform.vo.AdminEnterpriseUserRowVO;
import com.example.jobplatform.vo.AdminJobSeekerPendingVO;
import com.example.jobplatform.vo.AdminJobSeekerUserRowVO;
import com.example.jobplatform.vo.AdminPageVO;
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
    private static final String BUSINESS_JOB_SEEKER_PROFILE = "JOB_SEEKER_PROFILE";
    private static final String BUSINESS_ENTERPRISE_PROFILE = "ENTERPRISE_PROFILE";

    private final JobSeekerProfileMapper jobSeekerProfileMapper;
    private final EnterpriseProfileMapper enterpriseProfileMapper;
    private final SysUserMapper sysUserMapper;
    private final SysAuditLogMapper sysAuditLogMapper;
    private final JobMapper jobMapper;
    private final JobApplicationMapper jobApplicationMapper;
    private final MessageService messageService;

    public AdminServiceImpl(JobSeekerProfileMapper jobSeekerProfileMapper,
                            EnterpriseProfileMapper enterpriseProfileMapper,
                            SysUserMapper sysUserMapper,
                            SysAuditLogMapper sysAuditLogMapper,
                            JobMapper jobMapper,
                            JobApplicationMapper jobApplicationMapper,
                            MessageService messageService) {
        this.jobSeekerProfileMapper = jobSeekerProfileMapper;
        this.enterpriseProfileMapper = enterpriseProfileMapper;
        this.sysUserMapper = sysUserMapper;
        this.sysAuditLogMapper = sysAuditLogMapper;
        this.jobMapper = jobMapper;
        this.jobApplicationMapper = jobApplicationMapper;
        this.messageService = messageService;
    }

    @Override
    public AdminPageVO<AdminJobSeekerUserRowVO> pageJobSeekerUsers(int page, int size, String keyword) {
        requireAdminUserId();
        int[] ps = normalizePageParams(page, size);
        String kw = normalizeKeyword(keyword);
        long total = sysUserMapper.countJobSeekerUsersForAdmin(kw);
        List<AdminJobSeekerUserRowVO> records = total == 0
                ? List.of()
                : sysUserMapper.selectJobSeekerUsersPageForAdmin(kw, ps[0], ps[1]);
        AdminPageVO<AdminJobSeekerUserRowVO> vo = new AdminPageVO<>();
        vo.setRecords(records);
        vo.setTotal(total);
        return vo;
    }

    @Override
    public AdminPageVO<AdminEnterpriseUserRowVO> pageEnterpriseUsers(int page, int size, String keyword) {
        requireAdminUserId();
        int[] ps = normalizePageParams(page, size);
        String kw = normalizeKeyword(keyword);
        long total = sysUserMapper.countEnterpriseUsersForAdmin(kw);
        List<AdminEnterpriseUserRowVO> records = total == 0
                ? List.of()
                : sysUserMapper.selectEnterpriseUsersPageForAdmin(kw, ps[0], ps[1]);
        AdminPageVO<AdminEnterpriseUserRowVO> vo = new AdminPageVO<>();
        vo.setRecords(records);
        vo.setTotal(total);
        return vo;
    }

    @Override
    public List<AdminAuditLogVO> listAuditLogs(String businessType, Long businessId) {
        requireAdminUserId();
        if (businessId == null || businessId <= 0) {
            throw new BusinessException(400, "业务ID无效");
        }
        String type = normalizeAuditBusinessType(businessType);
        return sysAuditLogMapper.selectListForAdminByBusiness(type, businessId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void blacklistUser(Long userId, AdminBlacklistRequest request) {
        Long adminUserId = requireAdminUserId();
        String reason = normalizeBlacklistReason(request != null ? request.getReason() : null);
        SysUser target = sysUserMapper.selectById(userId);
        if (target == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (ROLE_ADMIN.equals(target.getRole())) {
            throw new BusinessException(400, "不可对管理员执行黑名单操作");
        }
        if (!ROLE_JOB_SEEKER.equals(target.getRole()) && !ROLE_ENTERPRISE.equals(target.getRole())) {
            throw new BusinessException(400, "仅支持对求职者或企业账号执行黑名单");
        }
        if (target.getBlacklisted() != null && target.getBlacklisted() == 1) {
            throw new BusinessException(400, "该用户已在黑名单中");
        }
        if (sysUserMapper.markBlacklisted(userId, LocalDateTime.now(), adminUserId, reason) <= 0) {
            throw new BusinessException("加入黑名单失败");
        }
        if (ROLE_JOB_SEEKER.equals(target.getRole())) {
            messageService.sendMessage(
                    userId,
                    adminUserId,
                    "BLACKLIST_NOTICE",
                    "账号已被列入黑名单",
                    "您的求职者账号已被管理员列入平台黑名单，无法再使用简历投递功能。原因：" + reason
                            + "。如需解除限制，请联系管理员处理。",
                    "SYS_USER",
                    userId
            );
        } else {
            messageService.sendMessage(
                    userId,
                    adminUserId,
                    "BLACKLIST_NOTICE",
                    "账号已被列入黑名单",
                    "您的企业账号已被管理员列入平台黑名单：在招岗位将对求职者不可见，且无法新建岗位或上架岗位。原因：" + reason
                            + "。如需解除限制，请联系管理员处理。",
                    "SYS_USER",
                    userId
            );
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unblacklistUser(Long userId) {
        Long adminUserId = requireAdminUserId();
        SysUser target = sysUserMapper.selectById(userId);
        if (target == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (ROLE_ADMIN.equals(target.getRole())) {
            throw new BusinessException(400, "不可对管理员执行黑名单操作");
        }
        if (target.getBlacklisted() == null || target.getBlacklisted() != 1) {
            throw new BusinessException(400, "该用户不在黑名单中");
        }
        if (sysUserMapper.clearBlacklist(userId) <= 0) {
            throw new BusinessException("解除黑名单失败");
        }
        if (ROLE_JOB_SEEKER.equals(target.getRole())) {
            messageService.sendMessage(
                    userId,
                    adminUserId,
                    "BLACKLIST_NOTICE",
                    "黑名单已解除",
                    "您的求职者账号黑名单已解除，投递功能已恢复。",
                    "SYS_USER",
                    userId
            );
        } else if (ROLE_ENTERPRISE.equals(target.getRole())) {
            messageService.sendMessage(
                    userId,
                    adminUserId,
                    "BLACKLIST_NOTICE",
                    "黑名单已解除",
                    "您的企业账号黑名单已解除，可正常新建、上架岗位，在招岗位将重新对求职者展示。",
                    "SYS_USER",
                    userId
            );
        }
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
        insertAuditLog(BUSINESS_JOB_SEEKER_PROFILE, id, verifyStatus, request.getComment(), adminUserId);
        sendAuditNotice(profile.getUserId(), adminUserId, "求职者认证审核结果", verifyStatus, request.getComment(), BUSINESS_JOB_SEEKER_PROFILE, id);
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
        insertAuditLog(BUSINESS_ENTERPRISE_PROFILE, id, verifyStatus, request.getComment(), adminUserId);
        sendAuditNotice(profile.getUserId(), adminUserId, "企业认证审核结果", verifyStatus, request.getComment(), BUSINESS_ENTERPRISE_PROFILE, id);
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

    private int[] normalizePageParams(int page, int size) {
        int p = page < 1 ? 1 : page;
        int s = size < 1 ? 10 : Math.min(size, 100);
        int offset = (p - 1) * s;
        return new int[]{offset, s};
    }

    private String normalizeKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        String trimmed = keyword.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeAuditBusinessType(String businessType) {
        if (!StringUtils.hasText(businessType)) {
            throw new BusinessException(400, "业务类型不能为空");
        }
        String value = businessType.trim().toUpperCase(Locale.ROOT);
        if (BUSINESS_JOB_SEEKER_PROFILE.equals(value)) {
            return BUSINESS_JOB_SEEKER_PROFILE;
        }
        if (BUSINESS_ENTERPRISE_PROFILE.equals(value)) {
            return BUSINESS_ENTERPRISE_PROFILE;
        }
        throw new BusinessException(400, "业务类型无效");
    }

    private String normalizeBlacklistReason(String reason) {
        if (!StringUtils.hasText(reason)) {
            throw new BusinessException(400, "拉黑原因不能为空");
        }
        String trimmed = reason.trim();
        if (trimmed.length() > 500) {
            throw new BusinessException(400, "拉黑原因不能超过500字");
        }
        return trimmed;
    }

    private void insertAuditLog(String businessType,
                                Long businessId,
                                String verifyStatus,
                                String comment,
                                Long operatorUserId) {
        SysAuditLog log = new SysAuditLog();
        log.setBusinessType(businessType);
        log.setBusinessId(businessId);
        log.setAuditStatus(verifyStatus);
        log.setAuditComment(StringUtils.hasText(comment) ? comment.trim() : null);
        log.setOperatorUserId(operatorUserId);
        log.setOperatedAt(LocalDateTime.now());
        if (sysAuditLogMapper.insert(log) <= 0) {
            throw new BusinessException("写入审核记录失败");
        }
    }
}
