package com.example.jobplatform.service;

import com.example.jobplatform.dto.EnterpriseRegisterRequest;
import com.example.jobplatform.dto.JobSeekerRegisterRequest;
import com.example.jobplatform.dto.LoginRequest;
import com.example.jobplatform.dto.UpdatePasswordRequest;
import com.example.jobplatform.vo.CurrentUserVO;
import com.example.jobplatform.vo.LoginResponseVO;

public interface AuthService {

    CurrentUserVO registerJobSeeker(JobSeekerRegisterRequest request);

    CurrentUserVO registerEnterprise(EnterpriseRegisterRequest request);

    LoginResponseVO login(LoginRequest request);

    CurrentUserVO getCurrentUserInfo();

    void updatePassword(UpdatePasswordRequest request);
}
