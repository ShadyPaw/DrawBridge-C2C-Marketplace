<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-banner">
        <div class="banner-content">
          <h1>加入闲置集市</h1>
          <p>开始你的闲置交易之旅</p>
          <div class="features">
            <div class="feature-item"><el-icon :size="24"><CircleCheck /></el-icon><span>快速发布闲置</span></div>
            <div class="feature-item"><el-icon :size="24"><CircleCheck /></el-icon><span>海量好物等你淘</span></div>
            <div class="feature-item"><el-icon :size="24"><CircleCheck /></el-icon><span>绿色循环生活</span></div>
          </div>
        </div>
      </div>

      <div class="login-form-wrap">
        <h2>用户注册</h2>
        <el-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="handleRegister">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名（3-20个字符）" prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="请输入密码（6位以上）" prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item prop="nickname">
            <el-input v-model="form.nickname" placeholder="请输入昵称（选填）" prefix-icon="UserFilled" />
          </el-form-item>
          <el-form-item prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号（选填）" prefix-icon="Phone" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" @click="handleRegister" class="login-btn">注 册</el-button>
          </el-form-item>
        </el-form>
        <div class="form-footer">
          已有账号？<router-link to="/login" class="link">去登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '', password: '', confirmPassword: '', nickname: '', phone: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== form.password) callback(new Error('两次密码不一致'))
  else callback()
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码不少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userApi.register(form)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e) { /* handled */ }
  finally { loading.value = false }
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
.login-container { display: flex; background: var(--bg-white); border-radius: var(--radius-xl); box-shadow: var(--shadow-lg); overflow: hidden; max-width: 900px; width: 100%; }
.login-banner { width: 400px; background: linear-gradient(135deg, #1677ff 0%, #0958d9 100%); color: #fff; display: flex; align-items: center; justify-content: center; padding: 40px; }
.banner-content h1 { font-size: 28px; font-weight: 700; margin-bottom: 8px; }
.banner-content p { font-size: 16px; opacity: 0.85; margin-bottom: 32px; }
.features { display: flex; flex-direction: column; gap: 16px; }
.feature-item { display: flex; align-items: center; gap: 10px; font-size: 15px; opacity: 0.9; }
.login-form-wrap { flex: 1; padding: 40px; display: flex; flex-direction: column; justify-content: center; }
.login-form-wrap h2 { font-size: 24px; font-weight: 600; color: var(--text-primary); margin-bottom: 28px; }
.login-btn { width: 100%; height: 44px; font-size: 16px; }
.form-footer { text-align: center; margin-top: 16px; color: var(--text-tertiary); font-size: 14px; }
.form-footer .link { color: var(--primary-color); font-weight: 500; }
@media (max-width: 768px) { .login-banner { display: none; } .login-form-wrap { padding: 24px; } }
</style>
