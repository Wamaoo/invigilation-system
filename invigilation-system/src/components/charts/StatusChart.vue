<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps<{
  data: { name: string; value: number }[]
  loading: boolean
}>()

const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null
let observer: ResizeObserver | null = null

const render = () => {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)
  const colors = ['#10b981', '#ef4444', '#f59e0b', '#6b7280']
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, textStyle: { fontSize: 12 } },
    series: [{
      type: 'pie',
      radius: ['45%', '70%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{c} 场', fontSize: 11 },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold' },
        itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.2)' }
      },
      data: props.data.map((item, idx) => ({
        ...item,
        itemStyle: { color: colors[idx % colors.length] }
      }))
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
      <span style="font-weight: 600; color: var(--text-primary)">监考状态分布</span>
    </template>
    <div ref="chartRef" style="width: 100%; height: 300px"></div>
  </el-card>
</template>
