<template>
  <el-card>
    <el-form inline>
      <el-form-item label="仓库">
        <el-select v-model="query.warehouseId" clearable placeholder="全部" style="width: 150px">
          <el-option v-for="w in warehouses" :key="w.id" :label="w.name" :value="w.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable placeholder="全部" style="width: 120px">
          <el-option label="待检" value="PENDING" /><el-option label="合格" value="QUALIFIED" />
          <el-option label="冻结" value="FROZEN" /><el-option label="报废" value="SCRAP" />
        </el-select>
      </el-form-item>
      <el-form-item label="批号"><el-input v-model="query.batchNo" clearable /></el-form-item>
      <el-form-item label="关键字"><el-input v-model="query.keyword" clearable /></el-form-item>
      <el-form-item><el-button type="primary" @click="load">查询</el-button></el-form-item>
    </el-form>

    <el-row :gutter="12" style="margin-bottom: 12px">
      <el-col :span="4" v-for="s in summary" :key="s.status">
        <el-card shadow="never" class="sum">
          <div class="sum-name">{{ statusName(s.status) }}</div>
          <div class="sum-qty">{{ s.qty }}</div>
          <div class="sum-cnt">{{ s.cnt }} 批次</div>
        </el-card>
      </el-col>
    </el-row>

    <el-table :data="pageData.records" border v-loading="loading">
      <el-table-column prop="warehouseName" label="仓库" width="110" />
      <el-table-column prop="locationCode" label="库位" width="110" />
      <el-table-column prop="materialCode" label="物料编码" width="140" />
      <el-table-column prop="materialName" label="物料名称" />
      <el-table-column prop="batchNo" label="批号" width="160" />
      <el-table-column prop="qty" label="数量" width="90" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="tagType(row.status)">{{ statusName(row.status) }}</el-tag>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination class="pager" layout="total, prev, pager, next" :total="pageData.total"
      v-model:current-page="page" @current-change="load" />
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import request from '../../utils/request'

const loading = ref(false)
const page = ref(1)
const warehouses = ref<any[]>([])
const summary = ref<any[]>([])
const pageData = reactive<{ records: any[]; total: number }>({ records: [], total: 0 })
const query = reactive<any>({ warehouseId: null, status: null, batchNo: '', keyword: '' })

onMounted(async () => {
  warehouses.value = await request.get('/wms/warehouses')
  load()
  const s: any = await request.get('/wms/inventory/summary')
  summary.value = s
})

async function load() {
  loading.value = true
  try {
    const data: any = await request.get('/wms/inventory', { params: { page: page.value, size: 15, ...query } })
    pageData.records = data.records
    pageData.total = data.total
  } finally {
    loading.value = false
  }
}

function statusName(s: string) {
  return { PENDING: '待检', QUALIFIED: '合格', FROZEN: '冻结', SCRAP: '报废', DISPOSED: '已处置' }[s] || s
}
function tagType(s: string) {
  return { PENDING: 'warning', QUALIFIED: 'success', FROZEN: 'danger', SCRAP: 'info' }[s] || ''
}
</script>

<style scoped>
.pager { margin-top: 12px; justify-content: flex-end; }
.sum { text-align: center; }
.sum-name { color: #909399; font-size: 13px; }
.sum-qty { font-size: 22px; font-weight: bold; }
.sum-cnt { color: #c0c4cc; font-size: 12px; }
</style>
