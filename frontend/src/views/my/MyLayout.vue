<template>
  <div class="my-layout container">
    <div class="my-sidebar">
      <div class="user-card">
        <el-avatar :size="64" :src="userStore.avatar || undefined">{{ userStore.nickname?.charAt(0) }}</el-avatar>
        <div class="user-meta">
          <div class="name">{{ userStore.nickname }}</div>
        </div>
      </div>
      <el-menu :default-active="activeMenu" router>
        <el-menu-item index="/my"><el-icon><User /></el-icon>个人中心</el-menu-item>
        <el-menu-item index="/my/products"><el-icon><Goods /></el-icon>我的发布</el-menu-item>
        <el-menu-item index="/my/orders"><el-icon><ShoppingCart /></el-icon>我买到的</el-menu-item>
        <el-menu-item index="/my/sold"><el-icon><Sell /></el-icon>我卖出的</el-menu-item>
        <el-menu-item index="/my/favorites"><el-icon><Star /></el-icon>我的收藏</el-menu-item>
        <el-menu-item index="/my/address"><el-icon><Location /></el-icon>收货地址</el-menu-item>
        <el-menu-item index="/my/settings"><el-icon><Setting /></el-icon>账号设置</el-menu-item>
      </el-menu>
    </div>
    <div class="my-content">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '../../stores/user'

const route = useRoute()
const userStore = useUserStore()
const activeMenu = computed(() => route.path)
</script>

<style scoped>
.my-layout { display: flex; gap: 24px; padding: 24px 20px; min-height: 70vh; }
.my-sidebar { width: 220px; flex-shrink: 0; }
.user-card { background: var(--bg-white); border-radius: var(--radius-lg); padding: 24px; text-align: center; margin-bottom: 16px; box-shadow: var(--shadow-sm); }
.user-meta { margin-top: 12px; }
.user-meta .name { font-size: 16px; font-weight: 600; }
.user-meta .name { font-size: 16px; font-weight: 600; }
.my-sidebar :deep(.el-menu) { border: none; background: var(--bg-white); border-radius: var(--radius-lg); box-shadow: var(--shadow-sm); }
.my-content { flex: 1; background: var(--bg-white); border-radius: var(--radius-lg); padding: 24px; box-shadow: var(--shadow-sm); }
@media (max-width: 768px) { .my-layout { flex-direction: column; } .my-sidebar { width: 100%; } }
</style>
