# AI 信用逻辑诊断与修复说明

## 根因结论

当前项目出现“商品详情页的信用度”和“用户主页的信用度”不一致，并不是单纯因为模型训练结果有问题，而是因为系统同时混用了两套不同的评分体系：

- 公开信用体系：`user.credit_score` / `user.user_level`
- AI 风控体系：`riskScore` / `sellerRiskScore`

这两套体系的用途本来就不同，但历史代码把 AI 风险分错误地拿去当公开信用度展示，导致同一用户在不同页面出现不同数值。

## 发现的关键错误

### 1. 商品详情页使用了伪造特征计算卖家风险分

历史 `ProductServiceImpl.getDetail()` 中，卖家风险是这样算的：

- 注册天数直接写死为 `30.0f`
- 交易金额直接写死为 `300.0f`
- 举报次数只查了 `countByTarget(userId, 2)`

这不是用户真实信用，也不是用户真实风控画像，因此商品详情页天然不可靠。

### 2. 用户主页公开展示了 AI 风险分而不是信用分

历史 `UserServiceImpl.getUserById()` 会临时计算 `riskScore`，而前端 `UserProfile.vue`、`MyCenter.vue` 又把它渲染成“信用度 %”。

这会让用户误以为这是平台正式信用分，但 README 和数据库设计里的正式信用字段其实是 `credit_score`。

### 3. AI 校验越权改写了公开信用分

历史 `TransactionVerificationService.verify()` 会把 AI 计算结果直接回写 `user.credit_score`。

这会污染 README 中定义的信用体系：

- 信用分原本应由评价、等级等业务规则驱动
- AI 风控分应该只用于交易拦截、告警、审核辅助

把 AI 结果直接写进 `credit_score`，会让公开信用分和业务信用体系彻底混在一起。

## 本次修复

### 1. 统一公开展示口径

以下页面现在统一展示 `creditScore`：

- `frontend/src/views/ProductDetail.vue`
- `frontend/src/views/UserProfile.vue`
- `frontend/src/views/my/MyCenter.vue`

### 2. 停止在公开接口中注入 AI 风险分

以下服务已收回风险分公开暴露：

- `backend/src/main/java/com/cargoco/service/impl/UserServiceImpl.java`
- `backend/src/main/java/com/cargoco/service/impl/ProductServiceImpl.java`

这样商品详情页和用户主页都会读取同一份数据库信用分，不再出现展示口径漂移。

### 3. 停止 AI 校验改写 `credit_score`

`TransactionVerificationService.verify()` 现在只做：

- 实时计算风控特征
- 调用 ONNX 模型进行交易前风控判断
- 返回本次校验结果

它不再覆盖 `user.credit_score`，避免 AI 风险分污染公开信用体系。

## 修复后的职责边界

### 公开信用体系

用于前台展示、用户主页、商品详情页卖家信用展示：

- `user.credit_score`
- `user.user_level`

### AI 风控体系

用于交易前校验、后台风控、违规预警：

- `CreditScoreAiService`
- `RiskInferenceService`
- `/api/transaction/verify`

## 后续建议

### 第一优先级

新增独立字段或独立表保存 AI 风险结果，例如：

- `user.ai_risk_score`
- `transaction_risk_log`

### 第二优先级

把 README 中 V1.6 的 AI 描述与前面的旧积分文档统一，否则后续开发仍会继续误用。

### 第三优先级

把后台管理页中的风险展示统一收口到专门的 VO，不要再让普通用户公开接口返回风险字段。