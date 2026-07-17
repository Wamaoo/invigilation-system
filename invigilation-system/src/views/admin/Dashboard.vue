<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  User,
  Document,
  Calendar,
  Warning,
  Bell,
  Download
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  getOverview as fetchOverview,
  getRecentInvigilation as fetchRecentInvigilation,
  getConflictApplyList,
  handleConflictApply,
  fetchChartsData,
  type ChartData,
  type ConflictApplyItem
} from '@/api/admin'
import { updateInvigilationStatus } from '@/api/admin'
import StatusChart from '@/components/charts/StatusChart.vue'
import TrendChart from '@/components/charts/TrendChart.vue'
import WorkloadChart from '@/components/charts/WorkloadChart.vue'
import DeptChart from '@/components/charts/DeptChart.vue'

// 加载状态
const recentLoading = ref(false)
const chartsLoading = ref(false)

// 近期监考安排类型
interface RecentInvigilationItem {
  examName: string
  classroom: string
  teacherNames: string
  examTime: string
  status: string
}

// 数据概览
const teacherCount = ref(0)
const examCount = ref(0)
const invigilationCount = ref(0)
const conflictCount = ref(0)
const recentInvigilation = ref<RecentInvigilationItem[]>([])

// 冲突申请
const newApplyCount = ref(0)
const newApplyList = ref<ConflictApplyItem[]>([])
const applyAuditDialogVisible = ref(false)
const applyLoading = ref(false)

// 图表数据
const chartData = ref<ChartData | null>(null)

// 获取数据概览
const getOverview = async () => {
  try {
    const overviewData = await fetchOverview()
    teacherCount.value = overviewData.teacherCount || 0
    examCount.value = overviewData.examCount || 0
    invigilationCount.value = overviewData.invigilationCount || 0
    conflictCount.value = overviewData.conflictCount || 0
  } catch (error) {
    console.error('加载数据概览失败：', error)
    ElMessage.error('数据概览加载失败')
  }
}

// 获取近期监考
const getRecentInvigilation = async () => {
  recentLoading.value = true
  try {
    const recentData = await fetchRecentInvigilation(3)
    recentInvigilation.value = recentData || []
  } catch (error) {
    console.error('加载近期监考失败：', error)
  } finally {
    recentLoading.value = false
  }
}

// 获取冲突申请
const getNewConflictApplies = async () => {
  applyLoading.value = true
  try {
    const applyData = await getConflictApplyList(0)
    newApplyList.value = applyData || []
    newApplyCount.value = newApplyList.value.length
  } catch (error) {
    console.error('加载冲突申请失败：', error)
  } finally {
    applyLoading.value = false
  }
}

const openApplyAuditDialog = () => {
  getNewConflictApplies()
  applyAuditDialogVisible.value = true
}

const auditApply = async (id: number, status: number, arrangeId?: string, teacherId?: string, teacherName?: string) => {
  try {
    await handleConflictApply(id, status, arrangeId, teacherId, teacherName)
    ElMessage.success(status === 1 ? '已同意申请' : '已拒绝申请')
    newApplyList.value = newApplyList.value.filter((item) => item.id !== id)
    newApplyCount.value = newApplyList.value.length
    if (status === 1 && arrangeId) {
      await updateInvigilationStatus({ arrangeId: arrangeId, status: '冲突预警' })
      getOverview()
      getRecentInvigilation()
    }
  } catch (error) {
    console.error('审核失败：', error)
    ElMessage.error('审核操作失败')
  }
}

// 获取图表数据
const getChartsData = async () => {
  chartsLoading.value = true
  try {
    const data = await fetchChartsData()
    chartData.value = data
  } catch (error) {
    console.error('加载图表数据失败：', error)
  } finally {
    chartsLoading.value = false
  }
}

// 生命周期
onMounted(async () => {
  await getOverview()
  await getRecentInvigilation()
  await getNewConflictApplies()
  await getChartsData()

  setInterval(() => {
    getNewConflictApplies()
  }, 30000)
})

</script>

