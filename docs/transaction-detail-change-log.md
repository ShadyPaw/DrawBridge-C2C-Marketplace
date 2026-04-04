# 交易风控与商品详情页变更文档

## 1. 本次已完成的改动 (V1.7 - 双向风控系统)

### 1.1 双端风控建模
- 重构 `TransactionVerificationService`，支持同时获取买家和卖家的信用维度
- **买家端**：执行实时特征聚合 + ONNX 动态模型推理，确保证明买家账户当前处于无风险状态
- **卖家端**：采用静态信用分透传模式 (`user.credit_score`)，在不增加算力负担的前提下引入反诈防御

### 1.2 严密风控规则链 (Strict Rule Chain)
- 在接口层实现了瀑布式规则校验：
  - **Rule A (强制阻断)**：买家信用分 < 60 -> HTTP 403
  - **Rule C (灰度限额)**：买家信用分 < 80 且 交易金额 > 500 -> HTTP 403
  - **Rule B (风险告知)**：卖家信用分 < 60 -> HTTP 409 (Conflict)
- 规则 A/C 具备最高优先级，确保高危买家无法通过 Rule B 的确认逻辑绕过限额拦截

### 1.3 前端多态熔断交互
- `ProductDetail.vue` 劫持了下单前的 verify 请求
- 对 `403` 状态码：弹出红色 Error Alert，仅提供“我知道了”，彻底阻断流程
- 对 `409` 状态码：弹出黄色 Warning Confirm，告知卖家风险并提供“继续购买”和“中止交易”选项

### 1.4 测试工程鲁棒性优化
- 彻底重构了 `TransactionControllerTest`
- 引入 `@MockBean` 模拟风控核心服务，不再依赖特定本地模型文件
- 注入 `JwtUtil` 实现完整的鉴权头模拟，覆盖了全部四类风控业务逻辑的自动化回归

## 2. 历史改动回顾

### 1.1 后端交易核验链路重构

- `/api/transaction/verify` 已改为只接收 `productId` 和 `transactionAmount`
- 接口不再信任前端传入任何风控特征
- 服务端通过登录态中的 `userId` 实时计算真实特征：
  - `registerDays`：当前时间减去用户 `create_time`
  - `productCount`：用户历史订单数量（买家 + 卖家）
  - `reportCount`：用户全域被举报次数
- ONNX 模型计算出最新信用分后，立即回写 `user.credit_score`

### 1.2 后端底层防线补强

- `verify` 接口已强制要求登录，不再放行匿名调用
- 新增商品存在性校验
- 新增“不能购买自己的商品”校验
- 新增“商品必须处于可售状态”校验
- 控制器内统一了信用分阈值常量，减少魔法数字散落

### 1.3 前端商品详情页清理

- 已删除演示用“高危/安全用户”单选框
- 已删除所有伪造特征 payload
- 购买前校验请求现在只发送：
  - `productId`
  - `transactionAmount`
- 保留并恢复了 403 高危拦截弹窗，后端拒绝时仍然阻断下单
- `ProductDetail.vue` 已重建为可正常编译的干净 SFC

### 1.4 前端请求层优化

- 全局 axios 拦截器对 `/api/transaction/verify` 的 `403` 不再弹通用“没有权限访问”
- 交易高危场景只保留业务级红色拦截模态框，避免双重提示

### 1.5 中文文案清理

- 已清理本次交易链路和商品详情页中涉及的乱码中文：
  - `TransactionController`
  - `TransactionVerificationService`
  - `CreditScoreAiService`
  - `ProductDetail.vue`
  - `frontend/src/utils/request.js`

## 2. 本次涉及文件

### 后端

- `backend/src/main/java/com/cargoco/controller/TransactionController.java`
- `backend/src/main/java/com/cargoco/service/TransactionVerificationService.java`
- `backend/src/main/java/com/cargoco/service/CreditScoreAiService.java`
- `backend/src/main/java/com/cargoco/mapper/OrderInfoMapper.java`
- `backend/src/main/resources/mapper/OrderInfoMapper.xml`
- `backend/src/main/java/com/cargoco/mapper/UserMapper.java`
- `backend/src/main/resources/mapper/UserMapper.xml`
- `backend/src/main/java/com/cargoco/config/WebMvcConfig.java`

### 前端

- `frontend/src/views/ProductDetail.vue`
- `frontend/src/utils/request.js`

## 3. 已完成验证

- `mvn -q -DskipTests compile` 通过
- `npm run build` 通过

## 4. 建议继续优化的底层逻辑

### 4.1 订单创建应复用交易核验结果

- 当前流程是前端先调 `verify`，再调 `order/create`
- 这意味着“核验”和“正式创建订单”之间仍然存在时间窗
- 更稳妥的做法是把 `verify + createOrder` 合并为后端同一事务或编排服务

### 4.2 信用分回写建议增加审计记录

- 现在 `verify` 会更新 `user.credit_score`
- 但尚未记录“由哪次交易触发、使用了哪些特征、旧分和新分分别是多少”
- 建议补一张信用评分流水表，便于排障、申诉和模型回溯

### 4.3 金额类型建议后端改为 `BigDecimal`

- 前端和风控接口目前使用 `Float`
- 金额相关字段长期使用浮点数有精度风险
- 建议接口层接收 `BigDecimal`，模型计算前再统一转 `float`

### 4.4 订单状态流转需要更强的一致性保护

- `OrderServiceImpl` 当前主要依靠状态码判断
- 但缺少并发保护，极端情况下可能出现重复支付、重复发货、重复确认收货
- 建议增加乐观锁字段或基于 SQL 条件更新的状态机保护

### 4.5 风控特征计算建议抽为统一特征服务

- 现在交易核验、用户详情风险展示、举报处理等逻辑都在分别计算风控相关数据
- 建议抽成统一 `UserRiskFeatureService`
- 这样可避免不同入口特征口径不一致

### 4.6 全局中文乱码仍建议分模块治理

- 本次只清理了交易链路和详情页相关文件
- 项目中其他历史模块仍存在部分乱码中文
- 建议按“用户端前端 -> 订单模块 -> 管理后台 -> 后端 Service/Controller”分批清理，避免一次性大改风险

## 5. 推荐下一步

### 第一优先级

- 把 `verify + createOrder` 合并到同一后端事务流程

### 第二优先级

- 给信用分更新补充审计流水

### 第三优先级

- 分批清理项目中剩余乱码中文，统一为 UTF-8 无 BOM
