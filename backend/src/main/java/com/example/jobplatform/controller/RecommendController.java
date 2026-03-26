package com.example.jobplatform.controller;

import com.example.jobplatform.common.Result;
import com.example.jobplatform.service.RecommendService;
import com.example.jobplatform.vo.RecommendJobVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/recommend")
public class RecommendController {

    private final RecommendService recommendService;

    public RecommendController(RecommendService recommendService) {
        this.recommendService = recommendService;
    }

    @GetMapping("/jobs")
    public Result<List<RecommendJobVO>> recommendJobs() {
        return Result.success(recommendService.recommendJobs());
    }
}
