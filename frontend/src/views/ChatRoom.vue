<template>
  <div class="chat-page container">
    <div class="chat-layout card">
      <!-- 左侧：联系人列表 -->
      <div class="contact-panel">
        <div class="panel-header">
          <el-icon :size="18"><ChatDotRound /></el-icon>
          <span>消息</span>
        </div>
        <div class="contact-list">
          <div v-if="contacts.length === 0" class="contact-empty">
            暂无聊天记录
          </div>
          <div
            v-for="c in contacts" :key="contactKey(c)"
            class="contact-item"
            :class="{ active: isActiveContact(c) }"
            @click="switchContact(c)"
          >
            <el-avatar :size="42" :src="getContactAvatar(c) || undefined">
              {{ getContactName(c)?.charAt(0) || '?' }}
            </el-avatar>
            <div class="contact-info">
              <div class="contact-name">{{ getContactName(c) }}</div>
              <div class="contact-last-msg">{{ c.content }}</div>
            </div>
            <div class="contact-meta">
              <span class="contact-time">{{ formatTime(c.createTime) }}</span>
              <el-badge class="contact-badge" :value="c.unreadCount" :hidden="!c.unreadCount || c.unreadCount === 0" type="danger" />
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：聊天面板 -->
      <div class="chat-panel" v-if="currentTargetId">
        <!-- 聊天顶部信息栏 -->
        <div class="chat-header">
          <div class="chat-target-info">
            <span class="chat-target-name">{{ currentTargetName }}</span>
          </div>
        </div>

        <!-- 商品上下文卡片 -->
        <div class="product-context" v-if="currentProduct" @click="goProduct">
          <img :src="currentProduct.coverImage || currentProduct.productCoverImage || ''" class="product-thumb" />
          <div class="product-brief">
            <div class="product-title-text">{{ currentProduct.title || currentProduct.productTitle }}</div>
            <div class="product-price price">{{ currentProduct.price || currentProduct.productPrice || '0.00' }}</div>
          </div>
          <el-icon class="product-arrow"><ArrowRight /></el-icon>
        </div>

        <!-- 消息列表 -->
        <div class="message-list" ref="messageListRef">
          <div v-if="messages.length === 0" class="empty-chat">
            开始你们的对话吧 👋
          </div>
          <div
            v-for="msg in messages" :key="msg.id"
            class="message-row"
            :class="{ 'is-self': msg.senderId === myUserId }"
          >
            <el-avatar
              :size="36"
              :src="msg.senderId === myUserId ? myAvatar : currentTargetAvatar || undefined"
            >
              {{ msg.senderId === myUserId ? myNickname?.charAt(0) : currentTargetName?.charAt(0) }}
            </el-avatar>
            <div class="bubble-wrapper">
              <div class="bubble">{{ msg.content }}</div>
              <div v-if="msg.senderId === myUserId" class="read-status" :class="{ 'is-read': msg.isRead === 1 }">
                {{ msg.isRead === 1 ? '已读' : '未读' }}
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区 -->
        <div class="chat-input-area">
          <el-input
            v-model="inputText"
            placeholder="输入消息..."
            @keydown.enter.prevent="sendMessage"
            :rows="1"
            resize="none"
            autofocus
          />
          <el-button type="primary" @click="sendMessage" :disabled="!inputText.trim()">
            发送
          </el-button>
        </div>
      </div>

      <!-- 未选择联系人 -->
      <div class="chat-panel chat-placeholder" v-else>
        <el-empty description="选择一个联系人开始聊天" :image-size="120" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { chatApi, productApi, userApi } from '../api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const myUserId = computed(() => userStore.userId)
const myNickname = computed(() => userStore.nickname)
const myAvatar = computed(() => userStore.avatar)

const contacts = ref([])
const messages = ref([])
const inputText = ref('')
const messageListRef = ref(null)

// 当前聊天对象
const currentTargetId = ref(null)
const currentTargetName = ref('')
const currentTargetAvatar = ref('')
const currentProductId = ref(null)
const currentProduct = ref(null)

// ==================== 生命周期 ====================
onMounted(async () => {
  await loadContacts()
  window.addEventListener('new-chat-message', handleGlobalMessage)
  window.addEventListener('chat-read-receipt', handleReadReceipt)

  // 如果从商品详情页跳转过来，自动打开对应会话
  if (route.query.userId) {
    currentTargetId.value = Number(route.query.userId)
    currentProductId.value = route.query.productId ? Number(route.query.productId) : null

    // 加载对方用户信息
    try {
      const res = await userApi.getDetail(currentTargetId.value)
      currentTargetName.value = res.data.nickname
      currentTargetAvatar.value = res.data.avatar
    } catch (e) {}

    // 加载商品上下文
    if (currentProductId.value) {
      try {
        const res = await productApi.detail(currentProductId.value)
        currentProduct.value = res.data
      } catch (e) {}
    }

    await loadHistory()
  }
})

