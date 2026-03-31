<template>
  <div class="order-detail-page container" v-if="order">
    <div class="detail-card card" style="padding: 24px;">
      <div class="order-status-bar" :class="'status-' + order.orderStatus">
        <el-icon :size="32"><component :is="statusIcon" /></el-icon>
        <div><div class="os-text">{{ orderStatusText(order.orderStatus) }}</div><div class="os-sub">{{ statusSubText }}</div></div>
      </div>
      <el-divider />
      <div class="order-product" @click="$router.push(`/product/${order.productId}`)">
        <img :src="order.productCover || '/placeholder.png'" />
        <div class="op-info"><div class="op-title">{{ order.productTitle }}</div><div class="op-price price">{{ order.price }}</div></div>
      </div>
      <el-divider />
      <el-descriptions title="订单信息" :column="2" border>
        <el-descriptions-item label="订单编号">{{ order.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ formatTime(order.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="卖家">{{ order.sellerNickname }}</el-descriptions-item>
        <el-descriptions-item label="买家">{{ order.buyerNickname }}</el-descriptions-item>
        <el-descriptions-item label="收货人">{{ order.receiverName }} {{ order.receiverPhone }}</el-descriptions-item>
        <el-descriptions-item label="收货地址">{{ order.fullAddress }}</el-descriptions-item>
        <el-descriptions-item v-if="order.remark" label="备注" :span="2">{{ order.remark }}</el-descriptions-item>
      </el-descriptions>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { orderApi } from '../api'

const route = useRoute()
const order = ref(null)

onMounted(async () => { try { const r = await orderApi.detail(route.params.id); order.value = r.data } catch(e){} })

function orderStatusText(s) { return { 0: '待付款', 1: '待发货', 2: '待收货', 3: '交易完成', 4: '已取消' }[s] || '未知' }
const statusIcon = computed(() => { const m = { 0: 'Timer', 1: 'Box', 2: 'Van', 3: 'CircleCheck', 4: 'CircleClose' }; return m[order.value?.orderStatus] || 'Document' })
const statusSubText = computed(() => { const m = { 0: '请尽快完成付款', 1: '等待卖家发货', 2: '卖家已发货，请确认收货', 3: '交易已完成', 4: '交易已取消' }; return m[order.value?.orderStatus] || '' })

function formatTime(t) { return t ? new Date(t).toLocaleString('zh-CN') : '' }
</script>

<style scoped>
.order-detail-page { padding: 24px 20px; }
.order-status-bar { display: flex; align-items: center; gap: 16px; padding: 16px; border-radius: var(--radius-md); }
.status-0 { background: #fffbe6; color: #ad8b00; }
.status-1 { background: var(--primary-bg); color: var(--primary-color); }
.status-2 { background: var(--primary-bg); color: var(--primary-color); }
.status-3 { background: #f6ffed; color: var(--success-color); }
.status-4 { background: #fff2f0; color: var(--danger-color); }
.os-text { font-size: 20px; font-weight: 600; }
.os-sub { font-size: 13px; opacity: 0.7; margin-top: 2px; }
.order-product { display: flex; gap: 16px; cursor: pointer; padding: 12px; border-radius: var(--radius-md); transition: background 0.2s; }
.order-product:hover { background: var(--bg-color); }
.order-product img { width: 80px; height: 80px; border-radius: 8px; object-fit: cover; }
.op-info { flex: 1; }
.op-title { font-size: 16px; font-weight: 500; margin-bottom: 8px; }
</style>
