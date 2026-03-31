<template>
  <div>
    <h2 class="page-title">我买到的</h2>
    <el-tabs v-model="activeTab" @tab-change="loadOrders">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="待付款" name="0" />
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
          <div class="order-info">
            <div class="order-title">{{ o.productTitle }}</div>
            <div class="order-seller">卖家: {{ o.sellerNickname }}</div>
          </div>
          <div class="order-price price">{{ o.price }}</div>
        </div>
        <div class="order-footer" @click.stop>
          <span class="order-time">{{ formatTime(o.createTime) }}</span>
          <div class="order-actions">
            <el-button v-if="o.orderStatus === 0" type="primary" size="small" @click="handlePay(o.id)">付款</el-button>
            <el-button v-if="o.orderStatus === 2" type="success" size="small" @click="handleReceive(o.id)">确认收货</el-button>
            <el-button v-if="o.orderStatus < 3" size="small" @click="handleCancel(o.id)">取消</el-button>
          </div>
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

onMounted(() => loadOrders())

async function loadOrders() {
  try {
    const params = { pageNum: 1, pageSize: 50 }
    if (activeTab.value !== 'all') params.orderStatus = Number(activeTab.value)
    const res = await orderApi.buyerOrders(params)
    orders.value = res.data.list || []
  } catch (e) {}
}

function orderStatusText(s) { return { 0: '待付款', 1: '待发货', 2: '待收货', 3: '已完成', 4: '已取消', 5: '退款中', 6: '已退款' }[s] || '未知' }
function orderStatusClass(s) { return { 0: 'warning', 1: 'info', 2: 'info', 3: 'success', 4: 'danger' }[s] || 'info' }

async function handlePay(id) {
  await ElMessageBox.confirm('确认付款？（模拟支付）', '付款确认')
  try { await orderApi.pay(id); ElMessage.success('支付成功'); loadOrders() } catch (e) {}
}

async function handleReceive(id) {
  await ElMessageBox.confirm('确认已收到商品？', '确认收货')
  try { await orderApi.receive(id); ElMessage.success('确认收货成功'); loadOrders() } catch (e) {}
}

async function handleCancel(id) {
  const { value } = await ElMessageBox.prompt('请输入取消原因', '取消订单', { inputPlaceholder: '选填' })
  try { await orderApi.cancel(id, value); ElMessage.success('取消成功'); loadOrders() } catch (e) {}
}

function formatTime(t) { return t ? new Date(t).toLocaleString('zh-CN') : '' }
</script>

<style scoped>
.page-title { font-size: 20px; font-weight: 600; margin-bottom: 16px; }
.order-list { display: flex; flex-direction: column; gap: 12px; }
.order-item { border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 16px; cursor: pointer; transition: all 0.2s; }
.order-item:hover { border-color: var(--primary-color); box-shadow: var(--shadow-sm); }
.order-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.order-no { font-size: 13px; color: var(--text-tertiary); }
.order-body { display: flex; align-items: center; gap: 16px; }
.order-img { width: 72px; height: 72px; border-radius: 8px; object-fit: cover; flex-shrink: 0; }
.order-info { flex: 1; }
.order-title { font-size: 15px; font-weight: 500; margin-bottom: 4px; }
.order-seller { font-size: 13px; color: var(--text-tertiary); }
.order-price { flex-shrink: 0; }
.order-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 12px; padding-top: 12px; border-top: 1px solid var(--divider-color); }
.order-time { font-size: 12px; color: var(--text-quaternary); }
</style>
