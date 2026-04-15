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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationServiceImpl.class);

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

    private static final DateTimeFormatter INTERVIEW_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int MESSAGE_CONTENT_MAX = 980;

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
        SysUser seeker = sysUserMapper.selectById(seekerUserId);
        if (seeker != null && seeker.getBlacklisted() != null && seeker.getBlacklisted() == 1) {
            throw new BusinessException(403, "您的账号已被列入黑名单，无法投递简历。请联系管理员解除。");
        }
        Job publishedJob = jobMapper.selectPublishedById(request.getJobId());
        if (publishedJob == null) {
            throw new BusinessException(404, "岗位不存在或未发布");
        }
        Resume resume = resumeMapper.selectByIdAndUserId(request.getResumeId(), seekerUserId);
        if (resume == null) {
            throw new BusinessException(400, "简历不存在或不属于当前求职者");
        }

        assertCanApplyWithSameResume(request.getJobId(), seekerUserId, request.getResumeId());

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
            String detail = NestedExceptionUtils.getMostSpecificCause(exception).getMessage();
            if (isDuplicateApplyToSameJobConstraint(detail)) {
                log.warn("Apply insert blocked by DB unique index uk_job_application_unique; run migration_20260412_job_application_multi_apply.sql: {}", detail);
                throw new BusinessException(400, "已投递过该岗位");
            }
            log.warn("Job application insert failed: {}", detail);
            throw new BusinessException(400, "投递失败，请确认岗位与简历有效");
        }
        notifyEnterpriseNewApplication(publishedJob, seekerUserId, resume, application.getId());
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
        if (request.getInterviewTime() != null) {
            update.setInterviewTime(request.getInterviewTime());
        }
        String inputAddress = normalize(request.getInterviewAddress());
        if (inputAddress != null) {
            update.setInterviewAddress(inputAddress);
        }
        String inputHr = normalize(request.getHrContact());
        if (inputHr != null) {
            update.setHrContact(inputHr);
        }
        update.setHandledTime(LocalDateTime.now());
        if (jobApplicationMapper.updateById(update) <= 0) {
            throw new BusinessException("更新投递状态失败");
        }
        if (STATUS_REJECTED.equals(targetStatus)) {
            jobApplicationMapper.clearInterviewMeta(application.getId());
        }
        ApplicationVO result = toVO(jobApplicationMapper.selectById(id));
        messageService.sendMessage(
                application.getJobSeekerUserId(),
                enterpriseUserId,
                "APPLY_STATUS",
                "投递状态更新",
                truncateMessage(buildApplyStatusContent(result)),
                "JOB_APPLICATION",
                application.getId()
        );
        return result;
    }

    /**
     * 同一岗位+同一求职者：使用相同简历时，仅当历史记录为「已拒绝」可再次投递；换用其他简历可并行再投（需库表无 uk_job_application_unique）。
     */
    private void assertCanApplyWithSameResume(Long jobId, Long seekerUserId, Long resumeId) {
        List<JobApplication> existing = jobApplicationMapper.selectAllByJobIdAndJobSeekerUserId(jobId, seekerUserId);
        for (JobApplication row : existing) {
            if (!resumeId.equals(row.getResumeId())) {
                continue;
            }
            String st = normalizeStatusForDuplicateRule(row.getStatus());
            if (!STATUS_REJECTED.equals(st)) {
                throw new BusinessException(400, "已投递过该岗位");
            }
        }
    }

    /** 与 {@link #normalizeStatus} 映射一致，用于重复投递判断，不因未知值抛错 */
    private String normalizeStatusForDuplicateRule(String status) {
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
        if (VALID_STATUS.contains(value)) {
            return value;
        }
        return STATUS_SUBMITTED;
    }

    /**
     * 旧库存在 uk_job_application_unique(job_id, job_seeker_user_id) 时，同一求职者再次投递同一岗位会触发 Duplicate entry。
     */
    private boolean isDuplicateApplyToSameJobConstraint(String dbMessage) {
        if (dbMessage == null || dbMessage.isBlank()) {
            return false;
        }
        String lower = dbMessage.toLowerCase(Locale.ROOT);
        if (lower.contains("uk_job_application_unique")) {
            return true;
        }
        return lower.contains("duplicate") && lower.contains("job_application");
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
        vo.setInterviewTime(application.getInterviewTime());
        vo.setInterviewAddress(application.getInterviewAddress());
        vo.setHrContact(application.getHrContact());
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

    /**
     * 求职者投递成功后，通知岗位所属企业用户（站内信 + 未读计数，与现有消息中心一致）。
     */
    private void notifyEnterpriseNewApplication(Job job, Long seekerUserId, Resume resume, Long applicationId) {
        Long enterpriseUserId = job.getEnterpriseUserId();
        if (enterpriseUserId == null || enterpriseUserId.equals(seekerUserId)) {
            return;
        }
        SysUser seeker = sysUserMapper.selectById(seekerUserId);
        String seekerLabel = seekerDisplayLabel(seeker);
        String jobTitle = StringUtils.hasText(job.getTitle()) ? job.getTitle() : "岗位";
        String resumeLabel = resume != null && StringUtils.hasText(resume.getResumeName()) ? resume.getResumeName() : "简历";
        String content = "求职者「" + seekerLabel + "」使用《" + resumeLabel + "》投递了您的岗位《" + jobTitle
                + "》，请到「投递记录」查看并处理。";
        messageService.sendMessage(
                enterpriseUserId,
                seekerUserId,
                "APPLY_RECEIVED",
                "收到新的简历投递",
                truncateMessage(content),
                "JOB_APPLICATION",
                applicationId
        );
    }

    private String seekerDisplayLabel(SysUser seeker) {
        if (seeker == null) {
            return "求职者";
        }
        if (StringUtils.hasText(seeker.getRealName())) {
            return seeker.getRealName().trim();
        }
        if (StringUtils.hasText(seeker.getUsername())) {
            return seeker.getUsername();
        }
        return "求职者";
    }

    private String buildApplyStatusContent(ApplicationVO vo) {
        String status = vo.getStatus();
        String statusLabel = switch (status) {
            case STATUS_VIEWED -> "企业已查阅";
            case STATUS_COMMUNICATING -> "待面试";
            case STATUS_ACCEPTED -> "已录用";
            case STATUS_REJECTED -> "流程中止";
            default -> "已投递";
        };
        String jobTitle = StringUtils.hasText(vo.getJobTitle()) ? vo.getJobTitle() : "岗位";
        StringBuilder content = new StringBuilder();
        content.append("你投递的岗位《").append(jobTitle).append("》进度已更新为：").append(statusLabel).append("。");
        if (StringUtils.hasText(vo.getRemark())) {
            content.append(" 企业回复：").append(vo.getRemark().trim());
        }
        if (vo.getInterviewTime() != null) {
            content.append(" 面试时间：").append(INTERVIEW_TIME_FORMAT.format(vo.getInterviewTime()));
        }
        if (StringUtils.hasText(vo.getInterviewAddress())) {
            content.append(" 面试地址：").append(vo.getInterviewAddress().trim());
        }
        if (StringUtils.hasText(vo.getHrContact())) {
            content.append(" HR联系方式：").append(vo.getHrContact().trim());
        }
        return content.toString();
    }

    private String truncateMessage(String content) {
        if (content == null || content.length() <= MESSAGE_CONTENT_MAX) {
            return content;
        }
        return content.substring(0, MESSAGE_CONTENT_MAX) + "…";
    }
}
