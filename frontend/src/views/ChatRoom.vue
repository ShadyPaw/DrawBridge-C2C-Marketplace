<template>
  <div class="chat-page container">
    <div class="chat-shell">
      <div class="chat-titlebar">
        <div class="chat-title">
          <el-icon :size="18"><ChatDotRound /></el-icon>
          <span>消息中心</span>
        </div>
      </div>

      <div class="chat-layout">
        <div class="contact-panel">
          <div class="contact-list">
            <div v-if="contacts.length === 0" class="contact-empty">
              <el-empty description="暂无聊天记录" :image-size="88" />
            </div>

            <div
              v-for="c in contacts"
              :key="contactKey(c)"
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

        <div class="chat-panel" v-if="currentTargetId">
          <div class="chat-header">
            <div class="chat-target-info">
              <span class="chat-target-name">{{ currentTargetName }}</span>
              <span class="chat-target-subtitle">与对方继续这段交易沟通</span>
            </div>
            <div class="chat-header-actions">
              <el-button link @click="showReportDialog = true" class="report-header-btn">
                <el-icon><Warning /></el-icon> 举报对方
              </el-button>
            </div>
          </div>

          <!-- 聊天举报弹窗 -->
          <el-dialog v-model="showReportDialog" title="举报聊天内容" width="420px" class="drawbridge-dialog">
            <el-form label-position="top">
              <el-form-item label="举报理由">
                <el-select v-model="reportForm.reasonType" placeholder="如骚扰谩骂、垃圾广告等" style="width: 100%">
                  <el-option label="言语骚扰/辱骂" :value="15" />
                  <el-option label="广告推销/垃圾信息" :value="16" />
                  <el-option label="诱导站外交易（离线支付等）" :value="17" />
                  <el-option label="色情低俗" :value="18" />
                  <el-option label="欺诈/虚假信息" :value="19" />
                  <el-option label="其他违规内容" :value="20" />
                </el-select>
              </el-form-item>
              <el-form-item label="详情描述">
                <el-input v-model="reportForm.reasonDetail" type="textarea" :rows="3" placeholder="请详细说明违规情况，方便后台调取证据..." />
              </el-form-item>
            </el-form>
            <template #footer>
              <el-button @click="showReportDialog = false" round>取消</el-button>
              <el-button type="primary" @click="submitReport" :loading="submittingReport" class="report-submit-btn">立即提交</el-button>
            </template>
          </el-dialog>

          <div class="product-context" v-if="currentProduct" @click="goProduct">
            <img :src="currentProduct.coverImage || currentProduct.productCoverImage || ''" class="product-thumb" />
            <div class="product-brief">
              <div class="product-title-text">{{ currentProduct.title || currentProduct.productTitle }}</div>
              <div class="product-price price">{{ currentProduct.price || currentProduct.productPrice || '0.00' }}</div>
            </div>
            <el-icon class="product-arrow"><ArrowRight /></el-icon>
          </div>

          <div class="message-list" ref="messageListRef">
            <div v-if="messages.length === 0" class="empty-chat">
              <div class="empty-chat-card">
                <el-icon :size="28"><ChatDotRound /></el-icon>
                <p>还没有消息内容</p>
                <span>发出第一句问候，让交易更快推进</span>
              </div>
            </div>

            <div
              v-for="msg in messages"
              :key="msg.id"
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

          <div class="chat-input-area">
            <el-input
              v-model="inputText"
              placeholder="输入消息，和对方聊聊商品细节..."
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

        <div class="chat-panel chat-placeholder" v-else>
          <div class="placeholder-card">
            <el-icon :size="30"><ChatDotRound /></el-icon>
            <h3>选择一个联系人开始聊天</h3>
            <p>左侧会展示最近的交易对话，点开后就可以继续沟通。</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { chatApi, productApi, userApi, reportApi } from '../api'
import { ElMessage } from 'element-plus'

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

const currentTargetId = ref(null)
const currentTargetName = ref('')
const currentTargetAvatar = ref('')
const currentProductId = ref(null)
const currentProduct = ref(null)

