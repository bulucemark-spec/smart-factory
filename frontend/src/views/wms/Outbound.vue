<template>
  <el-card>
    <div class="toolbar"><el-button type="success" @click="openAdd">新增出库单</el-button></div>
    <el-table :data="list" border v-loading="loading">
      <el-table-column prop="outboundNo" label="出库单号" width="190" />
      <el-table-column prop="customerName" label="客户" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === 'SHIPPED' ? 'success' : 'warning'">
            {{ { DRAFT: '草稿', PICKED: '已拣货', SHIPPED: '已发运', CANCELLED: '已取消' }[row.status] || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="190" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button link type="primary" @click="view(row)">详情</el-button>
          <el-button link type="success" :disabled="row.status === 'SHIPPED'" @click="ship(row)">发运</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" title="新增出库单" width="720px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="客户"><el-input v-model="form.customerName" /></el-form-item>
      </el-form>
      <el-table :data="form.items" border size="small">
        <el-table-column label="成品" min-width="240">
          <template #default="{ row }">
            <el-select v-model="row.materialId" filterable @change="(v:any)=>fill(row,v)">
              <el-option v-for="m in fgMaterials" :key="m.id" :label="`${m.code} ${m.name}`" :value="m.id" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="数量" width="140">
          <template #default="{ row }"><el-input-number v-model="row.qty" :min="0" style="width: 120px" /></template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ $index }">
            <el-button link type="danger" @click="form.items.splice($index,1)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-button style="margin-top: 10px" @click="form.items.push({ materialId: null, qty: 1 })">+ 添加成品</el-button>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialog" title="出库单详情" width="700px">
      <el-table :data="detail.items" border>
        <el-table-column prop="materialCode" label="成品编码" width="150" />
        <el-table-column prop="materialName" label="成品名称" />
        <el-table-column prop="qty" label="数量" width="100" />
      </el-table>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'

const loading = ref(false)
const dialog = ref(false)
const detailDialog = ref(false)
const list = ref<any[]>([])
const materials = ref<any[]>([])
const form = reactive<any>({ items: [] })
const detail = reactive<any>({ items: [] })
const fgMaterials = computed(() => materials.value.filter((m) => m.category === 'FG'))

onMounted(async () => {
  const [outbounds, mats]: any = await Promise.all([
    request.get('/wms/outbound'),
    request.get('/system/materials', { params: { page: 1, size: 500 } })
  ])
  list.value = outbounds
  materials.value = mats.records
})

function fill(row: any, id: number) {
  const m = materials.value.find((x) => x.id === id)
  row.materialCode = m?.code; row.materialName = m?.name
}
function openAdd() {
  Object.assign(form, { customerName: '', items: [] })
  dialog.value = true
}
async function save() {
  await request.post('/wms/outbound', form)
  ElMessage.success('保存成功'); dialog.value = false
  list.value = await request.get('/wms/outbound')
}
async function view(row: any) {
  const d: any = await request.get(`/wms/outbound/${row.id}/detail`)
  Object.assign(detail, d)
  detailDialog.value = true
}
async function ship(row: any) {
  await request.post(`/wms/outbound/${row.id}/ship`)
  ElMessage.success('发运成功')
  list.value = await request.get('/wms/outbound')
}
</script>

<style scoped>
.toolbar { margin-bottom: 12px; }
</style>
