<template>
  <el-card>
    <el-radio-group v-model="status" @change="load" style="margin-bottom: 12px">
      <el-radio-button label="OPEN">待评审</el-radio-button>
      <el-radio-button label="REVIEWED">已评审</el-radio-button>
      <el-radio-button label="CLOSED">已关闭</el-radio-button>
    </el-radio-group>

    <el-table :data="list" border v-loading="loading">
      <el-table-column prop="ncrNo" label="NCR 单号" width="180" />
      <el-table-column prop="materialName" label="物料" />
      <el-table-column prop="batchNo" label="批号" width="160" />
      <el-table-column prop="defectDesc" label="缺陷描述" min-width="200" show-overflow-tooltip />
      <el-table-column prop="disposition" label="处置" width="110" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status==='CLOSED'?'success':row.status==='REVIEWED'?'':'danger'">
            {{ { OPEN:'待评审', REVIEWED:'已评审', CLOSED:'已关闭' }[row.status] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button link type="primary" :disabled="row.status!=='OPEN'" @click="openMrb(row)">MRB评审</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" title="MRB 物料评审" width="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="处置方式">
          <el-select v-model="form.disposition" style="width: 100%">
            <el-option label="退货 RETURN" value="RETURN" />
            <el-option label="让步接收 CONCESSION" value="CONCESSION" />
            <el-option label="返工 REWORK" value="REWORK" />
            <el-option label="报废 SCRAP" value="SCRAP" />
          </el-select>
        </el-form-item>
        <el-form-item label="评审意见"><el-input v-model="form.comment" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog=false">取消</el-button>
        <el-button type="primary" @click="submit">确认评审</el-button>
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
const dialog = ref(false)
const form = reactive<any>({ id: null, disposition: 'RETURN', comment: '' })

onMounted(load)

async function load() {
  loading.value = true
  try {
    list.value = await request.get('/qms/ncrs', { params: { status: status.value } })
  } finally {
    loading.value = false
  }
}

function openMrb(row: any) {
  form.id = row.id; form.disposition = 'RETURN'; form.comment = ''
  dialog.value = true
}
async function submit() {
  await request.post(`/qms/ncrs/${form.id}/mrb`, null, {
    params: { disposition: form.disposition, comment: form.comment }
  })
  ElMessage.success('评审完成：WMS 已处置，CAPA 已自动建立')
  dialog.value = false; load()
}
</script>