onMounted(async () => {
  await loadContacts()
  window.addEventListener('new-chat-message', handleGlobalMessage)
  window.addEventListener('chat-read-receipt', handleReadReceipt)

  if (route.query.userId) {
    currentTargetId.value = Number(route.query.userId)
    currentProductId.value = route.query.productId ? Number(route.query.productId) : null

    try {
      const res = await userApi.getDetail(currentTargetId.value)
      currentTargetName.value = res.data.nickname
      currentTargetAvatar.value = res.data.avatar
    } catch (e) {}

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

function handleGlobalMessage(event) {
  try {
    const msg = event.detail
    const isCurrentConversation =
      (msg.senderId === currentTargetId.value && msg.receiverId === myUserId.value) ||
      (msg.senderId === myUserId.value && msg.receiverId === currentTargetId.value)

    if (isCurrentConversation) {
      if (!messages.value.find(m => m.id === msg.id)) {
        if (msg.isRead === undefined) msg.isRead = 0
        messages.value.push(msg)
        scrollToBottom()

        if (msg.senderId === currentTargetId.value) {
          chatApi.markRead(currentTargetId.value).then(() => {
            window.dispatchEvent(new Event('chat-read'))
          }).catch(() => {})
        }
      }
    }

    loadContacts()
  } catch (e) {}
}

function handleReadReceipt(event) {
  try {
    const msg = event.detail
    if (msg.readerId === currentTargetId.value) {
      messages.value.forEach(m => {
        if (m.senderId === myUserId.value && m.isRead !== 1) {
          m.isRead = 1
        }
      })
    }
  } catch (e) {}
}

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

    await chatApi.markRead(currentTargetId.value)
    window.dispatchEvent(new Event('chat-read'))
  } catch (e) {}
}

function sendMessage() {
  if (!inputText.value.trim()) return

  const msgPayload = {
    type: 'CHAT',
    receiverId: currentTargetId.value,
    productId: currentProductId.value,
    content: inputText.value.trim()
  }

  userStore.sendWsMessage(msgPayload)
  inputText.value = ''
}

// 举报相关
const showReportDialog = ref(false)
const submittingReport = ref(false)
const reportForm = reactive({
  reasonType: null,
  reasonDetail: ''
})

async function submitReport() {
  if (!reportForm.reasonType) { ElMessage.warning('请选择举报理由'); return }
  submittingReport.value = true
  try {
    // 【双轨制风控】: 提交时携带 reportType: 3 代表聊天内容违规，后端物理跳过 AI 评分
    await reportApi.submit({
      reportType: 3, // 私信内容举报
      targetType: 2, // 举报用户
      targetId: currentTargetId.value,
      reasonType: reportForm.reasonType,
      reasonDetail: reportForm.reasonDetail
    })
    ElMessage.success('举报成功，后台将核实聊天记录')
    showReportDialog.value = false
    reportForm.reasonType = null
    reportForm.reasonDetail = ''
  } catch (e) {
    console.error('举报提交失败:', e)
    ElMessage.error(e.response?.data?.message || '举报提交失败，请稍后重试')
  } finally {
    submittingReport.value = false
  }
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

  contact.unreadCount = 0
  await loadHistory()
}

function goProduct() {
  if (currentProductId.value) {
    router.push(`/product/${currentProductId.value}`)
  }
}

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
.chat-page {
  padding: 24px 20px 36px;
}

.chat-shell {
  min-height: calc(100vh - 164px);
  border-radius: 28px;
  background: linear-gradient(180deg, #fcfcfd 0%, #f3f5f8 100%);
  border: 1px solid rgba(17, 24, 39, 0.06);
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.06);
  overflow: hidden;
}

.chat-titlebar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px 16px;
  background: rgba(255, 255, 255, 0.88);
  border-bottom: 1px solid rgba(17, 24, 39, 0.05);
}

.chat-title {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-size: 26px;
  font-weight: 700;
  color: #111827;
}

.chat-layout {
  display: flex;
  min-height: calc(100vh - 225px);
}

.contact-panel {
  width: 320px;
  border-right: 1px solid rgba(17, 24, 39, 0.06);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: blur(12px);
}

.contact-list {
  flex: 1;
  overflow-y: auto;
  padding: 14px 10px 10px;
}

.contact-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100%;
  padding: 20px 14px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
  padding: 14px;
  cursor: pointer;
  transition: background 0.18s ease, transform 0.18s ease, box-shadow 0.18s ease;
  border-radius: 18px;
  border: 1px solid transparent;
}

.contact-item:hover {
  background: #f8fafc;
  transform: translateY(-1px);
}

.contact-item.active {
  background: #ffffff;
  border-color: rgba(17, 24, 39, 0.06);
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.06);
}

.contact-info {
  flex: 1;
  min-width: 0;
}

.contact-name {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
  color: #111827;
}

.contact-last-msg {
  font-size: 12px;
  color: #6b7280;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.4;
}

.contact-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  flex-shrink: 0;
}

.contact-time {
  font-size: 11px;
  color: #9ca3af;
}

