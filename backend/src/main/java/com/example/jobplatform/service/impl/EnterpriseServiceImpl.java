package com.example.jobplatform.service.impl;

import com.example.jobplatform.common.BusinessException;
import com.example.jobplatform.dto.EnterpriseProfileUpdateRequest;
import com.example.jobplatform.entity.EnterpriseProfile;
import com.example.jobplatform.entity.SysUser;
import com.example.jobplatform.mapper.EnterpriseProfileMapper;
import com.example.jobplatform.mapper.SysUserMapper;
import com.example.jobplatform.security.UserContext;
import com.example.jobplatform.service.EnterpriseService;
import com.example.jobplatform.service.MessageService;
import com.example.jobplatform.vo.EnterpriseProfileVO;
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
public class EnterpriseServiceImpl implements EnterpriseService {

    private static final String ROLE_ENTERPRISE = "ENTERPRISE";
    private static final Set<String> ALLOW_EXT = Set.of("jpg", "jpeg", "png", "pdf");

    private final SysUserMapper sysUserMapper;
    private final EnterpriseProfileMapper enterpriseProfileMapper;
    private final MessageService messageService;
    private final String uploadDir;

    public EnterpriseServiceImpl(SysUserMapper sysUserMapper,
                                 EnterpriseProfileMapper enterpriseProfileMapper,
                                 MessageService messageService,
                                 @Value("${app.upload-dir:uploads}") String uploadDir) {
        this.sysUserMapper = sysUserMapper;
        this.enterpriseProfileMapper = enterpriseProfileMapper;
        this.messageService = messageService;
        this.uploadDir = uploadDir;
    }

    @Override
    public EnterpriseProfileVO getProfile() {
        SysUser user = getCurrentEnterpriseUser();
        EnterpriseProfile profile = ensureProfileExists(user);
        return buildVO(user, profile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EnterpriseProfileVO updateProfile(EnterpriseProfileUpdateRequest request) {
        SysUser user = getCurrentEnterpriseUser();
        EnterpriseProfile profile = ensureProfileExists(user);

        SysUser updateUser = new SysUser();
        updateUser.setId(user.getId());
        updateUser.setRealName(normalize(request.getContactPerson()));
        updateUser.setPhone(normalize(request.getContactPhone()));
        updateUser.setEmail(normalize(request.getEmail()));
        sysUserMapper.updateById(updateUser);

        EnterpriseProfile updateProfile = new EnterpriseProfile();
        updateProfile.setId(profile.getId());
        updateProfile.setEnterpriseName(normalize(request.getEnterpriseName()));
        updateProfile.setIndustry(normalize(request.getIndustry()));
        updateProfile.setScaleType(normalize(request.getScaleType()));
        updateProfile.setContactPerson(normalize(request.getContactPerson()));
        updateProfile.setContactPhone(normalize(request.getContactPhone()));
        updateProfile.setAddress(normalize(request.getAddress()));
        updateProfile.setDescription(normalize(request.getDescription()));
        if (enterpriseProfileMapper.updateById(updateProfile) <= 0) {
            throw new BusinessException("更新企业资料失败");
        }
        return getProfile();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> uploadLicense(MultipartFile file) {
        SysUser user = getCurrentEnterpriseUser();
        EnterpriseProfile profile = ensureProfileExists(user);

        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请上传营业执照");
        }
        String ext = getFileExtension(file.getOriginalFilename());
        if (!ALLOW_EXT.contains(ext)) {
            throw new BusinessException(400, "仅支持 jpg/jpeg/png/pdf 格式");
        }

        try {
            Path licenseDir = Paths.get(uploadDir, "licenses").toAbsolutePath().normalize();
            Files.createDirectories(licenseDir);
            String fileName = "license-" + user.getId() + "-" + UUID.randomUUID().toString().replace("-", "") + "." + ext;
            Path targetFile = licenseDir.resolve(fileName);
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

            String relativePath = "/uploads/licenses/" + fileName;
            EnterpriseProfile updateProfile = new EnterpriseProfile();
            updateProfile.setId(profile.getId());
            updateProfile.setLicensePath(relativePath);
            if (enterpriseProfileMapper.updateById(updateProfile) <= 0) {
                throw new BusinessException("保存营业执照路径失败");
            }
            if (enterpriseProfileMapper.updateVerifyStatusAsPending(profile.getId(), "PENDING") <= 0) {
                throw new BusinessException("更新认证状态失败");
            }

            Map<String, String> result = new LinkedHashMap<>();
            result.put("licensePath", relativePath);
            return result;
        } catch (IOException exception) {
            throw new BusinessException(500, "营业执照上传失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EnterpriseProfileVO submitVerify() {
        SysUser user = getCurrentEnterpriseUser();
        EnterpriseProfile profile = ensureProfileExists(user);
        if (!StringUtils.hasText(profile.getLicensePath())) {
            throw new BusinessException(400, "请先上传营业执照");
        }
        if (enterpriseProfileMapper.updateVerifyStatusAsPending(profile.getId(), "PENDING") <= 0) {
            throw new BusinessException("提交认证失败");
        }
        messageService.sendMessage(
                user.getId(),
                null,
                "AUDIT_NOTICE",
                "认证申请已提交",
                "你的企业认证申请已提交，当前状态为待审核。",
                "ENTERPRISE_PROFILE",
                profile.getId()
        );
        return getProfile();
    }

    private SysUser getCurrentEnterpriseUser() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (!ROLE_ENTERPRISE.equals(user.getRole())) {
            throw new BusinessException(403, "仅企业用户可操作");
        }
        return user;
    }

    private EnterpriseProfile ensureProfileExists(SysUser user) {
        EnterpriseProfile profile = enterpriseProfileMapper.selectByUserId(user.getId());
        if (profile != null) {
            return profile;
        }
        EnterpriseProfile createProfile = new EnterpriseProfile();
        createProfile.setUserId(user.getId());
        createProfile.setEnterpriseName(user.getRealName() == null ? user.getUsername() : user.getRealName());
        createProfile.setContactPerson(user.getRealName());
        createProfile.setContactPhone(user.getPhone());
        createProfile.setVerifyStatus("PENDING");
        if (enterpriseProfileMapper.insert(createProfile) <= 0 || createProfile.getId() == null) {
            throw new BusinessException("初始化企业资料失败");
        }
        return createProfile;
    }

    private EnterpriseProfileVO buildVO(SysUser user, EnterpriseProfile profile) {
        EnterpriseProfileVO vo = new EnterpriseProfileVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEnterpriseName(profile.getEnterpriseName());
        vo.setIndustry(profile.getIndustry());
        vo.setScaleType(profile.getScaleType());
        vo.setContactPerson(profile.getContactPerson());
        vo.setContactPhone(profile.getContactPhone());
        vo.setEmail(user.getEmail());
        vo.setAddress(profile.getAddress());
        vo.setDescription(profile.getDescription());
        vo.setLicensePath(profile.getLicensePath());
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
