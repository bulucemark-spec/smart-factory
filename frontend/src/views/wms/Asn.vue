<template>
  <el-card>
    <div class="toolbar"><el-button type="success" @click="openAdd">新增到货预告</el-button></div>
    <el-table :data="list" border v-loading="loading">
      <el-table-column prop="asnNo" label="ASN 单号" width="180" />
      <el-table-column prop="supplierName" label="供应商" />
      <el-table-column prop="expectArrivalTime" label="预计到货" width="180" />
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="row.status === 'OPEN' ? 'warning' : row.status === 'RECEIVED' ? 'success' : 'info'">
            {{ { OPEN: '待收货', RECEIVED: '已收货', CLOSED: '已关闭', PARTIAL: '部分收货' }[row.status] || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button link type="primary" @click="view(row)">详情</el-button>
          <el-button link type="danger" @click="remove(row)" :disabled="row.status !== 'OPEN'">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" title="新增到货预告" width="760px">
      <el-form :model="form" label-width="100px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="供应商">
              <el-select v-model="form.supplierId" filterable @change="fillSupplier">
                <el-option v-for="s in suppliers" :key="s.id" :label="s.name" :value="s.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="收货仓库">
              <el-select v-model="form.warehouseId">
                <el-option v-for="w in warehouses" :key="w.id" :label="w.name" :value="w.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="预计到货">
          <el-date-picker v-model="form.expectArrivalTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
      </el-form>

      <el-divider content-position="left">物料明细</el-divider>
      <el-table :data="form.items" border size="small">
        <el-table-column label="物料" min-width="220">
          <template #default="{ row }">
            <el-select v-model="row.materialId" filterable @change="(v:any)=>fillMaterial(row,v)">
              <el-option v-for="m in materials" :key="m.id" :label="`${m.code} ${m.name}`" :value="m.id" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="计划数量" width="140">
          <template #default="{ row }"><el-input-number v-model="row.planQty" :min="0" style="width: 120px" /></template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ $index }">
            <el-button link type="danger" @click="form.items.splice($index,1)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-button style="margin-top: 10px" @click="form.items.push({ materialId: null, planQty: 1 })">+ 添加物料</el-button>

      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialog" title="ASN 详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="单号">{{ detail.asnNo }}</el-descriptions-item>
        <el-descriptions-item label="供应商">{{ detail.supplierName }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="detail.items" border style="margin-top: 12px">
        <el-table-column prop="materialCode" label="物料编码" width="150" />
        <el-table-column prop="materialName" label="物料名称" />
        <el-table-column prop="planQty" label="计划数量" width="100" />
        <el-table-column prop="receivedQty" label="已收数量" width="100" />
      </el-table>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'

const loading = ref(false)
const dialog = ref(false)
const detailDialog = ref(false)
const list = ref<any[]>([])
const suppliers = ref<any[]>([])
const materials = ref<any[]>([])
const warehouses = ref<any[]>([])
const form = reactive<any>({ items: [] })
const detail = reactive<any>({ items: [] })

onMounted(async () => {
  const [asns, sups, mats, whs]: any = await Promise.all([
    request.get('/wms/asns'),
    request.get('/system/suppliers', { params: { page: 1, size: 500 } }),
    request.get('/system/materials', { params: { page: 1, size: 500 } }),
    request.get('/wms/warehouses')
  ])
  list.value = asns
  suppliers.value = sups.records
  materials.value = mats.records
  warehouses.value = whs
})

function fillSupplier(id: number) {
  const s = suppliers.value.find((x) => x.id === id)
  form.supplierName = s?.name
}
function fillMaterial(row: any, id: number) {
  const m = materials.value.find((x) => x.id === id)
  row.materialCode = m?.code; row.materialName = m?.name
}
function openAdd() {
  Object.assign(form, { supplierId: null, supplierName: '', warehouseId: 1, expectArrivalTime: '', items: [] })
  dialog.value = true
}
async function save() {
  await request.post('/wms/asns', form)
  ElMessage.success('保存成功'); dialog.value = false
  list.value = await request.get('/wms/asns')
}
async function view(row: any) {
  const d: any = await request.get(`/wms/asns/${row.id}/detail`)
  Object.assign(detail, d)
  detailDialog.value = true
}
async function remove(row: any) {
  await ElMessageBox.confirm('确认删除该 ASN？', '提示', { type: 'warning' })
  await request.delete(`/wms/asns/${row.id}`)
  ElMessage.success('已删除')
  list.value = await request.get('/wms/asns')
}
</script>

<style scoped>
.toolbar { margin-bottom: 12px; }
</style>
