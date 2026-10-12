<template>
  <el-card>
    <div class="toolbar">
      <el-select v-model="filterStatus" clearable placeholder="全部状态" style="width: 150px" @change="load">
        <el-option v-for="s in statusOptions" :key="s" :label="statusName(s)" :value="s" />
      </el-select>
      <el-button type="success" @click="openAdd">创建工单</el-button>
    </div>

    <el-table :data="list" border v-loading="loading">
      <el-table-column prop="orderNo" label="工单号" width="180" />
      <el-table-column prop="productName" label="产品" />
      <el-table-column prop="qtyPlanned" label="计划" width="80" />
      <el-table-column prop="qtyCompleted" label="完工" width="80" />
      <el-table-column prop="qtyDefect" label="不良" width="80" />
      <el-table-column prop="finishedBatch" label="成品批次" width="160" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }"><el-tag :type="orderTag(row.status)">{{ statusName(row.status) }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="300">
        <template #default="{ row }">
          <el-button v-if="row.status==='CREATED'" link type="primary" @click="release(row)">下达</el-button>
          <el-button v-if="row.status==='RELEASED'" link type="primary" @click="receive(row)">线边交接</el-button>
          <el-button v-if="row.status==='PICKED'" link type="primary" @click="start(row)">开工</el-button>
          <el-button v-if="['STARTED','RUNNING'].includes(row.status)" link type="success" @click="complete(row)">完工</el-button>
          <el-button v-if="row.status==='STOCKED'" link type="info" @click="close(row)">关闭</el-button>
          <el-button v-if="['CREATED','RELEASED','PICKED'].includes(row.status)" link type="danger" @click="cancel(row)">取消</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" title="创建生产工单" width="720px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="产品">
          <el-select v-model="form.productMaterialId" filterable @change="fillProduct">
            <el-option v-for="m in fgMaterials" :key="m.id" :label="`${m.code} ${m.name}`" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="BOM">
              <el-select v-model="form.bomId" clearable>
                <el-option v-for="b in boms" :key="b.id" :label="b.code" :value="b.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工艺路线">
              <el-select v-model="form.routingId" clearable>
                <el-option v-for="r in routings" :key="r.id" :label="r.code" :value="r.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="工作中心">
              <el-select v-model="form.workCenterId">
                <el-option v-for="w in workCenters" :key="w.id" :label="w.name" :value="w.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划数量"><el-input-number v-model="form.qtyPlanned" :min="1" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="计划开始">
              <el-date-picker v-model="form.planStart" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划完成">
              <el-date-picker v-model="form.planEnd" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">创建</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'

const loading = ref(false)
const dialog = ref(false)
const filterStatus = ref('')
const list = ref<any[]>([])
const fgMaterials = ref<any[]>([])
const boms = ref<any[]>([])
const routings = ref<any[]>([])
const workCenters = ref<any[]>([])
const form = reactive<any>({})

const statusOptions = ['CREATED', 'RELEASED', 'PICKED', 'STARTED', 'RUNNING', 'STOCKED', 'CLOSED', 'CANCELLED']

onMounted(async () => {
  const [orders, mats, bs, rs, wcs]: any = await Promise.all([
    request.get('/mes/work-orders'),
    request.get('/system/materials', { params: { page: 1, size: 500 } }),
    request.get('/system/boms'),
    request.get('/system/routings'),
    request.get('/system/work-centers', { params: { page: 1, size: 500 } })
  ])
  list.value = orders
  fgMaterials.value = mats.records.filter((m: any) => m.category === 'FG')
  boms.value = bs
  routings.value = rs
  workCenters.value = wcs.records
})

async function load() {
  list.value = await request.get('/mes/work-orders', { params: { status: filterStatus.value || undefined } })
}

function fillProduct(id: number) {
  const m = fgMaterials.value.find((x) => x.id === id)
  form.productCode = m?.code; form.productName = m?.name
}
function openAdd() {
  Object.assign(form, { productMaterialId: null, productCode: '', productName: '', bomId: null, routingId: null, workCenterId: null, qtyPlanned: 10, planStart: '', planEnd: '' })
  dialog.value = true
}
async function save() {
  await request.post('/mes/work-orders', { type: 'NORMAL', ...form })
  ElMessage.success('工单已创建'); dialog.value = false; load()
}

async function release(row: any) {
  let wave: any = { sourceNo: row.orderNo, warehouseId: 1, items: [] }
  if (row.bomId) {
    const bom: any = await request.get(`/system/boms/${row.bomId}/detail`)
    wave.items = (bom.items || []).map((it: any) => ({
      materialId: it.materialId,
      materialCode: it.materialCode,
      materialName: it.materialName,
      qty: Number(it.quantity) * Number(row.qtyPlanned)
    }))
  }
  await request.post(`/mes/work-orders/${row.id}/release`, wave)
  ElMessage.success('工单已下达，领料波次已推送 WMS'); load()
}
async function receive(row: any) {
  try {
    await request.post(`/mes/work-orders/${row.id}/receive-materials`)
    ElMessage.success('线边交接完成，投料与谱系已回写'); load()
  } catch (e) { /* 拦截器提示：需先拣货 */ }
}
async function start(row: any) {
  await request.post(`/mes/work-orders/${row.id}/start`)
  ElMessage.success('已开工'); load()
}
async function complete(row: any) {
  await request.post(`/mes/work-orders/${row.id}/complete`)
  ElMessage.success('完工：成品已入库，OQC 任务已生成'); load()
}
async function close(row: any) {
  await request.post(`/mes/work-orders/${row.id}/close`)
  ElMessage.success('工单已关闭'); load()
}
async function cancel(row: any) {
  await ElMessageBox.confirm('确认取消该工单？', '提示', { type: 'warning' })
  await request.post(`/mes/work-orders/${row.id}/cancel`)
  load()
}

function statusName(s: string) {
  return { CREATED: '已创建', RELEASED: '已下达', PICKED: '已交接', STARTED: '已开工', RUNNING: '生产中', COMPLETED: '已完工', STOCKED: '已入库', CLOSED: '已关闭', CANCELLED: '已取消' }[s] || s
}
function orderTag(s: string) {
  return { CREATED: 'info', RELEASED: 'warning', PICKED: '', STARTED: '', RUNNING: '', STOCKED: 'success', CLOSED: 'success', CANCELLED: 'danger' }[s] || ''
}
</script>

<style scoped>
.toolbar { display: flex; gap: 8px; margin-bottom: 12px; }
</style>
