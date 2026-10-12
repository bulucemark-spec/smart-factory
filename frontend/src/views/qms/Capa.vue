<template>
  <el-card>
    <el-radio-group v-model="status" @change="load" style="margin-bottom: 12px">
      <el-radio-button label="OPEN">待执行</el-radio-button>
      <el-radio-button label="IN_PROGRESS">执行中/待验证</el-radio-button>
      <el-radio-button label="CLOSED">已关闭</el-radio-button>
    </el-radio-group>

    <el-table :data="list" border v-loading="loading">
      <el-table-column prop="capaNo" label="CAPA 单号" width="180" />
      <el-table-column prop="rootCause" label="根本原因" min-width="180" show-overflow-tooltip />
      <el-table-column prop="ownerName" label="责任人" width="110" />
      <el-table-column prop="dueDate" label="期限" width="120" />
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="row.status==='CLOSED'?'success':row.status==='OPEN'?'warning':''">
            {{ { OPEN:'待执行', IN_PROGRESS:'待验证', CLOSED:'已关闭' }[row.status] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button link type="primary" :disabled="row.status==='CLOSED'" @click="openExecute(row)">执行措施</el-button>
          <el-button link type="success" :disabled="row.status!=='IN_PROGRESS'" @click="openVerify(row)">验证</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="executeDialog" title="执行纠正/预防措施" width="640px">
      <el-form :model="exec" label-width="100px">
        <el-form-item label="根本原因"><el-input v-model="exec.rootCause" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="纠正措施"><el-input v-model="exec.correctiveAction" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="预防措施"><el-input v-model="exec.preventiveAction" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="责任人"><el-input v-model="exec.ownerName" /></el-form-item>
        <el-form-item label="完成期限">
          <el-date-picker v-model="exec.dueDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="executeDialog=false">取消</el-button>
        <el-button type="primary" @click="doExecute">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="verifyDialog" title="效果验证" width="480px">
      <el-form :model="ver" label-width="100px">
        <el-form-item label="验证结果"><el-input v-model="ver.verifyResult" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="是否通过">
          <el-radio-group v-model="ver.passed">
            <el-radio :label="true">通过并关闭</el-radio>
            <el-radio :label="false">不通过退回</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="verifyDialog=false">取消</el-button>
        <el-button type="primary" @click="doVerify">确认</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'

const loading = ref(false)
const status = ref('OPEN')
const list = ref<any[]>([])
const executeDialog = ref(false)
const verifyDialog = ref(false)
const exec = reactive<any>({})
const ver = reactive<any>({ id: null, verifyResult: '', passed: true })

onMounted(load)

async function load() {
  loading.value = true
  try {
    list.value = await request.get('/qms/capas', { params: { status: status.value } })
  } finally {
    loading.value = false
  }
}

function openExecute(row: any) {
  Object.assign(exec, {
    id: row.id, rootCause: row.rootCause || '', correctiveAction: row.correctiveAction || '',
    preventiveAction: row.preventiveAction || '', ownerName: row.ownerName || '', dueDate: row.dueDate || ''
  })
  executeDialog.value = true
}
async function doExecute() {
  await request.post('/qms/capas/execute', exec)
  ElMessage.success('措施已提交'); executeDialog.value = false; load()
}

function openVerify(row: any) {
  ver.id = row.id; ver.verifyResult = row.verifyResult || ''; ver.passed = true
  verifyDialog.value = true
}
async function doVerify() {
  await request.post(`/qms/capas/${ver.id}/verify`, null, {
    params: { verifyResult: ver.verifyResult, passed: ver.passed }
  })
  ElMessage.success(ver.passed ? '验证通过，CAPA 与 NCR 已关闭' : '已退回执行')
  verifyDialog.value = false; load()
}
</script>
