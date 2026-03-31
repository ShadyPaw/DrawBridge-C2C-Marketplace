<template>
  <div>
    <h2 style="margin-bottom: 20px;">订单管理</h2>
    <div class="toolbar">
      <el-select v-model="orderStatus" placeholder="订单状态" clearable style="width: 140px" @change="loadData">
        <el-option label="待付款" :value="0" /><el-option label="待发货" :value="1" /><el-option label="待收货" :value="2" />
        <el-option label="已完成" :value="3" /><el-option label="已取消" :value="4" />
      </el-select>
      <el-input v-model="keyword" placeholder="搜索订单号/商品标题" clearable @clear="loadData" style="width: 280px" />
      <el-button type="primary" @click="loadData">搜索</el-button>
    </div>
    <el-table :data="list" border stripe style="width: 100%; margin-top: 16px;">
      <el-table-column prop="orderNo" label="订单编号" width="220" show-overflow-tooltip />
      <el-table-column prop="productTitle" label="商品名称" min-width="160" show-overflow-tooltip />
      <el-table-column prop="price" label="金额" width="100"><template #default="{ row }">¥{{ row.price }}</template></el-table-column>
      <el-table-column prop="buyerNickname" label="买家" width="100" />
      <el-table-column prop="sellerNickname" label="卖家" width="100" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusType(row.orderStatus)" size="small">{{ statusText(row.orderStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="170" />
    </el-table>
    <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total" layout="total, prev, pager, next" @current-change="loadData" style="margin-top: 16px; justify-content: flex-end;" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '../../api'
const list = ref([]); const keyword = ref(''); const orderStatus = ref(null); const pageNum = ref(1); const pageSize = ref(10); const total = ref(0)
onMounted(loadData)
async function loadData() { try { const r = await adminApi.orderList({ keyword: keyword.value, orderStatus: orderStatus.value, pageNum: pageNum.value, pageSize: pageSize.value }); list.value = r.data.list || []; total.value = r.data.total || 0 } catch(e){} }
function statusText(s) { return { 0:'待付款',1:'待发货',2:'待收货',3:'已完成',4:'已取消',5:'退款中',6:'已退款' }[s] || '未知' }
function statusType(s) { return { 0:'warning',1:'info',2:'',3:'success',4:'danger',5:'warning',6:'info' }[s] || 'info' }
</script>

<style scoped>.toolbar { display: flex; gap: 12px; }</style>