onUnmounted(() => {
  window.removeEventListener('new-chat-message', handleGlobalMessage)
  window.removeEventListener('chat-read-receipt', handleReadReceipt)
})

// ==================== WebSocket ====================
function handleGlobalMessage(event) {
  try {
    const msg = event.detail
    // 只处理当前会话（或对方发来的消息）
    const isCurrentConversation =
      (msg.senderId === currentTargetId.value && msg.receiverId === myUserId.value) ||
      (msg.senderId === myUserId.value && msg.receiverId === currentTargetId.value)

    if (isCurrentConversation) {
      if (!messages.value.find(m => m.id === msg.id)) {
        if (msg.isRead === undefined) msg.isRead = 0 // 后端 websocket 返回时若没有携带 isRead 属性，默认为 0
        messages.value.push(msg)
        scrollToBottom()
        
        // 标记已读并通知全局去红点
        if (msg.senderId === currentTargetId.value) {
          chatApi.markRead(currentTargetId.value).then(() => {
            window.dispatchEvent(new Event('chat-read'))
          }).catch(() => {})
        }
      }
    }
    // 更新联系人列表
    loadContacts()
  } catch (e) {}
}

function handleReadReceipt(event) {
  try {
    const msg = event.detail
    // 对方（readerId）告诉我们，它已经读了我们的消息
    if (msg.readerId === currentTargetId.value) {
      messages.value.forEach(m => {
        // 只要不是 1（已读），都强制同步为已读
        if (m.senderId === myUserId.value && m.isRead !== 1) {
          m.isRead = 1
        }
      })
    }
  } catch(e) {}
}

// ==================== 数据加载 ====================
async function loadContacts() {
  try {
    const res = await chatApi.contacts()
    contacts.value = res.data || []
  } catch (e) {}
}

async function loadHistory() {
  if (!currentTargetId.value) return
  try {
    const params = { targetUserId: currentTargetId.value }
    if (currentProductId.value) params.productId = currentProductId.value
    const res = await chatApi.history(params)
    messages.value = res.data || []
    scrollToBottom()
    
    // 标记当前聊天对象发来的消息为已读，并通知全局立刻刷新右上角数字
    await chatApi.markRead(currentTargetId.value)
    window.dispatchEvent(new Event('chat-read'))
  } catch (e) {}
}

// ==================== 交互操作 ====================
function sendMessage() {
  if (!inputText.value.trim()) return

  const msgPayload = {
    type: 'CHAT',
    receiverId: currentTargetId.value,
    productId: currentProductId.value,
    content: inputText.value.trim()
  }

  userStore.sendWsMessage(msgPayload)

  // 移除了前端直接本地追加消息的手动操作，完全依赖 WebSocket 后端发回的确认消息，
  // 这样既能保证消息入库真实生成了ID，又自动实现了去重和排序，防止显示出重复的两条消息。
  
  inputText.value = ''
}

async function switchContact(contact) {
  const targetId = contact.senderId === myUserId.value ? contact.receiverId : contact.senderId
  currentTargetId.value = targetId
  currentTargetName.value = contact.senderId === myUserId.value
    ? contact.receiverNickname : contact.senderNickname
  currentTargetAvatar.value = contact.senderId === myUserId.value
    ? contact.receiverAvatar : contact.senderAvatar
  currentProductId.value = contact.productId
  currentProduct.value = contact.productTitle ? {
    productTitle: contact.productTitle,
    productPrice: contact.productPrice,
    productCoverImage: contact.productCoverImage
  } : null

  // 乐观地把左侧列表里的未读数清零，让数字立马消失
  contact.unreadCount = 0

  await loadHistory()
}

function goProduct() {
  if (currentProductId.value) {
    router.push(`/product/${currentProductId.value}`)
  }
}

