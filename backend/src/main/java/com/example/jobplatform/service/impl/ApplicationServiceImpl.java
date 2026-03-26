package com.example.jobplatform.service.impl;

import com.example.jobplatform.common.BusinessException;
import com.example.jobplatform.dto.ApplicationCreateRequest;
import com.example.jobplatform.dto.ApplicationStatusUpdateRequest;
import com.example.jobplatform.entity.Job;
import com.example.jobplatform.entity.JobApplication;
import com.example.jobplatform.entity.Resume;
import com.example.jobplatform.entity.SysUser;
import com.example.jobplatform.mapper.JobApplicationMapper;
import com.example.jobplatform.mapper.JobMapper;
import com.example.jobplatform.mapper.ResumeMapper;
import com.example.jobplatform.mapper.SysUserMapper;
import com.example.jobplatform.security.UserContext;
import com.example.jobplatform.service.ApplicationService;
import com.example.jobplatform.service.MessageService;
import com.example.jobplatform.vo.ApplicationVO;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    private static final String ROLE_JOB_SEEKER = "JOB_SEEKER";
    private static final String ROLE_ENTERPRISE = "ENTERPRISE";

    private static final String STATUS_SUBMITTED = "SUBMITTED";
    private static final String STATUS_VIEWED = "VIEWED";
    private static final String STATUS_COMMUNICATING = "COMMUNICATING";
    private static final String STATUS_ACCEPTED = "ACCEPTED";
    private static final String STATUS_REJECTED = "REJECTED";

    private static final Set<String> VALID_STATUS = Set.of(
            STATUS_SUBMITTED, STATUS_VIEWED, STATUS_COMMUNICATING, STATUS_ACCEPTED, STATUS_REJECTED
    );

    private final JobApplicationMapper jobApplicationMapper;
    private final JobMapper jobMapper;
    private final ResumeMapper resumeMapper;
    private final SysUserMapper sysUserMapper;
    private final MessageService messageService;

    public ApplicationServiceImpl(JobApplicationMapper jobApplicationMapper,
                                  JobMapper jobMapper,
                                  ResumeMapper resumeMapper,
                                  SysUserMapper sysUserMapper,
                                  MessageService messageService) {
        this.jobApplicationMapper = jobApplicationMapper;
        this.jobMapper = jobMapper;
        this.resumeMapper = resumeMapper;
        this.sysUserMapper = sysUserMapper;
        this.messageService = messageService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApplicationVO create(ApplicationCreateRequest request) {
        Long seekerUserId = getCurrentRoleUserId(ROLE_JOB_SEEKER);
        Job publishedJob = jobMapper.selectPublishedById(request.getJobId());
        if (publishedJob == null) {
            throw new BusinessException(404, "岗位不存在或未发布");
        }
        Resume resume = resumeMapper.selectByIdAndUserId(request.getResumeId(), seekerUserId);
        if (resume == null) {
            throw new BusinessException(400, "简历不存在或不属于当前求职者");
        }
        if (jobApplicationMapper.selectByJobIdAndJobSeekerUserId(request.getJobId(), seekerUserId) != null) {
            throw new BusinessException(400, "同一岗位不能重复投递");
        }

        JobApplication application = new JobApplication();
        application.setJobId(request.getJobId());
        application.setJobSeekerUserId(seekerUserId);
        application.setResumeId(request.getResumeId());
        application.setStatus(STATUS_SUBMITTED);
        application.setApplyTime(LocalDateTime.now());
        try {
            if (jobApplicationMapper.insert(application) <= 0 || application.getId() == null) {
                throw new BusinessException("投递失败");
            }
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(400, "同一岗位不能重复投递");
        }
        return toVO(jobApplicationMapper.selectById(application.getId()));
    }

    @Override
    public List<ApplicationVO> listMyApplications() {
        Long seekerUserId = getCurrentRoleUserId(ROLE_JOB_SEEKER);
        return jobApplicationMapper.selectByJobSeekerUserId(seekerUserId).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApplicationVO> listByJobId(Long jobId) {
        Long enterpriseUserId = getCurrentRoleUserId(ROLE_ENTERPRISE);
        Job ownJob = jobMapper.selectByIdAndEnterpriseUserId(jobId, enterpriseUserId);
        if (ownJob == null) {
            throw new BusinessException(404, "岗位不存在或无权限查看");
        }
        return jobApplicationMapper.selectByJobId(jobId).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApplicationVO updateStatus(Long id, ApplicationStatusUpdateRequest request) {
        Long enterpriseUserId = getCurrentRoleUserId(ROLE_ENTERPRISE);
        JobApplication application = jobApplicationMapper.selectById(id);
        if (application == null) {
            throw new BusinessException(404, "投递记录不存在");
        }
        Job ownJob = jobMapper.selectByIdAndEnterpriseUserId(application.getJobId(), enterpriseUserId);
        if (ownJob == null) {
            throw new BusinessException(403, "无权限处理该投递记录");
        }

        String targetStatus = normalizeStatus(request.getStatus());
        String sourceStatus = normalizeStatus(application.getStatus());
        if (!canTransit(sourceStatus, targetStatus)) {
            throw new BusinessException(400, "当前投递状态不允许变更到目标状态");
        }

        JobApplication update = new JobApplication();
        update.setId(application.getId());
        update.setStatus(targetStatus);
        String inputRemark = normalize(request.getRemark());
        update.setFeedbackContent(inputRemark == null ? application.getFeedbackContent() : inputRemark);
        update.setHandledTime(LocalDateTime.now());
        if (jobApplicationMapper.updateById(update) <= 0) {
            throw new BusinessException("更新投递状态失败");
        }
        ApplicationVO result = toVO(jobApplicationMapper.selectById(id));
        messageService.sendMessage(
                application.getJobSeekerUserId(),
                enterpriseUserId,
                "APPLY_STATUS",
                "投递状态更新",
                buildApplyStatusContent(result.getJobTitle(), targetStatus, result.getRemark()),
                "JOB_APPLICATION",
                application.getId()
        );
        return result;
    }

    private Long getCurrentRoleUserId(String role) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (!role.equals(user.getRole())) {
            throw new BusinessException(403, "当前角色无权限操作");
        }
        return userId;
    }

    private ApplicationVO toVO(JobApplication application) {
        ApplicationVO vo = new ApplicationVO();
        vo.setId(application.getId());
        vo.setJobId(application.getJobId());
        vo.setJobTitle(application.getJobTitle());
        vo.setEnterpriseName(application.getEnterpriseName());
        vo.setJobSeekerUserId(application.getJobSeekerUserId());
        vo.setJobSeekerName(application.getJobSeekerName());
        vo.setResumeId(application.getResumeId());
        vo.setResumeName(application.getResumeName());
        vo.setStatus(normalizeStatus(application.getStatus()));
        vo.setRemark(application.getFeedbackContent());
        vo.setApplyTime(application.getApplyTime());
        vo.setHandledTime(application.getHandledTime());
        return vo;
    }

    private boolean canTransit(String sourceStatus, String targetStatus) {
        if (sourceStatus.equals(targetStatus)) {
            return true;
        }
        if (STATUS_ACCEPTED.equals(sourceStatus) || STATUS_REJECTED.equals(sourceStatus)) {
            return false;
        }
        if (STATUS_SUBMITTED.equals(sourceStatus)) {
            return Set.of(STATUS_VIEWED, STATUS_COMMUNICATING, STATUS_ACCEPTED, STATUS_REJECTED).contains(targetStatus);
        }
        if (STATUS_VIEWED.equals(sourceStatus)) {
            return Set.of(STATUS_COMMUNICATING, STATUS_ACCEPTED, STATUS_REJECTED).contains(targetStatus);
        }
        if (STATUS_COMMUNICATING.equals(sourceStatus)) {
            return Set.of(STATUS_ACCEPTED, STATUS_REJECTED).contains(targetStatus);
        }
        return false;
    }

    private String normalizeStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return STATUS_SUBMITTED;
        }
        String value = status.trim().toUpperCase(Locale.ROOT);
        if ("PENDING".equals(value)) {
            return STATUS_SUBMITTED;
        }
        if ("INTERVIEW".equals(value)) {
            return STATUS_COMMUNICATING;
        }
        if ("PASSED".equals(value)) {
            return STATUS_ACCEPTED;
        }
        if (!VALID_STATUS.contains(value)) {
            throw new BusinessException(400, "投递状态不正确");
        }
        return value;
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String buildApplyStatusContent(String jobTitle, String status, String remark) {
        String statusLabel = switch (status) {
            case STATUS_VIEWED -> "已查看";
            case STATUS_COMMUNICATING -> "沟通中";
            case STATUS_ACCEPTED -> "已录用";
            case STATUS_REJECTED -> "已拒绝";
            default -> "已投递";
        };
        String content = "你投递的岗位《" + (StringUtils.hasText(jobTitle) ? jobTitle : "岗位") + "》状态已更新为：" + statusLabel + "。";
        if (StringUtils.hasText(remark)) {
            content += " 备注：" + remark;
        }
        return content;
    }
}
