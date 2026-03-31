package com.cargoco.entity;

import lombok.Data;
import java.util.Date;

/**
 * 商品图片实体类
 */
@Data
public class ProductImage {
    private Long id;
    private Long productId;
    private String imageUrl;
    private Integer isCover;
    private Integer sortOrder;
    private Date createTime;
}
