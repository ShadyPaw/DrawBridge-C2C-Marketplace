package com.cargoco.vo;

import com.cargoco.entity.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户视图对象，包含 AI 风险评分
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserVO extends User {
    private Float riskScore; // AI 风险评分 (0.0 ~ 1.0)
}
