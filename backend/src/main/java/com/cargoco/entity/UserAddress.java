package com.cargoco.entity;

import lombok.Data;
import java.util.Date;

/**
 * 用户收货地址实体类
 */
@Data
public class UserAddress {
    private Long id;
    private Long userId;
    private String receiverName;
    private String receiverPhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private Integer isDefault;
    private Date createTime;
    private Date updateTime;
    private Integer deleted;
}
