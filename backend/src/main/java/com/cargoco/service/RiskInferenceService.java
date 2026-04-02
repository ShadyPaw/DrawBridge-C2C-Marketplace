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
     * 
     * @param registerDays 注册天数
     * @param productCount 发布商品数
     * @param reportCount 被举报次数
     * @param avgReplyTime 平均回复耗时
     * @param creditScore 信用分
     * @return 风险概率值 (0.0 ~ 1.0, 越接近 1 则风险越高)
     */
    public float predictRiskScore(float registerDays, float productCount, float reportCount, float avgReplyTime, float creditScore) {
        if (session == null) {
            return 0.0f;
        }

        try {
            // 构建输入矩阵，形状为 [1, 5]
            float[][] inputData = new float[][]{{
                registerDays,
                productCount,
                reportCount,
                avgReplyTime,
                creditScore
            }};

            // 将输入封装为 OnnxTensor
            try (OnnxTensor tensor = OnnxTensor.createTensor(env, inputData)) {
                // 执行推理。注意：输入键名必须与训练导出时定义的 'float_input' 一致
                try (OrtSession.Result result = session.run(Collections.singletonMap("float_input", tensor))) {
                    
                    // 解析输出层
                    // XGBoost 输出通常是 [label, probabilities]
                    // 根据训练结果提取违规概率 (对应 is_violation=1 的得分，通常在 probabilities 索引为 1 的位置)
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
     * 
     * @param user 用户对象
     * @param productCount 发布商品数
     * @param reportCount 被举报次数
     * @return 风险评分 (0.0 ~ 1.0)
     */
    public float calculateUserRisk(com.cargoco.entity.User user, float productCount, float reportCount) {
        if (user == null || user.getCreateTime() == null) return 0.0f;
        float registerDays = (float) ((System.currentTimeMillis() - user.getCreateTime().getTime()) / (1000 * 60 * 60 * 24));
        float avgReplyTime = 2.0f; // Mock
        // 废除旧版信用分，张量输入硬编码为 100.0f 以保证模型维度稳定
        float creditScore = 100.0f; 
        return predictRiskScore(registerDays, productCount, reportCount, avgReplyTime, creditScore);
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
