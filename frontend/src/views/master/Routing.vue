<template>
  <el-card>
    <div class="toolbar"><el-button type="success" @click="openAdd">新增工艺路线</el-button></div>
    <el-table :data="list" border v-loading="loading">
      <el-table-column prop="code" label="路线编码" width="180" />
      <el-table-column label="产品">
        <template #default="{ row }">{{ matName(row.productMaterialId) }}</template>
      </el-table-column>
      <el-table-column prop="version" label="版本" width="100" />
      <el-table-column label="状态" width="100">
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

    <el-dialog v-model="dialog" :title="form.id ? '编辑工艺路线' : '新增工艺路线'" width="860px">
      <el-form :model="form" label-width="90px">
        <el-row :gutter="12">
          <el-col :span="8"><el-form-item label="编码"><el-input v-model="form.code" /></el-form-item></el-col>
          <el-col :span="9">
            <el-form-item label="产品">
              <el-select v-model="form.productMaterialId" filterable>
                <el-option v-for="m in materials" :key="m.id" :label="m.name" :value="m.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="7"><el-form-item label="版本"><el-input v-model="form.version" /></el-form-item></el-col>
        </el-row>
      </el-form>

      <el-divider content-position="left">工序明细</el-divider>
      <el-table :data="form.operations" border size="small">
        <el-table-column label="序号" width="80">
          <template #default="{ row }"><el-input-number v-model="row.sequence" :min="1" style="width: 70px" /></template>
        </el-table-column>
        <el-table-column label="工序名称" min-width="150">
          <template #default="{ row }"><el-input v-model="row.operationName" /></template>
        </el-table-column>
        <el-table-column label="工作中心" width="170">
          <template #default="{ row }">
            <el-select v-model="row.workCenterId" filterable>
              <el-option v-for="w in workCenters" :key="w.id" :label="w.name" :value="w.id" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="工时(分)" width="110">
          <template #default="{ row }"><el-input-number v-model="row.standardTime" :min="0" style="width: 100px" /></template>
        </el-table-column>
        <el-table-column label="检验" width="70">
          <template #default="{ row }"><el-switch v-model="row.isQcPoint" :active-value="1" :inactive-value="0" /></template>
        </el-table-column>
        <el-table-column label="CTQ" width="70">
          <template #default="{ row }"><el-switch v-model="row.ctq" :active-value="1" :inactive-value="0" /></template>
        </el-table-column>
        <el-table-column label="操作" width="70">
          <template #default="{ $index }">
            <el-button link type="danger" @click="form.operations.splice($index,1)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-button style="margin-top: 10px" @click="addOp">+ 添加工序</el-button>

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

const loading = ref(false)
const dialog = ref(false)
const list = ref<any[]>([])
const materials = ref<any[]>([])
const workCenters = ref<any[]>([])
const form = reactive<any>({ operations: [] })

onMounted(async () => {
  const [routings, mats, wcs]: any = await Promise.all([
    request.get('/system/routings'),
    request.get('/system/materials', { params: { page: 1, size: 500 } }),
    request.get('/system/work-centers', { params: { page: 1, size: 500 } })
  ])
  list.value = routings
  materials.value = mats.records
  workCenters.value = wcs.records
})

function matName(id: number) {
  const m = materials.value.find((x) => x.id === id)
  return m ? `${m.code} ${m.name}` : id
}
function addOp() {
  const seq = (form.operations.length + 1) * 10
  form.operations.push({ sequence: seq, operationName: '', workCenterId: null, standardTime: 1, isQcPoint: 0, ctq: 0 })
}
function openAdd() {
  Object.assign(form, { id: null, code: '', productMaterialId: null, version: 'V1.0', status: 1, operations: [] })
  dialog.value = true
}
async function openEdit(row: any) {
  const detail: any = await request.get(`/system/routings/${row.id}/detail`)
  detail.operations = detail.operations || []
  Object.assign(form, detail)
  dialog.value = true
}
async function save() {
  await request.post('/system/routings/save', form)
  ElMessage.success('保存成功'); dialog.value = false
  list.value = await request.get('/system/routings')
}
async function remove(row: any) {
  await ElMessageBox.confirm('确认删除该工艺路线？', '提示', { type: 'warning' })
  await request.delete(`/system/routings/${row.id}`)
  ElMessage.success('已删除')
  list.value = await request.get('/system/routings')
}
</script>

<style scoped>
.toolbar { margin-bottom: 12px; }
</style>
