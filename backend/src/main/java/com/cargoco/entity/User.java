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

    /**
     * 【正向特征预留】: 历史累计获得的好评总数
     * 用于后期 AI 模型迭代，作为加分特征维度
     */
    private Integer positiveReviewCount;

    /**
     * 【正向特征预留】: 历史上按时发货/履约的累计次数
     */
    private Integer onTimeDeliveryCount;

    private Date lastLoginTime;
    private String lastLoginIp;
    private Date createTime;
    private Date updateTime;
    private Integer deleted;

    /**
     * AI 风险评分 (不持久化到数据库)
     */
    private Float riskScore;
}
