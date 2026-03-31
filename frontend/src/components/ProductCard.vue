<template>
  <div class="product-card card" @click="goDetail">
    <div class="card-image">
      <img :src="product.coverImage || '/placeholder.png'" :alt="product.title" loading="lazy" />
      <span v-if="product.productStatus === 3" class="sold-overlay">已售出</span>
      <span v-if="product.quality === 1" class="quality-tag new">全新</span>
      <span v-else-if="product.quality === 2" class="quality-tag near-new">几乎全新</span>
    </div>
    <div class="card-body">
      <h3 class="title">{{ product.title }}</h3>
      <div class="price-row">
        <span class="price">{{ product.price }}</span>
        <span v-if="product.originalPrice" class="price-original">¥{{ product.originalPrice }}</span>
      </div>
      <div class="card-footer">
        <div class="seller-info">
          <el-avatar :size="20" :src="product.sellerAvatar || undefined">
            {{ product.sellerNickname?.charAt(0) || '用' }}
          </el-avatar>
          <span class="seller-name">{{ product.sellerNickname || '用户' }}</span>
        </div>
        <div class="stats">
          <span v-if="product.viewCount">
            <el-icon><View /></el-icon>{{ product.viewCount }}
          </span>
          <span v-if="product.favoriteCount">
            <el-icon><Star /></el-icon>{{ product.favoriteCount }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
  product: { type: Object, required: true }
})

const router = useRouter()

function goDetail() {
  router.push(`/product/${props.product.id}`)
}
</script>

<style scoped>
.product-card {
  cursor: pointer;
  overflow: hidden;
  transition: all 0.3s ease;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.card-image {
  position: relative;
  width: 100%;
  padding-top: 100%;
  overflow: hidden;
  background: #f5f5f5;
}

.card-image img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.product-card:hover .card-image img {
  transform: scale(1.05);
}

.sold-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 600;
}

.quality-tag {
  position: absolute;
  top: 8px;
  left: 8px;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  color: #fff;
}

.quality-tag.new { background: var(--primary-color); }
.quality-tag.near-new { background: var(--success-color); }

.card-body {
  padding: 12px;
}

.title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 8px;
  min-height: 39px;
}

.price-row {
  display: flex;
  align-items: baseline;
  margin-bottom: 8px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.seller-info {
  display: flex;
  align-items: center;
  gap: 6px;
}

.seller-name {
  font-size: 12px;
  color: var(--text-tertiary);
  max-width: 70px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.stats {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: var(--text-quaternary);
}

.stats span {
  display: flex;
  align-items: center;
  gap: 2px;
}
</style>
