<template>
  <div>
    <h2 style="margin-bottom: 20px;">用户管理</h2>
    <div class="toolbar"><el-input v-model="keyword" placeholder="搜索用户名/昵称/手机号" clearable @clear="loadData" style="width: 300px" /><el-button type="primary" @click="loadData">搜索</el-button></div>
    <el-table :data="list" border stripe style="width: 100%; margin-top: 16px;">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column prop="phone" label="手机" width="130" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="AI 风险评估" width="160">
        <template #default="{ row }">
          <el-popover placement="top" :width="200" trigger="hover">
            <template #reference>
              <el-tag 
                :class="['risk-tag', row.riskScore > 0.8 ? 'risk-high' : 'risk-safe']"
                size="large"
              >
                {{ row.riskScore > 0.8 ? '高危违规风险' : '系统核验通过' }}
              </el-tag>
            </template>
            <div style="text-align: center;">
              <p style="margin: 0; font-weight: bold; color: #666;">实时风险指数</p>
              <p :style="{ margin: '8px 0 0', fontSize: '24px', fontWeight: '900', color: row.riskScore > 0.8 ? '#E33E41' : '#67C23A' }">
                {{ (row.riskScore * 100).toFixed(1) }}%
              </p>
            </div>
          </el-popover>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="200" />
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

<style scoped>
.toolbar { display: flex; gap: 12px; }

/* Drawbridge 工业风 - 风险标签样式 */
.risk-tag {
  border-radius: 20px !important;
  border: none !important;
  font-weight: 700;
  padding: 0 16px;
  height: 32px;
  line-height: 30px;
  cursor: help;
  transition: transform 0.2s;
}

.risk-tag:hover {
  transform: translateY(-1px);
}

.risk-high {
  background-color: #E33E41 !important;
  color: #fff !important;
  box-shadow: 0 4px 12px rgba(227, 62, 65, 0.3);
}

.risk-safe {
  background-color: #f0f9eb !important;
  color: #67c23a !important;
  border: 1px solid #e1f3d8 !important;
}
</style>
