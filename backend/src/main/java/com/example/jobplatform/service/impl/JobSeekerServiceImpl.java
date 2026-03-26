package com.example.jobplatform.service.impl;

import com.example.jobplatform.common.BusinessException;
import com.example.jobplatform.dto.JobSeekerProfileUpdateRequest;
import com.example.jobplatform.entity.JobSeekerProfile;
import com.example.jobplatform.entity.SysUser;
import com.example.jobplatform.mapper.JobSeekerProfileMapper;
import com.example.jobplatform.mapper.SysUserMapper;
import com.example.jobplatform.security.UserContext;
import com.example.jobplatform.service.JobSeekerService;
import com.example.jobplatform.service.MessageService;
import com.example.jobplatform.vo.JobSeekerProfileVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class JobSeekerServiceImpl implements JobSeekerService {

    private static final String ROLE_JOB_SEEKER = "JOB_SEEKER";
    private static final Set<String> ALLOW_IMAGE_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final SysUserMapper sysUserMapper;
    private final JobSeekerProfileMapper jobSeekerProfileMapper;
    private final MessageService messageService;
    private final String uploadDir;

    public JobSeekerServiceImpl(SysUserMapper sysUserMapper,
                                JobSeekerProfileMapper jobSeekerProfileMapper,
                                MessageService messageService,
                                @Value("${app.upload-dir:uploads}") String uploadDir) {
        this.sysUserMapper = sysUserMapper;
        this.jobSeekerProfileMapper = jobSeekerProfileMapper;
        this.messageService = messageService;
        this.uploadDir = uploadDir;
    }

    @Override
    public JobSeekerProfileVO getProfile() {
        SysUser currentUser = getCurrentJobSeeker();
        JobSeekerProfile profile = ensureProfileExists(currentUser.getId());
        return buildProfileVO(currentUser, profile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JobSeekerProfileVO updateProfile(JobSeekerProfileUpdateRequest request) {
        SysUser currentUser = getCurrentJobSeeker();
        JobSeekerProfile profile = ensureProfileExists(currentUser.getId());

        SysUser updateUser = new SysUser();
        updateUser.setId(currentUser.getId());
        updateUser.setRealName(normalize(request.getRealName()));
        updateUser.setPhone(normalize(request.getPhone()));
        updateUser.setEmail(normalize(request.getEmail()));
        sysUserMapper.updateById(updateUser);

        JobSeekerProfile updateProfile = new JobSeekerProfile();
        updateProfile.setId(profile.getId());
        updateProfile.setDisabilityType(normalize(request.getDisabilityType()));
        updateProfile.setDisabilityLevel(normalize(request.getDisabilityLevel()));
        updateProfile.setSkills(normalize(request.getSkills()));
        updateProfile.setExpectedCity(normalize(request.getExpectedCity()));
        updateProfile.setExpectedSalary(normalize(request.getExpectedSalary()));
        updateProfile.setExpectedJob(normalize(request.getExpectedJob()));
        updateProfile.setAcceptRemote(Boolean.TRUE.equals(request.getAcceptRemote()) ? 1 : 0);
        updateProfile.setIntroduction(normalize(request.getIntroduction()));
        if (jobSeekerProfileMapper.updateById(updateProfile) <= 0) {
            throw new BusinessException("更新求职者资料失败");
        }

        return getProfile();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> uploadCertificate(MultipartFile file) {
        SysUser currentUser = getCurrentJobSeeker();
        JobSeekerProfile profile = ensureProfileExists(currentUser.getId());

        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请上传残疾证图片");
        }
        String originalFilename = file.getOriginalFilename();
        String ext = getFileExtension(originalFilename);
        if (!ALLOW_IMAGE_EXT.contains(ext)) {
            throw new BusinessException(400, "仅支持 jpg/jpeg/png/gif/webp 格式图片");
        }

        try {
            Path certificateDir = Paths.get(uploadDir, "certificates").toAbsolutePath().normalize();
            Files.createDirectories(certificateDir);

            String fileName = "cert-" + currentUser.getId() + "-" + UUID.randomUUID().toString().replace("-", "") + "." + ext;
            Path targetFile = certificateDir.resolve(fileName);
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

            String relativePath = "/uploads/certificates/" + fileName;
            JobSeekerProfile updateProfile = new JobSeekerProfile();
            updateProfile.setId(profile.getId());
            updateProfile.setCertificatePath(relativePath);
            if (jobSeekerProfileMapper.updateById(updateProfile) <= 0) {
                throw new BusinessException("保存证件路径失败");
            }
            if (jobSeekerProfileMapper.updateVerifyStatusAsPending(profile.getId(), "PENDING") <= 0) {
                throw new BusinessException("更新认证状态失败");
            }

            Map<String, String> result = new LinkedHashMap<>();
            result.put("certificatePath", relativePath);
            return result;
        } catch (IOException exception) {
            throw new BusinessException(500, "证件上传失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JobSeekerProfileVO submitVerify() {
        SysUser currentUser = getCurrentJobSeeker();
        JobSeekerProfile profile = ensureProfileExists(currentUser.getId());
        if (!StringUtils.hasText(profile.getCertificatePath())) {
            throw new BusinessException(400, "请先上传残疾证图片");
        }

        JobSeekerProfile updateProfile = new JobSeekerProfile();
        updateProfile.setId(profile.getId());
        if (jobSeekerProfileMapper.updateVerifyStatusAsPending(updateProfile.getId(), "PENDING") <= 0) {
            throw new BusinessException("提交认证失败");
        }
        messageService.sendMessage(
                currentUser.getId(),
                null,
                "AUDIT_NOTICE",
                "认证申请已提交",
                "你的求职者认证申请已提交，当前状态为待审核。",
                "JOB_SEEKER_PROFILE",
                profile.getId()
        );
        return getProfile();
    }

    private SysUser getCurrentJobSeeker() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        SysUser sysUser = sysUserMapper.selectById(userId);
        if (sysUser == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (!ROLE_JOB_SEEKER.equals(sysUser.getRole())) {
            throw new BusinessException(403, "仅求职者可操作该接口");
        }
        return sysUser;
    }

    private JobSeekerProfile ensureProfileExists(Long userId) {
        JobSeekerProfile profile = jobSeekerProfileMapper.selectByUserId(userId);
        if (profile != null) {
            return profile;
        }
        JobSeekerProfile createProfile = new JobSeekerProfile();
        createProfile.setUserId(userId);
        createProfile.setVerifyStatus("PENDING");
        createProfile.setAcceptRemote(0);
        if (jobSeekerProfileMapper.insert(createProfile) <= 0 || createProfile.getId() == null) {
            throw new BusinessException("初始化求职者资料失败");
        }
        return createProfile;
    }

    private JobSeekerProfileVO buildProfileVO(SysUser user, JobSeekerProfile profile) {
        JobSeekerProfileVO vo = new JobSeekerProfileVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setDisabilityType(profile.getDisabilityType());
        vo.setDisabilityLevel(profile.getDisabilityLevel());
        vo.setSkills(profile.getSkills());
        vo.setExpectedCity(profile.getExpectedCity());
        vo.setExpectedSalary(profile.getExpectedSalary());
        vo.setExpectedJob(profile.getExpectedJob());
        vo.setAcceptRemote(profile.getAcceptRemote() != null && profile.getAcceptRemote() == 1);
        vo.setIntroduction(profile.getIntroduction());
        vo.setCertificatePath(profile.getCertificatePath());
        vo.setVerifyStatus(normalizeVerifyStatus(profile.getVerifyStatus()));
        return vo;
    }

    private String normalizeVerifyStatus(String verifyStatus) {
        if (!StringUtils.hasText(verifyStatus)) {
            return "PENDING";
        }
        String status = verifyStatus.trim().toUpperCase(Locale.ROOT);
        if ("APPROVED".equals(status) || "PASS".equals(status)) {
            return "PASS";
        }
        if ("REJECTED".equals(status) || "REJECT".equals(status)) {
            return "REJECT";
        }
        return "PENDING";
    }

    private String getFileExtension(String fileName) {
        if (!StringUtils.hasText(fileName) || !fileName.contains(".")) {
            throw new BusinessException(400, "文件名不合法");
        }
        int dotIndex = fileName.lastIndexOf('.');
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
