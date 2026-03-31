<template>
  <div class="search-page container">
    <div class="search-header">
      <h2>搜索结果：<span class="keyword">{{ keyword }}</span></h2>
      <span class="count">共找到 {{ total }} 件商品</span>
    </div>

    <div class="filter-bar">
      <el-radio-group v-model="sortBy" size="small" @change="doSearch">
        <el-radio-button value="latest">综合排序</el-radio-button>
        <el-radio-button value="price_asc">价格最低</el-radio-button>
        <el-radio-button value="price_desc">价格最高</el-radio-button>
        <el-radio-button value="view">人气最高</el-radio-button>
      </el-radio-group>
    </div>

    <div v-if="loading" class="loading-wrap"><el-skeleton :rows="4" animated /></div>
    <div v-else-if="products.length === 0" class="empty-state">
      <el-empty description="暂无搜索结果，换个关键词试试~" />
    </div>
    <div v-else class="product-grid">
      <ProductCard v-for="item in products" :key="item.id" :product="item" />
    </div>

    <div class="pagination-wrap" v-if="total > pageSize">
      <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total" layout="prev, pager, next" @current-change="doSearch" />
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { productApi } from '../api'
import ProductCard from '../components/ProductCard.vue'

const route = useRoute()
const keyword = ref('')
const products = ref([])
const sortBy = ref('latest')
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)

watch(() => route.query.keyword, (val) => {
  if (val) {
    keyword.value = val
    pageNum.value = 1
    doSearch()
  }
}, { immediate: true })

async function doSearch() {
  if (!keyword.value) return
  loading.value = true
  try {
    const res = await productApi.search({
      keyword: keyword.value, orderBy: sortBy.value, pageNum: pageNum.value, pageSize: pageSize.value
    })
    products.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}
</script>

<style scoped>
.search-page { padding: 24px 20px; }
.search-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.search-header h2 { font-size: 20px; font-weight: 600; }
.keyword { color: var(--primary-color); }
.count { font-size: 14px; color: var(--text-tertiary); }
.filter-bar { margin-bottom: 20px; }
.pagination-wrap { display: flex; justify-content: center; margin-top: 32px; }
</style>
