package com.example.jobplatform.controller;

import com.example.jobplatform.common.Result;
import com.example.jobplatform.dto.AdminBlacklistRequest;
import com.example.jobplatform.dto.AdminVerifyRequest;
import com.example.jobplatform.service.AdminService;
import com.example.jobplatform.vo.AdminAuditLogVO;
import com.example.jobplatform.vo.AdminEnterprisePendingVO;
import com.example.jobplatform.vo.AdminEnterpriseUserRowVO;
import com.example.jobplatform.vo.AdminJobSeekerPendingVO;
import com.example.jobplatform.vo.AdminJobSeekerUserRowVO;
import com.example.jobplatform.vo.AdminPageVO;
import com.example.jobplatform.vo.AdminStatisticsVO;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/users/job-seekers")
    public Result<AdminPageVO<AdminJobSeekerUserRowVO>> pageJobSeekerUsers(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword) {
        return Result.success(adminService.pageJobSeekerUsers(page, size, keyword));
    }

    @GetMapping("/users/enterprises")
    public Result<AdminPageVO<AdminEnterpriseUserRowVO>> pageEnterpriseUsers(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword) {
        return Result.success(adminService.pageEnterpriseUsers(page, size, keyword));
    }

    @GetMapping("/audit-logs")
    public Result<List<AdminAuditLogVO>> listAuditLogs(
            @RequestParam("businessType") String businessType,
            @RequestParam("businessId") Long businessId) {
        return Result.success(adminService.listAuditLogs(businessType, businessId));
    }

    @PutMapping("/users/{id}/blacklist")
    public Result<Void> blacklistUser(@PathVariable("id") Long id,
                                      @Valid @RequestBody AdminBlacklistRequest request) {
        adminService.blacklistUser(id, request);
        return Result.success("已加入黑名单", null);
    }

    @PutMapping("/users/{id}/unblacklist")
    public Result<Void> unblacklistUser(@PathVariable("id") Long id) {
        adminService.unblacklistUser(id);
        return Result.success("已解除黑名单", null);
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
