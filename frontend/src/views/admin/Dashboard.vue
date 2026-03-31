<template>
  <div>
    <h2 style="margin-bottom: 24px;">控制台</h2>
    <div class="stats-grid">
      <div class="stat-card"><div class="stat-icon" style="background: #e6f4ff; color: #1677ff;"><el-icon :size="28"><User /></el-icon></div><div><div class="stat-number">{{ stats.userCount || 0 }}</div><div class="stat-label">用户总数</div></div></div>
      <div class="stat-card"><div class="stat-icon" style="background: #f6ffed; color: #52c41a;"><el-icon :size="28"><Goods /></el-icon></div><div><div class="stat-number">{{ stats.productCount || 0 }}</div><div class="stat-label">商品总数</div></div></div>
      <div class="stat-card"><div class="stat-icon" style="background: #e6f4ff; color: #1677ff;"><el-icon :size="28"><ShoppingCart /></el-icon></div><div><div class="stat-number">{{ stats.orderCount || 0 }}</div><div class="stat-label">订单总数</div></div></div>
      <div class="stat-card"><div class="stat-icon" style="background: #fff7e6; color: #fa8c16;"><el-icon :size="28"><DocumentChecked /></el-icon></div><div><div class="stat-number">{{ stats.pendingAudit || 0 }}</div><div class="stat-label">待审核商品</div></div></div>
      <div class="stat-card"><div class="stat-icon" style="background: #fff2f0; color: #ff4d4f;"><el-icon :size="28"><Warning /></el-icon></div><div><div class="stat-number">{{ stats.pendingReport || 0 }}</div><div class="stat-label">待处理举报</div></div></div>
      <div class="stat-card"><div class="stat-icon" style="background: #f6ffed; color: #52c41a;"><el-icon :size="28"><CircleCheck /></el-icon></div><div><div class="stat-number">{{ stats.completedOrders || 0 }}</div><div class="stat-label">已完成交易</div></div></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '../../api'
const stats = ref({})
onMounted(async () => { try { const r = await adminApi.dashboard(); stats.value = r.data } catch(e){} })
</script>

<style scoped>
.stats-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
.stat-card { display: flex; align-items: center; gap: 16px; background: #fff; padding: 24px; border-radius: 12px; box-shadow: var(--shadow-sm); }
.stat-icon { width: 56px; height: 56px; border-radius: 12px; display: flex; align-items: center; justify-content: center; }
.stat-number { font-size: 28px; font-weight: 700; color: var(--text-primary); }
.stat-label { font-size: 13px; color: var(--text-tertiary); margin-top: 2px; }
@media (max-width: 768px) { .stats-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