.contact-badge :deep(.el-badge__content) {
  transform: scale(0.9);
  transform-origin: right top;
  border: none;
}

.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: rgba(255, 255, 255, 0.62);
}

.chat-placeholder {
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.chat-header {
  padding: 22px 28px 18px;
  border-bottom: 1px solid rgba(17, 24, 39, 0.05);
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(255, 255, 255, 0.68);
}

.chat-header-actions {
  flex-shrink: 0;
}

.report-header-btn {
  color: var(--text-quaternary) !important;
  font-size: 13px;
}

.report-header-btn:hover {
  color: var(--drawbridge-red) !important;
}

.report-submit-btn {
  width: 140px;
  height: 44px;
  border-radius: 20px !important;
  background-color: var(--drawbridge-red) !important;
  border-color: var(--drawbridge-red) !important;
  color: #fff !important;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(235, 59, 62, 0.2);
  transition: all 0.3s;
}

.report-submit-btn:hover {
  background-color: #d63639 !important;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(235, 59, 62, 0.3);
}

:deep(.drawbridge-dialog) {
  border-radius: 20px !important;
  overflow: hidden;
}

:deep(.drawbridge-dialog .el-dialog__header) {
  padding-bottom: 0;
}

.chat-target-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.chat-target-name {
  font-size: 18px;
  font-weight: 700;
  color: #111827;
}

.chat-target-subtitle {
  font-size: 12px;
  color: #6b7280;
}

.product-context {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 16px 20px 0;
  padding: 12px 14px;
  background: #fff;
  cursor: pointer;
  border: 1px solid rgba(17, 24, 39, 0.06);
  border-radius: 18px;
  transition: background 0.15s ease, transform 0.15s ease;
}

.product-context:hover {
  background: #fbfbfc;
  transform: translateY(-1px);
}

.product-thumb {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  object-fit: cover;
  flex-shrink: 0;
}

.product-brief {
  flex: 1;
  min-width: 0;
}

.product-title-text {
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #111827;
}

.product-price {
  font-size: 13px;
  margin-top: 4px;
}

.product-arrow {
  color: #9ca3af;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px 22px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.empty-chat {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 30px 0;
}

.empty-chat-card,
.placeholder-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 12px;
  padding: 28px 32px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(17, 24, 39, 0.06);
  border-radius: 24px;
  box-shadow: 0 18px 30px rgba(15, 23, 42, 0.05);
  color: #111827;
}

.empty-chat-card p,
.placeholder-card h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
}

.empty-chat-card span,
.placeholder-card p {
  margin: 0;
  font-size: 13px;
  color: #6b7280;
  line-height: 1.6;
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  max-width: 78%;
}

.message-row.is-self {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.bubble-wrapper {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
}

.message-row.is-self .bubble-wrapper {
  align-items: flex-end;
}

.read-status {
  font-size: 11px;
  color: #9ca3af;
  padding: 0 4px;
}

.read-status.is-read {
  color: #111827;
}

.bubble {
  padding: 12px 16px;
  border-radius: 18px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
  background: #fff;
  border: 1px solid rgba(17, 24, 39, 0.06);
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.04);
  color: #111827;
}

.message-row.is-self .bubble {
  background: #1c1c1e;
  color: #fff;
  border-color: #1c1c1e;
}

.chat-input-area {
  padding: 18px 24px 22px;
  border-top: 1px solid rgba(17, 24, 39, 0.05);
  display: flex;
  gap: 12px;
  align-items: center;
  background: rgba(255, 255, 255, 0.82);
}

.chat-input-area .el-input {
  flex: 1;
}

.chat-input-area :deep(.el-input__wrapper) {
  min-height: 48px;
  background: #f4f5f7;
  border-radius: 18px;
  box-shadow: none;
  padding: 0 16px;
}

.chat-input-area :deep(.el-button) {
  height: 48px;
  padding: 0 20px;
  border-radius: 18px;
  border: 0;
  background: #1c1c1e;
}

@media (max-width: 768px) {
  .chat-page {
    padding: 16px 12px 24px;
  }

  .chat-shell {
    min-height: calc(100vh - 132px);
    border-radius: 20px;
  }

  .chat-titlebar {
    padding: 16px 16px 12px;
  }

  .chat-title {
    font-size: 22px;
  }

  .chat-layout {
    flex-direction: column;
    min-height: auto;
  }

  .contact-panel {
    width: 100%;
    border-right: 0;
    border-bottom: 1px solid rgba(17, 24, 39, 0.06);
  }

  .contact-list {
    max-height: 240px;
  }

  .message-row {
    max-width: 100%;
  }
}
</style>
