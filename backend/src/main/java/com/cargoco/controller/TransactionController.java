package com.cargoco.controller;

import com.cargoco.service.TransactionVerificationService;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/transaction")
public class TransactionController {

    private static final int PASS_THRESHOLD = 80;
    private static final int REVIEW_THRESHOLD = 60;

    private final TransactionVerificationService transactionVerificationService;

    public TransactionController(TransactionVerificationService transactionVerificationService) {
        this.transactionVerificationService = transactionVerificationService;
    }

    @Data
    public static class TransactionRequest {
        @NotNull(message = "商品ID不能为空")
        private Long productId;

        @NotNull(message = "交易金额不能为空")
        @Positive(message = "交易金额必须大于0")
        private Float transactionAmount;
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyTransaction(@Valid @RequestBody TransactionRequest request,
                                                                 HttpServletRequest httpServletRequest) {
        Long userId = (Long) httpServletRequest.getAttribute("userId");
        TransactionVerificationService.VerificationResult verificationResult =
                transactionVerificationService.verify(userId, request.getProductId(), request.getTransactionAmount());

        Map<String, Object> response = new HashMap<>();
        response.put("creditScore", verificationResult.getCreditScore());
        response.put("registerDays", verificationResult.getRegisterDays());
        response.put("productCount", verificationResult.getProductCount());
        response.put("reportCount", verificationResult.getReportCount());
        response.put("avgReplyTime", verificationResult.getAvgReplyTime());
        response.put("publicCreditScore", verificationResult.getPublicCreditScore());
        response.put("productId", request.getProductId());

        response.put("sellerCreditScore", verificationResult.getSellerCreditScore());

        // 规则 A：买家黑名单
        if (verificationResult.getCreditScore() < REVIEW_THRESHOLD) {
            response.put("message", "【买家风控】您的账号被判定为高危，已限制交易。");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        // 规则 C：买家灰度限额机制
        if (verificationResult.getCreditScore() < PASS_THRESHOLD && request.getTransactionAmount() > 500.0f) {
            response.put("message", "【风控限制】您的信用评级为中等风险，当前仅支持 500 元以下的小额交易。");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        // 规则 B：卖家高危反诈
        if (verificationResult.getSellerCreditScore() < REVIEW_THRESHOLD) {
            response.put("message", "【反诈提醒】该卖家信用极低，存在较高交易风险，系统建议您终止交易！");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response); // HTTP 409
        }

        // 规则 D：安全放行
        response.put("message", "信用良好，交易放行");
        return ResponseEntity.ok(response);
    }
}
