package com.cargoco.service;

import com.cargoco.entity.Product;
import com.cargoco.entity.User;
import com.cargoco.mapper.OrderInfoMapper;
import com.cargoco.mapper.ProductMapper;
import com.cargoco.mapper.ReportMapper;
import com.cargoco.mapper.UserMapper;
import com.cargoco.mapper.ChatMessageMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
public class TransactionVerificationService {

    private static final Integer PRODUCT_STATUS_ON_SALE = 1;

    private final CreditScoreAiService creditScoreAiService;
    private final UserMapper userMapper;
    private final OrderInfoMapper orderInfoMapper;
    private final ReportMapper reportMapper;
    private final ProductMapper productMapper;
    private final ChatMessageMapper chatMessageMapper;

    public TransactionVerificationService(CreditScoreAiService creditScoreAiService,
                                          UserMapper userMapper,
                                          OrderInfoMapper orderInfoMapper,
                                          ReportMapper reportMapper,
                                          ProductMapper productMapper,
                                          ChatMessageMapper chatMessageMapper) {
        this.creditScoreAiService = creditScoreAiService;
        this.userMapper = userMapper;
        this.orderInfoMapper = orderInfoMapper;
        this.reportMapper = reportMapper;
        this.productMapper = productMapper;
        this.chatMessageMapper = chatMessageMapper;
    }

    @Data
    @AllArgsConstructor
    public static class VerificationResult {
        private int creditScore;
        private float registerDays;
        private float productCount;
        private float reportCount;
        private float avgReplyTime;
        private int publicCreditScore;
        private int sellerCreditScore;
    }

    @Transactional(readOnly = true)
    public VerificationResult verify(Long userId, Long productId, Float transactionAmount) {
        if (userId == null) {
            throw new RuntimeException("未获取到当前登录用户");
        }
        if (productId == null) {
            throw new RuntimeException("商品ID不能为空");
        }
        if (transactionAmount == null || transactionAmount <= 0) {
            throw new RuntimeException("交易金额必须大于0");
        }

        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("当前用户不存在");
        }

        Product product = productMapper.findById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在或已删除");
        }
        if (userId.equals(product.getUserId())) {
            throw new RuntimeException("不能购买自己的商品");
        }
        if (!PRODUCT_STATUS_ON_SALE.equals(product.getProductStatus())) {
            throw new RuntimeException("当前商品不处于可购买状态");
        }

        float registerDays = calculateRegisterDays(user);
        float productCount = toFloat(orderInfoMapper.countByUserId(userId));
        float reportCount = toFloat(reportMapper.countGlobalReports(userId));
        float avgReplyTime = calculateAverageReplyHours(userId);
        int publicCreditScore = user.getCreditScore() == null ? 100 : user.getCreditScore();

        User seller = userMapper.findById(product.getUserId());
        int sellerStaticScore = (seller != null && seller.getCreditScore() != null) ? seller.getCreditScore() : 100;

        int creditScore = creditScoreAiService.calculateCreditScore(
                registerDays,
                productCount,
                reportCount,
                avgReplyTime,
                publicCreditScore,
                transactionAmount
        );

        return new VerificationResult(
                creditScore,
                registerDays,
                productCount,
                reportCount,
                avgReplyTime,
                publicCreditScore,
                sellerStaticScore
        );
    }

    private float calculateRegisterDays(User user) {
        if (user.getCreateTime() == null) {
            return 0.0f;
        }
        long durationMillis = System.currentTimeMillis() - user.getCreateTime().getTime();
        return Math.max(0.0f, durationMillis / (float) TimeUnit.DAYS.toMillis(1));
    }

    private float toFloat(Long value) {
        return value == null ? 0.0f : value.floatValue();
    }

    private float calculateAverageReplyHours(Long userId) {
        Float avgReplyHours = chatMessageMapper.findAverageReplyHoursByUserId(userId);
        if (avgReplyHours == null || avgReplyHours <= 0) {
            return 6.0f;
        }
        return avgReplyHours;
    }
}
