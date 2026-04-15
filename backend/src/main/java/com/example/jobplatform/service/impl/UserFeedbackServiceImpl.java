package com.example.jobplatform.service.impl;

import com.example.jobplatform.common.BusinessException;
import com.example.jobplatform.dto.AdminFeedbackResolveRequest;
import com.example.jobplatform.dto.UserFeedbackCreateRequest;
import com.example.jobplatform.entity.SysUser;
import com.example.jobplatform.entity.UserFeedback;
import com.example.jobplatform.mapper.SysUserMapper;
import com.example.jobplatform.mapper.UserFeedbackMapper;
import com.example.jobplatform.security.UserContext;
import com.example.jobplatform.service.MessageService;
import com.example.jobplatform.service.UserFeedbackService;
import com.example.jobplatform.vo.AdminPageVO;
import com.example.jobplatform.vo.AdminUserFeedbackRowVO;
import com.example.jobplatform.vo.UserFeedbackMineVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class UserFeedbackServiceImpl implements UserFeedbackService {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_JOB_SEEKER = "JOB_SEEKER";
    private static final String ROLE_ENTERPRISE = "ENTERPRISE";
    private static final String CATEGORY_GENERAL = "GENERAL";
    private static final String CATEGORY_BLACKLIST_APPEAL = "BLACKLIST_APPEAL";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_RESOLVED = "RESOLVED";
    private static final int MSG_TITLE_MAX = 100;
    private static final int MSG_CONTENT_MAX = 1000;

    private final UserFeedbackMapper userFeedbackMapper;
    private final SysUserMapper sysUserMapper;
    private final MessageService messageService;

    public UserFeedbackServiceImpl(UserFeedbackMapper userFeedbackMapper,
                                 SysUserMapper sysUserMapper,
                                 MessageService messageService) {
        this.userFeedbackMapper = userFeedbackMapper;
        this.sysUserMapper = sysUserMapper;
        this.messageService = messageService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(UserFeedbackCreateRequest request) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (!ROLE_JOB_SEEKER.equals(user.getRole()) && !ROLE_ENTERPRISE.equals(user.getRole())) {
            throw new BusinessException(403, "仅求职者或企业可提交反馈");
        }
        if (userFeedbackMapper.countPendingBySenderId(userId) > 0) {
            throw new BusinessException(400, "您已有待处理的反馈，请等待管理员处理后再提交");
        }

        String category = resolveCategory(request, user);

        UserFeedback fb = new UserFeedback();
        fb.setSenderUserId(userId);
        fb.setCategory(category);
        fb.setTitle(request.getTitle().trim());
        fb.setContent(request.getContent().trim());
        fb.setStatus(STATUS_PENDING);
        if (userFeedbackMapper.insert(fb) <= 0 || fb.getId() == null) {
            throw new BusinessException("提交反馈失败");
        }
        notifyAdminsNewTicket(fb, user);
    }

    @Override
    public AdminPageVO<UserFeedbackMineVO> pageMine(int page, int size) {
        Long userId = requireSeekerOrEnterpriseUserId();
        int[] ps = normalizePageParams(page, size);
        long total = userFeedbackMapper.countMine(userId);
        List<UserFeedbackMineVO> records = total == 0
                ? List.of()
                : userFeedbackMapper.selectMinePage(userId, ps[0], ps[1]);
        AdminPageVO<UserFeedbackMineVO> vo = new AdminPageVO<>();
        vo.setRecords(records);
        vo.setTotal(total);
        return vo;
    }

    @Override
    public AdminPageVO<AdminUserFeedbackRowVO> pageForAdmin(int page, int size, String status, String keyword) {
        requireAdminUserId();
        int[] ps = normalizePageParams(page, size);
        String st = normalizeStatusFilter(status);
        String kw = normalizeKeyword(keyword);
        long total = userFeedbackMapper.countForAdmin(st, kw);
        List<AdminUserFeedbackRowVO> records = total == 0
                ? List.of()
                : userFeedbackMapper.selectPageForAdmin(st, kw, ps[0], ps[1]);
        AdminPageVO<AdminUserFeedbackRowVO> vo = new AdminPageVO<>();
        vo.setRecords(records);
        vo.setTotal(total);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resolve(Long id, AdminFeedbackResolveRequest request) {
        Long adminId = requireAdminUserId();
        UserFeedback fb = userFeedbackMapper.selectById(id);
        if (fb == null) {
            throw new BusinessException(404, "工单不存在");
        }
        if (!STATUS_PENDING.equals(fb.getStatus())) {
            throw new BusinessException(400, "该工单已处理");
        }
        String remark = request.getRemark().trim();
        int updated = userFeedbackMapper.updateResolve(id, adminId, remark, LocalDateTime.now());
        if (updated <= 0) {
            throw new BusinessException("更新工单状态失败");
        }
        SysUser sender = sysUserMapper.selectById(fb.getSenderUserId());
        String senderName = sender != null ? sender.getUsername() : "用户";
        String title = clip("反馈处理结果（工单#" + id + "）", MSG_TITLE_MAX);
        String body = "管理员已处理您的反馈（工单编号 " + id + "）。处理说明：" + remark;
        messageService.sendMessage(
                fb.getSenderUserId(),
                adminId,
                "USER_FEEDBACK",
                title,
                clip(body, MSG_CONTENT_MAX),
                "USER_FEEDBACK",
                id
        );
    }

    private void notifyAdminsNewTicket(UserFeedback fb, SysUser sender) {
        List<Long> adminIds = sysUserMapper.selectIdsByRole(ROLE_ADMIN);
        if (adminIds == null || adminIds.isEmpty()) {
            return;
        }
        String title = clip("新用户反馈工单#" + fb.getId(), MSG_TITLE_MAX);
        String typeLabel = CATEGORY_BLACKLIST_APPEAL.equals(fb.getCategory()) ? "黑名单申诉" : "一般咨询";
        String content = "用户「" + sender.getUsername() + "」（" + roleLabel(sender.getRole()) + "）提交了「" + typeLabel
                + "」反馈，标题：" + fb.getTitle() + "。请到「用户反馈」菜单处理。";
        String clipped = clip(content, MSG_CONTENT_MAX);
        for (Long adminId : adminIds) {
            if (adminId == null) {
                continue;
            }
            messageService.sendMessage(adminId, sender.getId(), "USER_FEEDBACK", title, clipped, "USER_FEEDBACK", fb.getId());
        }
    }

    private static String roleLabel(String role) {
        if (ROLE_JOB_SEEKER.equals(role)) {
            return "求职者";
        }
        if (ROLE_ENTERPRISE.equals(role)) {
            return "企业";
        }
        return role != null ? role : "";
    }

    private static String clip(String text, int maxLen) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLen) {
            return text;
        }
        return text.substring(0, Math.max(0, maxLen - 1)) + "…";
    }

    private Long requireSeekerOrEnterpriseUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (!ROLE_JOB_SEEKER.equals(user.getRole()) && !ROLE_ENTERPRISE.equals(user.getRole())) {
            throw new BusinessException(403, "仅求职者或企业可查看");
        }
        return userId;
    }

    private Long requireAdminUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (!ROLE_ADMIN.equals(user.getRole())) {
            throw new BusinessException(403, "仅管理员可操作");
        }
        return userId;
    }

    private int[] normalizePageParams(int page, int size) {
        int p = page < 1 ? 1 : page;
        int s = size < 1 ? 10 : Math.min(size, 100);
        return new int[]{(p - 1) * s, s};
    }

    private String normalizeKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        String t = keyword.trim();
        return t.isEmpty() ? null : t;
    }

    private String resolveCategory(UserFeedbackCreateRequest request, SysUser user) {
        if (!StringUtils.hasText(request.getCategory())) {
            return CATEGORY_GENERAL;
        }
        String value = request.getCategory().trim().toUpperCase(Locale.ROOT);
        if (CATEGORY_GENERAL.equals(value)) {
            return CATEGORY_GENERAL;
        }
        if (CATEGORY_BLACKLIST_APPEAL.equals(value)) {
            if (user.getBlacklisted() == null || user.getBlacklisted() != 1) {
                throw new BusinessException(403, "仅被列入黑名单的用户可选择「黑名单申诉」类型");
            }
            return CATEGORY_BLACKLIST_APPEAL;
        }
        throw new BusinessException(400, "反馈类型无效，仅支持 GENERAL 或 BLACKLIST_APPEAL");
    }

    private String normalizeStatusFilter(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        String u = status.trim().toUpperCase(Locale.ROOT);
        if (STATUS_PENDING.equals(u) || STATUS_RESOLVED.equals(u)) {
            return u;
        }
        throw new BusinessException(400, "状态筛选仅支持 PENDING 或 RESOLVED");
    }
}
