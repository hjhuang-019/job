package com.example.jobplatform.controller;

import com.example.jobplatform.common.Result;
import com.example.jobplatform.dto.UserFeedbackCreateRequest;
import com.example.jobplatform.service.UserFeedbackService;
import com.example.jobplatform.vo.AdminPageVO;
import com.example.jobplatform.vo.UserFeedbackMineVO;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/user-feedback")
public class UserFeedbackController {

    private final UserFeedbackService userFeedbackService;

    public UserFeedbackController(UserFeedbackService userFeedbackService) {
        this.userFeedbackService = userFeedbackService;
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody UserFeedbackCreateRequest request) {
        userFeedbackService.create(request);
        return Result.success("提交成功，管理员将尽快处理", null);
    }

    @GetMapping("/my")
    public Result<AdminPageVO<UserFeedbackMineVO>> pageMine(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return Result.success(userFeedbackService.pageMine(page, size));
    }
}
