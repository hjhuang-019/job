package com.example.jobplatform.controller;

import com.example.jobplatform.common.Result;
import com.example.jobplatform.dto.EnterpriseRegisterRequest;
import com.example.jobplatform.dto.JobSeekerRegisterRequest;
import com.example.jobplatform.dto.LoginRequest;
import com.example.jobplatform.dto.UpdatePasswordRequest;
import com.example.jobplatform.service.AuthService;
import com.example.jobplatform.vo.CurrentUserVO;
import com.example.jobplatform.vo.LoginResponseVO;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/jobseeker")
    public Result<CurrentUserVO> registerJobSeeker(@Valid @RequestBody JobSeekerRegisterRequest request) {
        return Result.success("求职者注册成功", authService.registerJobSeeker(request));
    }

    @PostMapping("/register/enterprise")
    public Result<CurrentUserVO> registerEnterprise(@Valid @RequestBody EnterpriseRegisterRequest request) {
        return Result.success("企业注册成功", authService.registerEnterprise(request));
    }

    @PostMapping("/login")
    public Result<LoginResponseVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.success("登录成功", authService.login(request));
    }

    @GetMapping("/me")
    public Result<CurrentUserVO> getCurrentUserInfo() {
        return Result.success(authService.getCurrentUserInfo());
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        authService.updatePassword(request);
        return Result.success("密码修改成功", null);
    }
}
