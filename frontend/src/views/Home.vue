<template>
  <div class="home-page">


    <!-- 分类导航 -->
    <section class="category-section">
      <div class="container">
        <div class="category-bar">
          <div
            v-for="cat in displayCategories"
            :key="cat.id"
            class="category-item"
            :class="{ active: selectedCategory === cat.id }"
            @click="selectCategory(cat.id)"
          >
            {{ cat.name }}
          </div>
        </div>
      </div>
    </section>

    <!-- 推荐商品 -->
    <section class="products-section">
      <div class="container">
        <div class="section-header">
          <h2>
            <el-icon><Goods /></el-icon>
            {{ sectionTitle }}
          </h2>
          <div class="sort-options">
            <el-radio-group v-model="sortBy" size="small" @change="loadProducts">
              <el-radio-button value="latest">最新</el-radio-button>
              <el-radio-button value="price_asc">价格↑</el-radio-button>
              <el-radio-button value="price_desc">价格↓</el-radio-button>
              <el-radio-button value="view">最热</el-radio-button>
            </el-radio-group>
          </div>
        </div>

        <div v-if="loading" class="loading-wrap">
          <el-skeleton :rows="4" animated />
        </div>

        <div v-else-if="products.length === 0" class="empty-state">
          <el-empty description="暂无商品，快去发布第一件闲置吧~" />
        </div>

        <div v-else class="product-list-container">
          <ProductCard v-for="item in products" :key="item.id" :product="item" />
        </div>

        <!-- 分页 -->
        <div class="pagination-wrap" v-if="total > pageSize">
          <el-pagination
            v-model:current-page="pageNum"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="loadProducts"
          />
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { productApi, categoryApi, noticeApi } from '../api'
import ProductCard from '../components/ProductCard.vue'

const categories = ref([])
const products = ref([])
const notices = ref([])
const selectedCategory = ref(null)
const sortBy = ref('latest')
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)

function normalizeCategoryName(name) {
  return (name || '').replace(/市集$/u, '').replace(/商品$/u, '').trim()
}

const displayCategories = computed(() => {
  return categories.value.map(cat => ({
    ...cat,
    name: cat.id === null ? cat.name : normalizeCategoryName(cat.name)
  }))
})

const selectedCategoryName = computed(() => {
  if (selectedCategory.value == null) return ''
  return displayCategories.value.find(cat => cat.id === selectedCategory.value)?.name || ''
})

const sectionTitle = computed(() => {
  return selectedCategoryName.value || '最新发布'
})

onMounted(() => {
  loadNotices()
  loadCategories()
  loadProducts()
})

async function loadNotices() {}

async function loadCategories() {
  try {
    const res = await categoryApi.list()
    // 只取一级分类
    categories.value = [{ id: null, name: '全部' }, ...res.data.filter(c => c.parentId === 0)]
  } catch (e) { console.error(e) }
}

async function loadProducts() {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      orderBy: sortBy.value
    }
    if (selectedCategory.value) params.categoryId = selectedCategory.value
    const res = await productApi.list(params)
    products.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function selectCategory(id) {
  selectedCategory.value = id
  pageNum.value = 1
  loadProducts()
}
</script>

<style scoped>
.home-page {
  padding-bottom: 40px;
}

/* 公告栏 */
.notice-section {
  padding-top: 16px;
}

.notice-carousel {
  border-radius: var(--radius-md);
  cursor: pointer;
  background-color: var(--page-bg-color); /* 浅灰色底色 */
}

.notice-content {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 0 20px;
  gap: 12px;
}

.notice-title {
  flex: 1;
  font-size: 14px;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.notice-time {
  font-size: 12px;
  color: var(--text-tertiary);
}

.category-section {
  background: var(--bg-white);
  padding: 12px 0 8px;
  margin-bottom: 24px;
}

.category-bar {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  padding: 12px 0;
}

.category-bar::-webkit-scrollbar { height: 0; }

.category-item {
  flex-shrink: 0;
  padding: 8px 4px;
  font-size: 15px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
  background: transparent;
  position: relative;
}

.category-item.active {
  color: #1C1C1E;
  font-weight: bold;
}

.category-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background-color: #1C1C1E;
  border-radius: 2px;
}

.category-item:hover {
  color: #1C1C1E;
}

/* 1. 彻底抛弃死板的 Grid，使用响应式瀑布流/自适应网格 */
.product-list-container {
  column-count: 4; /* 桌面端4列，可视情况调整 */
  column-gap: 20px; /* 列间距 */
  padding: 24px;
  width: 100%;
  box-sizing: border-box;
}
@media (max-width: 1200px) { .product-list-container { column-count: 3; } }
@media (max-width: 768px) { .product-list-container { column-count: 2; } }



.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  font-size: 20px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--main-text-color);
}

.loading-wrap {
  padding: 40px 0;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}
</style>


