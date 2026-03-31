package com.cargoco.entity;

import lombok.Data;
import java.util.Date;

/**
 * 收藏实体类
 */
@Data
public class Favorite {
    private Long id;
    private Long userId;
    private Long productId;
    private Date createTime;

    /** 非数据库字段 */
    private String productTitle;
    private String productCover;
    private java.math.BigDecimal productPrice;
    private Integer productStatus;
}
