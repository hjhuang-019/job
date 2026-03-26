package com.example.jobplatform.service;

import com.example.jobplatform.vo.MessageVO;

import java.util.List;

public interface MessageService {

    List<MessageVO> listMyMessages();

    void markRead(Long id);

    Long getUnreadCount();

    void sendMessage(Long receiverUserId,
                     Long senderUserId,
                     String messageType,
                     String title,
                     String content,
                     String relatedBusinessType,
                     Long relatedBusinessId);
}
