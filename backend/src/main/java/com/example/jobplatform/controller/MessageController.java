package com.example.jobplatform.controller;

import com.example.jobplatform.common.Result;
import com.example.jobplatform.service.MessageService;
import com.example.jobplatform.vo.MessageVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public Result<List<MessageVO>> listMessages() {
        return Result.success(messageService.listMyMessages());
    }

    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable("id") Long id) {
        messageService.markRead(id);
        return Result.success("消息已标记为已读", null);
    }

    @GetMapping("/unread-count")
    public Result<Map<String, Long>> unreadCount() {
        Map<String, Long> data = new LinkedHashMap<>();
        data.put("count", messageService.getUnreadCount());
        return Result.success(data);
    }
}
