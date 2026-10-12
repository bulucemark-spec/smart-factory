<template>
  <el-card>
    <div class="toolbar">
      <el-select v-model="asnId" placeholder="选择待收货 ASN" filterable style="width: 360px" @change="onAsn">
        <el-option v-for="a in openAsns" :key="a.id" :label="`${a.asnNo} ${a.supplierName}`" :value="a.id" />
      </el-select>
      <el-button type="primary" :disabled="!form.items?.length" @click="submit">确认收货</el-button>
    </div>

    <el-table :data="form.items" border v-loading="loading">
      <el-table-column prop="materialCode" label="物料编码" width="140" />
      <el-table-column prop="materialName" label="物料名称" />
      <el-table-column label="生产批号" width="170">
        <template #default="{ row }"><el-input v-model="row.batchNo" placeholder="如 B2026101001" /></template>
      </el-table-column>
      <el-table-column label="生产日期" width="160">
        <template #default="{ row }">
          <el-date-picker v-model="row.productionDate" type="date" value-format="YYYY-MM-DD" style="width: 140px" />
        </template>
      </el-table-column>
      <el-table-column label="实收数量" width="140">
        <template #default="{ row }"><el-input-number v-model="row.qty" :min="0" style="width: 120px" /></template>
      </el-table-column>
    </el-table>

    <el-divider />
    <el-table :data="list" border>
      <el-table-column prop="receiptNo" label="收货单号" width="190" />
      <el-table-column prop="supplierName" label="供应商" />
      <el-table-column prop="receiveTime" label="收货时间" width="190" />
      <el-table-column prop="operatorName" label="收货人" width="120" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button link type="primary" @click="view(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="detailDialog" title="收货单详情" width="700px">
      <el-table :data="detail.items" border>
        <el-table-column prop="materialCode" label="物料编码" width="140" />
        <el-table-column prop="materialName" label="物料名称" />
        <el-table-column prop="batchNo" label="批号" width="170" />
        <el-table-column prop="qty" label="数量" width="90" />
        <el-table-column prop="status" label="状态" width="100" />
      </el-table>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'

const loading = ref(false)
const asnId = ref<number>()
const list = ref<any[]>([])
const openAsns = ref<any[]>([])
const detailDialog = ref(false)
const form = reactive<any>({ items: [] })
const detail = reactive<any>({ items: [] })

onMounted(loadAll)

async function loadAll() {
  const [asns, receipts]: any = await Promise.all([
    request.get('/wms/asns'),
    request.get('/wms/receipts')
  ])
  openAsns.value = asns.filter((a: any) => a.status === 'OPEN' || a.status === 'PARTIAL')
  list.value = receipts
}

async function onAsn(id: number) {
  loading.value = true
  try {
    const d: any = await request.get(`/wms/asns/${id}/detail`)
    form.asnId = d.id
    form.warehouseId = d.warehouseId
    form.supplierId = d.supplierId
    form.supplierName = d.supplierName
    form.items = (d.items || []).map((it: any) => ({
      asnItemId: it.id,
      materialId: it.materialId,
      materialCode: it.materialCode,
      materialName: it.materialName,
      batchNo: '',
      productionDate: '',
      qty: it.planQty
    }))
  } finally {
    loading.value = false
  }
}

async function submit() {
  for (const it of form.items) {
    if (!it.batchNo) {
      ElMessage.warning('请填写每个物料的生产批号')
      return
    }
  }
  await request.post('/wms/receipts/receive', form)
  ElMessage.success('收货成功，已自动生成 IQC 检验任务')
  asnId.value = undefined
  form.items = []
  loadAll()
}

async function view(row: any) {
  const d: any = await request.get(`/wms/receipts/${row.id}/detail`)
  Object.assign(detail, d)
  detailDialog.value = true
}
</script>

<style scoped>
.toolbar { display: flex; gap: 8px; margin-bottom: 12px; }
</style>
