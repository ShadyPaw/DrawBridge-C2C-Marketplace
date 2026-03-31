package com.cargoco.entity;

import lombok.Data;
import java.util.Date;

/**
 * 用户实体类
 */
@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private String phone;
    private String email;
    private Integer gender;
    private String realName;
    private String idCard;
    private Integer creditScore;
    private Integer userLevel;
    private Integer status;
    private Integer role;
    private Date lastLoginTime;
    private String lastLoginIp;
    private Date createTime;
    private Date updateTime;
    private Integer deleted;
}
