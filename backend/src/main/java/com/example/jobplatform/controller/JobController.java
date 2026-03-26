package com.example.jobplatform.controller;

import com.example.jobplatform.common.Result;
import com.example.jobplatform.dto.JobSaveRequest;
import com.example.jobplatform.dto.JobStatusUpdateRequest;
import com.example.jobplatform.service.JobService;
import com.example.jobplatform.vo.JobPageVO;
import com.example.jobplatform.vo.JobVO;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public Result<JobVO> create(@Valid @RequestBody JobSaveRequest request) {
        return Result.success("岗位发布成功", jobService.create(request));
    }

    @GetMapping
    public Result<JobPageVO> list(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                  @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                  @RequestParam(value = "keyword", required = false) String keyword,
                                  @RequestParam(value = "city", required = false) String city,
                                  @RequestParam(value = "workMode", required = false) String workMode,
                                  @RequestParam(value = "disabilitySupportType", required = false) String disabilitySupportType) {
        return Result.success(jobService.listPublishedJobs(pageNum, pageSize, keyword, city, workMode, disabilitySupportType));
    }

    @PutMapping("/{id}")
    public Result<JobVO> update(@PathVariable("id") Long id, @Valid @RequestBody JobSaveRequest request) {
        return Result.success("岗位更新成功", jobService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        jobService.delete(id);
        return Result.success("岗位删除成功", null);
    }

    @GetMapping("/my")
    public Result<List<JobVO>> listMyJobs() {
        return Result.success(jobService.listMyJobs());
    }

    @PutMapping("/{id}/status")
    public Result<JobVO> updateStatus(@PathVariable("id") Long id, @Valid @RequestBody JobStatusUpdateRequest request) {
        return Result.success("岗位状态更新成功", jobService.updateStatus(id, request.getStatus()));
    }

    @GetMapping("/{id}")
    public Result<JobVO> detail(@PathVariable("id") Long id) {
        return Result.success(jobService.detail(id));
    }
}
