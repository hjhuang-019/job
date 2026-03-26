package com.example.jobplatform.controller;

import com.example.jobplatform.common.Result;
import com.example.jobplatform.dto.ApplicationCreateRequest;
import com.example.jobplatform.dto.ApplicationStatusUpdateRequest;
import com.example.jobplatform.service.ApplicationService;
import com.example.jobplatform.vo.ApplicationVO;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public Result<ApplicationVO> create(@Valid @RequestBody ApplicationCreateRequest request) {
        return Result.success("岗位投递成功", applicationService.create(request));
    }

    @GetMapping("/my")
    public Result<List<ApplicationVO>> myApplications() {
        return Result.success(applicationService.listMyApplications());
    }

    @GetMapping("/job/{jobId}")
    public Result<List<ApplicationVO>> jobApplications(@PathVariable("jobId") Long jobId) {
        return Result.success(applicationService.listByJobId(jobId));
    }

    @PutMapping("/{id}/status")
    public Result<ApplicationVO> updateStatus(@PathVariable("id") Long id,
                                              @Valid @RequestBody ApplicationStatusUpdateRequest request) {
        return Result.success("投递状态更新成功", applicationService.updateStatus(id, request));
    }
}
