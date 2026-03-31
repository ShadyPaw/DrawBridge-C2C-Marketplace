package com.cargoco.entity;

import lombok.Data;
import java.util.Date;

/**
 * 系统通知实体类
 */
@Data
public class Notice {
    private Long id;
    private String title;
    private String content;
    private Integer type;
    private Long targetUserId;
    private Long senderId;
    private Integer isRead;
    private Integer status;
    private Date createTime;
    private Date updateTime;
    private Integer deleted;
}
