<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps<{
  data: { department: string; count: number }[]
  loading: boolean
}>()

const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null
let observer: ResizeObserver | null = null

const render = () => {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 90, right: 20, bottom: 30, top: 10 },
    xAxis: { type: 'value', minInterval: 1, axisLabel: { fontSize: 11 } },
    yAxis: {
      type: 'category',
      data: [...props.data].map(i => i.department).reverse(),
      axisLabel: { fontSize: 11, width: 80, overflow: 'truncate' }
    },
    series: [{
      type: 'bar',
      data: [...props.data].map(v => ({
        value: v.count,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#6366f1' },
            { offset: 1, color: '#3b82f6' }
          ]),
          borderRadius: [0, 4, 4, 0]
        }
      })),
      barWidth: 18,
      label: {
        show: true,
        position: 'right',
        fontSize: 11,
        formatter: '{c} 场'
      }
    }]
  })
}

onMounted(() => {
  nextTick(render)
  if (chartRef.value) {
    observer = new ResizeObserver(() => chart?.resize())
    observer.observe(chartRef.value)
  }
})

onUnmounted(() => {
  chart?.dispose()
  observer?.disconnect()
})

watch(() => props.data, () => nextTick(render), { deep: true })
</script>

<template>
  <el-card shadow="hover" v-loading="loading">
    <template #header>
      <span style="font-weight: 600; color: var(--text-primary)">各部门监考次数</span>
    </template>
    <div ref="chartRef" style="width: 100%; height: 300px"></div>
  </el-card>
</template>
