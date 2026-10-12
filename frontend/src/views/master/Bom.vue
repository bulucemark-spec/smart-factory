<template>
  <el-card>
    <div class="toolbar">
      <el-button type="success" @click="openAdd">新增 BOM</el-button>
    </div>
    <el-table :data="list" border v-loading="loading">
      <el-table-column prop="code" label="BOM 编码" width="180" />
      <el-table-column label="成品">
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

    <el-dialog v-model="dialog" :title="form.id ? '编辑 BOM' : '新增 BOM'" width="760px">
      <el-form :model="form" label-width="90px">
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="编码"><el-input v-model="form.code" /></el-form-item>
          </el-col>
          <el-col :span="9">
            <el-form-item label="成品">
              <el-select v-model="form.productMaterialId" filterable placeholder="选择成品">
                <el-option v-for="m in fgMaterials" :key="m.id" :label="m.name" :value="m.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="7">
            <el-form-item label="版本"><el-input v-model="form.version" /></el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <el-divider content-position="left">物料明细</el-divider>
      <el-table :data="form.items" border size="small">
        <el-table-column label="物料" min-width="200">
          <template #default="{ row }">
            <el-select v-model="row.materialId" filterable placeholder="选择物料" @change="(v:any)=>fillMat(row,v)">
              <el-option v-for="m in compMaterials" :key="m.id" :label="`${m.code} ${m.name}`" :value="m.id" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="用量" width="130">
          <template #default="{ row }"><el-input-number v-model="row.quantity" :min="0" :step="1" style="width: 110px" /></template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column label="操作" width="80">
          <template #default="{ $index }">
            <el-button link type="danger" @click="form.items.splice($index,1)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-button style="margin-top: 10px" @click="addItem">+ 添加物料</el-button>

      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
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
const list = ref<any[]>([])
const materials = ref<any[]>([])
const form = reactive<any>({ items: [] })

const fgMaterials = computed(() => materials.value.filter((m) => m.category === 'FG'))
const compMaterials = computed(() => materials.value.filter((m) => m.category !== 'FG'))

onMounted(async () => {
  const [boms, mats]: any = await Promise.all([
    request.get('/system/boms'),
    request.get('/system/materials', { params: { page: 1, size: 500 } })
  ])
  list.value = boms
  materials.value = mats.records
})

function matName(id: number) {
  const m = materials.value.find((x) => x.id === id)
  return m ? `${m.code} ${m.name}` : id
}
function fillMat(row: any, id: number) {
  const m = materials.value.find((x) => x.id === id)
  if (m) row.unit = m.unit
}
function addItem() {
  form.items.push({ materialId: null, quantity: 1, unit: '个' })
}
function openAdd() {
  Object.assign(form, { id: null, code: '', productMaterialId: null, version: 'V1.0', status: 1, items: [] })
  dialog.value = true
}
async function openEdit(row: any) {
  const detail: any = await request.get(`/system/boms/${row.id}/detail`)
  detail.items = detail.items || []
  detail.items.forEach((it: any) => {
    const m = materials.value.find((x) => x.id === it.materialId)
    if (m) { it.materialCode = m.code; it.materialName = m.name }
  })
  Object.assign(form, detail)
  dialog.value = true
}
async function save() {
  form.items.forEach((it: any, i: number) => { it.sequence = i + 1 })
  await request.post('/system/boms/save', form)
  ElMessage.success('保存成功'); dialog.value = false
  list.value = await request.get('/system/boms')
}
async function remove(row: any) {
  await ElMessageBox.confirm('确认删除该 BOM？', '提示', { type: 'warning' })
  await request.delete(`/system/boms/${row.id}`)
  ElMessage.success('已删除')
  list.value = await request.get('/system/boms')
}
</script>

<style scoped>
.toolbar { margin-bottom: 12px; }
</style>
