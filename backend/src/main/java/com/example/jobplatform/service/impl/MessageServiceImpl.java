package com.example.jobplatform.service.impl;

import com.example.jobplatform.common.BusinessException;
import com.example.jobplatform.entity.SysMessage;
import com.example.jobplatform.entity.SysUser;
import com.example.jobplatform.mapper.JobApplicationMapper;
import com.example.jobplatform.mapper.SysMessageMapper;
import com.example.jobplatform.mapper.SysUserMapper;
import com.example.jobplatform.security.UserContext;
import com.example.jobplatform.service.MessageService;
import com.example.jobplatform.vo.MessageVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService {

    private final SysMessageMapper sysMessageMapper;
    private final SysUserMapper sysUserMapper;
    private final JobApplicationMapper jobApplicationMapper;

    public MessageServiceImpl(SysMessageMapper sysMessageMapper,
                              SysUserMapper sysUserMapper,
                              JobApplicationMapper jobApplicationMapper) {
        this.sysMessageMapper = sysMessageMapper;
        this.sysUserMapper = sysUserMapper;
        this.jobApplicationMapper = jobApplicationMapper;
    }

    @Override
    public List<MessageVO> listMyMessages() {
        Long userId = getCurrentUserId();
        return sysMessageMapper.selectByReceiverUserId(userId).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long id) {
        Long userId = getCurrentUserId();
        if (sysMessageMapper.markRead(id, userId) <= 0) {
            throw new BusinessException(404, "消息不存在或无权限操作");
        }
    }

    @Override
    public Long getUnreadCount() {
        Long userId = getCurrentUserId();
        Long count = sysMessageMapper.countUnread(userId);
        return count == null ? 0L : count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendMessage(Long receiverUserId,
                            Long senderUserId,
                            String messageType,
                            String title,
                            String content,
                            String relatedBusinessType,
                            Long relatedBusinessId) {
        if (receiverUserId == null) {
            return;
        }
        if (sysUserMapper.selectById(receiverUserId) == null) {
            return;
        }

        SysMessage message = new SysMessage();
        message.setReceiverUserId(receiverUserId);
        message.setSenderUserId(senderUserId);
        message.setMessageType(StringUtils.hasText(messageType) ? messageType.trim() : "SYSTEM");
        message.setTitle(StringUtils.hasText(title) ? title.trim() : "系统通知");
        message.setContent(StringUtils.hasText(content) ? content.trim() : "您有一条新消息");
        message.setRelatedBusinessType(StringUtils.hasText(relatedBusinessType) ? relatedBusinessType.trim() : null);
        message.setRelatedBusinessId(relatedBusinessId);
        message.setIsRead(0);
        if (sysMessageMapper.insert(message) <= 0) {
            throw new BusinessException("消息发送失败");
        }
    }

    private Long getCurrentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return userId;
    }

    private MessageVO toVO(SysMessage message) {
        MessageVO vo = new MessageVO();
        vo.setId(message.getId());
        vo.setMessageType(message.getMessageType());
        vo.setTitle(message.getTitle());
        vo.setContent(message.getContent());
        vo.setRelatedBusinessType(message.getRelatedBusinessType());
        vo.setRelatedBusinessId(message.getRelatedBusinessId());
        if ("APPLY_RECEIVED".equals(message.getMessageType())
                && "JOB_APPLICATION".equals(message.getRelatedBusinessType())
                && message.getRelatedBusinessId() != null) {
            Long jobId = jobApplicationMapper.selectJobIdByApplicationId(message.getRelatedBusinessId());
            vo.setRelatedJobId(jobId);
        }
        vo.setRead(message.getIsRead() != null && message.getIsRead() == 1);
        vo.setReadTime(message.getReadTime());
        vo.setCreatedAt(message.getCreatedAt());
        return vo;
    }
}
