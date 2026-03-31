<template>
  <div>
    <div class="header-row"><h2 class="page-title">收货地址</h2><el-button type="primary" @click="showAdd">添加地址</el-button></div>
    <div v-if="addresses.length === 0"><el-empty description="暂无收货地址" /></div>
    <div v-else class="addr-list">
      <div v-for="a in addresses" :key="a.id" class="addr-item">
        <div class="addr-info">
          <span class="addr-name">{{ a.receiverName }}</span>
          <span class="addr-phone">{{ a.receiverPhone }}</span>
          <el-tag v-if="a.isDefault === 1" size="small" type="primary">默认</el-tag>
          <div class="addr-detail">{{ a.province }}{{ a.city }}{{ a.district || '' }}{{ a.detailAddress }}</div>
        </div>
        <div class="addr-actions">
          <el-button size="small" @click="showEdit(a)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(a.id)">删除</el-button>
        </div>
      </div>
    </div>
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑地址' : '添加地址'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="姓名" prop="receiverName"><el-input v-model="form.receiverName" /></el-form-item>
        <el-form-item label="电话" prop="receiverPhone"><el-input v-model="form.receiverPhone" /></el-form-item>
        <el-form-item label="省份" prop="province"><el-input v-model="form.province" /></el-form-item>
        <el-form-item label="城市" prop="city"><el-input v-model="form.city" /></el-form-item>
        <el-form-item label="区/县"><el-input v-model="form.district" /></el-form-item>
        <el-form-item label="详细地址" prop="detailAddress"><el-input v-model="form.detailAddress" /></el-form-item>
        <el-form-item label="默认地址"><el-switch v-model="form.isDefault" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { addressApi } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const addresses = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref()
const form = reactive({ id: null, receiverName: '', receiverPhone: '', province: '', city: '', district: '', detailAddress: '', isDefault: 0 })
const rules = {
  receiverName: [{ required: true, message: '请输入姓名' }],
  receiverPhone: [{ required: true, message: '请输入电话' }],
  province: [{ required: true, message: '请输入省份' }],
  city: [{ required: true, message: '请输入城市' }],
  detailAddress: [{ required: true, message: '请输入详细地址' }],
}

onMounted(loadAddresses)
async function loadAddresses() { try { const r = await addressApi.list(); addresses.value = r.data || [] } catch(e){} }

function showAdd() { isEdit.value = false; Object.assign(form, { id: null, receiverName: '', receiverPhone: '', province: '', city: '', district: '', detailAddress: '', isDefault: 0 }); dialogVisible.value = true }
function showEdit(a) { isEdit.value = true; Object.assign(form, a); dialogVisible.value = true }

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (isEdit.value) await addressApi.update(form); else await addressApi.add(form)
    ElMessage.success('保存成功'); dialogVisible.value = false; loadAddresses()
  } catch(e){} finally { saving.value = false }
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确定删除？', '提示')
  try { await addressApi.delete(id); ElMessage.success('删除成功'); loadAddresses() } catch(e){}
}
</script>

<style scoped>
.page-title { font-size: 20px; font-weight: 600; }
.header-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.addr-list { display: flex; flex-direction: column; gap: 12px; }
.addr-item { display: flex; justify-content: space-between; align-items: center; padding: 16px; border: 1px solid var(--border-color); border-radius: var(--radius-md); }
.addr-name { font-weight: 600; margin-right: 12px; }
.addr-phone { color: var(--text-secondary); margin-right: 8px; }
.addr-detail { margin-top: 6px; font-size: 13px; color: var(--text-tertiary); }
</style>
