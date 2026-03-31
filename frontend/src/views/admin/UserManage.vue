<template>
  <div>
    <h2 style="margin-bottom: 20px;">用户管理</h2>
    <div class="toolbar"><el-input v-model="keyword" placeholder="搜索用户名/昵称/手机号" clearable @clear="loadData" style="width: 300px" /><el-button type="primary" @click="loadData">搜索</el-button></div>
    <el-table :data="list" border stripe style="width: 100%; margin-top: 16px;">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column prop="phone" label="手机" width="130" />
      <el-table-column prop="creditScore" label="信用分" width="80" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="170" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button v-if="row.status === 1" size="small" type="danger" @click="toggleStatus(row.id, 0)">禁用</el-button>
          <el-button v-else size="small" type="success" @click="toggleStatus(row.id, 1)">启用</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total" layout="total, prev, pager, next" @current-change="loadData" style="margin-top: 16px; justify-content: flex-end;" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '../../api'
import { ElMessage } from 'element-plus'
const list = ref([]); const keyword = ref(''); const pageNum = ref(1); const pageSize = ref(10); const total = ref(0)
onMounted(loadData)
async function loadData() { try { const r = await adminApi.userList({ keyword: keyword.value, pageNum: pageNum.value, pageSize: pageSize.value }); list.value = r.data.list || []; total.value = r.data.total || 0 } catch(e){} }
async function toggleStatus(id, status) { try { await adminApi.updateUserStatus(id, status); ElMessage.success('操作成功'); loadData() } catch(e){} }
</script>

<style scoped>.toolbar { display: flex; gap: 12px; }</style>
