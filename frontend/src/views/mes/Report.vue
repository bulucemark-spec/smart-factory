<template>
  <el-card>
    <el-form :model="form" label-width="110px" style="max-width: 720px">
      <el-form-item label="选择工单">
        <el-select v-model="form.workOrderId" filterable placeholder="仅可对开工/生产中工单报工">
          <el-option v-for="o in activeOrders" :key="o.id"
            :label="`${o.orderNo} ${o.productName}`" :value="o.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="工位">
        <el-select v-model="form.workStationId" filterable>
          <el-option v-for="w in workstations" :key="w.id" :label="`${w.code} ${w.name}`" :value="w.id" />
        </el-select>
      </el-form-item>
      <el-row :gutter="12">
        <el-col :span="12">
          <el-form-item label="工序序号"><el-input-number v-model="form.operationSeq" :min="1" /></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="工序名称"><el-input v-model="form.operationName" /></el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="12">
        <el-col :span="8"><el-form-item label="合格数"><el-input-number v-model="form.goodQty" :min="0" /></el-form-item></el-col>
        <el-col :span="8"><el-form-item label="不良数"><el-input-number v-model="form.defectQty" :min="0" /></el-form-item></el-col>
        <el-col :span="8"><el-form-item label="报废数"><el-input-number v-model="form.scrapQty" :min="0" /></el-form-item></el-col>
      </el-row>
      <el-form-item label="不良代码"><el-input v-model="form.defectCode" placeholder="如 SOLDER-BRIDGE" /></el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submit">提交报工</el-button>
      </el-form-item>
    </el-form>

    <el-divider>本工单报工记录</el-divider>
    <el-table :data="reports" border>
      <el-table-column prop="reportTime" label="时间" width="190" />
      <el-table-column prop="operationName" label="工序" />
      <el-table-column prop="workstationCode" label="工位" width="130" />
      <el-table-column prop="goodQty" label="合格" width="90" />
      <el-table-column prop="defectQty" label="不良" width="90" />
      <el-table-column prop="scrapQty" label="报废" width="90" />
      <el-table-column prop="userName" label="报工人" width="110" />
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'

const activeOrders = ref<any[]>([])
const workstations = ref<any[]>([])
const reports = ref<any[]>([])
const form = reactive<any>({
  workOrderId: null, workStationId: null, operationSeq: 10, operationName: '',
  goodQty: 1, defectQty: 0, scrapQty: 0, defectCode: ''
})

onMounted(async () => {
  const [orders, ws]: any = await Promise.all([
    request.get('/mes/work-orders'),
    request.get('/mes/workstations', { params: { page: 1, size: 100 } })
  ])
  activeOrders.value = orders.filter((o: any) => ['STARTED', 'RUNNING'].includes(o.status))
  workstations.value = ws.records
})

watch(() => form.workOrderId, async (id) => {
  if (id) reports.value = await request.get(`/mes/work-orders/${id}/reports`)
})

async function submit() {
  if (!form.workOrderId || !form.workStationId) {
    ElMessage.warning('请选择工单与工位')
    return
  }
  await request.post('/mes/work-orders/report', {
    workOrderId: form.workOrderId,
    workstationId: form.workStationId,
    operationSeq: form.operationSeq,
    operationName: form.operationName,
    goodQty: form.goodQty,
    defectQty: form.defectQty,
    scrapQty: form.scrapQty,
    defectCode: form.defectCode
  })
  ElMessage.success('报工成功')
  reports.value = await request.get(`/mes/work-orders/${form.workOrderId}/reports`)
}
</script>
