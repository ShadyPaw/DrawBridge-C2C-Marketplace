<template>
  <div>
    <h2 class="page-title">个人中心</h2>
    <div class="stats-row">
      <div class="stat-card"><div class="stat-number">{{ myProducts.length }}</div><div class="stat-label">发布的商品</div></div>
      <div class="stat-card"><div class="stat-number">{{ user?.creditScore || 100 }}</div><div class="stat-label">信用积分</div></div>
      <div class="stat-card"><div class="stat-number">{{ levelText }}</div><div class="stat-label">用户等级</div></div>
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
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { userApi, productApi } from '../../api'

const userStore = useUserStore()
const user = ref(null)
const myProducts = ref([])

const levelMap = { 1: '普通用户', 2: '信用良好', 3: '优质用户' }
const levelText = computed(() => levelMap[user.value?.userLevel] || '普通用户')
const genderMap = { 0: '未知', 1: '男', 2: '女' }
const genderText = computed(() => genderMap[user.value?.gender ?? 0])

onMounted(async () => {
  try {
    const res = await userApi.getInfo()
    user.value = res.data
    const pRes = await productApi.myProducts()
    myProducts.value = pRes.data || []
  } catch (e) { console.error(e) }
})

function formatTime(t) { return t ? new Date(t).toLocaleDateString('zh-CN') : '-' }
</script>

<style scoped>
.page-title { font-size: 20px; font-weight: 600; margin-bottom: 20px; }
.stats-row { display: flex; gap: 16px; }
.stat-card { flex: 1; text-align: center; padding: 20px; background: var(--bg-color); border-radius: var(--radius-md); }
.stat-number { font-size: 28px; font-weight: 700; color: var(--primary-color); }
.stat-label { font-size: 13px; color: var(--text-tertiary); margin-top: 4px; }
h3 { font-size: 16px; font-weight: 600; }
</style>
