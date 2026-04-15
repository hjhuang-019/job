package com.example.jobplatform.controller;

import com.example.jobplatform.common.BusinessException;
import com.example.jobplatform.common.Result;
import com.example.jobplatform.dto.JobSeekerAiInterpretRequest;
import com.example.jobplatform.security.LoginUser;
import com.example.jobplatform.security.UserContext;
import com.example.jobplatform.service.JobSeekerAiService;
import com.example.jobplatform.vo.JobSeekerAiInterpretVO;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/jobseeker/ai")
public class JobSeekerAiController {

    private final JobSeekerAiService jobSeekerAiService;

    public JobSeekerAiController(JobSeekerAiService jobSeekerAiService) {
        this.jobSeekerAiService = jobSeekerAiService;
    }

    @PostMapping("/interpret")
    public Result<JobSeekerAiInterpretVO> interpret(@Valid @RequestBody JobSeekerAiInterpretRequest request) {
        LoginUser user = UserContext.get();
        if (user == null || !"JOB_SEEKER".equals(user.getRole())) {
            throw new BusinessException(403, "仅求职者可使用语音智能助手");
        }
        return Result.success(jobSeekerAiService.interpret(request));
    }
}
