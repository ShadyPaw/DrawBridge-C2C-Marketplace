<template>
  <aside class="floating-panel" aria-label="快捷功能面板">
    <router-link to="/publish" class="panel-item panel-item-primary">
      <el-icon :size="20"><Plus /></el-icon>
      <span>发闲置</span>
    </router-link>

    <router-link to="/chat" class="panel-item panel-item-with-dot">
      <span class="dot" v-if="showMessageDot"></span>
      <el-icon :size="20"><ChatDotRound /></el-icon>
      <span>消息</span>
    </router-link>

    <button class="panel-item" type="button">
      <el-icon :size="20"><EditPen /></el-icon>
      <span>反馈</span>
    </button>

    <button class="panel-item" type="button">
      <el-icon :size="20"><Service /></el-icon>
      <span>客服</span>
    </button>

    <button class="panel-item" type="button" @click="scrollToTop">
      <el-icon :size="20"><ArrowUpBold /></el-icon>
      <span>顶部</span>
    </button>
  </aside>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { chatApi } from '../api'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const unreadTotal = ref(0)

const showMessageDot = computed(() => userStore.isLoggedIn && unreadTotal.value > 0)

onMounted(() => {
  if (userStore.isLoggedIn) {
    fetchUnread()
    window.addEventListener('chat-read', fetchUnread)
    window.addEventListener('new-chat-message', fetchUnread)
  }
})

onUnmounted(() => {
  window.removeEventListener('chat-read', fetchUnread)
  window.removeEventListener('new-chat-message', fetchUnread)
})

async function fetchUnread() {
  if (!userStore.isLoggedIn) {
    unreadTotal.value = 0
    return
  }
  try {
    const res = await chatApi.unreadCount()
    unreadTotal.value = res.data || 0
  } catch (e) {
    unreadTotal.value = 0
  }
}

function scrollToTop() {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}
</script>

<style scoped>
.floating-panel {
  position: fixed;
  right: 24px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 960;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  width: 88px;
  padding: 14px 10px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
}

.panel-item {
  position: relative;
  width: 100%;
  min-height: 64px;
  padding: 10px 8px;
  border: 0;
  border-radius: 12px;
  background: transparent;
  color: #1c1c1e;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 7px;
  font-size: 12px;
  font-weight: 500;
  line-height: 1.1;
  cursor: pointer;
  transition: background-color 0.2s ease, transform 0.2s ease, color 0.2s ease;
}

.panel-item:hover {
  background: #f4f5f7;
  transform: scale(1.04);
}

.panel-item-primary {
  color: #1c1c1e;
}

.panel-item-with-dot .dot {
  position: absolute;
  top: 10px;
  right: 16px;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #ff4d4f;
  box-shadow: 0 0 0 2px #fff;
}

@media (max-width: 1200px) {
  .floating-panel {
    right: 16px;
  }
}

@media (max-width: 768px) {
  .floating-panel {
    display: none;
  }
}
</style>
