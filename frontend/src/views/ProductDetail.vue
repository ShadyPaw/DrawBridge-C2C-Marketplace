<template>
  <div class="detail-page container" v-if="product">
    <div class="detail-content">
      <div class="detail-images">
        <div class="main-image">
          <img :src="currentImage" :alt="product.title" />
        </div>
        <div class="image-list" v-if="product.images && product.images.length > 1">
          <div
            v-for="(img, index) in product.images"
            :key="index"
            class="thumb"
            :class="{ active: currentIndex === index }"
            @click="currentIndex = index"
          >
            <img :src="img.imageUrl" :alt="product.title" />
          </div>
        </div>
      </div>

      <div class="detail-info">
        <h1 class="product-title">{{ product.title }}</h1>
        <div class="price-section">
          <span class="price-big">{{ product.price }}</span>
          <span v-if="product.originalPrice" class="price-original">原价 ￥{{ product.originalPrice }}</span>
        </div>

        <div class="info-tags">
          <el-tag size="small" :type="qualityType">{{ qualityText }}</el-tag>
          <el-tag size="small" type="info">{{ tradeModeText }}</el-tag>
          <el-tag v-if="product.location" size="small" type="info">{{ product.location }}</el-tag>
        </div>

        <el-divider />

        <div class="desc-section">
          <h3>商品描述</h3>
          <p>{{ product.description || '暂无描述' }}</p>
        </div>

        <el-divider />

        <div class="seller-section" @click="$router.push(`/user/${product.userId}`)">
          <el-avatar :size="48" :src="product.sellerAvatar || undefined">
            {{ product.sellerNickname?.charAt(0) }}
          </el-avatar>
          <div class="seller-detail">
            <div class="seller-name">{{ product.sellerNickname }}</div>
            <div class="seller-meta">
              <span :class="['credit-badge', sellerCreditTone.className]">信用分 {{ sellerCreditScore }}</span>
            </div>
          </div>
        </div>

        <el-divider />

        <div class="action-buttons">
          <el-button @click="toggleFavorite" size="large" round>
            {{ isFavorited ? '已收藏' : '收藏' }}
          </el-button>
          <el-button v-if="!isOwner" size="large" round @click="goChat">联系卖家</el-button>
          <el-button
            type="primary"
            size="large"
            round
            @click="buyNow"
            :disabled="product.productStatus !== 1 || isOwner"
            class="buy-now-btn"
          >
            {{ buyButtonText }}
          </el-button>
        </div>

        <div class="report-entry" v-if="!isOwner">
          <el-button link @click="showReportDialog = true">举报该商品</el-button>
        </div>
      </div>
    </div>

    <div class="comments-section">
      <h3>留言 ({{ messages.length }})</h3>
      <div class="comment-input" v-if="userStore.isLoggedIn">
        <el-input
          v-model="newMessage"
          placeholder="说点什么..."
          :rows="2"
          type="textarea"
          resize="none"
        />
        <el-button type="primary" @click="sendMessage" :disabled="!newMessage.trim()">发送</el-button>
      </div>
      <div v-else class="comment-login-tip">
        <router-link to="/login">登录</router-link> 后参与留言
      </div>

      <div v-if="messages.length === 0" class="empty-comments">暂无留言，快来留下第一条吧</div>
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
                <span v-if="reply.replyUserNickname"> 回复 <span class="reply-name">{{ reply.replyUserNickname }}</span></span>：
                {{ reply.content }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <el-dialog v-model="showBuyDialog" title="确认购买" width="480px">
    <div class="buy-info">
      <p>商品：{{ product?.title }}</p>
      <p class="buy-price">价格：<span class="price">￥{{ product?.price }}</span></p>
    </div>
    <el-form label-width="80px" style="margin-top: 16px;">
      <el-form-item label="收货地址">
        <el-select v-model="selectedAddressId" placeholder="请选择收货地址" style="width: 100%">
          <el-option
            v-for="address in addresses"
            :key="address.id"
            :label="`${address.receiverName} ${address.province}${address.city}${address.district || ''}${address.detailAddress}`"
            :value="address.id"
          />
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

  <el-dialog v-model="showAddressDialog" title="添加收货地址" width="460px" append-to-body class="drawbridge-dialog">
    <el-form ref="addrFormRef" :model="addrForm" :rules="addrRules" label-width="80px">
      <el-form-item label="收货人" prop="receiverName">
        <el-input v-model="addrForm.receiverName" placeholder="姓名" />
      </el-form-item>
      <el-form-item label="联系电话" prop="receiverPhone">
        <el-input v-model="addrForm.receiverPhone" placeholder="手机号" />
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
      <el-form-item label="区 / 县">
        <el-input v-model="addrForm.district" placeholder="区 / 县（选填）" />
      </el-form-item>
      <el-form-item label="详细地址" prop="detailAddress">
        <el-input v-model="addrForm.detailAddress" type="textarea" :rows="2" placeholder="街道、楼栋号等" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showAddressDialog = false" round>取消</el-button>
      <el-button class="save-addr-btn" @click="saveNewAddress" :loading="savingAddr" round>保存并使用</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="showReportDialog" title="举报商品" width="420px" class="drawbridge-dialog">
    <el-form label-position="top">
      <el-form-item label="举报理由">
        <el-select v-model="reportForm.reasonType" placeholder="请选择举报理由" style="width: 100%">
          <el-option label="违禁物品" :value="1" />
          <el-option label="虚假信息或诈骗" :value="2" />
          <el-option label="出售假货" :value="3" />
          <el-option label="骚扰或不当言论" :value="4" />
          <el-option label="其他" :value="5" />
        </el-select>
      </el-form-item>
      <el-form-item label="详情描述">
        <el-input
          v-model="reportForm.reasonDetail"
          type="textarea"
          :rows="3"
          placeholder="请提供更多证据或说明..."
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showReportDialog = false" round>取消</el-button>
      <el-button type="primary" @click="submitReport" :loading="submittingReport" class="report-submit-btn">
        提交举报
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../stores/user'
import { addressApi, favoriteApi, messageApi, orderApi, productApi, reportApi, transactionApi } from '../api'

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
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  detailAddress: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

const currentImage = computed(() => {
  if (!product.value?.images?.length) {
    return product.value?.coverImage || ''
  }
  return product.value.images[currentIndex.value]?.imageUrl || product.value?.coverImage || ''
})

const isOwner = computed(() => userStore.userId && product.value?.userId === userStore.userId)

const qualityMap = { 1: '全新', 2: '几乎全新', 3: '轻微使用', 4: '明显使用', 5: '其他' }
const tradeModeMap = { 1: '邮寄', 2: '自提 / 面交', 3: '邮寄 / 自提均可' }

const qualityText = computed(() => qualityMap[product.value?.quality] || '其他')
const qualityType = computed(() => (product.value?.quality <= 2 ? 'success' : 'info'))
const tradeModeText = computed(() => tradeModeMap[product.value?.tradeMode] || '邮寄')
const sellerCreditScore = computed(() => product.value?.sellerCreditScore ?? 0)
const sellerCreditTone = computed(() => getCreditTone(sellerCreditScore.value))
const buyButtonText = computed(() => {
  if (product.value?.productStatus === 3) return '已售出'
  if (product.value?.productStatus === 2) return '已下架'
  if (isOwner.value) return '自己的商品'
  return '立即购买'
})

function getCreditTone(score) {
  if (score >= 100) {
    return { className: 'credit-badge--safe' }
  }
  if (score >= 60) {
    return { className: 'credit-badge--medium' }
  }
  return { className: 'credit-badge--danger' }
}

onMounted(() => {
  loadProduct()
  loadMessages()
})

async function loadProduct() {
  try {
    const res = await productApi.detail(route.params.id)
    product.value = res.data
    if (userStore.isLoggedIn) {
      const favRes = await favoriteApi.check(route.params.id)
      isFavorited.value = favRes.data.isFavorited
    }
  } catch (error) {
    console.error('加载商品详情失败:', error)
  }
}

async function loadMessages() {
  try {
    const res = await messageApi.getByProduct(route.params.id)
    messages.value = res.data || []
  } catch (error) {
    console.error('加载留言失败:', error)
  }
}

async function sendMessage() {
  if (!newMessage.value.trim()) return

  try {
    await messageApi.send({
      productId: Number(route.params.id),
      content: newMessage.value,
      parentId: 0
    })
    newMessage.value = ''
    await loadMessages()
    ElMessage.success('留言成功')
  } catch (error) {
    console.error('发送留言失败:', error)
  }
}

async function toggleFavorite() {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }

  try {
    const res = await favoriteApi.toggle(route.params.id)
    isFavorited.value = res.data.isFavorited
  } catch (error) {
    console.error('收藏操作失败:', error)
  }
}

