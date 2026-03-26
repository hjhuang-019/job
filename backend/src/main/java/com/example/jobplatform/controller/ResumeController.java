package com.example.jobplatform.controller;

import com.example.jobplatform.common.Result;
import com.example.jobplatform.dto.ResumeSaveRequest;
import com.example.jobplatform.service.ResumeService;
import com.example.jobplatform.vo.ResumeVO;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping
    public Result<ResumeVO> create(@Valid @RequestBody ResumeSaveRequest request) {
        return Result.success("简历创建成功", resumeService.createResume(request));
    }

    @GetMapping
    public Result<List<ResumeVO>> list() {
        return Result.success(resumeService.listResumes());
    }

    @GetMapping("/{id}")
    public Result<ResumeVO> detail(@PathVariable("id") Long id) {
        return Result.success(resumeService.getResume(id));
    }

    @PutMapping("/{id}")
    public Result<ResumeVO> update(@PathVariable("id") Long id, @Valid @RequestBody ResumeSaveRequest request) {
        return Result.success("简历更新成功", resumeService.updateResume(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        resumeService.deleteResume(id);
        return Result.success("简历删除成功", null);
    }
}
