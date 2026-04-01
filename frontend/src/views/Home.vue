<template>
  <div class="home-page">
    <!-- 系统公告 -->
    <section class="notice-section container" v-if="notices.length > 0">
      <el-carousel height="44px" direction="vertical" :autoplay="true" indicator-position="none" class="notice-carousel card">
        <el-carousel-item v-for="notice in notices" :key="notice.id">
          <div class="notice-content">
            <el-tag size="small" type="danger" effect="dark">系统公告</el-tag>
            <span class="notice-title">{{ notice.title }}</span>
            <span class="notice-time">{{ new Date(notice.createTime).toLocaleDateString() }}</span>
          </div>
        </el-carousel-item>
      </el-carousel>
    </section>

    <!-- 分类导航 -->
    <section class="category-section">
      <div class="container">
        <div class="category-bar">
          <div
            v-for="cat in categories"
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
            {{ selectedCategory ? '分类商品' : '最新发布' }}
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

        <div v-else class="product-grid">
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
import { ref, onMounted } from 'vue'
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

onMounted(() => {
  loadNotices()
  loadCategories()
  loadProducts()
})

async function loadNotices() {
  try {
    const res = await noticeApi.list()
    // 只取最新的 5 条
    notices.value = (res.data || []).slice(0, 5)
  } catch (e) { console.error(e) }
}

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
  padding: 16px 0;
  margin-bottom: 24px;
  box-shadow: var(--shadow-sm);
}

.category-bar {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 4px;
}

.category-bar::-webkit-scrollbar { height: 0; }

.category-item {
  flex-shrink: 0;
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 14px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s;
  background: var(--bg-color);
}

.category-item:hover {
  color: var(--primary-color);
  background: var(--primary-bg);
}

.category-item.active {
  color: #fff;
  background: var(--primary-color);
}

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
  color: var(--text-primary);
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
