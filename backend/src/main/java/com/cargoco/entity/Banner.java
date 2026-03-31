package com.cargoco.entity;

import lombok.Data;
import java.util.Date;

/**
 * 轮播图实体类
 */
@Data
public class Banner {
    private Long id;
    private String title;
    private String imageUrl;
    private String linkUrl;
    private Integer sortOrder;
    private Integer status;
    private Date createTime;
    private Date updateTime;
    private Integer deleted;
}
