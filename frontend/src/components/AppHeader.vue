<template>
  <header class="app-header">
    <div class="header-inner container">
      <!-- Logo -->
      <router-link to="/" class="logo">
        <el-icon :size="28" color="#1677ff"><Shop /></el-icon>
        <span class="logo-text">闲置集市</span>
      </router-link>

      <!-- 搜索框 -->
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索你想要的闲置好物..."
          size="large"
          clearable
          @keyup.enter="doSearch"
        >
          <template #append>
            <el-button :icon="Search" @click="doSearch" />
          </template>
        </el-input>
      </div>

      <!-- 右侧操作 -->
      <div class="header-actions">
        <router-link to="/publish" class="publish-btn" v-if="userStore.isLoggedIn">
          <el-button type="primary" round>
            <el-icon><Plus /></el-icon>
            发布闲置
          </el-button>
        </router-link>

        <template v-if="userStore.isLoggedIn">
          <router-link to="/chat" class="action-link">
            <el-badge :value="unreadTotal" :hidden="unreadTotal === 0">
              <el-icon :size="22"><ChatDotRound /></el-icon>
            </el-badge>
            <span>消息</span>
          </router-link>

          <router-link to="/my/orders" class="action-link">
            <el-badge :value="0" :hidden="true">
              <el-icon :size="22"><Document /></el-icon>
            </el-badge>
            <span>订单</span>
          </router-link>

          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-avatar-wrap">
              <el-avatar :size="32" :src="userStore.avatar || undefined">
                {{ userStore.nickname?.charAt(0) }}
              </el-avatar>
              <span class="username">{{ userStore.nickname }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="center">
                  <el-icon><User /></el-icon> 个人中心
                </el-dropdown-item>
                <el-dropdown-item command="products">
                  <el-icon><Goods /></el-icon> 我的发布
                </el-dropdown-item>
                <el-dropdown-item command="favorites">
                  <el-icon><Star /></el-icon> 我的收藏
                </el-dropdown-item>
                <el-dropdown-item command="address">
                  <el-icon><Location /></el-icon> 收货地址
                </el-dropdown-item>
                <el-dropdown-item v-if="userStore.isAdmin" command="admin" divided>
                  <el-icon><Setting /></el-icon> 管理后台
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>

        <template v-else>
          <router-link to="/login">
            <el-button type="primary" plain round>登录</el-button>
          </router-link>
          <router-link to="/register">
            <el-button round>注册</el-button>
          </router-link>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { chatApi } from '../api'
import { Search } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const searchKeyword = ref('')
const unreadTotal = ref(0)
let timer = null

onMounted(() => {
  if (userStore.isLoggedIn) {
    fetchUnread()
    timer = setInterval(fetchUnread, 30000) // 轮询最新消息条数
    userStore.connectWs()
    window.addEventListener('chat-read', fetchUnread)
    window.addEventListener('new-chat-message', fetchUnread)
  }
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  window.removeEventListener('chat-read', fetchUnread)
  window.removeEventListener('new-chat-message', fetchUnread)
  userStore.disconnectWs()
})

async function fetchUnread() {
  if (!userStore.isLoggedIn) return
  try {
    const res = await chatApi.unreadCount()
    unreadTotal.value = res.data || 0
  } catch(e) {}
}

function doSearch() {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/search', query: { keyword: searchKeyword.value.trim() } })
  }
}

function handleCommand(command) {
  switch (command) {
    case 'center': router.push('/my'); break
    case 'products': router.push('/my/products'); break
    case 'favorites': router.push('/my/favorites'); break
    case 'address': router.push('/my/address'); break
    case 'admin': router.push('/admin'); break
    case 'logout':
      ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
        .then(() => {
          userStore.logout()
          router.push('/')
        }).catch(() => {})
      break
  }
}
</script>

<style scoped>
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  height: var(--header-height);
  background: var(--bg-white);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.header-inner {
  height: 100%;
  display: flex;
  align-items: center;
  gap: 24px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
  cursor: pointer;
  transition: opacity 0.2s;
}

.logo:hover { opacity: 0.8; }

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: var(--primary-color);
  white-space: nowrap;
}

.search-box {
  flex: 1;
  max-width: 480px;
}

.search-box :deep(.el-input-group__append) {
  background: var(--primary-color);
  border-color: var(--primary-color);
  color: #fff;
  cursor: pointer;
}

.search-box :deep(.el-input-group__append:hover) {
  background: var(--primary-hover);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.action-link {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  font-size: 12px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: color 0.2s;
}

.action-link:hover { color: var(--primary-color); }

.user-avatar-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 20px;
  transition: background 0.2s;
}

.user-avatar-wrap:hover { background: var(--bg-color); }

.username {
  font-size: 14px;
  color: var(--text-primary);
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 768px) {
  .search-box { max-width: 200px; }
  .logo-text { display: none; }
  .username { display: none; }
  .publish-btn span { display: none; }
}
</style>
