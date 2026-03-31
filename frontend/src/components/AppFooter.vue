<template>
  <footer class="app-footer">
    <div class="footer-inner container">
      <div class="footer-links">
        <div class="footer-col">
          <h4>关于我们</h4>
          <a href="#">平台介绍</a>
          <a href="#">联系我们</a>
          <a href="#">用户协议</a>
        </div>
        <div class="footer-col">
          <h4>帮助中心</h4>
          <a href="#">如何发布</a>
          <a href="#">交易指南</a>
          <a href="#">安全须知</a>
        </div>
        <div class="footer-col">
          <h4>交易保障</h4>
          <a href="#">信用体系</a>
          <a href="#">举报投诉</a>
          <a href="#">隐私保护</a>
        </div>
      </div>
      <div class="footer-bottom">
        <p>© 2026 闲置集市 - 让闲置流转，让价值延续</p>
        <p class="sub">基于 SpringBoot + Vue3 构建的C2C闲置物品交易平台</p>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.app-footer {
  background: #1f1f1f;
  color: #a0a0a0;
  padding: 40px 0 24px;
  margin-top: 60px;
}

.footer-inner {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.footer-links {
  display: flex;
  gap: 80px;
  flex-wrap: wrap;
}

.footer-col {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.footer-col h4 {
  color: #e0e0e0;
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 4px;
}

.footer-col a {
  font-size: 13px;
  color: #888;
  transition: color 0.2s;
}

.footer-col a:hover { color: #fff; }

.footer-bottom {
  border-top: 1px solid #333;
  padding-top: 20px;
  text-align: center;
  font-size: 13px;
}

.footer-bottom .sub {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

@media (max-width: 768px) {
  .footer-links { gap: 40px; }
}
</style>
