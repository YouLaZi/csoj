<template>
  <div class="leaderboard-container">
    <a-card class="leaderboard-card" :bordered="false">
      <template #title>
        <div class="leaderboard-title">
          <div class="title-with-badge">
            <a-typography-title :heading="4">积分排行榜</a-typography-title>
            <a-badge :count="timeRangeLabel" type="primary" />
          </div>
          <a-radio-group
            v-model="timeRange"
            type="button"
            @change="handleTimeRangeChange"
          >
            <a-radio value="day">日榜</a-radio>
            <a-radio value="week">周榜</a-radio>
            <a-radio value="month">月榜</a-radio>
            <a-radio value="all">总榜</a-radio>
          </a-radio-group>
        </div>
      </template>

      <a-spin :loading="loading">
        <a-table
          :data="leaderboardData"
          :pagination="false"
          :bordered="false"
          row-key="userId"
          class="leaderboard-table"
          :row-class="getRowClass"
          :scroll="{ x: '100%' }"
        >
          <template #columns>
            <a-table-column title="排名" data-index="rank" :width="100">
              <template #cell="{ rowIndex }">
                <div class="rank-cell">
                  <template v-if="rowIndex < 3">
                    <a-avatar
                      shape="circle"
                      :style="getRankStyle(rowIndex)"
                      class="rank-avatar"
                    >
                      {{ rowIndex + 1 }}
                    </a-avatar>
                  </template>
                  <template v-else>
                    <span class="rank-number">{{ rowIndex + 1 }}</span>
                  </template>
                </div>
              </template>
            </a-table-column>
            <a-table-column title="用户" data-index="userName" :width="200">
              <template #cell="{ record }">
                <div class="user-cell">
                  <a-avatar :size="32" :style="getAvatarStyle(record)">
                    {{ record.userName ? record.userName.charAt(0) : "?" }}
                  </a-avatar>
                  <span class="user-name">{{ record.userName }}</span>
                </div>
              </template>
            </a-table-column>
            <a-table-column title="积分" data-index="totalPoints" :width="150">
              <template #cell="{ record }">
                <a-statistic
                  :value="record.totalPoints"
                  :precision="0"
                  show-group-separator
                  :value-style="getPointsStyle(record.totalPoints)"
                  class="points-value"
                >
                  <template #suffix>
                    <a-tooltip content="积分">
                      <icon-fire
                        :style="{
                          marginLeft: '4px',
                          color:
                            typeof document !== 'undefined' &&
                            document.documentElement
                              ? getComputedStyle(document.documentElement)
                                  .getPropertyValue('--warning-color')
                                  .trim() || '#ff7d00'
                              : '#ff7d00',
                        }"
                      />
                    </a-tooltip>
                  </template>
                </a-statistic>
              </template>
            </a-table-column>
          </template>
        </a-table>

        <div
          v-if="leaderboardData.length === 0 && !loading"
          class="empty-state"
        >
          <a-empty description="暂无排行数据">
            <template #image>
              <icon-trophy
                style="font-size: 48px; color: var(--color-text-3)"
              />
            </template>
          </a-empty>
        </div>
      </a-spin>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { Message } from "@arco-design/web-vue";
import { IconFire, IconTrophy } from "@arco-design/web-vue/es/icon";
import { LeaderboardControllerService } from "../../generated/services/LeaderboardControllerService";
import type { UserPointsRankVO } from "../../generated/models/UserPointsRankVO";

// 时间范围选择
const timeRange = ref("week");

// 加载状态
const loading = ref(false);

// 排行榜数据
const leaderboardData = ref<UserPointsRankVO[]>([]);

// 时间范围标签
const timeRangeLabel = computed(() => {
  const labels: Record<string, string> = {
    day: "今日",
    week: "本周",
    month: "本月",
    all: "全部",
  };
  return labels[timeRange.value] || "";
});

