package com.cargoco.controller;

import com.cargoco.common.Result;
import com.cargoco.entity.ChatMessage;
import com.cargoco.mapper.ChatMessageMapper;
import com.cargoco.websocket.ChatWebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 聊天消息 REST 接口
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    /**
     * 获取与某用户的聊天历史记录
     */
    @GetMapping("/history")
    public Result<List<ChatMessage>> history(
            @RequestParam Long targetUserId,
            @RequestParam(required = false) Long productId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<ChatMessage> messages = chatMessageMapper.findConversation(userId, targetUserId, productId);
        // 同时标记对方发给我的消息为已读，并通知对方的 WebSocket 客户端
        chatMessageMapper.markAsRead(targetUserId, userId);
        ChatWebSocketServer.sendReadReceipt(targetUserId, userId);
        return Result.success(messages);
    }

    /**
     * 获取最近联系人列表
     */
    @GetMapping("/contacts")
    public Result<List<ChatMessage>> contacts(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<ChatMessage> contacts = chatMessageMapper.findRecentContacts(userId);
        return Result.success(contacts);
    }

    /**
     * 获取未读消息数
     */
    @GetMapping("/unread")
    public Result<Integer> unreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        int count = chatMessageMapper.countUnread(userId);
        return Result.success(count);
    }

    /**
     * 标记某个会话已读
     */
    @PutMapping("/read")
    public Result<?> markRead(@RequestParam Long senderId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        chatMessageMapper.markAsRead(senderId, userId);
        ChatWebSocketServer.sendReadReceipt(senderId, userId);
        return Result.success("已标记为已读", null);
    }
}
