package com.example.jobplatform.service.impl;

import com.example.jobplatform.common.BusinessException;
import com.example.jobplatform.dto.JobSaveRequest;
import com.example.jobplatform.entity.Job;
import com.example.jobplatform.entity.JobSeekerProfile;
import com.example.jobplatform.entity.SysUser;
import com.example.jobplatform.mapper.JobMapper;
import com.example.jobplatform.mapper.JobSeekerProfileMapper;
import com.example.jobplatform.mapper.SysUserMapper;
import com.example.jobplatform.security.UserContext;
import com.example.jobplatform.service.JobService;
import com.example.jobplatform.vo.JobPageVO;
import com.example.jobplatform.vo.JobVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class JobServiceImpl implements JobService {

    private static final String ROLE_ENTERPRISE = "ENTERPRISE";
    private static final String ROLE_JOB_SEEKER = "JOB_SEEKER";
    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_OPEN = "OPEN";
    private static final String STATUS_CLOSED = "CLOSED";

    private final JobMapper jobMapper;
    private final SysUserMapper sysUserMapper;
    private final JobSeekerProfileMapper jobSeekerProfileMapper;

    public JobServiceImpl(JobMapper jobMapper, SysUserMapper sysUserMapper, JobSeekerProfileMapper jobSeekerProfileMapper) {
        this.jobMapper = jobMapper;
        this.sysUserMapper = sysUserMapper;
        this.jobSeekerProfileMapper = jobSeekerProfileMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JobVO create(JobSaveRequest request) {
        Long userId = getCurrentEnterpriseUserId();
        assertEnterpriseNotBlacklisted(userId);
        validateSalary(request.getSalaryMin(), request.getSalaryMax());

        Job job = buildJobFromRequest(request);
        job.setEnterpriseUserId(userId);
        job.setStatus(STATUS_DRAFT);
        job.setPublishTime(null);
        if (jobMapper.insert(job) <= 0 || job.getId() == null) {
            throw new BusinessException("发布岗位失败");
        }
        return toVO(jobMapper.selectByIdAndEnterpriseUserId(job.getId(), userId));
    }

    @Override
    public JobPageVO listPublishedJobs(Integer pageNum, Integer pageSize, String keyword, String city, String workMode, String disabilitySupportType) {
        SysUser user = getCurrentUser();
        if (!ROLE_JOB_SEEKER.equals(user.getRole())) {
            throw new BusinessException(403, "仅求职者可浏览岗位列表");
        }

        int validPageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int validPageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 50);
        int offset = (validPageNum - 1) * validPageSize;

        String normalizedKeyword = normalize(keyword);
        String normalizedCity = normalize(city);
        String normalizedWorkMode = normalizeWorkModeForFilter(workMode);
        String normalizedDisabilitySupportType = normalize(disabilitySupportType);
        List<String> disabilityFilterTokens = splitDisabilityTokens(normalizedDisabilitySupportType);
        List<String> tokenParam = disabilityFilterTokens.isEmpty() ? null : disabilityFilterTokens;

        List<Job> all = jobMapper.selectPublishedForSeekerBrowse(
                normalizedKeyword,
                normalizedCity,
                normalizedWorkMode,
                tokenParam
        );
        Long total = jobMapper.countPublishedForSeekerBrowse(
                normalizedKeyword,
                normalizedCity,
                normalizedWorkMode,
                tokenParam
        );

        JobSeekerProfile profile = jobSeekerProfileMapper.selectByUserId(user.getId());
        List<String> seekerDisabilityParts = profile != null ? splitDisabilityTokens(profile.getDisabilityType()) : List.of();
        all.sort(disabilityMatchComparator(seekerDisabilityParts));

        int from = offset;
        int to = Math.min(offset + validPageSize, all.size());
        List<JobVO> records;
        if (from >= all.size()) {
            records = List.of();
        } else {
            records = all.subList(from, to).stream().map(this::toVO).collect(Collectors.toList());
        }

        JobPageVO page = new JobPageVO();
        page.setRecords(records);
        page.setTotal(total == null ? 0L : total);
        page.setPageNum(validPageNum);
        page.setPageSize(validPageSize);
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JobVO update(Long id, JobSaveRequest request) {
        Long userId = getCurrentEnterpriseUserId();
        Job exists = getOwnJob(userId, id);
        validateSalary(request.getSalaryMin(), request.getSalaryMax());

        Job update = buildJobFromRequest(request);
        update.setId(exists.getId());
        if (jobMapper.updateById(update) <= 0) {
            throw new BusinessException("更新岗位失败");
        }
        return toVO(getOwnJob(userId, id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Long userId = getCurrentEnterpriseUserId();
        Job exists = getOwnJob(userId, id);
        if (jobMapper.deleteById(exists.getId()) <= 0) {
            throw new BusinessException("删除岗位失败");
        }
    }

    @Override
    public List<JobVO> listMyJobs() {
        Long userId = getCurrentEnterpriseUserId();
        return jobMapper.selectByEnterpriseUserId(userId).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JobVO updateStatus(Long id, String status) {
        Long userId = getCurrentEnterpriseUserId();
        Job exists = getOwnJob(userId, id);

        String targetStatus = normalizeStatus(status);
        if (!STATUS_OPEN.equals(targetStatus) && !STATUS_CLOSED.equals(targetStatus)) {
            throw new BusinessException(400, "仅支持上架(OPEN)或下架(CLOSED)");
        }
        if (!canTransit(exists.getStatus(), targetStatus)) {
            throw new BusinessException(400, "当前岗位状态不允许变更到目标状态");
        }
        if (STATUS_OPEN.equals(targetStatus)) {
            assertEnterpriseNotBlacklisted(userId);
        }

        Job update = new Job();
        update.setId(exists.getId());
        update.setStatus(targetStatus);
        if (STATUS_OPEN.equals(targetStatus)) {
            update.setPublishTime(LocalDateTime.now());
        }

        if (jobMapper.updateById(update) <= 0) {
            throw new BusinessException("更新岗位状态失败");
        }
        return toVO(getOwnJob(userId, id));
    }

    @Override
    public JobVO detail(Long id) {
        SysUser user = getCurrentUser();
        if (ROLE_ENTERPRISE.equals(user.getRole())) {
            return toVO(getOwnJob(user.getId(), id));
        }
        if (ROLE_JOB_SEEKER.equals(user.getRole())) {
            Job publishedJob = jobMapper.selectPublishedById(id);
            if (publishedJob == null) {
                throw new BusinessException(404, "岗位不存在或未发布");
            }
            return toVO(publishedJob);
        }
        throw new BusinessException(403, "当前角色无权限查看岗位详情");
    }

    private Long getCurrentEnterpriseUserId() {
        SysUser user = getCurrentUser();
        if (!ROLE_ENTERPRISE.equals(user.getRole())) {
            throw new BusinessException(403, "仅企业用户可操作岗位");
        }
        return user.getId();
    }

    private SysUser getCurrentUser() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }

    private void assertEnterpriseNotBlacklisted(Long enterpriseUserId) {
        SysUser enterpriseUser = sysUserMapper.selectById(enterpriseUserId);
        if (enterpriseUser != null && enterpriseUser.getBlacklisted() != null && enterpriseUser.getBlacklisted() == 1) {
            throw new BusinessException(403, "您的企业账号已被列入黑名单，无法新建或上架岗位。请联系管理员解除。");
        }
    }

    private Job getOwnJob(Long userId, Long jobId) {
        Job job = jobMapper.selectByIdAndEnterpriseUserId(jobId, userId);
        if (job == null) {
            throw new BusinessException(404, "岗位不存在或无权限");
        }
        return job;
    }

    private Job buildJobFromRequest(JobSaveRequest request) {
        Job job = new Job();
        job.setTitle(normalize(request.getTitle()));
        job.setCategory(normalize(request.getCategory()));
        job.setCity(normalize(request.getCity()));
        job.setSalary(buildSalaryRange(request.getSalaryMin(), request.getSalaryMax()));
        job.setWorkMode(normalizeWorkMode(request.getWorkMode()));
        job.setEducationRequirement(normalize(request.getEducationRequirement()));
        job.setExperienceRequirement(normalize(request.getExperienceRequirement()));
        job.setSkillRequirements(normalize(request.getSkillRequirements()));
        job.setDisabilitySupportType(normalize(request.getDisabilitySupportType()));
        job.setAccessibilitySupportDesc(normalize(request.getWelfare()));
        job.setJobDescription(normalize(request.getJobDescription()));
        return job;
    }

    private JobVO toVO(Job job) {
        if (job == null) {
            return null;
        }
        JobVO vo = new JobVO();
        vo.setId(job.getId());
        vo.setEnterpriseName(job.getEnterpriseName());
        vo.setTitle(job.getTitle());
        vo.setCategory(job.getCategory());
        vo.setCity(job.getCity());
        Integer[] salary = parseSalary(job.getSalary());
        vo.setSalaryMin(salary[0]);
        vo.setSalaryMax(salary[1]);
        vo.setEducationRequirement(job.getEducationRequirement());
        vo.setExperienceRequirement(job.getExperienceRequirement());
        vo.setWorkMode(job.getWorkMode());
        vo.setSkillRequirements(job.getSkillRequirements());
        vo.setDisabilitySupportType(job.getDisabilitySupportType());
        vo.setWelfare(job.getAccessibilitySupportDesc());
        vo.setJobDescription(job.getJobDescription());
        vo.setStatus(normalizeStatus(job.getStatus()));
        vo.setPublishTime(job.getPublishTime());
        vo.setCreatedAt(job.getCreatedAt());
        vo.setUpdatedAt(job.getUpdatedAt());
        return vo;
    }

    private void validateSalary(Integer salaryMin, Integer salaryMax) {
        if (salaryMin == null || salaryMax == null) {
            throw new BusinessException(400, "薪资区间不能为空");
        }
        if (salaryMin < 0 || salaryMax < 0) {
            throw new BusinessException(400, "薪资不能为负数");
        }
        if (salaryMin > salaryMax) {
            throw new BusinessException(400, "最低薪资不能大于最高薪资");
        }
    }

    private boolean canTransit(String sourceStatus, String targetStatus) {
        String normalizedSource = normalizeStatus(sourceStatus);
        if (normalizedSource.equals(targetStatus)) {
            return true;
        }
        return (STATUS_DRAFT.equals(normalizedSource) && STATUS_OPEN.equals(targetStatus))
                || (STATUS_DRAFT.equals(normalizedSource) && STATUS_CLOSED.equals(targetStatus))
                || (STATUS_OPEN.equals(normalizedSource) && STATUS_CLOSED.equals(targetStatus))
                || (STATUS_CLOSED.equals(normalizedSource) && STATUS_OPEN.equals(targetStatus));
    }

    private String normalizeStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return STATUS_DRAFT;
        }
        String value = status.trim().toUpperCase(Locale.ROOT);
        if (STATUS_OPEN.equals(value)) {
            return STATUS_OPEN;
        }
        if (STATUS_CLOSED.equals(value)) {
            return STATUS_CLOSED;
        }
        return STATUS_DRAFT;
    }

    private String normalizeWorkMode(String workMode) {
        if (!StringUtils.hasText(workMode)) {
            return "OFFLINE";
        }
        String mode = workMode.trim().toUpperCase(Locale.ROOT);
        if ("REMOTE".equals(mode) || "HYBRID".equals(mode)) {
            return mode;
        }
        return "OFFLINE";
    }

    private String normalizeWorkModeForFilter(String workMode) {
        if (!StringUtils.hasText(workMode)) {
            return null;
        }
        String mode = workMode.trim().toUpperCase(Locale.ROOT);
        if ("OFFLINE".equals(mode) || "REMOTE".equals(mode) || "HYBRID".equals(mode)) {
            return mode;
        }
        throw new BusinessException(400, "工作方式筛选值不正确");
    }

    private String buildSalaryRange(Integer salaryMin, Integer salaryMax) {
        return salaryMin + "-" + salaryMax;
    }

    private Integer[] parseSalary(String salary) {
        Integer[] result = new Integer[]{0, 0};
        if (!StringUtils.hasText(salary)) {
            return result;
        }
        String[] values = salary.trim().split("-");
        if (values.length == 2) {
            result[0] = parseIntSafe(values[0]);
            result[1] = parseIntSafe(values[1]);
            return result;
        }
        Integer single = parseIntSafe(salary);
        result[0] = single;
        result[1] = single;
        return result;
    }

    private Integer parseIntSafe(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception ignore) {
            return 0;
        }
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    /** 与个人中心 disability_type 存储规则一致：逗号/顿号等分隔 */
    private List<String> splitDisabilityTokens(String raw) {
        if (!StringUtils.hasText(raw)) {
            return List.of();
        }
        return Arrays.stream(raw.split("[,，、;；\\s]+"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }

    /**
     * 岗位浏览排序：求职者资料中每种残疾类型在岗位「适合招收的残疾类型」文案中命中则计 1 分，分高靠前；不向前端返回分值。
     */
    private Comparator<Job> disabilityMatchComparator(List<String> seekerParts) {
        return Comparator
                .comparingInt((Job j) -> countDisabilityTokenHits(seekerParts, j.getDisabilitySupportType()))
                .reversed()
                .thenComparing(Job::getPublishTime, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Job::getId, Comparator.reverseOrder());
    }

    private int countDisabilityTokenHits(List<String> seekerParts, String supportType) {
        if (seekerParts.isEmpty() || !StringUtils.hasText(supportType)) {
            return 0;
        }
        String st = supportType.toLowerCase(Locale.ROOT);
        int n = 0;
        for (String p : seekerParts) {
            String t = p.trim().toLowerCase(Locale.ROOT);
            if (StringUtils.hasText(t) && st.contains(t)) {
                n++;
            }
        }
        return n;
    }
}
