package com.cargoco.entity;

import lombok.Data;
import java.util.Date;
import java.util.List;

/**
 * 留言信息实体类
 */
@Data
public class Message {
    private Long id;
    private Long productId;
    private Long userId;
    private Long parentId;
    private Long replyUserId;
    private String content;
    private Integer status;
    private Date createTime;
    private Integer deleted;

    /** 非数据库字段 */
    private String userNickname;
    private String userAvatar;
    private String replyUserNickname;
    private List<Message> replies;
}
