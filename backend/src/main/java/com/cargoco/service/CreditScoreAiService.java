package com.cargoco.service;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.TensorInfo;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

/**
 * AI 信用评分服务。
 * 基于 ONNX 风险模型计算交易信用分。
 */
@Service
public class CreditScoreAiService {

    private static final float MEAN_AMOUNT = 300.0f;
    private static final float STD_AMOUNT = 150.0f;
    private static final int DEFAULT_SCORE = 80;
    private static final float DEFAULT_REPLY_HOURS = 6.0f;

    private OrtEnvironment env;
    private OrtSession session;
    private int inputFeatureCount = 4;

    @PostConstruct
    public void init() {
        try {
            this.env = OrtEnvironment.getEnvironment();
            InputStream is = new ClassPathResource("ai/cargo_risk_model.onnx").getInputStream();
            byte[] modelBytes = StreamUtils.copyToByteArray(is);
            this.session = env.createSession(modelBytes);
            this.inputFeatureCount = resolveInputFeatureCount(this.session);
            System.out.println("[Credit AI] Model loaded successfully.");
        } catch (Exception e) {
            System.err.println("[Credit AI] Initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public int calculateCreditScore(float registerDays,
                                    float productCount,
                                    float reportCount,
                                    float avgReplyTime,
                                    float creditScore,
                                    float transactionAmount) {
        if (session == null || env == null) {
            return applyBusinessCalibration(
                    DEFAULT_SCORE,
                    registerDays,
                    productCount,
                    reportCount,
                    avgReplyTime,
                    creditScore
            );
        }

        try {
            float[][] inputData = buildModelInput(
                    registerDays,
                    productCount,
                    reportCount,
                    avgReplyTime,
                    creditScore,
                    transactionAmount
            );

            try (OnnxTensor tensor = OnnxTensor.createTensor(env, inputData)) {
                try (OrtSession.Result result = session.run(Collections.singletonMap("float_input", tensor))) {
                    float[][] outputProbs = (float[][]) result.get(1).getValue();
                    float fraudProbability = outputProbs[0][1];
                    int rawScore = clampScore((int) (100 - (fraudProbability * 100)));
                    return applyBusinessCalibration(
                            rawScore,
                            registerDays,
                            productCount,
                            reportCount,
                            avgReplyTime,
                            creditScore
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("[Credit AI] Calculation failed: " + e.getMessage());
            int fallbackScore = buildRuleBasedScore(
                    registerDays,
                    productCount,
                    reportCount,
                    avgReplyTime,
                    creditScore
            );
            return applyBusinessCalibration(
                    fallbackScore,
                    registerDays,
                    productCount,
                    reportCount,
                    avgReplyTime,
                    creditScore
            );
        }
    }

    private float[][] buildModelInput(float registerDays,
                                      float productCount,
                                      float reportCount,
                                      float avgReplyTime,
                                      float creditScore,
                                      float transactionAmount) {
        if (inputFeatureCount >= 5) {
            return new float[][]{{
                    registerDays,
                    productCount,
                    reportCount,
                    sanitizeReplyHours(avgReplyTime),
                    sanitizeCreditScore(creditScore)
            }};
        }

        float scaledAmount = (transactionAmount - MEAN_AMOUNT) / STD_AMOUNT;
        return new float[][]{{
                registerDays,
                productCount,
                reportCount,
                scaledAmount
        }};
    }

    private int applyBusinessCalibration(int rawScore,
                                         float registerDays,
                                         float productCount,
                                         float reportCount,
                                         float avgReplyTime,
                                         float creditScore) {
        if (!hasHardRiskSignal(registerDays, productCount, reportCount, avgReplyTime, creditScore)
                && creditScore >= 100.0f) {
            return Math.max(rawScore, DEFAULT_SCORE);
        }
        return clampScore(rawScore);
    }

    private int buildRuleBasedScore(float registerDays,
                                    float productCount,
                                    float reportCount,
                                    float avgReplyTime,
                                    float creditScore) {
        if (hasHardRiskSignal(registerDays, productCount, reportCount, avgReplyTime, creditScore)) {
            if (creditScore < 60.0f) {
                return 25;
            }
            if (reportCount > 3.0f && sanitizeReplyHours(avgReplyTime) > 12.0f) {
                return 40;
            }
            return 50;
        }
        if (creditScore >= 100.0f) {
            return 88;
        }
        if (creditScore >= 80.0f) {
            return 76;
        }
        return 65;
    }

    private boolean hasHardRiskSignal(float registerDays,
                                      float productCount,
                                      float reportCount,
                                      float avgReplyTime,
                                      float creditScore) {
        float safeReplyHours = sanitizeReplyHours(avgReplyTime);
        return creditScore < 60.0f
                || (reportCount > 3.0f && safeReplyHours > 12.0f)
                || (registerDays < 7.0f && productCount > 15.0f);
    }

    private float sanitizeReplyHours(float avgReplyTime) {
        if (avgReplyTime <= 0) {
            return DEFAULT_REPLY_HOURS;
        }
        return Math.max(0.1f, Math.min(avgReplyTime, 72.0f));
    }

    private float sanitizeCreditScore(float creditScore) {
        return Math.max(0.0f, Math.min(creditScore, 120.0f));
    }

    private int clampScore(int score) {
        return Math.max(0, Math.min(100, score));
    }

    private int resolveInputFeatureCount(OrtSession session) {
        try {
            Map<String, ai.onnxruntime.NodeInfo> inputInfo = session.getInputInfo();
            if (inputInfo.isEmpty()) {
                return 4;
            }
            TensorInfo tensorInfo = (TensorInfo) inputInfo.values().iterator().next().getInfo();
            long[] shape = tensorInfo.getShape();
            if (shape.length >= 2 && shape[1] > 0) {
                return (int) shape[1];
            }
        } catch (Exception e) {
            System.err.println("[Credit AI] Failed to resolve input feature count: " + e.getMessage());
        }
        return 4;
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (session != null) {
                session.close();
            }
            if (env != null) {
                env.close();
            }
            System.out.println("[Credit AI] ONNX environment released.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