<template>
  <div>
    <!-- 新申请提示 -->
    <div
      v-if="newApplyCount > 0"
      class="new-apply-notice"
      @click="openApplyAuditDialog"
    >
      <el-badge :value="newApplyCount" type="danger" class="notice-badge">
        <el-icon style="font-size: 20px; color: #ef4444"><Bell /></el-icon>
        <span style="margin-left: 5px">您有新的冲突申请</span>
      </el-badge>
    </div>

    <h2 style="margin-bottom: 20px; color: var(--text-primary)">数据概览</h2>
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" style="text-align: center; padding: 20px">
          <el-icon style="font-size: 36px; color: #3b82f6; margin-bottom: 10px"><User /></el-icon>
          <h3 style="font-size: 24px; margin: 0">{{ teacherCount }}</h3>
          <p style="color: var(--text-secondary); margin-top: 6px">教师总数</p>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" style="text-align: center; padding: 20px">
          <el-icon style="font-size: 36px; color: #10b981; margin-bottom: 10px"><Document /></el-icon>
          <h3 style="font-size: 24px; margin: 0">{{ examCount }}</h3>
          <p style="color: var(--text-secondary); margin-top: 6px">考试科目</p>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" style="text-align: center; padding: 20px">
          <el-icon style="font-size: 36px; color: #f59e0b; margin-bottom: 10px"><Calendar /></el-icon>
          <h3 style="font-size: 24px; margin: 0">{{ invigilationCount }}</h3>
          <p style="color: var(--text-secondary); margin-top: 6px">监考安排</p>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" style="text-align: center; padding: 20px">
          <el-icon style="font-size: 36px; color: #ef4444; margin-bottom: 10px"><Warning /></el-icon>
          <h3 style="font-size: 24px; margin: 0">{{ conflictCount }}</h3>
          <p style="color: var(--text-secondary); margin-top: 6px">冲突预警</p>
        </el-card>
      </el-col>
    </el-row>

    <!-- 近期监考安排 -->
    <el-card shadow="hover" style="margin-top: 24px">
      <template #header>
        <span style="font-size: 16px; font-weight: 600; color: var(--text-primary)">近期监考安排</span>
      </template>
      <el-table :data="recentInvigilation" border style="width: 100%" v-loading="recentLoading">
        <el-table-column prop="examName" label="考试科目" />
        <el-table-column prop="classroom" label="考场" />
        <el-table-column prop="teacherNames" label="监考教师" />
        <el-table-column prop="examTime" label="考试时间" />
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag
              :type="scope.row.status === '已安排' ? 'success' : scope.row.status === '冲突预警' ? 'warning' : 'info'"
            >
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- ==================== 数据可视化图表 ==================== -->
    <h2 style="margin: 30px 0 20px; color: var(--text-primary)">数据可视化</h2>
    <div class="dashboard-charts">
      <el-row :gutter="20">
        <el-col :span="12">
          <StatusChart :data="chartData?.statusData ?? []" :loading="chartsLoading" />
        </el-col>
        <el-col :span="12">
          <TrendChart :labels="chartData?.monthLabels ?? []" :values="chartData?.monthValues ?? []" :loading="chartsLoading" />
        </el-col>
      </el-row>
      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="12">
          <DeptChart :data="chartData?.departmentData ?? []" :loading="chartsLoading" />
        </el-col>
        <el-col :span="12">
          <WorkloadChart :data="chartData?.workloadTop10 ?? []" :loading="chartsLoading" />
        </el-col>
      </el-row>
    </div>

    <!-- 冲突申请审核弹窗 -->
    <el-dialog v-model="applyAuditDialogVisible" title="冲突申请审核" width="1050px" top="3vh">
      <el-table :data="newApplyList" border style="width: 100%" v-loading="applyLoading" max-height="560">
        <el-table-column prop="id" label="申请ID" width="80" />
        <el-table-column prop="teacherId" label="教师工号" width="100" />
        <el-table-column prop="teacherName" label="教师姓名" width="100" />
        <el-table-column prop="arrangeId" label="安排ID" width="120" />
        <el-table-column prop="reason" label="申请原因" min-width="140" />
        <el-table-column prop="createTime" label="申请时间" width="175" />
        <el-table-column label="申请材料" width="130">
          <template #default="scope">
            <span v-if="scope.row.fileUrl">
              <el-link :href="scope.row.fileUrl" target="_blank" type="primary" :underline="false">
                <el-icon style="margin-right: 4px"><Download /></el-icon>查看附件
              </el-link>
            </span>
            <span v-else style="color: #909399; font-size: 13px">无</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="scope">
            <el-space>
              <el-button type="success" size="small" @click="auditApply(scope.row.id, 1, scope.row.arrangeId, scope.row.teacherId, scope.row.teacherName)">同意</el-button>
              <el-button type="danger" size="small" @click="auditApply(scope.row.id, 2, undefined, scope.row.teacherId, scope.row.teacherName)">拒绝</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="applyAuditDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.new-apply-notice {
  position: absolute;
  top: 25px;
  right: 200px;
  cursor: pointer;
}
.notice-badge {
  font-size: 14px;
  display: flex;
  align-items: center;
}
.dashboard-charts {
  margin-bottom: 30px;
}
</style>