function goChat() {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }

  router.push({
    path: '/chat',
    query: {
      userId: product.value.userId,
      productId: product.value.id
    }
  })
}

async function buyNow() {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }

  try {
    const res = await addressApi.list()
    addresses.value = res.data || []
    if (addresses.value.length > 0) {
      const defaultAddress = addresses.value.find(item => item.isDefault === 1)
      selectedAddressId.value = defaultAddress ? defaultAddress.id : addresses.value[0].id
    }
    showBuyDialog.value = true
  } catch (error) {
    console.error('加载地址失败:', error)
  }
}

async function confirmBuy() {
  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }

  buying.value = true

  try {
    try {
      await transactionApi.verify({
        productId: product.value.id,
        transactionAmount: Number(product.value.price)
      })
    } catch (error) {
      if (error.response && error.response.status === 403) {
        ElMessageBox.alert(
          `<div style="color: #E64141; font-weight: 500;">
            <p style="font-size: 18px; margin-bottom: 12px; font-weight: 600;">⚠️ 高危拦截</p>
            <p>${error.response.data?.message || '检测到当前账号存在高风险异常行为，本次交易已被冻结。'}</p>
            <p>为保护资金安全，请联系客服处理后再尝试下单。</p>
          </div>`,
          'AI 安全中枢提示',
          {
            dangerouslyUseHTMLString: true,
            confirmButtonText: '我知道了',
            type: 'error',
            center: true
          }
        )
        return
      }
      
      if (error.response && error.response.status === 409) {
        try {
          await ElMessageBox.confirm(
            `<div style="color: #E64141; font-weight: 500;">
              <p style="font-size: 18px; margin-bottom: 12px; font-weight: 600;">🚨 反诈骗高危提醒</p>
              <p>${error.response.data?.message || '该卖家信用极低，存在较高交易风险，系统建议您终止交易！'}</p>
              <p>如果您执意继续购买，可能面临资金损失风险。</p>
            </div>`,
            '安全警告 (请二次确认)',
            {
              dangerouslyUseHTMLString: true,
              confirmButtonText: '继续购买 (风险自担)',
              cancelButtonText: '终止交易',
              type: 'warning',
              center: true
            }
          );
          // 如果用户点击了“继续购买”，就跳过验证，继续往下走执行真正的 orderApi.create
        } catch (cancelErr) {
          // 用户点击了取消交易，直接终止当前函数
          return;
        }
      } else {
          // 其他网络错误等
          throw error;
      }
    }

    const res = await orderApi.create({
      productId: product.value.id,
      addressId: selectedAddressId.value,
      remark: orderRemark.value
    })

    ElMessage.success('下单成功')
    showBuyDialog.value = false
    router.push(`/order/${res.data.id}`)
  } catch (error) {
    if (error.response?.status !== 403) {
      console.error('购买流程异常:', error)
    }
  } finally {
    buying.value = false
  }
}

