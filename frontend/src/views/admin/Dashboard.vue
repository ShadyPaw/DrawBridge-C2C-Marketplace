<template>
  <div>
    <h2 style="margin-bottom: 24px;">控制台</h2>
    <div class="stats-grid">
      <div class="stat-card"><div class="stat-icon" style="background: #e6f4ff; color: #1677ff;"><el-icon :size="28"><User /></el-icon></div><div><div class="stat-number">{{ stats.userCount || 0 }}</div><div class="stat-label">用户总数</div></div></div>
      <div class="stat-card"><div class="stat-icon" style="background: #f6ffed; color: #52c41a;"><el-icon :size="28"><Goods /></el-icon></div><div><div class="stat-number">{{ stats.productCount || 0 }}</div><div class="stat-label">商品总数</div></div></div>
      <div class="stat-card"><div class="stat-icon" style="background: #e6f4ff; color: #1677ff;"><el-icon :size="28"><ShoppingCart /></el-icon></div><div><div class="stat-number">{{ stats.orderCount || 0 }}</div><div class="stat-label">订单总数</div></div></div>
      <div class="stat-card"><div class="stat-icon" style="background: #fff7e6; color: #d4a017;"><el-icon :size="28"><DocumentChecked /></el-icon></div><div><div class="stat-number">{{ stats.pendingAudit || 0 }}</div><div class="stat-label">待审核商品</div></div></div>
      <div class="stat-card"><div class="stat-icon" style="background: #fff2f0; color: #ff4d4f;"><el-icon :size="28"><Warning /></el-icon></div><div><div class="stat-number">{{ stats.pendingReport || 0 }}</div><div class="stat-label">待处理举报</div></div></div>
      <div class="stat-card"><div class="stat-icon" style="background: #f6ffed; color: #52c41a;"><el-icon :size="28"><CircleCheck /></el-icon></div><div><div class="stat-number">{{ stats.completedOrders || 0 }}</div><div class="stat-label">已完成交易</div></div></div>
    </div>

    <!-- 快捷操作区 -->
    <div class="quick-actions">
      <h3 style="margin-bottom: 16px;">快捷操作</h3>
      <div class="action-card">
        <div class="action-info">
          <el-icon :size="24" style="color: #1677ff;"><MagicStick /></el-icon>
          <div>
            <div class="action-title">初始化模拟社区数据</div>
            <div class="action-desc">一键生成 10 个模拟用户 + 20 件二手商品，用于系统演示和功能测试</div>
          </div>
        </div>
        <el-button type="primary" :loading="initLoading" @click="handleInitData">
          {{ initLoading ? '正在初始化...' : '初始化模拟社区数据' }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const stats = ref({})
const initLoading = ref(false)

onMounted(async () => { try { const r = await adminApi.dashboard(); stats.value = r.data } catch(e){} })

async function handleInitData() {
  try {
    await ElMessageBox.confirm(
      '将生成 10 个模拟用户和 20 件二手商品数据，确认执行？',
      '初始化社区数据',
      { confirmButtonText: '确认执行', cancelButtonText: '取消', type: 'info' }
    )
  } catch { return }

  initLoading.value = true
  try {
    await adminApi.initData()
    ElMessage.success('社区生态数据初始化成功，请刷新首页查看！')
    // 刷新统计数据
    const r = await adminApi.dashboard()
    stats.value = r.data
  } catch (e) {
    ElMessage.error(e.message || '初始化失败，请检查后端日志')
  } finally {
    initLoading.value = false
  }
}
</script>

<style scoped>
.stats-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
.stat-card { display: flex; align-items: center; gap: 16px; background: #fff; padding: 24px; border-radius: 12px; box-shadow: var(--shadow-sm); }
.stat-icon { width: 56px; height: 56px; border-radius: 12px; display: flex; align-items: center; justify-content: center; }
.stat-number { font-size: 28px; font-weight: 700; color: var(--text-primary); }
.stat-label { font-size: 13px; color: var(--text-tertiary); margin-top: 2px; }

.quick-actions { margin-top: 32px; }
.quick-actions h3 { font-size: 16px; font-weight: 600; }
.action-card {
  display: flex; justify-content: space-between; align-items: center;
  background: #fff; padding: 20px 24px; border-radius: 12px;
  box-shadow: var(--shadow-sm); border: 1px solid var(--border-color);
}
.action-info { display: flex; align-items: center; gap: 16px; }
.action-title { font-size: 15px; font-weight: 600; margin-bottom: 4px; }
.action-desc { font-size: 13px; color: var(--text-tertiary); }

@media (max-width: 768px) { .stats-grid { grid-template-columns: repeat(2, 1fr); } .action-card { flex-direction: column; gap: 16px; } }
</style>
