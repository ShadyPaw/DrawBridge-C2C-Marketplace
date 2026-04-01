<template>
  <div class="detail-page container" v-if="product">
    <div class="detail-content">
      <!-- 左侧：图片 -->
      <div class="detail-images">
        <div class="main-image">
          <img :src="currentImage" :alt="product.title" />
        </div>
        <div class="image-list" v-if="product.images && product.images.length > 1">
          <div
            v-for="(img, i) in product.images"
            :key="i"
            class="thumb"
            :class="{ active: currentIndex === i }"
            @click="currentIndex = i"
          >
            <img :src="img.imageUrl" />
          </div>
        </div>
      </div>

      <!-- 右侧：信息 -->
      <div class="detail-info">
        <h1 class="product-title">{{ product.title }}</h1>
        <div class="price-section">
          <span class="price-big">{{ product.price }}</span>
          <span v-if="product.originalPrice" class="price-original">原价 ¥{{ product.originalPrice }}</span>
        </div>

        <div class="info-tags">
          <el-tag size="small" :type="qualityType">{{ qualityText }}</el-tag>
          <el-tag size="small" type="info">{{ tradeModeText }}</el-tag>
          <el-tag v-if="product.location" size="small" type="info">📍 {{ product.location }}</el-tag>
        </div>

        <el-divider />

        <div class="desc-section">
          <h3>商品描述</h3>
          <p>{{ product.description || '暂无描述' }}</p>
        </div>

        <el-divider />

        <!-- 卖家信息 -->
        <div class="seller-section" @click="$router.push(`/user/${product.userId}`)">
          <el-avatar :size="48" :src="product.sellerAvatar || undefined">
            {{ product.sellerNickname?.charAt(0) }}
          </el-avatar>
          <div class="seller-detail">
            <div class="seller-name">{{ product.sellerNickname }}</div>
            <div class="seller-credit">
              信用积分: <span>{{ product.sellerCreditScore }}</span>
            </div>
          </div>
          <el-icon><ArrowRight /></el-icon>
        </div>

        <el-divider />

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <el-button @click="toggleFavorite" :icon="isFavorited ? 'StarFilled' : 'Star'" size="large" round>
            {{ isFavorited ? '已收藏' : '收藏' }}
          </el-button>
          <el-button v-if="!isOwner" size="large" round @click="goChat">
            <el-icon><ChatDotRound /></el-icon> 私聊卖家
          </el-button>
          <el-button type="primary" size="large" round @click="buyNow" :disabled="product.productStatus !== 1 || isOwner">
            {{ product.productStatus === 3 ? '已售出' : product.productStatus === 2 ? '已下架' : isOwner ? '自己的商品' : '立即购买' }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- 留言区 -->
    <div class="comments-section">
      <h3><el-icon><ChatDotRound /></el-icon> 留言 ({{ messages.length }})</h3>
      <div class="comment-input" v-if="userStore.isLoggedIn">
        <el-input v-model="newMessage" placeholder="说点什么..." :rows="2" type="textarea" resize="none" />
        <el-button type="primary" @click="sendMessage" :disabled="!newMessage.trim()">发送</el-button>
      </div>
      <div v-else class="comment-login-tip">
        <router-link to="/login">登录</router-link> 后参与留言
      </div>

      <div v-if="messages.length === 0" class="empty-comments">暂无留言，快来第一个留言吧~</div>
      <div v-else class="comment-list">
        <div v-for="msg in messages" :key="msg.id" class="comment-item">
          <el-avatar :size="36" :src="msg.userAvatar || undefined">{{ msg.userNickname?.charAt(0) }}</el-avatar>
          <div class="comment-body">
            <div class="comment-header">
              <span class="comment-name">{{ msg.userNickname }}</span>
              <span class="comment-time">{{ formatTime(msg.createTime) }}</span>
            </div>
            <p class="comment-content">{{ msg.content }}</p>
            <div v-if="msg.replies && msg.replies.length" class="reply-list">
              <div v-for="reply in msg.replies" :key="reply.id" class="reply-item">
                <span class="reply-name">{{ reply.userNickname }}</span>
                <span v-if="reply.replyUserNickname"> 回复 <span class="reply-name">{{ reply.replyUserNickname }}</span></span>:
                {{ reply.content }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 购买弹窗 -->
  <el-dialog v-model="showBuyDialog" title="确认购买" width="480px">
    <div class="buy-info">
      <p>商品: {{ product?.title }}</p>
      <p class="buy-price">价格: <span class="price">{{ product?.price }}</span></p>
    </div>
    <el-form label-width="80px" style="margin-top: 16px;">
      <el-form-item label="收货地址">
        <el-select v-model="selectedAddressId" placeholder="请选择收货地址" style="width: 100%">
          <el-option v-for="a in addresses" :key="a.id" :label="`${a.receiverName} ${a.province}${a.city}${a.district||''}${a.detailAddress}`" :value="a.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="orderRemark" type="textarea" placeholder="选填" :rows="2" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showBuyDialog = false">取消</el-button>
      <el-button type="primary" @click="confirmBuy" :loading="buying">确认下单</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { productApi, messageApi, favoriteApi, orderApi, addressApi } from '../api'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const product = ref(null)
const messages = ref([])
const newMessage = ref('')
const isFavorited = ref(false)
const currentIndex = ref(0)
const showBuyDialog = ref(false)
const addresses = ref([])
const selectedAddressId = ref(null)
const orderRemark = ref('')
const buying = ref(false)

const currentImage = computed(() => {
  if (!product.value?.images?.length) return product.value?.coverImage || ''
  return product.value.images[currentIndex.value]?.imageUrl || ''
})

const isOwner = computed(() => userStore.userId && product.value?.userId === userStore.userId)

const qualityMap = { 1: '全新', 2: '几乎全新', 3: '轻微使用', 4: '明显使用', 5: '其他' }
const qualityText = computed(() => qualityMap[product.value?.quality] || '其他')
const qualityType = computed(() => product.value?.quality <= 2 ? 'success' : 'info')
const tradeModeMap = { 1: '邮寄', 2: '自提/面交', 3: '邮寄/自提均可' }
const tradeModeText = computed(() => tradeModeMap[product.value?.tradeMode] || '邮寄')

onMounted(() => { loadProduct(); loadMessages() })

async function loadProduct() {
  try {
    const res = await productApi.detail(route.params.id)
    product.value = res.data
    if (userStore.isLoggedIn) {
      const favRes = await favoriteApi.check(route.params.id)
      isFavorited.value = favRes.data.isFavorited
    }
  } catch (e) { console.error(e) }
}

async function loadMessages() {
  try {
    const res = await messageApi.getByProduct(route.params.id)
    messages.value = res.data || []
  } catch (e) { console.error(e) }
}

async function sendMessage() {
  if (!newMessage.value.trim()) return
  try {
    await messageApi.send({ productId: Number(route.params.id), content: newMessage.value, parentId: 0 })
    newMessage.value = ''
    loadMessages()
    ElMessage.success('留言成功')
  } catch (e) { /* handled */ }
}

async function toggleFavorite() {
  if (!userStore.isLoggedIn) { router.push('/login'); return }
  try {
    const res = await favoriteApi.toggle(route.params.id)
    isFavorited.value = res.data.isFavorited
  } catch (e) { /* handled */ }
}

function goChat() {
  if (!userStore.isLoggedIn) { router.push('/login'); return }
  router.push({
    path: '/chat',
    query: {
      userId: product.value.userId,
      productId: product.value.id
    }
  })
}

async function buyNow() {
  if (!userStore.isLoggedIn) { router.push('/login'); return }
  try {
    const res = await addressApi.list()
    addresses.value = res.data || []
    if (addresses.value.length > 0) {
      const def = addresses.value.find(a => a.isDefault === 1)
      selectedAddressId.value = def ? def.id : addresses.value[0].id
    }
    showBuyDialog.value = true
  } catch (e) { /* handled */ }
}

async function confirmBuy() {
  if (!selectedAddressId.value) { ElMessage.warning('请选择收货地址'); return }
  buying.value = true
  try {
    const res = await orderApi.create({
      productId: product.value.id,
      addressId: selectedAddressId.value,
      remark: orderRemark.value
    })
    ElMessage.success('下单成功')
    showBuyDialog.value = false
    router.push(`/order/${res.data.id}`)
  } catch (e) { /* handled */ }
  finally { buying.value = false }
}

function formatTime(time) {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.detail-page { padding: 24px 20px 40px; }
.detail-content { display: flex; gap: 32px; background: var(--bg-white); border-radius: var(--radius-lg); padding: 24px; box-shadow: var(--shadow-sm); }
.detail-images { width: 420px; flex-shrink: 0; }
.main-image { width: 100%; border-radius: var(--radius-md); overflow: hidden; background: #f5f5f5; }
.main-image img { width: 100%; aspect-ratio: 1; object-fit: cover; }
.image-list { display: flex; gap: 8px; margin-top: 12px; overflow-x: auto; }
.thumb { width: 64px; height: 64px; border-radius: 6px; overflow: hidden; cursor: pointer; border: 2px solid transparent; transition: border-color 0.2s; flex-shrink: 0; }
.thumb.active { border-color: var(--primary-color); }
.thumb img { width: 100%; height: 100%; object-fit: cover; }
.detail-info { flex: 1; }
.product-title { font-size: 22px; font-weight: 600; color: var(--text-primary); line-height: 1.4; margin-bottom: 12px; }
.price-section { margin-bottom: 16px; }
.price-big { font-size: 28px; color: var(--danger-color); font-weight: 700; }
.price-big::before { content: '¥'; font-size: 18px; }
.info-tags { display: flex; gap: 8px; flex-wrap: wrap; }
.desc-section h3 { font-size: 15px; font-weight: 600; margin-bottom: 8px; color: var(--text-primary); }
.desc-section p { font-size: 14px; color: var(--text-secondary); line-height: 1.8; white-space: pre-wrap; }
.seller-section { display: flex; align-items: center; gap: 12px; padding: 12px; border-radius: var(--radius-md); cursor: pointer; transition: background 0.2s; }
.seller-section:hover { background: var(--bg-color); }
.seller-detail { flex: 1; }
.seller-name { font-size: 15px; font-weight: 500; }
.seller-credit { font-size: 13px; color: var(--text-tertiary); margin-top: 2px; }
.seller-credit span { color: var(--primary-color); font-weight: 600; }
.action-buttons { display: flex; gap: 12px; }
.action-buttons .el-button { flex: 1; height: 48px; font-size: 16px; }
.comments-section { background: var(--bg-white); border-radius: var(--radius-lg); padding: 24px; margin-top: 24px; box-shadow: var(--shadow-sm); }
.comments-section h3 { font-size: 18px; font-weight: 600; display: flex; align-items: center; gap: 8px; margin-bottom: 20px; }
.comment-input { display: flex; gap: 12px; margin-bottom: 20px; align-items: flex-end; }
.comment-input .el-input { flex: 1; }
.comment-login-tip { text-align: center; padding: 12px; color: var(--text-tertiary); font-size: 14px; }
.comment-login-tip a { color: var(--primary-color); }
.empty-comments { text-align: center; padding: 32px; color: var(--text-quaternary); }
.comment-list { display: flex; flex-direction: column; gap: 16px; }
.comment-item { display: flex; gap: 12px; }
.comment-body { flex: 1; }
.comment-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
.comment-name { font-size: 14px; font-weight: 500; }
.comment-time { font-size: 12px; color: var(--text-quaternary); }
.comment-content { font-size: 14px; color: var(--text-secondary); line-height: 1.6; }
.reply-list { background: var(--bg-color); border-radius: 6px; padding: 10px 12px; margin-top: 8px; font-size: 13px; color: var(--text-secondary); display: flex; flex-direction: column; gap: 6px; }
.reply-name { color: var(--primary-color); font-weight: 500; }
.buy-info { font-size: 15px; }
.buy-price { margin-top: 8px; }

@media (max-width: 768px) {
  .detail-content { flex-direction: column; }
  .detail-images { width: 100%; }
}
</style>
