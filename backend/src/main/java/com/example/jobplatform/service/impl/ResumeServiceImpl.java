package com.example.jobplatform.service.impl;

import com.example.jobplatform.common.BusinessException;
import com.example.jobplatform.dto.ResumeSaveRequest;
import com.example.jobplatform.entity.Resume;
import com.example.jobplatform.entity.SysUser;
import com.example.jobplatform.mapper.ResumeMapper;
import com.example.jobplatform.mapper.SysUserMapper;
import com.example.jobplatform.security.UserContext;
import com.example.jobplatform.service.ResumeService;
import com.example.jobplatform.vo.ResumeVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResumeServiceImpl implements ResumeService {

    private static final String ROLE_JOB_SEEKER = "JOB_SEEKER";

    private final ResumeMapper resumeMapper;
    private final SysUserMapper sysUserMapper;

    public ResumeServiceImpl(ResumeMapper resumeMapper, SysUserMapper sysUserMapper) {
        this.resumeMapper = resumeMapper;
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResumeVO createResume(ResumeSaveRequest request) {
        Long userId = getCurrentJobSeekerUserId();
        List<Resume> existResumes = resumeMapper.selectByUserId(userId);
        boolean shouldSetDefault = Boolean.TRUE.equals(request.getIsDefault()) || existResumes.isEmpty();

        Resume resume = buildResumeFromRequest(request);
        resume.setUserId(userId);
        resume.setResumeType("ONLINE");
        resume.setStatus(1);
        resume.setIsDefault(shouldSetDefault ? 1 : 0);

        if (shouldSetDefault) {
            resumeMapper.clearDefaultByUserId(userId);
        }
        if (resumeMapper.insert(resume) <= 0 || resume.getId() == null) {
            throw new BusinessException("新增简历失败");
        }
        return toVO(resumeMapper.selectByIdAndUserId(resume.getId(), userId));
    }

    @Override
    public List<ResumeVO> listResumes() {
        Long userId = getCurrentJobSeekerUserId();
        return resumeMapper.selectByUserId(userId).stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public ResumeVO getResume(Long id) {
        Long userId = getCurrentJobSeekerUserId();
        Resume resume = getOwnResume(userId, id);
        return toVO(resume);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResumeVO updateResume(Long id, ResumeSaveRequest request) {
        Long userId = getCurrentJobSeekerUserId();
        Resume exist = getOwnResume(userId, id);

        boolean shouldSetDefault = Boolean.TRUE.equals(request.getIsDefault());
        if (shouldSetDefault) {
            resumeMapper.clearDefaultByUserId(userId);
        }

        Resume update = buildResumeFromRequest(request);
        update.setId(exist.getId());
        update.setIsDefault(shouldSetDefault ? 1 : 0);
        // 动态 SQL 对残疾字段曾用 if 判断，单参数下偶发不写入；改为 UPDATE 始终写两列，请求未带键时保留原值
        if (request.getDisabilityType() == null) {
            update.setDisabilityType(exist.getDisabilityType());
        }
        if (request.getDisabilityLevel() == null) {
            update.setDisabilityLevel(exist.getDisabilityLevel());
        }
        if (resumeMapper.updateById(update) <= 0) {
            throw new BusinessException("更新简历失败");
        }
        return toVO(getOwnResume(userId, id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteResume(Long id) {
        Long userId = getCurrentJobSeekerUserId();
        Resume exist = getOwnResume(userId, id);
        if (resumeMapper.deleteById(exist.getId()) <= 0) {
            throw new BusinessException("删除简历失败");
        }

        if (exist.getIsDefault() != null && exist.getIsDefault() == 1) {
            Resume latestResume = resumeMapper.selectLatestByUserId(userId);
            if (latestResume != null) {
                Resume setDefaultResume = new Resume();
                setDefaultResume.setId(latestResume.getId());
                setDefaultResume.setIsDefault(1);
                // updateById 始终写入 disability 列，需带上当前行原值以免被置空
                setDefaultResume.setDisabilityType(latestResume.getDisabilityType());
                setDefaultResume.setDisabilityLevel(latestResume.getDisabilityLevel());
                resumeMapper.updateById(setDefaultResume);
            }
        }
    }

    private Long getCurrentJobSeekerUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (!ROLE_JOB_SEEKER.equals(user.getRole())) {
            throw new BusinessException(403, "仅求职者可操作简历");
        }
        return userId;
    }

    private Resume getOwnResume(Long userId, Long resumeId) {
        Resume resume = resumeMapper.selectByIdAndUserId(resumeId, userId);
        if (resume == null) {
            throw new BusinessException(404, "简历不存在");
        }
        return resume;
    }

    private Resume buildResumeFromRequest(ResumeSaveRequest request) {
        Resume resume = new Resume();
        resume.setResumeName(normalize(request.getTitle()));
        resume.setEducationExperience(normalize(request.getEducation()));
        resume.setWorkExperience(normalize(request.getExperience()));
        resume.setProjectExperience(normalize(request.getProjectExperience()));
        resume.setSkillSummary(normalize(request.getSkills()));
        resume.setContentText(normalize(request.getSelfEvaluation()));
        if (request.getDisabilityType() != null) {
            resume.setDisabilityType(trimDisabilityField(request.getDisabilityType()));
        }
        if (request.getDisabilityLevel() != null) {
            resume.setDisabilityLevel(trimDisabilityField(request.getDisabilityLevel()));
        }
        return resume;
    }

    private ResumeVO toVO(Resume resume) {
        if (resume == null) {
            return null;
        }
        ResumeVO vo = new ResumeVO();
        vo.setId(resume.getId());
        vo.setTitle(resume.getResumeName());
        vo.setEducation(resume.getEducationExperience());
        vo.setExperience(resume.getWorkExperience());
        vo.setProjectExperience(resume.getProjectExperience());
        vo.setSkills(resume.getSkillSummary());
        vo.setSelfEvaluation(resume.getContentText());
        vo.setDisabilityType(resume.getDisabilityType());
        vo.setDisabilityLevel(resume.getDisabilityLevel());
        vo.setIsDefault(resume.getIsDefault() != null && resume.getIsDefault() == 1);
        vo.setCreatedAt(resume.getCreatedAt());
        vo.setUpdatedAt(resume.getUpdatedAt());
        return vo;
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    /** 允许写入空串以清空库中残疾字段；null 表示请求体未携带该键时不更新 */
    private String trimDisabilityField(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }
}
