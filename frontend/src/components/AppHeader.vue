<template>
  <header class="app-header">
    <div class="top-notice-bar" v-if="currentNotice">
      <div class="top-notice-inner container">
        <span class="notice-text">
          <span class="notice-tag">系统公告</span>
          {{ currentNotice.title }}
        </span>
      </div>
    </div>

    <div class="header-nav">
      <div class="header-inner container">
        <router-link to="/" class="logo">
          <el-icon :size="28" color="#1C1C1E"><Shop /></el-icon>
          <span class="logo-text">闲置集市</span>
        </router-link>

        <div class="header-main">
          <div class="search-shell">
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
          </div>
        </div>

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
                <el-icon :size="21"><ChatDotRound /></el-icon>
              </el-badge>
              <span>消息</span>
            </router-link>

            <router-link to="/my/orders" class="action-link">
              <el-badge :value="0" :hidden="true">
                <el-icon :size="21"><Document /></el-icon>
              </el-badge>
              <span>订单</span>
            </router-link>

            <el-dropdown trigger="click" @command="handleCommand">
              <div class="user-avatar-wrap">
                <el-avatar :size="34" :src="userStore.avatar || undefined">
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
            <router-link to="/login" class="auth-link">
              <el-button type="primary" plain round>登录</el-button>
            </router-link>
            <router-link to="/register" class="auth-link">
              <el-button round>注册</el-button>
            </router-link>
          </template>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { chatApi, noticeApi } from '../api'
import { Search } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const searchKeyword = ref('')
const unreadTotal = ref(0)
const notices = ref([])
let timer = null

const currentNotice = computed(() => notices.value[0] || null)

onMounted(() => {
  loadNotices()
  if (userStore.isLoggedIn) {
    fetchUnread()
    timer = setInterval(fetchUnread, 30000)
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

async function loadNotices() {
  try {
    const res = await noticeApi.list()
    notices.value = (res.data || []).slice(0, 5)
  } catch (e) {
    console.error(e)
  }
}

async function fetchUnread() {
  if (!userStore.isLoggedIn) return
  try {
    const res = await chatApi.unreadCount()
    unreadTotal.value = res.data || 0
  } catch (e) {}
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
  position: sticky;
  top: 0;
  z-index: 1000;
  background: #fff;
  color: var(--main-text-color);
  border-bottom: 1px solid rgba(17, 24, 39, 0.06);
}

.top-notice-bar {
  height: 36px;
  background: #ebecef;
  border-bottom: 1px solid rgba(17, 24, 39, 0.04);
}

.top-notice-inner {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.notice-text {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  max-width: min(100%, 760px);
  font-size: 11px;
  line-height: 1;
  color: #4b5563;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.notice-tag {
  flex-shrink: 0;
  color: #1c1c1e;
  font-weight: 600;
}

.header-nav {
  background: #fff;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.04);
}

.header-inner {
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 28px;
}

.logo {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 10px;
  flex-shrink: 0;
  min-width: fit-content;
  cursor: pointer;
  transition: opacity 0.2s;
}

.logo:hover {
  opacity: 0.82;
}

.logo-text {
  font-size: 21px;
  font-weight: 700;
  letter-spacing: 0.01em;
  color: #1c1c1e;
  white-space: nowrap;
}

.header-main {
  flex: 1;
  min-width: 320px;
  display: flex;
  align-items: center;
}

.search-shell {
  width: 100%;
  max-width: 680px;
}

.search-box {
  width: 100%;
}

.search-box :deep(.el-input) {
  width: 100%;
  height: 48px;
}

.search-box :deep(.el-input__wrapper) {
  background: #f4f5f7;
  border: 0;
  border-radius: 24px 0 0 24px;
  box-shadow: none !important;
  padding-left: 18px;
  padding-right: 10px;
}

.search-box :deep(.el-input__wrapper.is-focus) {
  background: #f4f5f7;
  box-shadow: none !important;
}

.search-box :deep(.el-input-group__append) {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 52px;
  padding: 0 12px 0 10px;
  border: 0;
  border-radius: 0 24px 24px 0;
  background: #1c1c1e;
  color: #fff;
  box-shadow: none;
}

.search-box :deep(.el-input-group__append:hover) {
  background: #1c1c1e;
}

.search-box :deep(.el-input-group__append .el-button) {
  min-width: 24px;
  min-height: 48px;
  padding: 0;
  border: 0;
  background: transparent;
  color: inherit;
}

.search-box :deep(.el-input__inner) {
  color: #111827;
  font-size: 15px;
}

.search-box :deep(.el-input__inner::placeholder) {
  color: #8e939c;
}

.publish-btn {
  flex-shrink: 0;
}

.publish-btn :deep(.el-button) {
  height: 44px;
  padding: 0 18px;
  border: 0;
  border-radius: 22px;
  background: #1c1c1e;
  box-shadow: none;
  font-weight: 600;
}

.publish-btn :deep(.el-button:hover) {
  background: #2a2a2d;
}

.header-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 18px;
  flex-shrink: 0;
  min-width: fit-content;
}

.action-link {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-height: 40px;
  padding: 0 2px;
  font-size: 12px;
  color: #4b5563;
  cursor: pointer;
  transition: color 0.2s;
}

.action-link:hover {
  color: #1c1c1e;
}

.user-avatar-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  min-height: 44px;
  padding: 0 12px;
  border-radius: 22px;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}

.user-avatar-wrap:hover {
  background: #f4f5f7;
  color: #1c1c1e;
}

.username {
  max-width: 96px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
  color: var(--main-text-color);
}

.auth-link :deep(.el-button) {
  height: 42px;
  padding: 0 18px;
  border-radius: 21px;
}

@media (max-width: 768px) {
  .header-inner {
    height: auto;
    gap: 12px;
    flex-wrap: wrap;
    padding: 14px 0;
  }

  .header-main {
    order: 3;
    width: 100%;
    min-width: 0;
  }

  .search-shell {
    max-width: 100%;
  }

  .header-actions {
    gap: 12px;
    margin-left: auto;
  }

  .publish-btn {
    order: -1;
  }

  .logo-text {
    display: none;
  }

  .username {
    display: none;
  }

  .publish-btn span {
    display: none;
  }

  .notice-text {
    max-width: 100%;
  }
}
</style>
