<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-banner">
        <div class="banner-content">
          <h1>闲置集市</h1>
          <p>让闲置流转，让价值延续</p>
          <div class="features">
            <div class="feature-item">
              <el-icon :size="24"><CircleCheck /></el-icon>
              <span>安全交易保障</span>
            </div>
            <div class="feature-item">
              <el-icon :size="24"><CircleCheck /></el-icon>
              <span>信用评价体系</span>
            </div>
            <div class="feature-item">
              <el-icon :size="24"><CircleCheck /></el-icon>
              <span>便捷发布管理</span>
            </div>
          </div>
        </div>
      </div>

      <div class="login-form-wrap">
        <h2>用户登录</h2>
        <el-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="handleLogin">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名" prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" @click="handleLogin" class="login-btn">
              登 录
            </el-button>
          </el-form-item>
        </el-form>
        <div class="form-footer">
          还没有账号？<router-link to="/register" class="link">立即注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { userApi } from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await userApi.login(form)
    userStore.setLoginData(res.data)
    ElMessage.success('登录成功')
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (e) {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e6f4ff 0%, #f5f5f5 50%, #e6f4ff 100%);
  padding: 20px;
}

.login-container {
  display: flex;
  background: var(--bg-white);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  overflow: hidden;
  max-width: 900px;
  width: 100%;
}

.login-banner {
  width: 400px;
  background: linear-gradient(135deg, #1677ff 0%, #0958d9 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.banner-content h1 {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 8px;
}

.banner-content p {
  font-size: 16px;
  opacity: 0.85;
  margin-bottom: 32px;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  opacity: 0.9;
}

.login-form-wrap {
  flex: 1;
  padding: 48px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-form-wrap h2 {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 32px;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
}

.form-footer {
  text-align: center;
  margin-top: 16px;
  color: var(--text-tertiary);
  font-size: 14px;
}

.form-footer .link {
  color: var(--primary-color);
  font-weight: 500;
}

.form-footer .link:hover {
  text-decoration: underline;
}

@media (max-width: 768px) {
  .login-banner { display: none; }
  .login-form-wrap { padding: 32px 24px; }
}
</style>
