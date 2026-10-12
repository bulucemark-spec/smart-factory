<template>
  <el-card>
    <el-table :data="waves" border v-loading="loading">
      <el-table-column prop="waveNo" label="波次号" width="190" />
      <el-table-column prop="sourceNo" label="来源工单" width="180" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="waveTag(row.status)">{{ waveName(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="190" />
      <el-table-column label="操作" width="240">
        <template #default="{ row }">
          <el-button link type="primary" @click="viewTasks(row)">查看任务</el-button>
          <el-button link type="success" :disabled="row.status !== 'OPEN'" @click="execute(row)">执行拣货</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" title="拣货任务明细" width="780px">
      <el-alert type="info" :closable="false" style="margin-bottom: 10px"
        title="系统按 FIFO 自动分配合格批次；执行拣货时进行条件库存扣减。" />
      <el-table :data="tasks" border>
        <el-table-column prop="materialCode" label="物料编码" width="140" />
        <el-table-column prop="materialName" label="物料名称" />
        <el-table-column prop="batchNo" label="批次" width="160" />
        <el-table-column prop="locationCode" label="库位" width="120" />
        <el-table-column prop="qty" label="需求/实拣" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'DONE' ? 'success' : 'warning'" size="small">
              {{ row.status === 'DONE' ? '已拣' : '待拣' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'

const loading = ref(false)
const dialog = ref(false)
const waves = ref<any[]>([])
const tasks = ref<any[]>([])

onMounted(load)

async function load() {
  loading.value = true
  try {
    waves.value = await request.get('/wms/pick/waves')
  } finally {
    loading.value = false
  }
}

async function viewTasks(row: any) {
  tasks.value = await request.get(`/wms/pick/waves/${row.id}/tasks`)
  dialog.value = true
}

async function execute(row: any) {
  await request.post(`/wms/pick/waves/${row.id}/execute`)
  ElMessage.success('拣货完成，库存已扣减')
  await viewTasks(row)
  load()
}

function waveName(s: string) {
  return { OPEN: '待拣货', ALLOCATED: '已分配', DONE: '已完成', CANCELLED: '已取消' }[s] || s
}
function waveTag(s: string) {
  return { OPEN: 'warning', ALLOCATED: '', DONE: 'success', CANCELLED: 'info' }[s] || ''
}
</script>
