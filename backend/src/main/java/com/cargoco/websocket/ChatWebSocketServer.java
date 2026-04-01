package com.cargoco.websocket;

import com.cargoco.entity.ChatMessage;
import com.cargoco.mapper.ChatMessageMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 聊天服务端
 * 连接地址: ws://host:port/ws/chat/{userId}
 * 使用 userId 作为标识，实现点对点消息推送
 */
@ServerEndpoint("/ws/chat/{userId}")
@Component
public class ChatWebSocketServer {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketServer.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /** 在线用户会话池: userId -> Session */
    private static final Map<Long, Session> onlineSessions = new ConcurrentHashMap<>();

    /**
     * 由于 WebSocket 端点是非 Spring 管理的多实例对象，
     * 需要通过静态变量注入 Mapper
     */
    private static ChatMessageMapper chatMessageMapper;

    @Autowired
    public void setChatMessageMapper(ChatMessageMapper mapper) {
        ChatWebSocketServer.chatMessageMapper = mapper;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        onlineSessions.put(userId, session);
        log.info("WebSocket 连接建立: userId={}, 当前在线人数={}", userId, onlineSessions.size());
    }

    @OnClose
    public void onClose(@PathParam("userId") Long userId) {
        onlineSessions.remove(userId);
        log.info("WebSocket 连接关闭: userId={}, 当前在线人数={}", userId, onlineSessions.size());
    }

    @OnError
    public void onError(Session session, Throwable error, @PathParam("userId") Long userId) {
        log.error("WebSocket 异常: userId={}, error={}", userId, error.getMessage());
        onlineSessions.remove(userId);
    }

    /**
     * 收到客户端消息
     * 消息格式 JSON: { "receiverId": 123, "productId": 456, "content": "你好" }
     */
    @OnMessage
    public void onMessage(String message, @PathParam("userId") Long senderId) {
        try {
            JsonNode node = objectMapper.readTree(message);
            Long receiverId = node.get("receiverId").asLong();
            Long productId = node.has("productId") && !node.get("productId").isNull()
                    ? node.get("productId").asLong() : null;
            String content = node.get("content").asText();

            // 1. 消息落库
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSenderId(senderId);
            chatMessage.setReceiverId(receiverId);
            chatMessage.setProductId(productId);
            chatMessage.setContent(content);
            chatMessage.setIsRead(0);
            chatMessageMapper.insert(chatMessage);

            // 2. 构建推送消息（带上数据库生成的 id 和时间戳）
            java.util.Map<String, Object> pushData = new java.util.HashMap<>();
            pushData.put("type", "CHAT");
            pushData.put("id", chatMessage.getId());
            pushData.put("senderId", senderId);
            pushData.put("receiverId", receiverId);
            pushData.put("productId", productId != null ? productId : "");
            pushData.put("content", content);
            pushData.put("createTime", System.currentTimeMillis());
            String pushMessage = objectMapper.writeValueAsString(pushData);

            // 3. 推送给接收方（如果在线）
            Session receiverSession = onlineSessions.get(receiverId);
            if (receiverSession != null && receiverSession.isOpen()) {
                receiverSession.getBasicRemote().sendText(pushMessage);
            }

            // 4. 也回推给发送方（确认消息已送达，含服务端生成的 id）
            Session senderSession = onlineSessions.get(senderId);
            if (senderSession != null && senderSession.isOpen()) {
                senderSession.getBasicRemote().sendText(pushMessage);
            }

        } catch (Exception e) {
            log.error("处理 WebSocket 消息异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 主动向指定用户推送消息（供其他 Service 调用）
     */
    public static void sendToUser(Long userId, String message) {
        Session session = onlineSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                log.error("推送消息给用户 {} 失败: {}", userId, e.getMessage());
            }
        }
    }

    /**
     * 发送已读回执（系统通知）
     * 向发送方 (receiverId/senderId) 回推其消息已被 readerId 读取的事件
     */
    public static void sendReadReceipt(Long receiverId, Long readerId) {
        Session session = onlineSessions.get(receiverId);
        if (session != null && session.isOpen()) {
            try {
                java.util.Map<String, Object> rcpt = new java.util.HashMap<>();
                rcpt.put("type", "READ_RECEIPT");
                rcpt.put("readerId", readerId);
                session.getBasicRemote().sendText(objectMapper.writeValueAsString(rcpt));
            } catch (Exception e) {
                log.error("推送已读回执给用户 {} 失败: {}", receiverId, e.getMessage());
            }
        }
    }
}