async function submitReport() {
  if (!reportForm.reasonType) {
    ElMessage.warning('请选择举报理由')
    return
  }

  submittingReport.value = true
  try {
    await reportApi.submit({
      reportType: 1,
      targetType: 1,
      targetId: product.value.id,
      reasonType: reportForm.reasonType,
      reasonDetail: reportForm.reasonDetail
    })
    ElMessage.success('举报成功')
    showReportDialog.value = false
    reportForm.reasonType = null
    reportForm.reasonDetail = ''
  } catch (error) {
    console.error('提交举报失败:', error)
    ElMessage.error(error.response?.data?.message || '举报提交失败，请稍后重试')
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

    const addrRes = await addressApi.list()
    addresses.value = addrRes.data || []

    if (res.data && res.data.id) {
      selectedAddressId.value = res.data.id
    }

    showAddressDialog.value = false
    Object.assign(addrForm, {
      receiverName: '',
      receiverPhone: '',
      province: '',
      city: '',
      district: '',
      detailAddress: '',
      isDefault: 0
    })
  } catch (error) {
    console.error('添加地址失败:', error)
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
.detail-page {
  padding: 24px 20px 40px;
}

.detail-content {
  display: flex;
  gap: 32px;
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  padding: 24px;
  box-shadow: var(--shadow-sm);
}

.detail-images {
  width: 420px;
  flex-shrink: 0;
}

.main-image {
  width: 100%;
  border-radius: var(--radius-md);
  overflow: hidden;
  background: #f5f5f5;
}

.main-image img {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
}

.image-list {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  overflow-x: auto;
}

.thumb {
  width: 64px;
  height: 64px;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: border-color 0.2s;
  flex-shrink: 0;
}

.thumb.active {
  border-color: var(--primary-color);
}

.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-info {
  flex: 1;
}

.product-title {
  font-size: 22px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.4;
  margin-bottom: 12px;
}

.price-section {
  margin-bottom: 16px;
}

.price-big {
  font-size: 28px;
  color: var(--danger-color);
  font-weight: 700;
}

.price-big::before {
  content: '￥';
  font-size: 18px;
}

.price-original {
  margin-left: 8px;
  color: var(--text-quaternary);
  text-decoration: line-through;
}

.info-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.desc-section h3 {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 8px;
  color: var(--text-primary);
}

.desc-section p {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.8;
  white-space: pre-wrap;
}

.seller-section {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background 0.2s;
}

.seller-section:hover {
  background: var(--bg-color);
}

.seller-detail {
  flex: 1;
  overflow: hidden;
}

.seller-name {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 6px;
}

.seller-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.credit-badge {
  display: inline-flex;
  align-items: center;
  min-height: 32px;
  padding: 0 14px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 700;
  border: 1px solid transparent;
}

.credit-badge--safe {
  color: #1f7a35;
  background: #edf9f0;
  border-color: #b7e4c1;
}

.credit-badge--medium {
  color: #9a6700;
  background: #fff7e6;
  border-color: #f7d58a;
}

.credit-badge--danger {
  color: #c2412d;
  background: #fff0ed;
  border-color: #f2b8ae;
}

.report-entry {
  text-align: right;
  margin-top: 16px;
}

.report-entry .el-button {
  color: var(--text-quaternary);
  font-size: 12px;
}

.report-entry .el-button:hover {
  color: var(--drawbridge-red);
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

.action-buttons {
  display: flex;
  gap: 12px;
}

.action-buttons .el-button {
  flex: 1;
  height: 48px;
  font-size: 16px;
}

.buy-now-btn {
  background-color: var(--drawbridge-red) !important;
  border-color: var(--drawbridge-red) !important;
  color: #ffffff !important;
}

.buy-now-btn:hover {
  background-color: #e33e41 !important;
  border-color: #e33e41 !important;
}

.buy-now-btn.is-disabled {
  background-color: #f7c6c7 !important;
  border-color: #f7c6c7 !important;
}

.comments-section {
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  padding: 24px;
  margin-top: 24px;
  box-shadow: var(--shadow-sm);
}

.comments-section h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
}

.comment-input {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  align-items: flex-end;
}

.comment-input .el-input {
  flex: 1;
}

.comment-login-tip {
  text-align: center;
  padding: 12px;
  color: var(--text-tertiary);
  font-size: 14px;
}

.comment-login-tip a {
  color: var(--primary-color);
}

.empty-comments {
  text-align: center;
  padding: 32px;
  color: var(--text-quaternary);
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-item {
  display: flex;
  gap: 12px;
}

.comment-body {
  flex: 1;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.comment-name {
  font-size: 14px;
  font-weight: 500;
}

.comment-time {
  font-size: 12px;
  color: var(--text-quaternary);
}

.comment-content {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
}

.reply-list {
  background: var(--bg-color);
  border-radius: 6px;
  padding: 10px 12px;
  margin-top: 8px;
  font-size: 13px;
  color: var(--text-secondary);
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.reply-name {
  color: var(--primary-color);
  font-weight: 500;
}

.buy-price {
  margin-top: 8px;
}

.price {
  color: var(--danger-color);
  font-weight: 700;
}

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
  color: #1c1c1e !important;
}

:deep(.drawbridge-dialog) {
  border-radius: 20px !important;
  overflow: hidden;
}

:deep(.drawbridge-dialog .el-dialog__header) {
  padding-bottom: 0;
}

.save-addr-btn {
  background-color: #1c1c1e !important;
  border-color: #1c1c1e !important;
  color: #ffffff !important;
}

.save-addr-btn:hover {
  background-color: #333333 !important;
  border-color: #333333 !important;
}

@media (max-width: 768px) {
  .detail-content {
    flex-direction: column;
  }

  .detail-images {
    width: 100%;
  }

  .action-buttons {
    flex-direction: column;
  }
}
</style>
