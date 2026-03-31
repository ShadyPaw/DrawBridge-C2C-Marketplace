<template>
  <div class="user-profile container" v-if="user">
    <div class="profile-header card" style="padding: 32px; text-align: center;">
      <el-avatar :size="80" :src="user.avatar || undefined">{{ user.nickname?.charAt(0) }}</el-avatar>
      <h2>{{ user.nickname }}</h2>
      <div class="profile-stats">
        <span>信用积分: <b>{{ user.creditScore }}</b></span>
        <el-divider direction="vertical" />
        <span>等级: <b>{{ levelText }}</b></span>
      </div>
    </div>
    <h3 style="margin: 24px 0 16px;">TA的商品</h3>
    <div v-if="products.length === 0"><el-empty description="暂无商品" /></div>
    <div v-else class="product-grid">
      <ProductCard v-for="p in products" :key="p.id" :product="p" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { userApi, productApi } from '../api'
import ProductCard from '../components/ProductCard.vue'

const route = useRoute()
const user = ref(null)
const products = ref([])
const levelMap = { 1: '普通用户', 2: '信用良好', 3: '优质用户' }
const levelText = computed(() => levelMap[user.value?.userLevel] || '普通用户')

onMounted(async () => {
  try {
    const r = await userApi.getDetail(route.params.id); user.value = r.data
    const p = await productApi.list({ userId: route.params.id, pageNum: 1, pageSize: 50 })
    products.value = p.data.list || []
  } catch(e){}
})
</script>

<style scoped>
.user-profile { padding: 24px 20px; }
.profile-header h2 { margin-top: 12px; font-size: 22px; }
.profile-stats { margin-top: 8px; color: var(--text-tertiary); }
.profile-stats b { color: var(--primary-color); }
</style>
