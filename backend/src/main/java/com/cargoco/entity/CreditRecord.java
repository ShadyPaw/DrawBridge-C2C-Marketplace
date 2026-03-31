package com.cargoco.entity;

import lombok.Data;
import java.util.Date;

/**
 * 信用记录实体类
 */
@Data
public class CreditRecord {
    private Long id;
    private Long userId;
    private Integer changeType;
    private Integer changeValue;
    private Integer beforeScore;
    private Integer afterScore;
    private Long relatedId;
    private String remark;
    private Date createTime;
}
