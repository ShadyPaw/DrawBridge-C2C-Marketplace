<template>
  <div>
    <h2 style="margin-bottom: 20px;">商品审核</h2>
    <div class="toolbar">
      <el-select v-model="auditStatus" placeholder="审核状态" clearable style="width: 140px" @change="loadData"><el-option label="待审核" :value="0" /><el-option label="已通过" :value="1" /><el-option label="已拒绝" :value="2" /></el-select>
      <el-input v-model="keyword" placeholder="搜索商品标题" clearable @clear="loadData" style="width: 250px" />
      <el-button type="primary" @click="loadData">搜索</el-button>
    </div>
    <el-table :data="list" border stripe style="width: 100%; margin-top: 16px;">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="price" label="价格" width="100"><template #default="{ row }">¥{{ row.price }}</template></el-table-column>
      <el-table-column prop="sellerNickname" label="发布者" width="120" />
      <el-table-column label="审核状态" width="100">
        <template #default="{ row }"><el-tag :type="{ 0:'warning', 1:'success', 2:'danger' }[row.auditStatus]" size="small">{{ { 0:'待审核', 1:'已通过', 2:'已拒绝' }[row.auditStatus] }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="createTime" label="发布时间" width="170" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <template v-if="row.auditStatus === 0">
            <el-button size="small" type="success" @click="audit(row.id, 1)">通过</el-button>
            <el-button size="small" type="danger" @click="rejectDialog(row.id)">拒绝</el-button>
          </template>
          <el-button size="small" @click="$router.push(`/product/${row.id}`)">查看</el-button>
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
const list = ref([]); const keyword = ref(''); const auditStatus = ref(null); const pageNum = ref(1); const pageSize = ref(10); const total = ref(0)
onMounted(loadData)
async function loadData() { try { const r = await adminApi.productList({ keyword: keyword.value, auditStatus: auditStatus.value, pageNum: pageNum.value, pageSize: pageSize.value }); list.value = r.data.list || []; total.value = r.data.total || 0 } catch(e){} }
async function audit(id, status) { try { await adminApi.auditProduct(id, { auditStatus: status, auditRemark: '' }); ElMessage.success('审核通过'); loadData() } catch(e){} }
async function rejectDialog(id) { const { value } = await ElMessageBox.prompt('请输入拒绝原因', '审核拒绝').catch(() => ({ value: null })); if (value === null) return; try { await adminApi.auditProduct(id, { auditStatus: 2, auditRemark: value }); ElMessage.success('已拒绝'); loadData() } catch(e){} }
</script>

<style scoped>.toolbar { display: flex; gap: 12px; }</style>
