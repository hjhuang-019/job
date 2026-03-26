package com.example.jobplatform.service;

import com.example.jobplatform.vo.RecommendJobVO;

import java.util.List;

public interface RecommendService {

    List<RecommendJobVO> recommendJobs();
}
