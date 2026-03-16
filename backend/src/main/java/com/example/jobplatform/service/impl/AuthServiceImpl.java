package com.example.jobplatform.service.impl;

import com.example.jobplatform.common.BusinessException;
import com.example.jobplatform.dto.EnterpriseRegisterRequest;
import com.example.jobplatform.dto.JobSeekerRegisterRequest;
import com.example.jobplatform.dto.LoginRequest;
import com.example.jobplatform.dto.UpdatePasswordRequest;
import com.example.jobplatform.entity.EnterpriseProfile;
import com.example.jobplatform.entity.JobSeekerProfile;
import com.example.jobplatform.entity.SysUser;
import com.example.jobplatform.mapper.EnterpriseProfileMapper;
import com.example.jobplatform.mapper.JobSeekerProfileMapper;
import com.example.jobplatform.mapper.SysUserMapper;
import com.example.jobplatform.security.JwtUtils;
import com.example.jobplatform.security.UserContext;
import com.example.jobplatform.service.AuthService;
import com.example.jobplatform.utils.PasswordUtils;
import com.example.jobplatform.vo.CurrentUserVO;
import com.example.jobplatform.vo.LoginResponseVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    private static final String ROLE_JOB_SEEKER = "JOB_SEEKER";
    private static final String ROLE_ENTERPRISE = "ENTERPRISE";

    private final SysUserMapper sysUserMapper;
    private final JobSeekerProfileMapper jobSeekerProfileMapper;
    private final EnterpriseProfileMapper enterpriseProfileMapper;
    private final PasswordUtils passwordUtils;
    private final JwtUtils jwtUtils;

    public AuthServiceImpl(SysUserMapper sysUserMapper,
                           JobSeekerProfileMapper jobSeekerProfileMapper,
                           EnterpriseProfileMapper enterpriseProfileMapper,
                           PasswordUtils passwordUtils,
                           JwtUtils jwtUtils) {
        this.sysUserMapper = sysUserMapper;
        this.jobSeekerProfileMapper = jobSeekerProfileMapper;
        this.enterpriseProfileMapper = enterpriseProfileMapper;
        this.passwordUtils = passwordUtils;
        this.jwtUtils = jwtUtils;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CurrentUserVO registerJobSeeker(JobSeekerRegisterRequest request) {
        validatePasswordConfirmation(request.getPassword(), request.getConfirmPassword());
        validateUniqueUser(request.getUsername(), request.getPhone());

        SysUser sysUser = buildSysUser(
                request.getUsername(),
                request.getPassword(),
                ROLE_JOB_SEEKER,
                request.getRealName(),
                request.getPhone(),
                request.getEmail()
        );
        saveUser(sysUser);

        JobSeekerProfile profile = new JobSeekerProfile();
        profile.setUserId(sysUser.getId());
        profile.setVerifyStatus("PENDING");
        if (jobSeekerProfileMapper.insert(profile) <= 0) {
            throw new BusinessException("求职者资料初始化失败");
        }

        return buildCurrentUserVO(sysUser, profile, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CurrentUserVO registerEnterprise(EnterpriseRegisterRequest request) {
        validatePasswordConfirmation(request.getPassword(), request.getConfirmPassword());
        validateUniqueUser(request.getUsername(), request.getPhone());

        SysUser sysUser = buildSysUser(
                request.getUsername(),
                request.getPassword(),
                ROLE_ENTERPRISE,
                request.getRealName(),
                request.getPhone(),
                request.getEmail()
        );
        saveUser(sysUser);

        EnterpriseProfile profile = new EnterpriseProfile();
        profile.setUserId(sysUser.getId());
        profile.setEnterpriseName(request.getEnterpriseName().trim());
        profile.setContactPerson(normalize(request.getRealName()));
        profile.setContactPhone(normalize(request.getPhone()));
        profile.setVerifyStatus("PENDING");
        if (enterpriseProfileMapper.insert(profile) <= 0) {
            throw new BusinessException("企业资料初始化失败");
        }

        return buildCurrentUserVO(sysUser, null, profile);
    }

    @Override
    public LoginResponseVO login(LoginRequest request) {
        SysUser sysUser = sysUserMapper.selectByUsername(request.getUsername().trim());
        if (sysUser == null || !passwordUtils.matches(request.getPassword(), sysUser.getPassword())) {
            throw new BusinessException(400, "用户名或密码错误");
        }
        if (sysUser.getStatus() == null || sysUser.getStatus() != 1) {
            throw new BusinessException(403, "当前账号已被禁用");
        }

        SysUser updateUser = new SysUser();
        updateUser.setId(sysUser.getId());
        updateUser.setLastLoginTime(LocalDateTime.now());
        sysUserMapper.updateById(updateUser);

        SysUser latestUser = sysUserMapper.selectById(sysUser.getId());
        LoginResponseVO responseVO = new LoginResponseVO();
        responseVO.setToken(jwtUtils.generateToken(latestUser));
        responseVO.setTokenType("Bearer");
        responseVO.setExpiresIn(jwtUtils.getExpiration());
        responseVO.setUserInfo(loadCurrentUser(latestUser));
        return responseVO;
    }

    @Override
    public CurrentUserVO getCurrentUserInfo() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        SysUser sysUser = sysUserMapper.selectById(userId);
        if (sysUser == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return loadCurrentUser(sysUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(UpdatePasswordRequest request) {
        validatePasswordConfirmation(request.getNewPassword(), request.getConfirmPassword());
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }

        SysUser sysUser = sysUserMapper.selectById(userId);
        if (sysUser == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (!passwordUtils.matches(request.getOldPassword(), sysUser.getPassword())) {
            throw new BusinessException(400, "原密码错误");
        }
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new BusinessException(400, "新密码不能与原密码相同");
        }

        SysUser updateUser = new SysUser();
        updateUser.setId(userId);
        updateUser.setPassword(passwordUtils.encode(request.getNewPassword()));
        if (sysUserMapper.updateById(updateUser) <= 0) {
            throw new BusinessException("密码修改失败");
        }
    }

    private void saveUser(SysUser sysUser) {
        if (sysUserMapper.insert(sysUser) <= 0 || sysUser.getId() == null) {
            throw new BusinessException("用户注册失败");
        }
    }

    private SysUser buildSysUser(String username, String password, String role, String realName, String phone, String email) {
        SysUser sysUser = new SysUser();
        sysUser.setUsername(username.trim());
        sysUser.setPassword(passwordUtils.encode(password));
        sysUser.setRole(role);
        sysUser.setStatus(1);
        sysUser.setRealName(normalize(realName));
        sysUser.setPhone(normalize(phone));
        sysUser.setEmail(normalize(email));
        return sysUser;
    }

    private void validatePasswordConfirmation(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new BusinessException(400, "两次输入的密码不一致");
        }
    }

    private void validateUniqueUser(String username, String phone) {
        if (sysUserMapper.selectByUsername(username.trim()) != null) {
            throw new BusinessException(400, "用户名已存在");
        }
        String normalizedPhone = normalize(phone);
        if (normalizedPhone != null && sysUserMapper.selectByPhone(normalizedPhone) != null) {
            throw new BusinessException(400, "手机号已存在");
        }
    }

    private CurrentUserVO loadCurrentUser(SysUser sysUser) {
        if (ROLE_JOB_SEEKER.equals(sysUser.getRole())) {
            return buildCurrentUserVO(sysUser, jobSeekerProfileMapper.selectByUserId(sysUser.getId()), null);
        }
        if (ROLE_ENTERPRISE.equals(sysUser.getRole())) {
            return buildCurrentUserVO(sysUser, null, enterpriseProfileMapper.selectByUserId(sysUser.getId()));
        }
        return buildCurrentUserVO(sysUser, null, null);
    }

    private CurrentUserVO buildCurrentUserVO(SysUser sysUser, JobSeekerProfile jobSeekerProfile, EnterpriseProfile enterpriseProfile) {
        CurrentUserVO currentUserVO = new CurrentUserVO();
        currentUserVO.setId(sysUser.getId());
        currentUserVO.setUsername(sysUser.getUsername());
        currentUserVO.setRole(sysUser.getRole());
        currentUserVO.setStatus(sysUser.getStatus());
        currentUserVO.setRealName(sysUser.getRealName());
        currentUserVO.setPhone(sysUser.getPhone());
        currentUserVO.setEmail(sysUser.getEmail());
        currentUserVO.setLastLoginTime(sysUser.getLastLoginTime());
        currentUserVO.setProfile(buildProfileMap(jobSeekerProfile, enterpriseProfile));
        return currentUserVO;
    }

    private Map<String, Object> buildProfileMap(JobSeekerProfile jobSeekerProfile, EnterpriseProfile enterpriseProfile) {
        Map<String, Object> profile = new LinkedHashMap<>();
        if (jobSeekerProfile != null) {
            profile.put("profileId", jobSeekerProfile.getId());
            profile.put("verifyStatus", jobSeekerProfile.getVerifyStatus());
            profile.put("disabilityType", jobSeekerProfile.getDisabilityType());
            profile.put("expectedCity", jobSeekerProfile.getExpectedCity());
            return profile;
        }
        if (enterpriseProfile != null) {
            profile.put("profileId", enterpriseProfile.getId());
            profile.put("enterpriseName", enterpriseProfile.getEnterpriseName());
            profile.put("verifyStatus", enterpriseProfile.getVerifyStatus());
            profile.put("contactPerson", enterpriseProfile.getContactPerson());
            return profile;
        }
        return profile;
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
