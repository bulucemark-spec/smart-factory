<template>
  <div>
    <el-row :gutter="16">
      <el-col :span="6" v-for="card in cards" :key="card.label">
        <el-card shadow="hover">
          <div class="stat-label">{{ card.label }}</div>
          <div class="stat-value" :style="{ color: card.color }">{{ card.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card>
          <template #header>库存状态分布</template>
          <div ref="inventoryChart" style="height: 320px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>工单状态分布</template>
          <div ref="orderChart" style="height: 320px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import * as echarts from 'echarts'
import request from '../utils/request'

const inventoryChart = ref<HTMLElement>()
const orderChart = ref<HTMLElement>()
const cards = reactive([
  { label: '待检任务', value: '-', color: '#e6a23c' },
  { label: '不合格 NCR(未关闭)', value: '-', color: '#f56c6c' },
  { label: '在制工单', value: '-', color: '#409eff' },
  { label: '库存批次总数', value: '-', color: '#67c23a' }
])

onMounted(async () => {
  const [inspections, ncrs, orders, summary] = await Promise.all([
    request.get('/qms/inspections', { params: { status: 'PENDING' } }),
    request.get('/qms/ncrs', { params: { status: 'OPEN' } }),
    request.get('/mes/work-orders'),
    request.get('/wms/inventory/summary')
  ])

  cards[0].value = inspections.length
  cards[1].value = ncrs.length
  cards[2].value = orders.filter((o: any) =>
    ['RELEASED', 'PICKED', 'STARTED', 'RUNNING'].includes(o.status)).length
  cards[3].value = (summary as any[]).reduce((s, x) => s + Number(x.cnt || 0), 0)

  const invData = (summary as any[]).map((x) => ({ name: statusName(x.status), value: Number(x.cnt) }))
  echarts.init(inventoryChart.value!).setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{ type: 'pie', radius: ['40%', '65%'], data: invData }]
  })

  const orderCount: Record<string, number> = {}
  ;(orders as any[]).forEach((o) => { orderCount[o.status] = (orderCount[o.status] || 0) + 1 })
  echarts.init(orderChart.value!).setOption({
    tooltip: {},
    xAxis: { type: 'category', data: Object.keys(orderCount) },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: Object.values(orderCount), barWidth: 30,
      itemStyle: { color: '#409eff' } }]
  })
})

function statusName(s: string) {
  return { PENDING: '待检', QUALIFIED: '合格', FROZEN: '冻结', SCRAP: '报废', DISPOSED: '已处置' }[s] || s
}
</script>

<style scoped>
.stat-label {
  color: #909399;
  font-size: 14px;
}
.stat-value {
  font-size: 30px;
  font-weight: bold;
  margin-top: 8px;
}
</style>
