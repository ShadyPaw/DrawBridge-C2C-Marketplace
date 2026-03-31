<template>
  <div>
    <h2 class="page-title">我的发布</h2>
    <div v-if="products.length === 0" class="empty-state"><el-empty description="还没有发布过商品" /></div>
    <div v-else class="product-list">
      <div v-for="p in products" :key="p.id" class="product-item" @click="$router.push(`/product/${p.id}`)">
        <img :src="p.coverImage || '/placeholder.png'" class="item-img" />
        <div class="item-info">
          <div class="item-title">{{ p.title }}</div>
          <div class="item-price price">{{ p.price }}</div>
          <div class="item-meta">
            <span class="status-badge" :class="auditClass(p.auditStatus)">{{ auditText(p.auditStatus) }}</span>
            <span class="status-badge" :class="statusClass(p.productStatus)">{{ statusText(p.productStatus) }}</span>
            <span class="item-time">{{ formatTime(p.createTime) }}</span>
          </div>
        </div>
        <div class="item-actions" @click.stop>
          <el-button v-if="p.productStatus === 1" size="small" @click="changeStatus(p.id, 2)">下架</el-button>
          <el-button v-if="p.productStatus === 2" size="small" type="primary" @click="changeStatus(p.id, 1)">上架</el-button>
          <el-button size="small" type="danger" @click="handleDelete(p.id)">删除</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { productApi } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const products = ref([])

onMounted(loadProducts)

async function loadProducts() {
  try { const res = await productApi.myProducts(); products.value = res.data || [] } catch (e) {}
}

function auditText(s) { return { 0: '待审核', 1: '已通过', 2: '已拒绝' }[s] || '未知' }
function auditClass(s) { return { 0: 'warning', 1: 'success', 2: 'danger' }[s] || 'info' }
function statusText(s) { return { 1: '在售', 2: '已下架', 3: '已售出' }[s] || '未知' }
function statusClass(s) { return { 1: 'success', 2: 'warning', 3: 'info' }[s] || 'info' }

async function changeStatus(id, status) {
  try { await productApi.updateStatus(id, status); ElMessage.success('操作成功'); loadProducts() } catch (e) {}
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除此商品吗？', '提示', { type: 'warning' })
  try { await productApi.delete(id); ElMessage.success('删除成功'); loadProducts() } catch (e) {}
}

function formatTime(t) { return t ? new Date(t).toLocaleDateString('zh-CN') : '' }
</script>

<style scoped>
.page-title { font-size: 20px; font-weight: 600; margin-bottom: 20px; }
.product-list { display: flex; flex-direction: column; gap: 12px; }
.product-item { display: flex; align-items: center; gap: 16px; padding: 16px; border-radius: var(--radius-md); border: 1px solid var(--border-color); cursor: pointer; transition: all 0.2s; }
.product-item:hover { border-color: var(--primary-color); box-shadow: var(--shadow-sm); }
.item-img { width: 80px; height: 80px; border-radius: 8px; object-fit: cover; flex-shrink: 0; }
.item-info { flex: 1; }
.item-title { font-size: 15px; font-weight: 500; margin-bottom: 4px; }
.item-price { margin-bottom: 6px; }
.item-meta { display: flex; align-items: center; gap: 8px; }
.item-time { font-size: 12px; color: var(--text-quaternary); }
.item-actions { flex-shrink: 0; }
</style>
