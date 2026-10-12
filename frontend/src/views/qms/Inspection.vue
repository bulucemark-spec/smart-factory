<template>
  <el-card>
    <el-form inline>
      <el-form-item label="类型">
        <el-select v-model="type" clearable placeholder="全部" style="width: 110px" @change="load">
          <el-option label="来料 IQC" value="IQC" /><el-option label="成品 OQC" value="OQC" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="status" clearable placeholder="全部" style="width: 130px" @change="load">
          <el-option label="待检" value="PENDING" /><el-option label="合格" value="QUALIFIED" />
          <el-option label="不合格" value="UNQUALIFIED" />
        </el-select>
      </el-form-item>
    </el-form>

    <el-table :data="list" border v-loading="loading">
      <el-table-column prop="taskNo" label="任务号" width="180" />
      <el-table-column prop="inspectionType" label="类型" width="90">
        <template #default="{ row }"><el-tag size="small">{{ row.inspectionType }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="sourceNo" label="来源单号" width="170" />
      <el-table-column prop="materialName" label="物料" />
      <el-table-column prop="batchNo" label="批号" width="160" />
      <el-table-column prop="sampleSize" label="抽样" width="80" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }"><el-tag :type="taskTag(row.status)">{{ taskName(row.status) }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">
            {{ row.status === 'PENDING' ? '录入检验' : '查看' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" title="检验结果录入" width="820px">
      <el-descriptions :column="3" border size="small" style="margin-bottom: 12px">
        <el-descriptions-item label="任务号">{{ detail.task?.taskNo }}</el-descriptions-item>
        <el-descriptions-item label="物料">{{ detail.task?.materialName }}</el-descriptions-item>
        <el-descriptions-item label="批号">{{ detail.task?.batchNo }}</el-descriptions-item>
      </el-descriptions>

      <el-table :data="rows" border size="small">
        <el-table-column label="检验项" min-width="150">
          <template #default="{ row }">{{ row.name }}
            <el-tag v-if="row.ctq===1" type="danger" size="small">CTQ</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="规格/量具" width="200">
          <template #default="{ row }">
            <span v-if="row.upperLimit!=null">{{ row.lowerLimit }}~{{ row.upperLimit }} {{ row.unit }}</span>
            <span v-else>定性 · {{ row.tool || '目检' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="实测值" width="160">
          <template #default="{ row }">
            <el-input-number v-if="row.upperLimit!=null" v-model="row.measuredValue"
              :controls="false" style="width: 130px" />
            <el-select v-else v-model="row.judge" style="width: 110px">
              <el-option label="合格 OK" value="OK" /><el-option label="不合格 NG" value="NG" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="备注" width="130">
          <template #default="{ row }"><el-input v-model="row.remark" /></template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :disabled="detail.task?.status!=='PENDING'" @click="submit">提交判定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'

const loading = ref(false)
const dialog = ref(false)
const type = ref('')
const status = ref('')
const list = ref<any[]>([])
const detail = reactive<any>({ task: {} })
const rows = ref<any[]>([])

onMounted(load)

async function load() {
  loading.value = true
  try {
    list.value = await request.get('/qms/inspections', {
      params: { type: type.value || undefined, status: status.value || undefined }
    })
  } finally {
    loading.value = false
  }
}

async function openDetail(row: any) {
  const d: any = await request.get(`/qms/inspections/${row.id}/detail`)
  detail.task = d.task
  const existing = d.results || []
  rows.value = (d.items || []).map((it: any) => {
    const saved = existing.find((r: any) => r.itemId === it.id)
    return {
      itemId: it.id, name: it.name, unit: it.unit, tool: it.tool, ctq: it.ctq,
      upperLimit: it.upperLimit, lowerLimit: it.lowerLimit,
      measuredValue: saved?.measuredValue ?? null,
      judge: saved?.judge ?? 'OK',
      remark: saved?.remark ?? ''
    }
  })
  dialog.value = true
}

async function submit() {
  for (const r of rows.value) {
    if (r.upperLimit != null && r.measuredValue == null) {
      ElMessage.warning(`请录入“${r.name}”的实测值`)
      return
    }
  }
  await request.post('/qms/inspections/submit', {
    taskId: detail.task.id,
    results: rows.value.map((r) => ({
      itemId: r.itemId, measuredValue: r.measuredValue, judge: r.judge, remark: r.remark
    }))
  })
  ElMessage.success('判定已提交')
  dialog.value = false
  load()
}

function taskName(s: string) {
  return { PENDING: '待检', QUALIFIED: '合格', UNQUALIFIED: '不合格' }[s] || s
}
function taskTag(s: string) {
  return { PENDING: 'warning', QUALIFIED: 'success', UNQUALIFIED: 'danger' }[s] || ''
}
</script>
