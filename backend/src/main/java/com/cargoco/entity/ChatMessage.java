package com.cargoco.entity;

import lombok.Data;
import java.util.Date;

/**
 * 私聊消息实体类
 */
@Data
public class ChatMessage {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private Long productId;
    private String content;
    private Integer isRead;
    private Date createTime;

    /** 非数据库字段 - 关联查询 */
    private String senderNickname;
    private String senderAvatar;
    private String receiverNickname;
    private String receiverAvatar;
    private String productTitle;
    private java.math.BigDecimal productPrice;
    private String productCoverImage;
    private Integer unreadCount; // 每个会话的未读条数
}
