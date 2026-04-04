package com.cargoco.controller;

import com.cargoco.service.TransactionVerificationService;
import com.cargoco.utils.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * TransactionController 交易风控集成测试
 * 使用随机端口防止冲突，依赖 MockBean 进行风控规则链断言
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TransactionControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private TransactionVerificationService transactionVerificationService;

    private static final String VERIFY_URL = "/api/transaction/verify";

    private HttpHeaders createHeaders() {
        String token = jwtUtil.generateToken(1L, "testUser", 0);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return headers;
    }

    @Test
    @DisplayName("测试规则A：买家高危拦截 (403)")
    public void testRuleA_BuyerHighRisk() {
        when(transactionVerificationService.verify(anyLong(), anyLong(), anyFloat()))
                .thenReturn(new TransactionVerificationService.VerificationResult(50, 0f, 0f, 0f, 0f, 100, 100));

        Map<String, Object> request = new HashMap<>();
        request.put("productId", 100L);
        request.put("transactionAmount", 100.0f);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, createHeaders());
        ResponseEntity<Map> response = restTemplate.postForEntity(VERIFY_URL, entity, Map.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(), "应拦截低分买家");
        assertEquals("【买家风控】您的账号被判定为高危，已限制交易。", response.getBody().get("message"));
    }

    @Test
    @DisplayName("测试规则C：买家限额拦截 (403)")
    public void testRuleC_BuyerLimit() {
        when(transactionVerificationService.verify(anyLong(), anyLong(), anyFloat()))
                .thenReturn(new TransactionVerificationService.VerificationResult(65, 0f, 0f, 0f, 0f, 100, 100));

        Map<String, Object> request = new HashMap<>();
        request.put("productId", 100L);
        request.put("transactionAmount", 600.0f); // 超过 500

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, createHeaders());
        ResponseEntity<Map> response = restTemplate.postForEntity(VERIFY_URL, entity, Map.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(), "应拦截中危买家的大额交易");
        assertEquals("【风控限制】您的信用评级为中等风险，当前仅支持 500 元以下的小额交易。", response.getBody().get("message"));
    }

    @Test
    @DisplayName("测试规则B：卖家高危反诈提示 (409)")
    public void testRuleB_SellerHighRisk() {
        // 买家 90 分，卖家 40 分
        when(transactionVerificationService.verify(anyLong(), anyLong(), anyFloat()))
                .thenReturn(new TransactionVerificationService.VerificationResult(90, 0f, 0f, 0f, 0f, 100, 40));

        Map<String, Object> request = new HashMap<>();
        request.put("productId", 100L);
        request.put("transactionAmount", 100.0f);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, createHeaders());
        ResponseEntity<Map> response = restTemplate.postForEntity(VERIFY_URL, entity, Map.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode(), "应返回409要求确认卖家风险");
        assertEquals("【反诈提醒】该卖家信用极低，存在较高交易风险，系统建议您终止交易！", response.getBody().get("message"));
    }

    @Test
    @DisplayName("测试规则D：安全放行 (200)")
    public void testRuleD_SafePass() {
        // 买家卖家都安全，或者买家中危但金额 < 500
        when(transactionVerificationService.verify(anyLong(), anyLong(), anyFloat()))
                .thenReturn(new TransactionVerificationService.VerificationResult(90, 0f, 0f, 0f, 0f, 100, 90));

        Map<String, Object> request = new HashMap<>();
        request.put("productId", 100L);
        request.put("transactionAmount", 100.0f);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, createHeaders());
        ResponseEntity<Map> response = restTemplate.postForEntity(VERIFY_URL, entity, Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "应放行安全交易");
        assertEquals("信用良好，交易放行", response.getBody().get("message"));
    }
}
