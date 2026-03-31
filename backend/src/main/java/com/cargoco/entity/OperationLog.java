package com.cargoco.entity;

import lombok.Data;
import java.util.Date;

/**
 * 操作日志实体类
 */
@Data
public class OperationLog {
    private Long id;
    private Long userId;
    private String username;
    private String operation;
    private String method;
    private String requestUrl;
    private String requestParams;
    private String ip;
    private Integer status;
    private String errorMsg;
    private Long costTime;
    private Date createTime;
}
