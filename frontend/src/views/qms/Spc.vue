<template>
  <el-row :gutter="16">
    <el-col :span="8">
      <el-card>
        <template #header>特性值录入</template>
        <el-form :model="form" label-width="90px">
          <el-form-item label="特性名称">
            <el-input v-model="form.characteristic" placeholder="如 PCB长度" />
          </el-form-item>
          <el-form-item label="物料编码"><el-input v-model="form.materialCode" /></el-form-item>
          <el-form-item label="工单号"><el-input v-model="form.workOrderNo" /></el-form-item>
          <el-form-item label="测量值">
            <el-input-number v-model="form.value" :controls="false" style="width: 100%" />
          </el-form-item>
          <el-button type="primary" style="width: 100%" @click="add">提交数据</el-button>
        </el-form>

        <el-divider />
        <el-form label-width="90px">
          <el-form-item label="查询特性">
            <el-input v-model="chartChar" placeholder="如 PCB长度" />
          </el-form-item>
          <el-button style="width: 100%" @click="loadChart">绘制控制图 / 计算 Cpk</el-button>
          <el-form-item label="规格下限 LSL" style="margin-top: 10px">
            <el-input-number v-model="lsl" :controls="false" style="width: 100%" />
          </el-form-item>
          <el-form-item label="规格上限 USL">
            <el-input-number v-model="usl" :controls="false" style="width: 100%" />
          </el-form-item>
        </el-form>
      </el-card>
    </el-col>

    <el-col :span="16">
      <el-card>
        <template #header>控制图（X 图，UCL/LCL = ±3σ）</template>
        <div ref="chartEl" style="height: 300px"></div>
      </el-card>
      <el-card style="margin-top: 16px">
        <template #header>过程能力</template>
        <el-row :gutter="16">
          <el-col :span="6" v-for="x in cpkCards" :key="x.label">
            <el-statistic :title="x.label" :value="x.value" />
          </el-col>
        </el-row>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'

const chartEl = ref<HTMLElement>()
const form = reactive<any>({ characteristic: 'PCB长度', materialCode: 'RM-PCB-01', workOrderNo: '', value: 100 })
const chartChar = ref('PCB长度')
const lsl = ref(99.8)
const usl = ref(100.2)
const cpkCards = ref([
  { label: 'Cp', value: 0 }, { label: 'Cpk', value: 0 },
  { label: '均值', value: 0 }, { label: 'σ', value: 0 }
])
let chart: echarts.ECharts

onMounted(() => {
  chart = echarts.init(chartEl.value!)
  loadChart()
})

async function add() {
  if (!form.characteristic || form.value == null) {
    ElMessage.warning('请填写特性与测量值')
    return
  }
  await request.post('/qms/spc/record', { sourceType: 'MANUAL', ...form })
  ElMessage.success('已录入')
  if (form.characteristic === chartChar.value) loadChart()
}

async function loadChart() {
  if (!chartChar.value) return
  const data: any = await request.get('/qms/spc/chart', { params: { characteristic: chartChar.value } })
  const pts = (data.records || []).map((r: any) => r.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['测量值', 'UCL', 'CL', 'LCL'] },
    xAxis: { type: 'category', data: pts.map((_: number, i: number) => i + 1) },
    yAxis: { type: 'value' },
    series: [
      { name: '测量值', type: 'line', data: pts, itemStyle: { color: '#409eff' } },
      { name: 'UCL', type: 'line', data: pts.map(() => data.ucl), lineStyle: { type: 'dashed', color: '#f56c6c' }, symbol: 'none' },
      { name: 'CL', type: 'line', data: pts.map(() => data.mean), lineStyle: { color: '#67c23a' }, symbol: 'none' },
      { name: 'LCL', type: 'line', data: pts.map(() => data.lcl), lineStyle: { type: 'dashed', color: '#f56c6c' }, symbol: 'none' }
    ]
  })

  const cpk: any = await request.get('/qms/spc/cpk', {
    params: { characteristic: chartChar.value, lsl: lsl.value, usl: usl.value }
  })
  cpkCards.value = [
    { label: 'Cp', value: cpk.cp }, { label: 'Cpk', value: cpk.cpk },
    { label: '均值', value: cpk.mean }, { label: 'σ', value: cpk.sigma }
  ]
}
</script>
