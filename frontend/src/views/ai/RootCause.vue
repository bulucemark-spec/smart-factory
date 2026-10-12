<template>
  <el-row :gutter="16">
    <el-col :span="9">
      <el-card>
        <template #header>质量异常信息</template>
        <el-form :model="form" label-width="90px">
          <el-form-item label="缺陷现象">
            <el-input v-model="form.defect" type="textarea" :rows="2" placeholder="如 成品功能测试不良" />
          </el-form-item>
          <el-form-item label="产品"><el-input v-model="form.product" /></el-form-item>
          <el-form-item label="工序"><el-input v-model="form.process" /></el-form-item>
          <el-form-item label="批次号"><el-input v-model="form.batchNo" /></el-form-item>
          <el-form-item label="补充信息">
            <el-input v-model="form.extra" type="textarea" :rows="3"
              placeholder="不良率、测量值、发生时间、设备等" />
          </el-form-item>
          <el-button type="primary" style="width: 100%" :loading="loading" @click="analyze">
            生成根因分析
          </el-button>
        </el-form>
      </el-card>
    </el-col>

    <el-col :span="15">
      <el-card v-if="result">
        <template #header>
          分析结果
          <el-tag size="small" :type="result.llmEnabled ? 'success' : 'info'" style="margin-left: 8px">
            {{ result.llmEnabled ? '大模型分析' : '离线 Mock' }}
          </el-tag>
        </template>
        <el-divider content-position="left">可能根本原因</el-divider>
        <ol>
          <li v-for="(c, i) in result.possibleCauses" :key="i">{{ c }}</li>
        </ol>
        <el-divider content-position="left">详细分析（8D / 鱼骨图）</el-divider>
        <div style="white-space: pre-wrap; line-height: 1.8">{{ result.analysis }}</div>
        <el-divider content-position="left">参考来源</el-divider>
        <el-tag v-for="s in result.sources" :key="s.name" style="margin-right: 6px">
          {{ s.name }}
        </el-tag>
      </el-card>
      <el-empty v-else description="请填写异常信息并生成分析" />
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import request from '../../utils/request'

const loading = ref(false)
const result = ref<any>(null)
const form = reactive<any>({
  defect: '', product: '智能控制器', process: '功能测试', batchNo: '', extra: ''
})

async function analyze() {
  if (!form.defect) return
  loading.value = true
  try {
    const data: any = await request.post('/ai/root-cause', form)
    result.value = data
  } finally {
    loading.value = false
  }
}
</script>
