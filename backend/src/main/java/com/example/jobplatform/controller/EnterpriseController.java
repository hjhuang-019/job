package com.example.jobplatform.controller;

import com.example.jobplatform.common.Result;
import com.example.jobplatform.dto.EnterpriseProfileUpdateRequest;
import com.example.jobplatform.service.EnterpriseService;
import com.example.jobplatform.vo.EnterpriseProfileVO;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/enterprise")
public class EnterpriseController {

    private final EnterpriseService enterpriseService;

    public EnterpriseController(EnterpriseService enterpriseService) {
        this.enterpriseService = enterpriseService;
    }

    @GetMapping("/profile")
    public Result<EnterpriseProfileVO> getProfile() {
        return Result.success(enterpriseService.getProfile());
    }

    @PutMapping("/profile")
    public Result<EnterpriseProfileVO> updateProfile(@Valid @RequestBody EnterpriseProfileUpdateRequest request) {
        return Result.success("企业资料更新成功", enterpriseService.updateProfile(request));
    }

    @PostMapping(value = "/license", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String, String>> uploadLicense(@RequestPart("file") MultipartFile file) {
        return Result.success("营业执照上传成功", enterpriseService.uploadLicense(file));
    }

    @PostMapping("/verify")
    public Result<EnterpriseProfileVO> submitVerify() {
        return Result.success("企业认证申请已提交", enterpriseService.submitVerify());
    }
}
