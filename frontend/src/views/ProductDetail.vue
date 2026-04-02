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
            <div class="seller-trust-container">
              <el-tag 
                :type="trustType" 
                class="trust-tag"
                size="small"
              >
                信用度: {{ trustScore }}%
              </el-tag>
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
          <el-button type="primary" size="large" round @click="buyNow" :disabled="product.productStatus !== 1 || isOwner" class="buy-now-btn">
            {{ product.productStatus === 3 ? '已售出' : product.productStatus === 2 ? '已下架' : isOwner ? '自己的商品' : '立即购买' }}
          </el-button>
        </div>
        
        <div class="report-entry" v-if="!isOwner">
          <el-button link @click="showReportDialog = true">
            <el-icon><Warning /></el-icon> 举报此商品
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
        <div class="add-addr-link">
          <el-button link @click="showAddressDialog = true">+ 使用新地址</el-button>
        </div>
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

  <!-- 新增地址弹窗 -->
  <el-dialog v-model="showAddressDialog" title="添加收货地址" width="460px" append-to-body class="drawbridge-dialog">
    <el-form ref="addrFormRef" :model="addrForm" :rules="addrRules" label-width="80px">
      <el-form-item label="收货人" prop="receiverName">
        <el-input v-model="addrForm.receiverName" placeholder="姓名" />
      </el-form-item>
      <el-form-item label="联系电话" prop="receiverPhone">
        <el-input v-model="addrForm.receiverPhone" placeholder="手机号码" />
      </el-form-item>
      <el-row :gutter="10">
        <el-col :span="12">
          <el-form-item label="省份" prop="province">
            <el-input v-model="addrForm.province" placeholder="省" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="城市" prop="city" label-width="50px">
            <el-input v-model="addrForm.city" placeholder="市" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="区/县">
        <el-input v-model="addrForm.district" placeholder="区/县（选填）" />
      </el-form-item>
      <el-form-item label="详细地址" prop="detailAddress">
        <el-input v-model="addrForm.detailAddress" type="textarea" :rows="2" placeholder="街道、楼牌号等" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showAddressDialog = false" round>取消</el-button>
      <el-button class="save-addr-btn" @click="saveNewAddress" :loading="savingAddr" round>保存并使用</el-button>
    </template>
  </el-dialog>

  <!-- 举报弹窗 -->
  <el-dialog v-model="showReportDialog" title="举报商品" width="420px" class="drawbridge-dialog">
    <el-form label-position="top">
      <el-form-item label="举报理由">
        <el-select v-model="reportForm.reasonType" placeholder="请选择举报理由" style="width: 100%">
          <el-option label="违禁物品" :value="1" />
          <el-option label="虚假信息/诈骗" :value="2" />
          <el-option label="售卖假货" :value="3" />
          <el-option label="辱骂/不当言论" :value="4" />
          <el-option label="其他" :value="5" />
        </el-select>
      </el-form-item>
      <el-form-item label="详情描述">
        <el-input v-model="reportForm.reasonDetail" type="textarea" :rows="3" placeholder="请提供更多证据或描述..." />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showReportDialog = false" round>取消</el-button>
      <el-button type="primary" @click="submitReport" :loading="submittingReport" class="report-submit-btn">提交举报</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { productApi, messageApi, favoriteApi, orderApi, addressApi, reportApi } from '../api'
import { ElMessage } from 'element-plus'
import { reactive } from 'vue'

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

// 新增地址相关
const showAddressDialog = ref(false)
const addrFormRef = ref()
const savingAddr = ref(false)
const addrForm = reactive({
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  isDefault: 0
})

// 举报相关
const showReportDialog = ref(false)
const submittingReport = ref(false)
const reportForm = reactive({
  reasonType: null,
  reasonDetail: ''
})

const addrRules = {
  receiverName: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  receiverPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  detailAddress: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

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

// 信用度计算
const trustScore = computed(() => {
  if (!product.value?.sellerRiskScore) return 100
  return Math.max(0, Math.min(100, Math.round((1 - product.value.sellerRiskScore) * 100)))
})
const trustType = computed(() => {
  if (trustScore.value >= 80) return 'success'
  if (trustScore.value >= 50) return ''
  return 'warning'
})

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

async function submitReport() {
  if (!reportForm.reasonType) { ElMessage.warning('请选择举报理由'); return }
  submittingReport.value = true
  try {
    await reportApi.submit({
      reportType: 1, // 交易性举报
      targetType: 1, // 举报商品
      targetId: product.value.id,
      reasonType: reportForm.reasonType,
      reasonDetail: reportForm.reasonDetail
    })
    ElMessage.success('举报成功')
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

async function saveNewAddress() {
  const valid = await addrFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  savingAddr.value = true
  try {
    const res = await addressApi.add(addrForm)
    ElMessage.success('地址添加成功')
    
    // 重新加载地址列表
    const addrRes = await addressApi.list()
    addresses.value = addrRes.data || []
    
    // 自动选中新创建的地址 (假设后端返回的对象包含生成的ID，或者通过匹配找到最新的一条)
    // 根据业务逻辑，通常后端返回的是创建成功的完整对象或ID
    if (res.data && res.data.id) {
      selectedAddressId.value = res.data.id
    }
    
    showAddressDialog.value = false
    // 重置表单
    Object.assign(addrForm, {
      receiverName: '',
      receiverPhone: '',
      province: '',
      city: '',
      district: '',
      detailAddress: '',
      isDefault: 0
    })
  } catch (e) {
    console.error('添加地址失败:', e)
  } finally {
    savingAddr.value = false
  }
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
.seller-detail { flex: 1; overflow: hidden; }
.seller-name { font-size: 15px; font-weight: 500; margin-bottom: 4px; }
.seller-trust-container { display: flex; align-items: center; gap: 8px; }
.seller-credit { font-size: 13px; color: var(--text-tertiary); }
.trust-tag { border-radius: 20px !important; font-weight: 600; }
.report-entry { text-align: right; margin-top: 16px; }
.report-entry .el-button { color: var(--text-quaternary); font-size: 12px; }
.report-entry .el-button:hover { color: var(--drawbridge-red); }

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
.action-buttons { display: flex; gap: 12px; }
.action-buttons .el-button { flex: 1; height: 48px; font-size: 16px; }
.buy-now-btn {
  background-color: var(--drawbridge-red) !important;
  border-color: var(--drawbridge-red) !important;
  color: #ffffff !important;
}
.buy-now-btn:hover {
  background-color: #E33E41 !important;
  border-color: #E33E41 !important;
}
.buy-now-btn.is-disabled {
  background-color: #F7C6C7 !important;
  border-color: #F7C6C7 !important;
}
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
.buy-price { margin-top: 8px; }

.add-addr-link {
  text-align: right;
  margin-top: 4px;
}
.add-addr-link .el-button {
  color: #666 !important;
  font-size: 13px;
  padding: 0;
}
.add-addr-link .el-button:hover {
  color: #1C1C1E !important;
}

:deep(.drawbridge-dialog) {
  border-radius: 20px !important;
  overflow: hidden;
}
:deep(.drawbridge-dialog .el-dialog__header) {
  padding-bottom: 0;
}
.save-addr-btn {
  background-color: #1C1C1E !important;
  border-color: #1C1C1E !important;
  color: #ffffff !important;
}
.save-addr-btn:hover {
  background-color: #333333 !important;
  border-color: #333333 !important;
}

@media (max-width: 768px) {
  .detail-content { flex-direction: column; }
  .detail-images { width: 100%; }
}
</style>
