package com.cargoco.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单信息实体类
 */
@Data
public class OrderInfo {
    private Long id;
    private String orderNo;
    private Long productId;
    private Long sellerId;
    private Long buyerId;
    private Long addressId;
    private BigDecimal price;
    private Integer tradeMode;
    private Integer orderStatus;
    private Date payTime;
    private Date shipTime;
    private Date receiveTime;
    private Date cancelTime;
    private String cancelReason;
    private String remark;
    private Date createTime;
    private Date updateTime;
    private Integer deleted;

    /** 非数据库字段 - 关联查询 */
    private String productTitle;
    private String productCover;
    private String sellerNickname;
    private String sellerAvatar;
    private String buyerNickname;
    private String buyerAvatar;
    private String receiverName;
    private String receiverPhone;
    private String fullAddress;
}
