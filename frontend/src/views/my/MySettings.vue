<template>
  <div>
    <h2 class="page-title">账号设置</h2>
    <el-tabs>
      <el-tab-pane label="修改资料">
        <el-form :model="profileForm" label-width="80px" style="max-width: 500px; margin-top: 16px;">
          <el-form-item label="昵称"><el-input v-model="profileForm.nickname" /></el-form-item>
          <el-form-item label="手机号"><el-input v-model="profileForm.phone" /></el-form-item>
          <el-form-item label="邮箱"><el-input v-model="profileForm.email" /></el-form-item>
          <el-form-item label="性别">
            <el-radio-group v-model="profileForm.gender"><el-radio :value="1">男</el-radio><el-radio :value="2">女</el-radio><el-radio :value="0">保密</el-radio></el-radio-group>
          </el-form-item>
          <el-form-item><el-button type="primary" @click="saveProfile" :loading="saving">保存修改</el-button></el-form-item>
        </el-form>
      </el-tab-pane>
      <el-tab-pane label="修改密码">
        <el-form ref="pwdRef" :model="pwdForm" :rules="pwdRules" label-width="100px" style="max-width: 500px; margin-top: 16px;">
          <el-form-item label="原密码" prop="oldPassword"><el-input v-model="pwdForm.oldPassword" type="password" show-password /></el-form-item>
          <el-form-item label="新密码" prop="newPassword"><el-input v-model="pwdForm.newPassword" type="password" show-password /></el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword"><el-input v-model="pwdForm.confirmPassword" type="password" show-password /></el-form-item>
          <el-form-item><el-button type="primary" @click="changePassword" :loading="changingPwd">修改密码</el-button></el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { userApi } from '../../api'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const saving = ref(false)
const changingPwd = ref(false)
const pwdRef = ref()

const profileForm = reactive({ nickname: '', phone: '', email: '', gender: 0 })
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const validateConfirm = (r, v, cb) => { v !== pwdForm.newPassword ? cb(new Error('两次密码不一致')) : cb() }
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码' }],
  newPassword: [{ required: true, message: '请输入新密码' }, { min: 6, message: '不少于6位' }],
  confirmPassword: [{ required: true, message: '请确认新密码' }, { validator: validateConfirm, trigger: 'blur' }]
}

onMounted(async () => {
  try { const r = await userApi.getInfo(); Object.assign(profileForm, { nickname: r.data.nickname, phone: r.data.phone, email: r.data.email, gender: r.data.gender || 0 }) } catch(e){}
})

async function saveProfile() {
  saving.value = true
  try { const r = await userApi.update(profileForm); userStore.updateUser(r.data); ElMessage.success('保存成功') } catch(e){} finally { saving.value = false }
}

async function changePassword() {
  const valid = await pwdRef.value.validate().catch(() => false)
  if (!valid) return
  changingPwd.value = true
  try { await userApi.updatePassword(pwdForm); ElMessage.success('密码修改成功'); Object.assign(pwdForm, { oldPassword: '', newPassword: '', confirmPassword: '' }) } catch(e){} finally { changingPwd.value = false }
}
</script>

<style scoped>
.page-title { font-size: 20px; font-weight: 600; margin-bottom: 16px; }
</style>
