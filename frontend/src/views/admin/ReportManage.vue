<template>
  <div>
    <h2 style="margin-bottom: 20px;">举报管理</h2>
    <div class="toolbar">
      <el-select v-model="handleStatus" placeholder="处理状态" clearable style="width: 140px" @change="loadData">
        <el-option label="待处理" :value="0" /><el-option label="处理中" :value="1" /><el-option label="已处理" :value="2" /><el-option label="已驳回" :value="3" />
      </el-select>
      <el-select v-model="targetType" placeholder="举报类型" clearable style="width: 140px" @change="loadData">
        <el-option label="商品" :value="1" /><el-option label="用户" :value="2" /><el-option label="留言" :value="3" />
      </el-select>
      <el-button type="primary" @click="loadData">搜索</el-button>
    </div>
    <el-table :data="list" border stripe style="width: 100%; margin-top: 16px;">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="reporterNickname" label="举报人" width="100" />
      <el-table-column label="类型" width="80">
        <template #default="{ row }">{{ { 1:'商品',2:'用户',3:'留言' }[row.targetType] }}</template>
      </el-table-column>
      <el-table-column prop="targetId" label="目标ID" width="80" />
      <el-table-column label="原因" width="100">
        <template #default="{ row }">{{ { 1:'虚假信息',2:'违禁物品',3:'欺诈行为',4:'不当言论',5:'其他' }[row.reasonType] }}</template>
      </el-table-column>
      <el-table-column prop="reasonDetail" label="详细说明" min-width="180" show-overflow-tooltip />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="{ 0:'warning',1:'',2:'success',3:'info' }[row.handleStatus]" size="small">{{ { 0:'待处理',1:'处理中',2:'已处理',3:'已驳回' }[row.handleStatus] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="举报时间" width="170" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <template v-if="row.handleStatus === 0 || row.handleStatus === 1">
            <el-button size="small" type="success" @click="handleReport(row.id, 2)">处理</el-button>
            <el-button size="small" @click="handleReport(row.id, 3)">驳回</el-button>
          </template>
          <span v-else class="handled-text">{{ row.handleResult || '已处理' }}</span>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="pageNum" :page-size="pageSize" :total="total" layout="total, prev, pager, next" @current-change="loadData" style="margin-top: 16px; justify-content: flex-end;" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'
const list = ref([]); const handleStatus = ref(null); const targetType = ref(null); const pageNum = ref(1); const pageSize = ref(10); const total = ref(0)
onMounted(loadData)
async function loadData() { try { const r = await adminApi.reportList({ handleStatus: handleStatus.value, targetType: targetType.value, pageNum: pageNum.value, pageSize: pageSize.value }); list.value = r.data.list || []; total.value = r.data.total || 0 } catch(e){} }
async function handleReport(id, status) {
  const title = status === 2 ? '处理举报' : '驳回举报'
  const { value } = await ElMessageBox.prompt('请输入处理说明', title, { inputPlaceholder: '处理结果说明' }).catch(() => ({ value: null }))
  if (value === null) return
  try { await adminApi.handleReport(id, { handleStatus: status, handleResult: value }); ElMessage.success('操作成功'); loadData() } catch(e){}
}
</script>

<style scoped>.toolbar { display: flex; gap: 12px; } .handled-text { font-size: 12px; color: var(--text-tertiary); }</style>
