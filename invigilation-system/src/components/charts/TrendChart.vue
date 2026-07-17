<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps<{
  labels: string[]
  values: number[]
  loading: boolean
}>()

const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null
let observer: ResizeObserver | null = null

const render = () => {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 20, bottom: 30, top: 20 },
    xAxis: {
      type: 'category',
      data: props.labels.map(m => m.replace('2026-', '').replace('2025-', '') + '月'),
      axisLabel: { fontSize: 11 }
    },
    yAxis: { type: 'value', minInterval: 1, axisLabel: { fontSize: 11 } },
    series: [{
      type: 'line',
      data: props.values,
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: { width: 3, color: '#3b82f6' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(59,130,246,0.3)' },
          { offset: 1, color: 'rgba(59,130,246,0.02)' }
        ])
      },
      itemStyle: { color: '#3b82f6' }
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

watch(() => [props.labels, props.values], () => nextTick(render), { deep: true })
</script>

<template>
  <el-card shadow="hover" v-loading="loading">
    <template #header>
      <span style="font-weight: 600; color: var(--text-primary)">各月监考数量趋势</span>
    </template>
    <div ref="chartRef" style="width: 100%; height: 300px"></div>
  </el-card>
</template>
