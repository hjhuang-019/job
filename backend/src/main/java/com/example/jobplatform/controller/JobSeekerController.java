package com.example.jobplatform.controller;

import com.example.jobplatform.common.Result;
import com.example.jobplatform.dto.JobSeekerProfileUpdateRequest;
import com.example.jobplatform.service.JobSeekerService;
import com.example.jobplatform.vo.JobSeekerProfileVO;
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
@RequestMapping("/api/jobseeker")
public class JobSeekerController {

    private final JobSeekerService jobSeekerService;

    public JobSeekerController(JobSeekerService jobSeekerService) {
        this.jobSeekerService = jobSeekerService;
    }

    @GetMapping("/profile")
    public Result<JobSeekerProfileVO> getProfile() {
        return Result.success(jobSeekerService.getProfile());
    }

    @PutMapping("/profile")
    public Result<JobSeekerProfileVO> updateProfile(@Valid @RequestBody JobSeekerProfileUpdateRequest request) {
        return Result.success("求职者资料更新成功", jobSeekerService.updateProfile(request));
    }

    @PostMapping(value = "/certificate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String, String>> uploadCertificate(@RequestPart("file") MultipartFile file) {
        return Result.success("证件上传成功", jobSeekerService.uploadCertificate(file));
    }

    @PostMapping("/verify")
    public Result<JobSeekerProfileVO> submitVerify() {
        return Result.success("认证申请已提交", jobSeekerService.submitVerify());
    }
}