// 获取排行榜数据
const fetchLeaderboardData = async () => {
  loading.value = true;
  try {
    // 调用后端API获取排行榜数据
    const res = await LeaderboardControllerService.getPointsRankingUsingGet(
      timeRange.value
    );

    if (res.code === 0 && res.data) {
      leaderboardData.value = res.data || [];
      // 如果返回的数据为空，且是周榜，则使用模拟数据
      if (leaderboardData.value.length === 0 && timeRange.value === "week") {
        leaderboardData.value = [
          { userId: 1, userName: "用户A", totalPoints: 1250, rank: 1 },
          { userId: 2, userName: "用户B", totalPoints: 980, rank: 2 },
          { userId: 3, userName: "用户C", totalPoints: 820, rank: 3 },
          { userId: 4, userName: "用户D", totalPoints: 750, rank: 4 },
          { userId: 5, userName: "用户E", totalPoints: 680, rank: 5 },
        ];
      }
    } else {
      Message.error(res.message || "获取排行榜数据失败");
      // 如果API尚未实现或出错，使用模拟数据
      leaderboardData.value = [
        { userId: 1, userName: "用户A", totalPoints: 1250, rank: 1 },
        { userId: 2, userName: "用户B", totalPoints: 980, rank: 2 },
        { userId: 3, userName: "用户C", totalPoints: 820, rank: 3 },
        { userId: 4, userName: "用户D", totalPoints: 750, rank: 4 },
        { userId: 5, userName: "用户E", totalPoints: 680, rank: 5 },
      ];
    }
  } catch (error: any) {
    console.error("获取排行榜数据失败", error);
    Message.error(error.message || "获取排行榜数据失败，请稍后重试");
    // 使用模拟数据作为后备
    leaderboardData.value = [
      { userId: 1, userName: "用户A", totalPoints: 1250, rank: 1 },
      { userId: 2, userName: "用户B", totalPoints: 980, rank: 2 },
      { userId: 3, userName: "用户C", totalPoints: 820, rank: 3 },
      { userId: 4, userName: "用户D", totalPoints: 750, rank: 4 },
      { userId: 5, userName: "用户E", totalPoints: 680, rank: 5 },
    ];
  } finally {
    loading.value = false;
  }
};

// 处理时间范围变化
const handleTimeRangeChange = () => {
  fetchLeaderboardData();
};

// 获取排名样式
const getRankStyle = (index: number) => {
  const getThemeColor = (colorVar: string, fallback: string) => {
    if (typeof document === "undefined" || !document.documentElement) {
      return fallback;
    }
    return (
      getComputedStyle(document.documentElement)
        .getPropertyValue(colorVar)
        .trim() || fallback
    );
  };

  const styles = [
    {
      backgroundColor: getThemeColor("--warning-color", "#FFD700"),
      color: "var(--color-white)",
    }, // 金牌
    {
      backgroundColor: getThemeColor("--info-color", "#C0C0C0"),
      color: "var(--color-white)",
    }, // 银牌
    {
      backgroundColor: getThemeColor("--danger-color", "#CD7F32"),
      color: "var(--color-white)",
    }, // 铜牌
  ];
  return styles[index] || {};
};

// 获取头像样式
const getAvatarStyle = (record: UserPointsRankVO) => {
  // 根据用户ID生成一个固定的颜色，使用主题变量
  const getThemeColor = (colorVar: string, fallback: string) => {
    if (typeof document === "undefined" || !document.documentElement) {
      return fallback;
    }
    return (
      getComputedStyle(document.documentElement)
        .getPropertyValue(colorVar)
        .trim() || fallback
    );
  };

  const colors = [
    getThemeColor("--primary-color", "#165DFF"),
    getThemeColor("--success-color", "#0FC6C2"),
    getThemeColor("--warning-color", "#722ED1"),
    getThemeColor("--danger-color", "#F5222D"),
    getThemeColor("--info-color", "#FA8C16"),
    "#FADB14", // 保留一些固定颜色以增加多样性
    "#52C41A",
    "#EB2F96",
    "#F5222D",
    "#13C2C2",
  ];
  const colorIndex = record.userId ? record.userId % colors.length : 0;
  return { backgroundColor: colors[colorIndex] };
};

// 获取积分样式
const getPointsStyle = (points: number | undefined) => {
  if (points === undefined) return {};

  const getThemeColor = (colorVar: string, fallback: string) => {
    if (typeof document === "undefined" || !document.documentElement) {
      return fallback;
    }
    return (
      getComputedStyle(document.documentElement)
        .getPropertyValue(colorVar)
        .trim() || fallback
    );
  };

  // 根据积分值返回不同的颜色
  if (points >= 1000) {
    return {
      color: getThemeColor("--danger-color", "#f5222d"),
      fontWeight: "bold",
    };
  } else if (points >= 500) {
    return {
      color: getThemeColor("--warning-color", "#fa8c16"),
      fontWeight: "bold",
    };
  } else {
    return {
      color: getThemeColor("--success-color", "#3f8600"),
      fontWeight: "bold",
    };
  }
};

// 获取行样式
const getRowClass = (_record: UserPointsRankVO, rowIndex: number) => {
  if (rowIndex < 3) {
    return `top-${rowIndex + 1}-row`;
  }
  return "";
};

