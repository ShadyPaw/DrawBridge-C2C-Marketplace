<template>
  <div class="user-profile container" v-if="user">
    <div class="profile-header card">
      <el-avatar :size="88" :src="user.avatar || undefined">{{ user.nickname?.charAt(0) }}</el-avatar>
      <h2>{{ user.nickname }}</h2>
      <div class="profile-badges">
        <span :class="['credit-badge', creditTone.className]">信用分 {{ displayCreditScore }}</span>
        <el-button
          v-if="user?.id && userStore.userId !== user.id"
          class="chat-btn"
          round
          @click="goChat"
        >
          私聊
        </el-button>
      </div>
      <div class="profile-actions" v-if="userStore.userId && userStore.userId !== user?.id">
        <el-button link @click="showReportDialog = true" class="report-link-btn">举报该用户</el-button>
      </div>
    </div>

    <el-dialog v-model="showReportDialog" title="举报用户" width="420px" class="drawbridge-dialog">
      <el-form label-position="top">
        <el-form-item label="举报原因">
          <el-select v-model="reportForm.reasonType" placeholder="请选择举报原因" style="width: 100%">
            <el-option label="头像或背景图违规" :value="10" />
            <el-option label="昵称或简介含低俗内容" :value="11" />
            <el-option label="虚假身份或诈骗" :value="12" />
            <el-option label="骚扰或不当言论" :value="13" />
            <el-option label="其他资料违规" :value="14" />
          </el-select>
        </el-form-item>
        <el-form-item label="详情描述">
          <el-input
            v-model="reportForm.reasonDetail"
            type="textarea"
            :rows="3"
            placeholder="请补充违规细节，便于管理员核查"
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

    <h3 class="section-title">TA 发布的商品</h3>
    <div v-if="products.length === 0">
      <el-empty description="暂未发布商品" />
    </div>
    <div v-else class="product-grid">
      <ProductCard v-for="item in products" :key="item.id" :product="item" />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { productApi, reportApi, userApi } from '../api'
import ProductCard from '../components/ProductCard.vue'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const user = ref(null)
const products = ref([])
const showReportDialog = ref(false)
const submittingReport = ref(false)
const reportForm = reactive({
  reasonType: null,
  reasonDetail: ''
})

const displayCreditScore = computed(() => user.value?.creditScore ?? 0)
const creditTone = computed(() => getCreditTone(displayCreditScore.value))

function getCreditTone(score) {
  if (score >= 100) {
    return { className: 'credit-badge--safe' }
  }
  if (score >= 60) {
    return { className: 'credit-badge--medium' }
  }
  return { className: 'credit-badge--danger' }
}

function goChat() {
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }

  router.push({
    path: '/chat',
    query: {
      userId: user.value.id
    }
  })
}

async function submitReport() {
  if (!reportForm.reasonType) {
    ElMessage.warning('请选择举报原因')
    return
  }

  submittingReport.value = true
  try {
    await reportApi.submit({
      reportType: 2,
      targetType: 2,
      targetId: user.value.id,
      reasonType: reportForm.reasonType,
      reasonDetail: reportForm.reasonDetail
    })
    ElMessage.success('举报已提交，管理员会尽快处理')
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

onMounted(async () => {
  try {
    const [userRes, productRes] = await Promise.all([
      userApi.getDetail(route.params.id),
      productApi.list({ userId: route.params.id, pageNum: 1, pageSize: 50 })
    ])
    user.value = userRes.data
    products.value = productRes.data.list || []
  } catch (error) {
    console.error('加载用户主页失败:', error)
  }
})
</script>

<style scoped>
.user-profile {
  padding: 24px 20px;
}

.profile-header {
  padding: 32px;
  text-align: center;
}

.profile-header h2 {
  margin: 16px 0 0;
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
}

.profile-badges {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
  flex-wrap: wrap;
}

.credit-badge {
  display: inline-flex;
  align-items: center;
  min-height: 34px;
  padding: 0 16px;
  border-radius: 999px;
  font-size: 14px;
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

.chat-btn {
  height: 34px;
  padding: 0 18px;
  border: 1px solid #1c1c1e !important;
  color: #1c1c1e !important;
  font-weight: 600;
  background: #ffffff !important;
}

.chat-btn:hover {
  background: #f5f5f5 !important;
}

.profile-actions {
  margin-top: 18px;
  padding-top: 14px;
  border-top: 1px solid #f0f0f0;
}

.report-link-btn {
  color: var(--text-quaternary) !important;
  font-size: 13px;
}

.report-link-btn:hover {
  color: var(--drawbridge-red) !important;
}

.section-title {
  margin: 24px 0 16px;
  font-size: 18px;
  font-weight: 600;
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
</style>
