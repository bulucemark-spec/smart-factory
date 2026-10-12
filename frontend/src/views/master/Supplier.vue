<template>
  <el-card>
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="编码/名称" style="width: 240px" clearable @keyup.enter="load" />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="success" @click="openAdd">新增供应商</el-button>
    </div>
    <el-table :data="pageData.records" border v-loading="loading">
      <el-table-column prop="code" label="供应商编码" width="150" />
      <el-table-column prop="name" label="供应商名称" />
      <el-table-column prop="contact" label="联系人" width="120" />
      <el-table-column prop="phone" label="电话" width="150" />
      <el-table-column label="等级" width="90">
        <template #default="{ row }">
          <el-tag :type="row.level === 'A' ? 'success' : row.level === 'B' ? 'warning' : 'info'">{{ row.level }}级</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination class="pager" layout="total, prev, pager, next" :total="pageData.total"
      v-model:current-page="page" @current-change="load" />

    <el-dialog v-model="dialog" :title="form.id ? '编辑供应商' : '新增供应商'" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="编码"><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="联系人"><el-input v-model="form.contact" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="等级">
          <el-select v-model="form.level">
            <el-option label="A级" value="A" /><el-option label="B级" value="B" /><el-option label="C级" value="C" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'

const keyword = ref('')
const page = ref(1)
const loading = ref(false)
const dialog = ref(false)
const pageData = reactive<{ records: any[]; total: number }>({ records: [], total: 0 })
const form = reactive<any>({})

onMounted(load)

async function load() {
  loading.value = true
  try {
    const data: any = await request.get('/system/suppliers', { params: { page: page.value, size: 10, keyword: keyword.value } })
    pageData.records = data.records
    pageData.total = data.total
  } finally {
    loading.value = false
  }
}
function openAdd() {
  Object.assign(form, { id: null, code: '', name: '', contact: '', phone: '', level: 'B', status: 1 })
  dialog.value = true
}
function openEdit(row: any) { Object.assign(form, row); dialog.value = true }
async function save() {
  await request.put('/system/suppliers', form)
  ElMessage.success('保存成功'); dialog.value = false; load()
}
async function remove(row: any) {
  await ElMessageBox.confirm(`确认删除供应商 ${row.name}？`, '提示', { type: 'warning' })
  await request.delete(`/system/suppliers/${row.id}`)
  ElMessage.success('已删除'); load()
}
</script>

<style scoped>
.toolbar { display: flex; gap: 8px; margin-bottom: 12px; }
.pager { margin-top: 12px; justify-content: flex-end; }
</style>
