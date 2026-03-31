package com.cargoco.entity;

import lombok.Data;
import java.util.Date;

/**
 * 举报实体类
 */
@Data
public class Report {
    private Long id;
    private Long reporterId;
    private Integer targetType;
    private Long targetId;
    private Integer reasonType;
    private String reasonDetail;
    private String evidenceUrls;
    private Integer handleStatus;
    private String handleResult;
    private Long handleUserId;
    private Date handleTime;
    private Date createTime;
    private Integer deleted;

    /** 非数据库字段 */
    private String reporterNickname;
    private String handleUserNickname;
}
