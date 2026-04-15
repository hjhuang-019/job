package com.example.jobplatform.service.impl;

import com.example.jobplatform.common.BusinessException;
import com.example.jobplatform.entity.Job;
import com.example.jobplatform.entity.JobSeekerProfile;
import com.example.jobplatform.entity.SysUser;
import com.example.jobplatform.mapper.JobMapper;
import com.example.jobplatform.mapper.JobSeekerProfileMapper;
import com.example.jobplatform.mapper.SysUserMapper;
import com.example.jobplatform.security.UserContext;
import com.example.jobplatform.service.RecommendService;
import com.example.jobplatform.vo.RecommendJobVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecommendServiceImpl implements RecommendService {

    private static final String ROLE_JOB_SEEKER = "JOB_SEEKER";
    private static final int SKILL_SCORE = 50;
    private static final int DISABILITY_SCORE = 25;
    private static final int WORK_MODE_SCORE = 15;
    private static final int CITY_SCORE = 10;

    private final SysUserMapper sysUserMapper;
    private final JobSeekerProfileMapper jobSeekerProfileMapper;
    private final JobMapper jobMapper;

    public RecommendServiceImpl(SysUserMapper sysUserMapper,
                                JobSeekerProfileMapper jobSeekerProfileMapper,
                                JobMapper jobMapper) {
        this.sysUserMapper = sysUserMapper;
        this.jobSeekerProfileMapper = jobSeekerProfileMapper;
        this.jobMapper = jobMapper;
    }

    @Override
    public List<RecommendJobVO> recommendJobs() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (!ROLE_JOB_SEEKER.equals(user.getRole())) {
            throw new BusinessException(403, "仅求职者可查看推荐岗位");
        }

        JobSeekerProfile profile = jobSeekerProfileMapper.selectByUserId(userId);
        if (profile == null) {
            throw new BusinessException(400, "请先完善求职者资料后再查看推荐");
        }

        List<Job> jobs = jobMapper.selectPublishedForRecommend();
        List<RecommendJobVO> result = jobs.stream()
                .map(job -> buildRecommend(profile, job))
                .filter(item -> item.getMatchScore() > 0)
                .sorted(Comparator.comparing(RecommendJobVO::getMatchScore).reversed())
                .limit(20)
                .collect(Collectors.toList());
        return result;
    }

    private RecommendJobVO buildRecommend(JobSeekerProfile profile, Job job) {
        int skillScore = calcSkillScore(profile.getSkills(), job.getSkillRequirements());
        int disabilityScore = calcDisabilityScore(profile.getDisabilityType(), job.getDisabilitySupportType());
        int workModeScore = calcWorkModeScore(profile.getAcceptRemote(), job.getWorkMode());
        int cityScore = calcCityScore(profile.getExpectedCity(), job.getCity());
        int totalScore = skillScore + disabilityScore + workModeScore + cityScore;

        List<String> reasons = new ArrayList<>();
        if (skillScore > 0) {
            reasons.add("技能匹配+" + skillScore);
        }
        if (disabilityScore > 0) {
            reasons.add("残疾适配+" + disabilityScore);
        }
        if (workModeScore > 0) {
            reasons.add("工作方式匹配+" + workModeScore);
        }
        if (cityScore > 0) {
            reasons.add("城市匹配+" + cityScore);
        }
        if (reasons.isEmpty()) {
            reasons.add("基础信息匹配度较低");
        }

        RecommendJobVO vo = new RecommendJobVO();
        vo.setJobId(job.getId());
        vo.setTitle(job.getTitle());
        vo.setEnterpriseName(job.getEnterpriseName());
        vo.setMatchScore(totalScore);
        vo.setMatchReason(String.join("；", reasons));
        return vo;
    }

    /**
     * 规则化技能匹配：按“命中的技能数 / 求职者技能总数”比例折算到50分，简单且便于答辩解释。
     */
    private int calcSkillScore(String seekerSkillsRaw, String jobSkillsRaw) {
        List<String> seekerSkills = splitSkills(seekerSkillsRaw);
        if (seekerSkills.isEmpty() || !StringUtils.hasText(jobSkillsRaw)) {
            return 0;
        }
        String jobSkillsText = jobSkillsRaw.toLowerCase(Locale.ROOT);
        long matchedCount = seekerSkills.stream()
                .filter(skill -> jobSkillsText.contains(skill.toLowerCase(Locale.ROOT)))
                .count();
        if (matchedCount <= 0) {
            return 0;
        }
        return (int) Math.round((matchedCount * 1.0 / seekerSkills.size()) * SKILL_SCORE);
    }

    private int calcDisabilityScore(String disabilityType, String supportType) {
        if (!StringUtils.hasText(disabilityType) || !StringUtils.hasText(supportType)) {
            return 0;
        }
        String st = supportType.toLowerCase(Locale.ROOT);
        for (String part : disabilityType.split("[,，、\\s]+")) {
            String t = part.trim().toLowerCase(Locale.ROOT);
            if (StringUtils.hasText(t) && st.contains(t)) {
                return DISABILITY_SCORE;
            }
        }
        return 0;
    }

    private int calcWorkModeScore(Integer acceptRemote, String workMode) {
        if (!StringUtils.hasText(workMode)) {
            return 0;
        }
        String mode = workMode.trim().toUpperCase(Locale.ROOT);
        boolean remotePreferred = acceptRemote != null && acceptRemote == 1;
        if (remotePreferred) {
            return Set.of("REMOTE", "HYBRID").contains(mode) ? WORK_MODE_SCORE : 0;
        }
        return Set.of("OFFLINE", "HYBRID").contains(mode) ? WORK_MODE_SCORE : 0;
    }

    private int calcCityScore(String expectedCity, String jobCity) {
        if (!StringUtils.hasText(expectedCity) || !StringUtils.hasText(jobCity)) {
            return 0;
        }
        return expectedCity.trim().equalsIgnoreCase(jobCity.trim()) ? CITY_SCORE : 0;
    }

    private List<String> splitSkills(String rawSkills) {
        if (!StringUtils.hasText(rawSkills)) {
            return List.of();
        }
        return java.util.Arrays.stream(rawSkills.split("[,，、\\s]+"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
    }
}
