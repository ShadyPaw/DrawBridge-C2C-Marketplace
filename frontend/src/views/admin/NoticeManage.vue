<template>
  <div>
    <div class="header-row"><h2>公告管理</h2><el-button type="primary" @click="showAdd">发布公告</el-button></div>
    <el-table :data="list" border stripe style="width: 100%; margin-top: 16px;">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">{{ { 1:'系统公告',2:'交易通知',3:'审核通知',4:'举报通知' }[row.type] || '其他' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '正常' : '下架' }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="createTime" label="发布时间" width="170" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" @click="showEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total" layout="total, prev, pager, next" @current-change="loadData" style="margin-top: 16px; justify-content: flex-end;" />

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑公告' : '发布公告'" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.type" style="width: 100%"><el-option label="系统公告" :value="1" /><el-option label="交易通知" :value="2" /></el-select>
        </el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="5" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="正常" inactive-text="下架" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="handleSave">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { adminApi } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'
const list = ref([]); const pageNum = ref(1); const pageSize = ref(10); const total = ref(0)
const dialogVisible = ref(false); const isEdit = ref(false)
const form = reactive({ id: null, title: '', content: '', type: 1, status: 1 })
onMounted(loadData)
async function loadData() { try { const r = await adminApi.noticeList({ pageNum: pageNum.value, pageSize: pageSize.value }); list.value = r.data.list || []; total.value = r.data.total || 0 } catch(e){} }
function showAdd() { isEdit.value = false; Object.assign(form, { id: null, title: '', content: '', type: 1, status: 1 }); dialogVisible.value = true }
function showEdit(row) { isEdit.value = true; Object.assign(form, row); dialogVisible.value = true }
async function handleSave() {
  try { if (isEdit.value) await adminApi.updateNotice(form); else await adminApi.addNotice(form); ElMessage.success('保存成功'); dialogVisible.value = false; loadData() } catch(e){}
}
async function handleDelete(id) { await ElMessageBox.confirm('确定删除？','提示'); try { await adminApi.deleteNotice(id); ElMessage.success('删除成功'); loadData() } catch(e){} }
</script>

<style scoped>.header-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }</style>
