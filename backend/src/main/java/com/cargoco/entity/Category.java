package com.cargoco.entity;

import lombok.Data;
import java.util.Date;
import java.util.List;

/**
 * 商品分类实体类
 */
@Data
public class Category {
    private Long id;
    private Long parentId;
    private String name;
    private String icon;
    private Integer sortOrder;
    private Integer status;
    private Date createTime;
    private Date updateTime;
    private Integer deleted;

    /** 子分类列表 (非数据库字段) */
    private List<Category> children;
}
