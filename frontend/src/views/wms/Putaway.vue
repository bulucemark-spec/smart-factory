<template>
  <el-card>
    <el-radio-group v-model="status" @change="load" style="margin-bottom: 12px">
      <el-radio-button label="OPEN">待上架</el-radio-button>
      <el-radio-button label="DONE">已上架</el-radio-button>
    </el-radio-group>

    <el-table :data="list" border v-loading="loading">
      <el-table-column prop="materialCode" label="物料编码" width="140" />
      <el-table-column prop="materialName" label="物料名称" />
      <el-table-column prop="batchNo" label="批号" width="160" />
      <el-table-column prop="qty" label="数量" width="90" />
      <el-table-column prop="fromLocationCode" label="来源库位" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'DONE' ? 'success' : 'warning'">
            {{ row.status === 'DONE' ? '已上架' : '待上架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button link type="primary" :disabled="row.status === 'DONE'" @click="openExecute(row)">上架</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" title="选择目标库位" width="560px">
      <el-table :data="locations" border highlight-current-row @current-change="(row:any)=>toLocationId=row?.id">
        <el-table-column prop="code" label="库位编码" width="150" />
        <el-table-column prop="zone" label="库区" width="120" />
        <el-table-column prop="locationType" label="类型" width="120" />
      </el-table>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :disabled="!toLocationId" @click="execute">确认上架</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'

const loading = ref(false)
const status = ref('OPEN')
const list = ref<any[]>([])
const dialog = ref(false)
const locations = ref<any[]>([])
const current = ref<any>({})
const toLocationId = ref<number>()

onMounted(load)

async function load() {
  loading.value = true
  try {
    list.value = await request.get('/wms/putaway/tasks', { params: { status: status.value } })
  } finally {
    loading.value = false
  }
}

async function openExecute(row: any) {
  current.value = row
  toLocationId.value = undefined
  locations.value = await request.get('/wms/putaway/recommend', { params: { warehouseId: row.toWarehouseId || 1 } })
  dialog.value = true
}

async function execute() {
  await request.post(`/wms/putaway/${current.value.id}/execute`, null, { params: { toLocationId: toLocationId.value } })
  ElMessage.success('上架成功'); dialog.value = false; load()
}
</script>
