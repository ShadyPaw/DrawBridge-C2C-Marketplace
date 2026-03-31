<template>
  <div>
    <h2 class="page-title">我的收藏</h2>
    <div v-if="favorites.length === 0"><el-empty description="暂无收藏，去首页逛逛吧~" /></div>
    <div v-else class="product-grid">
      <div v-for="f in favorites" :key="f.id" class="fav-item card" @click="$router.push(`/product/${f.productId}`)">
        <img :src="f.productCover || '/placeholder.png'" />
        <div class="fav-info">
          <div class="fav-title">{{ f.productTitle }}</div>
          <div class="fav-price price">{{ f.productPrice }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { favoriteApi } from '../../api'
const favorites = ref([])
onMounted(async () => { try { const res = await favoriteApi.list(); favorites.value = res.data || [] } catch(e){} })
</script>

<style scoped>
.page-title { font-size: 20px; font-weight: 600; margin-bottom: 20px; }
.fav-item { cursor: pointer; overflow: hidden; }
.fav-item img { width: 100%; aspect-ratio: 1; object-fit: cover; }
.fav-info { padding: 10px; }
.fav-title { font-size: 14px; margin-bottom: 4px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
</style>
