<template>
  <div class="publish-page container">
    <div class="publish-card card">
      <h2><el-icon><EditPen /></el-icon> 发布闲置</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" size="large">
        <el-form-item label="商品标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题，描述你要卖什么" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="商品分类" prop="categoryId">
          <el-cascader v-model="form.categoryId" :options="categoryOptions" :props="{ value: 'id', label: 'name', emitPath: false }" placeholder="请选择分类" clearable style="width: 100%" />
        </el-form-item>
        <el-form-item label="出售价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" placeholder="0.00" controls-position="right" style="width: 200px" />
          <span style="margin-left: 8px; color: var(--text-tertiary)">元</span>
        </el-form-item>
        <el-form-item label="原价">
          <el-input-number v-model="form.originalPrice" :min="0" :precision="2" placeholder="选填" controls-position="right" style="width: 200px" />
          <span style="margin-left: 8px; color: var(--text-tertiary)">元（选填）</span>
        </el-form-item>
        <el-form-item label="成色" prop="quality">
          <el-radio-group v-model="form.quality">
            <el-radio :value="1">全新</el-radio>
            <el-radio :value="2">几乎全新</el-radio>
            <el-radio :value="3">轻微使用</el-radio>
            <el-radio :value="4">明显使用</el-radio>
            <el-radio :value="5">其他</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="交易方式">
          <el-radio-group v-model="form.tradeMode">
            <el-radio :value="1">邮寄</el-radio>
            <el-radio :value="2">自提/面交</el-radio>
            <el-radio :value="3">均可</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="所在地">
          <el-input v-model="form.location" placeholder="选填，如：北京市海淀区" />
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="5" placeholder="描述一下商品的详细情况，如购买渠道、使用时长、转手原因等" maxlength="2000" show-word-limit />
        </el-form-item>
        <el-form-item label="商品图片" prop="imageUrls">
          <el-upload
            v-model:file-list="fileList"
            action="/api/file/upload"
            list-type="picture-card"
            :on-success="handleUploadSuccess"
            :on-remove="handleUploadRemove"
            :limit="9"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">最多上传9张图片，第一张为封面图</div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handlePublish" :loading="loading" size="large" style="width: 200px">
            发布闲置
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { productApi, categoryApi } from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const fileList = ref([])
const categoryOptions = ref([])

const form = reactive({
  title: '', categoryId: null, price: null, originalPrice: null,
  quality: 2, tradeMode: 3, location: '', description: '', imageUrls: []
})

const rules = {
  title: [{ required: true, message: '请输入商品标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入出售价格', trigger: 'change' }],
  quality: [{ required: true, message: '请选择成色', trigger: 'change' }],
  description: [{ required: true, message: '请输入商品描述', trigger: 'blur' }],
}

onMounted(loadCategories)

async function loadCategories() {
  try {
    const res = await categoryApi.tree()
    categoryOptions.value = res.data || []
  } catch (e) { console.error(e) }
}

function handleUploadSuccess(response) {
  if (response.code === 200) {
    form.imageUrls.push(response.data.url)
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

function handleUploadRemove(file) {
  const url = file.response?.data?.url
  if (url) {
    const index = form.imageUrls.indexOf(url)
    if (index > -1) form.imageUrls.splice(index, 1)
  }
}

async function handlePublish() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (form.imageUrls.length === 0) {
    ElMessage.warning('请至少上传一张商品图片')
    return
  }
  loading.value = true
  try {
    await productApi.publish(form)
    ElMessage.success('发布成功，等待审核')
    router.push('/my/products')
  } catch (e) { /* handled */ }
  finally { loading.value = false }
}
</script>

<style scoped>
.publish-page { padding: 24px 20px; }
.publish-card { padding: 32px; max-width: 800px; margin: 0 auto; }
.publish-card h2 { font-size: 22px; font-weight: 600; margin-bottom: 28px; display: flex; align-items: center; gap: 8px; }
.upload-tip { font-size: 12px; color: var(--text-quaternary); margin-top: 8px; }
</style>
