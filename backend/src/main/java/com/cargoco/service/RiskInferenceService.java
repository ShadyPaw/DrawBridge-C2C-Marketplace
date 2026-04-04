package com.cargoco.service;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.InputStream;
import java.util.Collections;

/**
 * 风险推理服务类
 * 使用 ONNX Runtime 加载预训练的 XGBoost 模型进行实时交易风险评估
 */
@Service
public class RiskInferenceService {

    private OrtEnvironment env;
    private OrtSession session;

    /**
     * 服务初始化方法
     * 从 resources/ai 目录下加载训练好的 cargo_risk_model.onnx 模型文件
     */
    @PostConstruct
    public void init() {
        try {
            this.env = OrtEnvironment.getEnvironment();
            // 使用 ClassPathResource 读取 resources 目录下的模型文件流
            InputStream is = new ClassPathResource("ai/cargo_risk_model.onnx").getInputStream();
            byte[] modelBytes = StreamUtils.copyToByteArray(is);
            
            // 初始化推理会话
            this.session = env.createSession(modelBytes);
            System.out.println("[AI Service] ONNX Risk Model loaded successfully.");
        } catch (Exception e) {
            System.err.println("[AI Service] Failed to initialize ONNX model: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 预测交易风险评分 (违规概率)
     */
    public float predictRiskScore(float registerDays, float productCount, float reportCount, float transactionAmount) {
        if (session == null) {
            return 0.0f;
        }

        try {
            // 特征清洗：标准化金额 (Mean=300, Std=150)
            float scaledAmount = (transactionAmount - 300.0f) / 150.0f;

            // 严格对齐模型 4 维张量：[注册天数, 商品数, 举报数, 缩放后的金额]
            float[][] inputData = new float[][]{{
                registerDays,
                productCount,
                reportCount,
                scaledAmount
            }};

            try (OnnxTensor tensor = OnnxTensor.createTensor(env, inputData)) {
                try (OrtSession.Result result = session.run(Collections.singletonMap("float_input", tensor))) {
                    float[][] outputProbs = (float[][]) result.get(1).getValue();
                    return outputProbs[0][1];
                }
            }
        } catch (Exception e) {
            System.err.println("[AI Service] Inference failed: " + e.getMessage());
            return 0.0f;
        }
    }

    /**
     * 辅助方法：为指定用户计算 AI 风险分
     */
    public float calculateUserRisk(com.cargoco.entity.User user, float productCount, float reportCount) {
        if (user == null || user.getCreateTime() == null) return 0.0f;
        float registerDays = (float) ((System.currentTimeMillis() - user.getCreateTime().getTime()) / (1000 * 60 * 60 * 24));
        // 普适性风险评估，交易金额取历史均值 300.0f (标准化后为 0.0)
        return predictRiskScore(registerDays, productCount, reportCount, 300.0f);
    }

    /**
     * 资源释放方法
     * 遵循 C++ 底层内存管理，关闭 OrtSession 和 OrtEnvironment
     */
    @PreDestroy
    public void cleanup() {
        try {
            if (session != null) {
                session.close();
            }
            if (env != null) {
                env.close();
            }
            System.out.println("[AI Service] ONNX resources released.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