onMounted(() => {
  fetchLeaderboardData();
});
</script>

<style scoped>
.leaderboard-container {
  padding: 20px;
  background-color: var(--color-bg-1);
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.leaderboard-container:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.leaderboard-card {
  border-radius: 12px;
  background-color: var(--color-bg-2);
  overflow: hidden;
}

.leaderboard-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.title-with-badge {
  display: flex;
  align-items: center;
  gap: 12px;
}

.leaderboard-table {
  border: none;
  width: 100%;
}

.leaderboard-table :deep(.arco-table) {
  width: 100%;
}

.leaderboard-table :deep(.arco-table-container) {
  width: 100%;
}

.leaderboard-table :deep(.arco-table-cell) {
  border-bottom: 1px solid var(--color-border-2);
  padding: 12px 16px;
  text-align: center;
}

.leaderboard-table :deep(.arco-table-tr:last-child .arco-table-td) {
  border-bottom: none;
}

.leaderboard-table :deep(.arco-table-tr:hover .arco-table-td) {
  background-color: var(--color-fill-2);
}

.leaderboard-table :deep(.arco-table-th) {
  text-align: center;
  font-weight: 600;
}

.user-cell {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 8px;
}

.rank-cell {
  display: flex;
  justify-content: center;
  align-items: center;
}

.points-value {
  display: flex;
  justify-content: center;
  align-items: center;
}

.rank-cell {
  display: flex;
  align-items: center;
  justify-content: center;
}

.rank-avatar {
  font-weight: bold;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.rank-avatar:hover {
  transform: scale(1.1);
}

.rank-number {
  font-weight: bold;
  color: var(--color-text-2);
  font-size: 16px;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-cell .arco-avatar {
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.user-cell:hover .arco-avatar {
  transform: scale(1.1);
  border-color: var(--color-primary-light-3);
}

.user-name {
  font-weight: 500;
  color: var(--color-text-1);
  transition: color 0.3s ease;
}

.user-cell:hover .user-name {
  color: var(--color-primary);
}

.points-value :deep(.arco-statistic-value-content) {
  font-size: 18px;
  transition: all 0.3s ease;
}

.points-value:hover :deep(.arco-statistic-value-content) {
  transform: scale(1.1);
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

.top-1-row .arco-table-td {
  background-color: rgba(255, 215, 0, 0.15) !important; /* 金牌背景 */
}

.top-2-row .arco-table-td {
  background-color: rgba(192, 192, 192, 0.15) !important; /* 银牌背景 */
}

.top-3-row .arco-table-td {
  background-color: rgba(205, 127, 50, 0.15) !important; /* 铜牌背景 */
}

/* 深色模式适配 */
:root[data-theme="dark"] .leaderboard-container {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.25);
}

:root[data-theme="dark"] .leaderboard-card {
  background-color: var(--color-bg-2);
}

:root[data-theme="dark"] .rank-number {
  color: var(--color-text-2);
}

:root[data-theme="dark"] .user-name {
  color: var(--color-text-1);
}

:root[data-theme="dark"] .top-1-row .arco-table-td {
  background-color: rgba(255, 215, 0, 0.2) !important;
}

:root[data-theme="dark"] .top-2-row .arco-table-td {
  background-color: rgba(192, 192, 192, 0.2) !important;
}

:root[data-theme="dark"] .top-3-row .arco-table-td {
  background-color: rgba(205, 127, 50, 0.2) !important;
}

/* 添加动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.leaderboard-table :deep(.arco-table-tr) {
  animation: fadeIn 0.5s ease-out;
  animation-fill-mode: both;
}

.leaderboard-table :deep(.arco-table-tr:nth-child(1)) {
  animation-delay: 0.1s;
}

.leaderboard-table :deep(.arco-table-tr:nth-child(2)) {
  animation-delay: 0.2s;
}

.leaderboard-table :deep(.arco-table-tr:nth-child(3)) {
  animation-delay: 0.3s;
}

.leaderboard-table :deep(.arco-table-tr:nth-child(4)) {
  animation-delay: 0.4s;
}

.leaderboard-table :deep(.arco-table-tr:nth-child(5)) {
  animation-delay: 0.5s;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .leaderboard-title {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .rank-avatar {
    width: 28px !important;
    height: 28px !important;
    font-size: 14px !important;
  }

  .user-cell {
    gap: 8px;
  }

  .user-cell .arco-avatar {
    width: 28px !important;
    height: 28px !important;
  }

  .user-name {
    font-size: 14px;
  }

  .points-value :deep(.arco-statistic-value-content) {
    font-size: 14px;
  }
}
</style>
