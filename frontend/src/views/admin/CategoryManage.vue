<template>
  <div>
    <div class="header-row"><h2>分类管理</h2><el-button type="primary" @click="showAdd(0)">添加顶级分类</el-button></div>
    <el-table :data="treeData" border row-key="id" :tree-props="{ children: 'children' }" style="width: 100%; margin-top: 16px;">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="分类名称" min-width="200" />
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="240">
        <template #default="{ row }">
          <el-button size="small" @click="showEdit(row)">编辑</el-button>
          <el-button v-if="row.parentId === 0" size="small" type="primary" @click="showAdd(row.id)">添加子分类</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '添加分类'" width="480px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="分类名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="排序号"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="handleSave">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { categoryApi, adminApi } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'
const treeData = ref([]); const dialogVisible = ref(false); const isEdit = ref(false)
const form = reactive({ id: null, parentId: 0, name: '', sortOrder: 0, status: 1 })
onMounted(loadData)
async function loadData() { try { const r = await categoryApi.tree(); treeData.value = r.data || [] } catch(e){} }
function showAdd(parentId) { isEdit.value = false; Object.assign(form, { id: null, parentId, name: '', sortOrder: 0, status: 1 }); dialogVisible.value = true }
function showEdit(row) { isEdit.value = true; Object.assign(form, { id: row.id, parentId: row.parentId, name: row.name, sortOrder: row.sortOrder, status: row.status }); dialogVisible.value = true }
async function handleSave() {
  if (!form.name.trim()) { ElMessage.warning('请输入分类名称'); return }
  try { if (isEdit.value) await adminApi.updateCategory(form); else await adminApi.addCategory(form); ElMessage.success('保存成功'); dialogVisible.value = false; loadData() } catch(e){}
}
async function handleDelete(id) { await ElMessageBox.confirm('确定删除？删除后关联的子分类也会被影响','提示',{ type: 'warning' }); try { await adminApi.deleteCategory(id); ElMessage.success('删除成功'); loadData() } catch(e){} }
</script>

<style scoped>.header-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }</style>
