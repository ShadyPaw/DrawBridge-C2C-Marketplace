package com.cargoco.entity;

import lombok.Data;
import java.util.Date;

/**
 * 用户评价实体类
 */
@Data
public class Review {
    private Long id;
    private Long orderId;
    private Long productId;
    private Long fromUserId;
    private Long toUserId;
    private Integer type;
    private Integer rating;
    private String content;
    private Integer isAnonymous;
    private Integer status;
    private Date createTime;
    private Integer deleted;

    /**
     * 【权重预留】: 卖家是否按时发货 (由买家评价时勾选或系统判定)
     */
    private Integer onTimeDelivery;

    /** 非数据库字段 */
    private String fromUserNickname;
    private String fromUserAvatar;
    private String productTitle;
}
