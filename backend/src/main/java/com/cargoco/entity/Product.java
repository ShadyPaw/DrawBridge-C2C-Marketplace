package com.cargoco.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 商品信息实体类
 */
@Data
public class Product {
    private Long id;
    private Long userId;
    private Long categoryId;
    private String title;
    private String description;
    private BigDecimal originalPrice;
    private BigDecimal price;
    private Integer quality;
    private String location;
    private Integer tradeMode;
    private Integer viewCount;
    private Integer favoriteCount;
    private Integer productStatus;
    private Integer auditStatus;
    private String auditRemark;
    private Date auditTime;
    private Long auditUserId;
    private Date createTime;
    private Date updateTime;
    private Integer deleted;

    /** 非数据库字段 - 关联查询 */
    private String sellerNickname;
    private String sellerAvatar;
    private Integer sellerCreditScore;
    private Float sellerRiskScore;
    private String categoryName;
    private List<ProductImage> images;
    private String coverImage;
    private Boolean isFavorited;
}
