<template>
  <div class="user-profile container" v-if="user">
    <div class="profile-header card" style="padding: 32px; text-align: center;">
      <el-avatar :size="80" :src="user.avatar || undefined">{{ user.nickname?.charAt(0) }}</el-avatar>
      <h2>{{ user.nickname }}</h2>
      <div class="profile-stats">
        <span>信用积分: <b>{{ user.creditScore }}</b></span>
        <el-divider direction="vertical" />
        <el-tag 
          :type="trustType" 
          size="small" 
          style="border-radius: 20px; font-weight: 600; padding: 0 12px;"
        >
          信用度: {{ trustScore }}%
        </el-tag>
        <el-divider direction="vertical" />
        <span>等级: <b>{{ levelText }}</b></span>
      </div>
      <div class="profile-actions" v-if="userStore.userId && userStore.userId !== user?.id">
        <el-button link @click="showReportDialog = true" class="report-link-btn">
          <el-icon><Warning /></el-icon> 举报该用户
        </el-button>
      </div>
    </div>

    <!-- 举报用户弹窗 -->
    <el-dialog v-model="showReportDialog" title="举报用户" width="420px" class="drawbridge-dialog">
      <el-form label-position="top">
        <el-form-item label="举报原因">
          <el-select v-model="reportForm.reasonType" placeholder="如头像违规、资料低俗等" style="width: 100%">
            <el-option label="头像/背景图违规" :value="10" />
            <el-option label="昵称/简介含低俗内容" :value="11" />
            <el-option label="虚假身份/诈骗" :value="12" />
            <el-option label="辱骂/不当言论" :value="13" />
            <el-option label="其他资料违规" :value="14" />
          </el-select>
        </el-form-item>
        <el-form-item label="详情描述">
          <el-input v-model="reportForm.reasonDetail" type="textarea" :rows="3" placeholder="请详细说明违规情况..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReportDialog = false" round>取消</el-button>
        <el-button type="primary" @click="submitReport" :loading="submittingReport" class="report-submit-btn">提交举报</el-button>
      </template>
    </el-dialog>
    <h3 style="margin: 24px 0 16px;">TA的商品</h3>
    <div v-if="products.length === 0"><el-empty description="暂无商品" /></div>
    <div v-else class="product-grid">
      <ProductCard v-for="p in products" :key="p.id" :product="p" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useRoute } from 'vue-router'
import { userApi, productApi, reportApi } from '../api'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import ProductCard from '../components/ProductCard.vue'

const route = useRoute()
const user = ref(null)
const products = ref([])
const levelMap = { 1: '普通用户', 2: '信用良好', 3: '优质用户' }
const levelText = computed(() => levelMap[user.value?.userLevel] || '普通用户')

// 信用度计算
const trustScore = computed(() => {
  if (!user.value?.riskScore) return 100
  return Math.max(0, Math.min(100, Math.round((1 - user.value.riskScore) * 100)))
})
const trustType = computed(() => {
  if (trustScore.value >= 80) return 'success'
  if (trustScore.value >= 50) return ''
  return 'warning'
})

// 举报相关
const userStore = useUserStore()
const showReportDialog = ref(false)
const submittingReport = ref(false)
const reportForm = reactive({
  reasonType: null,
  reasonDetail: ''
})

async function submitReport() {
  if (!reportForm.reasonType) { ElMessage.warning('请选择举报原因'); return }
  submittingReport.value = true
  try {
    await reportApi.submit({
      reportType: 2, // 资料性举报 (强制跳过 AI 开销)
      targetType: 2, // 举报用户
      targetId: user.value.id,
      reasonType: reportForm.reasonType,
      reasonDetail: reportForm.reasonDetail
    })
    ElMessage.success('举报成功，管理员将尽快处理')
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

onMounted(async () => {
  try {
    const r = await userApi.getDetail(route.params.id); user.value = r.data
    const p = await productApi.list({ userId: route.params.id, pageNum: 1, pageSize: 50 })
    products.value = p.data.list || []
  } catch(e){}
})
</script>

<style scoped>
.user-profile { padding: 24px 20px; }
.profile-header h2 { margin-top: 12px; font-size: 22px; }
.profile-stats { margin-top: 8px; color: var(--text-tertiary); }
.profile-stats b { color: var(--primary-color); }
.profile-actions { margin-top: 16px; border-top: 1px solid #f0f0f0; padding-top: 12px; }
.report-link-btn { color: var(--text-quaternary) !important; font-size: 13px; }
.report-link-btn:hover { color: var(--drawbridge-red) !important; }

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
