<template>
  <div>
    <h2 class="page-title">我卖出的</h2>
    <el-tabs v-model="activeTab" @tab-change="loadOrders">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="待发货" name="1" />
      <el-tab-pane label="待收货" name="2" />
      <el-tab-pane label="已完成" name="3" />
    </el-tabs>
    <div v-if="orders.length === 0" class="empty-state"><el-empty description="暂无订单" /></div>
    <div v-else class="order-list">
      <div v-for="o in orders" :key="o.id" class="order-item" @click="$router.push(`/order/${o.id}`)">
        <div class="order-header">
          <span class="order-no">订单号: {{ o.orderNo }}</span>
          <span class="status-badge" :class="orderStatusClass(o.orderStatus)">{{ orderStatusText(o.orderStatus) }}</span>
        </div>
        <div class="order-body">
          <img :src="o.productCover || '/placeholder.png'" class="order-img" />
          <div class="order-info"><div class="order-title">{{ o.productTitle }}</div><div class="order-buyer">买家: {{ o.buyerNickname }}</div></div>
          <div class="order-price price">{{ o.price }}</div>
        </div>
        <div class="order-footer" @click.stop>
          <span class="order-time">{{ formatTime(o.createTime) }}</span>
          <div><el-button v-if="o.orderStatus === 1" type="primary" size="small" @click="handleShip(o.id)">确认发货</el-button></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { orderApi } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeTab = ref('all')
const orders = ref([])

onMounted(loadOrders)

async function loadOrders() {
  try {
    const params = { pageNum: 1, pageSize: 50 }
    if (activeTab.value !== 'all') params.orderStatus = Number(activeTab.value)
    const res = await orderApi.sellerOrders(params)
    orders.value = res.data.list || []
  } catch (e) {}
}

function orderStatusText(s) { return { 0: '待付款', 1: '待发货', 2: '待收货', 3: '已完成', 4: '已取消' }[s] || '未知' }
function orderStatusClass(s) { return { 0: 'warning', 1: 'info', 2: 'info', 3: 'success', 4: 'danger' }[s] || 'info' }

async function handleShip(id) {
  await ElMessageBox.confirm('确认发货？', '发货确认')
  try { await orderApi.ship(id); ElMessage.success('发货成功'); loadOrders() } catch (e) {}
}

function formatTime(t) { return t ? new Date(t).toLocaleString('zh-CN') : '' }
</script>

<style scoped>
.page-title { font-size: 20px; font-weight: 600; margin-bottom: 16px; }
.order-list { display: flex; flex-direction: column; gap: 12px; }
.order-item { border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 16px; cursor: pointer; transition: all 0.2s; }
.order-item:hover { border-color: var(--primary-color); }
.order-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.order-no { font-size: 13px; color: var(--text-tertiary); }
.order-body { display: flex; align-items: center; gap: 16px; }
.order-img { width: 72px; height: 72px; border-radius: 8px; object-fit: cover; }
.order-info { flex: 1; }
.order-title { font-size: 15px; font-weight: 500; margin-bottom: 4px; }
.order-buyer { font-size: 13px; color: var(--text-tertiary); }
.order-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 12px; padding-top: 12px; border-top: 1px solid var(--divider-color); }
.order-time { font-size: 12px; color: var(--text-quaternary); }
</style>
