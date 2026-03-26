package com.example.jobplatform.controller;

import com.example.jobplatform.common.Result;
import com.example.jobplatform.dto.AdminVerifyRequest;
import com.example.jobplatform.service.AdminService;
import com.example.jobplatform.vo.AdminEnterprisePendingVO;
import com.example.jobplatform.vo.AdminJobSeekerPendingVO;
import com.example.jobplatform.vo.AdminStatisticsVO;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/jobseekers/pending")
    public Result<List<AdminJobSeekerPendingVO>> listPendingJobSeekers() {
        return Result.success(adminService.listPendingJobSeekers());
    }

    @PutMapping("/jobseekers/{id}/verify")
    public Result<Void> verifyJobSeeker(@PathVariable("id") Long id,
                                        @Valid @RequestBody AdminVerifyRequest request) {
        adminService.verifyJobSeeker(id, request);
        return Result.success("求职者认证审核成功", null);
    }

    @GetMapping("/enterprises/pending")
    public Result<List<AdminEnterprisePendingVO>> listPendingEnterprises() {
        return Result.success(adminService.listPendingEnterprises());
    }

    @PutMapping("/enterprises/{id}/verify")
    public Result<Void> verifyEnterprise(@PathVariable("id") Long id,
                                         @Valid @RequestBody AdminVerifyRequest request) {
        adminService.verifyEnterprise(id, request);
        return Result.success("企业认证审核成功", null);
    }

    @GetMapping("/statistics")
    public Result<AdminStatisticsVO> statistics() {
        return Result.success(adminService.getStatistics());
    }
}
