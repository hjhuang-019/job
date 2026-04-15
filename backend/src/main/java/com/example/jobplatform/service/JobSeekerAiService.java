package com.example.jobplatform.service;

import com.example.jobplatform.dto.JobSeekerAiInterpretRequest;
import com.example.jobplatform.vo.JobSeekerAiInterpretVO;

public interface JobSeekerAiService {

    JobSeekerAiInterpretVO interpret(JobSeekerAiInterpretRequest request);
}
