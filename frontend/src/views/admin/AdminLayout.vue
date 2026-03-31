<template>
  <div class="admin-layout">
    <el-container>
      <el-aside width="220px">
        <div class="admin-logo"><el-icon :size="24"><Setting /></el-icon> 管理后台</div>
        <el-menu :default-active="activeMenu" router background-color="#1f1f1f" text-color="#a0a0a0" active-text-color="#1677ff">
          <el-menu-item index="/admin"><el-icon><DataAnalysis /></el-icon>控制台</el-menu-item>
          <el-menu-item index="/admin/users"><el-icon><User /></el-icon>用户管理</el-menu-item>
          <el-menu-item index="/admin/products"><el-icon><Goods /></el-icon>商品审核</el-menu-item>
          <el-menu-item index="/admin/orders"><el-icon><ShoppingCart /></el-icon>订单管理</el-menu-item>
          <el-menu-item index="/admin/reports"><el-icon><Warning /></el-icon>举报管理</el-menu-item>
          <el-menu-item index="/admin/notices"><el-icon><Bell /></el-icon>公告管理</el-menu-item>
          <el-menu-item index="/admin/categories"><el-icon><Menu /></el-icon>分类管理</el-menu-item>
          <el-divider style="border-color: #333;" />
          <el-menu-item index="/"><el-icon><Back /></el-icon>返回前台</el-menu-item>
        </el-menu>
      </el-aside>
      <el-main style="background: var(--bg-color); min-height: 100vh; padding: 24px;">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style scoped>
.admin-layout { min-height: 100vh; }
.el-aside { background: #1f1f1f; min-height: 100vh; }
.admin-logo { height: 64px; display: flex; align-items: center; justify-content: center; gap: 8px; color: #fff; font-size: 18px; font-weight: 600; border-bottom: 1px solid #333; }
:deep(.el-menu) { border-right: none; }
</style>
