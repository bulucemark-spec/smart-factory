<template>
  <el-card>
    <el-alert type="info" :closable="false" style="margin-bottom: 14px"
      title="正向追溯：输入成品批次，查询所用子件批次；反向追溯：输入子件批次，查询用于哪些成品。" />
    <el-form inline>
      <el-form-item label="批次号">
        <el-input v-model="batch" placeholder="如 FG2026... 或 B2026..." style="width: 260px" />
      </el-form-item>
      <el-form-item>
        <el-radio-group v-model="direction">
          <el-radio-button label="backward">正向（成品→子件）</el-radio-button>
          <el-radio-button label="forward">反向（子件→成品）</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item><el-button type="primary" @click="query">追溯查询</el-button></el-form-item>
    </el-form>

    <el-table :data="result" border v-loading="loading">
      <el-table-column prop="workOrderId" label="工单ID" width="100" />
      <el-table-column prop="parentBatch" label="成品批次" width="180" />
      <el-table-column prop="childBatch" label="子件批次" width="180" />
      <el-table-column prop="materialCode" label="物料编码" width="150" />
      <el-table-column prop="qty" label="数量" width="100" />
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'

const batch = ref('')
const direction = ref('backward')
const loading = ref(false)
const result = ref<any[]>([])

async function query() {
  if (!batch.value) {
    ElMessage.warning('请输入批次号')
    return
  }
  loading.value = true
  try {
    result.value = await request.get(`/mes/genealogy/${direction.value}/${batch.value}`)
    if (!result.value.length) ElMessage.info('未查询到谱系记录')
  } finally {
    loading.value = false
  }
}
</script>
