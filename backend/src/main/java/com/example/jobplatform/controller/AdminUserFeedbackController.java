package com.example.jobplatform.controller;

import com.example.jobplatform.common.Result;
import com.example.jobplatform.dto.AdminFeedbackResolveRequest;
import com.example.jobplatform.service.UserFeedbackService;
import com.example.jobplatform.vo.AdminPageVO;
import com.example.jobplatform.vo.AdminUserFeedbackRowVO;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin/user-feedback")
public class AdminUserFeedbackController {

    private final UserFeedbackService userFeedbackService;

    public AdminUserFeedbackController(UserFeedbackService userFeedbackService) {
        this.userFeedbackService = userFeedbackService;
    }

    @GetMapping
    public Result<AdminPageVO<AdminUserFeedbackRowVO>> page(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "keyword", required = false) String keyword) {
        return Result.success(userFeedbackService.pageForAdmin(page, size, status, keyword));
    }

    @PutMapping("/{id}/resolve")
    public Result<Void> resolve(@PathVariable("id") Long id,
                                 @Valid @RequestBody AdminFeedbackResolveRequest request) {
        userFeedbackService.resolve(id, request);
        return Result.success("已标记处理并通知用户", null);
    }
}
