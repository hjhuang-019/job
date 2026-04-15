package com.example.jobplatform.service;

import com.example.jobplatform.dto.AdminFeedbackResolveRequest;
import com.example.jobplatform.dto.UserFeedbackCreateRequest;
import com.example.jobplatform.vo.AdminPageVO;
import com.example.jobplatform.vo.AdminUserFeedbackRowVO;
import com.example.jobplatform.vo.UserFeedbackMineVO;

public interface UserFeedbackService {

    void create(UserFeedbackCreateRequest request);

    AdminPageVO<UserFeedbackMineVO> pageMine(int page, int size);

    AdminPageVO<AdminUserFeedbackRowVO> pageForAdmin(int page, int size, String status, String keyword);

    void resolve(Long id, AdminFeedbackResolveRequest request);
}
