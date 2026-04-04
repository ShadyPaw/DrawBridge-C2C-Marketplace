<template>
  <div>
    <h2 class="page-title">个人中心</h2>
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-number">{{ myProducts.length }}</div>
        <div class="stat-label">发布的商品</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">{{ levelText }}</div>
        <div class="stat-label">用户等级</div>
      </div>
      <div class="stat-card">
        <div :class="['credit-pill', creditTone.className]">信用分 {{ displayCreditScore }}</div>
        <div class="stat-label">当前信用状态</div>
      </div>
    </div>
    <el-divider />
    <h3>个人信息</h3>
    <el-descriptions :column="2" border style="margin-top: 16px">
      <el-descriptions-item label="用户名">{{ user?.username }}</el-descriptions-item>
      <el-descriptions-item label="昵称">{{ user?.nickname || '-' }}</el-descriptions-item>
      <el-descriptions-item label="手机号">{{ user?.phone || '-' }}</el-descriptions-item>
      <el-descriptions-item label="邮箱">{{ user?.email || '-' }}</el-descriptions-item>
      <el-descriptions-item label="性别">{{ genderText }}</el-descriptions-item>
      <el-descriptions-item label="注册时间">{{ formatTime(user?.createTime) }}</el-descriptions-item>
    </el-descriptions>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { productApi, userApi } from '../../api'

const user = ref(null)
const myProducts = ref([])

const levelMap = { 1: '普通用户', 2: '信用良好', 3: '优质用户' }
const genderMap = { 0: '未知', 1: '男', 2: '女' }

const levelText = computed(() => levelMap[user.value?.userLevel] || '普通用户')
const genderText = computed(() => genderMap[user.value?.gender ?? 0])
const displayCreditScore = computed(() => user.value?.creditScore ?? 0)
const creditTone = computed(() => getCreditTone(displayCreditScore.value))

function getCreditTone(score) {
  if (score >= 100) {
    return { className: 'credit-pill--safe' }
  }
  if (score >= 60) {
    return { className: 'credit-pill--medium' }
  }
  return { className: 'credit-pill--danger' }
}

onMounted(async () => {
  try {
    const [userRes, productRes] = await Promise.all([
      userApi.getInfo(),
      productApi.myProducts()
    ])
    user.value = userRes.data
    myProducts.value = productRes.data || []
  } catch (error) {
    console.error('加载个人中心失败:', error)
  }
})

function formatTime(time) {
  return time ? new Date(time).toLocaleDateString('zh-CN') : '-'
}
</script>

<style scoped>
.page-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
}

.stats-row {
  display: flex;
  gap: 16px;
}

.stat-card {
  flex: 1;
  text-align: center;
  padding: 20px;
  background: var(--bg-color);
  border-radius: var(--radius-md);
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: var(--primary-color);
}

.credit-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 40px;
  padding: 0 18px;
  border-radius: 999px;
  font-size: 18px;
  font-weight: 700;
  border: 1px solid transparent;
}

.credit-pill--safe {
  color: #1f7a35;
  background: #edf9f0;
  border-color: #b7e4c1;
}

.credit-pill--medium {
  color: #9a6700;
  background: #fff7e6;
  border-color: #f7d58a;
}

.credit-pill--danger {
  color: #c2412d;
  background: #fff0ed;
  border-color: #f2b8ae;
}

.stat-label {
  font-size: 13px;
  color: var(--text-tertiary);
  margin-top: 8px;
}

h3 {
  font-size: 16px;
  font-weight: 600;
}
</style>
