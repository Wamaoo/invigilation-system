<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps<{
  data: { teacherName: string; count: number }[]
  loading: boolean
}>()

const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null
let observer: ResizeObserver | null = null

const render = () => {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)
  const names = [...props.data].map(i => i.teacherName).reverse()
  const values = [...props.data].map(i => i.count).reverse()
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 80, right: 20, bottom: 10, top: 10 },
    xAxis: { type: 'value', minInterval: 1, axisLabel: { fontSize: 11 } },
    yAxis: {
      type: 'category',
      data: names,
      axisLabel: { fontSize: 11, width: 60, overflow: 'truncate' }
    },
    series: [{
      type: 'bar',
      data: values.map(v => ({
        value: v,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#f59e0b' },
            { offset: 1, color: '#ef4444' }
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
      <span style="font-weight: 600; color: var(--text-primary)">教师监考工作量 TOP10</span>
    </template>
    <div ref="chartRef" style="width: 100%; height: 300px"></div>
  </el-card>
</template>