// ==================== 辅助方法 ====================
function getContactName(c) {
  return c.senderId === myUserId.value ? c.receiverNickname : c.senderNickname
}
function getContactAvatar(c) {
  return c.senderId === myUserId.value ? c.receiverAvatar : c.senderAvatar
}
function contactKey(c) {
  const other = c.senderId === myUserId.value ? c.receiverId : c.senderId
  return `${other}`
}
function isActiveContact(c) {
  const other = c.senderId === myUserId.value ? c.receiverId : c.senderId
  return other === currentTargetId.value
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  if (d.toDateString() === now.toDateString()) {
    return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  return d.toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' })
}

function scrollToBottom() {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}
</script>

<style scoped>
.chat-page { padding: 20px; height: calc(100vh - 120px); }
.chat-layout { display: flex; height: 100%; overflow: hidden; }

/* ====== 左侧联系人 ====== */
.contact-panel {
  width: 280px; border-right: 1px solid var(--border-color);
  display: flex; flex-direction: column; flex-shrink: 0;
}
.panel-header {
  padding: 16px 20px; font-size: 16px; font-weight: 600;
  display: flex; align-items: center; gap: 8px;
  border-bottom: 1px solid var(--border-color);
}
.contact-list { flex: 1; overflow-y: auto; }
.contact-empty { padding: 40px 20px; text-align: center; color: var(--text-quaternary); font-size: 14px; }
.contact-item {
  display: flex; align-items: center; gap: 12px;
  padding: 14px 16px; cursor: pointer; transition: background 0.15s;
  border-bottom: 1px solid var(--border-light, #f5f5f5);
}
.contact-item:hover { background: var(--bg-color); }
.contact-item.active { background: #e6f4ff; }
.contact-info { flex: 1; min-width: 0; }
.contact-name { font-size: 14px; font-weight: 500; margin-bottom: 3px; }
.contact-last-msg {
  font-size: 12px; color: var(--text-tertiary);
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.contact-meta { display: flex; flex-direction: column; align-items: flex-end; gap: 6px; flex-shrink: 0; }
.contact-time { font-size: 11px; color: var(--text-quaternary); }
.contact-badge :deep(.el-badge__content) { transform: scale(0.9); transform-origin: right top; border: none; }

/* ====== 右侧聊天面板 ====== */
.chat-panel { flex: 1; display: flex; flex-direction: column; min-width: 0; }
.chat-placeholder { align-items: center; justify-content: center; }

.chat-header {
  padding: 14px 20px; border-bottom: 1px solid var(--border-color);
  display: flex; align-items: center;
}
.chat-target-name { font-size: 16px; font-weight: 600; }

/* 商品上下文卡片 */
.product-context {
  display: flex; align-items: center; gap: 12px;
  padding: 10px 20px; background: #f7f9fc; cursor: pointer;
  border-bottom: 1px solid var(--border-color); transition: background 0.15s;
}
.product-context:hover { background: #eef2f8; }
.product-thumb { width: 44px; height: 44px; border-radius: 6px; object-fit: cover; flex-shrink: 0; }
.product-brief { flex: 1; min-width: 0; }
.product-title-text { font-size: 13px; font-weight: 500; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.product-price { font-size: 13px; margin-top: 2px; }
.product-arrow { color: var(--text-quaternary); }

/* 消息列表 */
.message-list {
  flex: 1; overflow-y: auto; padding: 20px;
  display: flex; flex-direction: column; gap: 16px;
}
.empty-chat { text-align: center; color: var(--text-quaternary); padding: 60px 0; font-size: 15px; }

.message-row { display: flex; align-items: flex-start; gap: 10px; max-width: 75%; }
.message-row.is-self { align-self: flex-end; flex-direction: row-reverse; }

.bubble-wrapper { display: flex; flex-direction: column; align-items: flex-start; gap: 4px; }
.message-row.is-self .bubble-wrapper { align-items: flex-end; }

.read-status { font-size: 11px; color: var(--text-quaternary); padding: 0 4px; }
.read-status.is-read { color: var(--primary-color); }

.bubble {
  padding: 10px 14px; border-radius: 12px; font-size: 14px; line-height: 1.5;
  word-break: break-word; background: #fff; border: 1px solid var(--border-color);
  box-shadow: 0 1px 2px rgba(0,0,0,0.04);
}
.message-row.is-self .bubble {
  background: #1677ff; color: #fff; border-color: #1677ff;
}

/* 输入区 */
.chat-input-area {
  padding: 14px 20px; border-top: 1px solid var(--border-color);
  display: flex; gap: 10px; align-items: center; background: #fafbfc;
}
.chat-input-area .el-input { flex: 1; }

/* 响应式 */
@media (max-width: 768px) {
  .contact-panel { width: 72px; }
  .contact-info, .contact-meta { display: none; }
  .contact-item { justify-content: center; padding: 12px 8px; }
}
</style>
