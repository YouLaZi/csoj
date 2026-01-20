<template>
  <div class="user-statistics">
    <a-card class="statistics-card" :bordered="false">
      <template #title>
        <div class="card-title"><icon-dashboard /> 学习数据统计</div>
      </template>

      <!-- 数据概览 -->
      <a-row :gutter="16" class="stat-overview">
        <a-col :span="8" :xs="24" :sm="8">
          <a-statistic
            title="已解决题目"
            :value="statistics.solvedCount"
            :loading="loading"
          >
            <template #suffix>
              <span class="suffix-text">题</span>
            </template>
          </a-statistic>
        </a-col>
        <a-col :span="8" :xs="24" :sm="8">
          <a-statistic
            title="提交次数"
            :value="statistics.submissionCount"
            :loading="loading"
          >
            <template #suffix>
              <span class="suffix-text">次</span>
            </template>
          </a-statistic>
        </a-col>
        <a-col :span="8" :xs="24" :sm="8">
          <a-statistic
            title="通过率"
            :value="statistics.passRate"
            :precision="1"
            :loading="loading"
          >
            <template #suffix>
              <span class="suffix-text">%</span>
            </template>
          </a-statistic>
        </a-col>
      </a-row>

      <!-- 学习趋势图表 -->
      <div class="chart-section">
        <div class="section-title">学习趋势</div>
        <div class="chart-tabs">
          <a-radio-group v-model="activeChartTab" type="button" size="small">
            <a-radio value="week">周</a-radio>
            <a-radio value="month">月</a-radio>
            <a-radio value="year">年</a-radio>
          </a-radio-group>
        </div>
        <div class="chart-container" v-if="!loading">
          <div ref="trendChartRef" class="trend-chart"></div>
        </div>
        <a-skeleton v-else animation :loading="loading" />
      </div>

      <!-- 解题进度 -->
      <div class="progress-section">
        <div class="section-title">解题进度</div>
        <a-progress
          :percent="statistics.progressPercent"
          :animation="true"
          :stroke-color="getProgressColors()"
        />
      </div>

      <!-- 难度分布 -->
      <div class="difficulty-distribution">
        <div class="section-title">难度分布</div>
        <div class="chart-container" v-if="!loading">
          <div ref="difficultyChartRef" class="difficulty-chart"></div>
        </div>
        <a-skeleton v-else animation :loading="loading" />
      </div>

      <!-- 最近活动 -->
      <div class="recent-activity">
        <div class="section-title">最近活动</div>
        <a-empty
          v-if="!statistics.recentActivities.length && !loading"
          description="暂无活动记录"
        />
        <a-spin v-else-if="loading" />
        <a-timeline v-else>
          <a-timeline-item
            v-for="(activity, index) in statistics.recentActivities"
            :key="index"
            :dot-color="getActivityColor(activity.type)"
          >
            <div class="activity-item">
              <span class="activity-time">{{ formatTime(activity.time) }}</span>
              <span class="activity-content">{{ activity.content }}</span>
            </div>
          </a-timeline-item>
        </a-timeline>
      </div>

      <!-- 推荐题目 -->
      <div class="recommended-questions">
        <div class="section-title">推荐题目</div>
        <a-empty
          v-if="!statistics.recommendedQuestions.length && !loading"
          description="暂无推荐题目"
        />
        <a-spin v-else-if="loading" />
        <a-list v-else>
          <a-list-item
            v-for="(question, index) in statistics.recommendedQuestions"
            :key="index"
          >
            <a-list-item-meta>
              <template #title>
                <router-link
                  :to="`/question/view/${question.id}`"
                  class="question-link"
                >
                  {{ question.title }}
                </router-link>
              </template>
              <template #description>
                <div class="question-tags">
                  <a-tag v-for="tag in question.tags" :key="tag" size="small">{{
                    tag
                  }}</a-tag>
                  <a-tag color="#165DFF" size="small">{{
                    question.difficulty
                  }}</a-tag>
                </div>
              </template>
            </a-list-item-meta>
          </a-list-item>
        </a-list>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from "vue";
import { IconDashboard } from "@arco-design/web-vue/es/icon";
import { Message } from "@arco-design/web-vue";
import moment from "moment";
import * as echarts from "echarts/core";
import { BarChart, LineChart, PieChart } from "echarts/charts";
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  ToolboxComponent,
} from "echarts/components";
import { CanvasRenderer } from "echarts/renderers";

// 注册必要的 ECharts 组件
echarts.use([
  BarChart,
  LineChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  ToolboxComponent,
  CanvasRenderer,
]);

// 加载状态
const loading = ref(true);

// 图表引用
const trendChartRef = ref(null);
const difficultyChartRef = ref(null);
let trendChart = null;
let difficultyChart = null;

// 活动图表选项卡
const activeChartTab = ref("week");

// 用户统计数据
const statistics = reactive({
  solvedCount: 0,
  submissionCount: 0,
  passRate: 0,
  progressPercent: 0,
  recentActivities: [] as {
    time: string;
    content: string;
    type: "submit" | "solve" | "comment" | "other";
  }[],
  recommendedQuestions: [] as {
    id: number;
    title: string;
    tags: string[];
    difficulty: string;
  }[],
  // 添加趋势数据
  trendData: {
    week: [] as { date: string; solved: number; submitted: number }[],
    month: [] as { date: string; solved: number; submitted: number }[],
    year: [] as { date: string; solved: number; submitted: number }[],
  },
  // 添加难度分布数据
  difficultyDistribution: [] as {
    value: number;
    name: string;
    itemStyle: { color: string };
  }[],
});

// 获取用户统计数据
const fetchUserStatistics = async () => {
  loading.value = true;
  try {
    // 这里应该调用后端API获取真实数据
    // 目前使用模拟数据
    setTimeout(() => {
      statistics.solvedCount = 42;
      statistics.submissionCount = 87;
      statistics.passRate = 48.3;
      statistics.progressPercent = 35;

      statistics.recentActivities = [
        {
          time: "2025-06-15T14:30:00",
          content: "解决了题目「两数之和」",
          type: "solve",
        },
        {
          time: "2025-06-14T10:15:00",
          content: "提交了题目「合并区间」的解答",
          type: "submit",
        },
        {
          time: "2025-06-13T16:45:00",
          content: "评论了题目「最长回文子串」",
          type: "comment",
        },
        {
          time: "2025-06-12T09:20:00",
          content: "提交了题目「二叉树的最大深度」的解答",
          type: "submit",
        },
      ];

      statistics.recommendedQuestions = [
        {
          id: 101,
          title: "有效的括号",
          tags: ["栈", "字符串"],
          difficulty: "简单",
        },
        {
          id: 102,
          title: "最长递增子序列",
          tags: ["动态规划", "二分查找"],
          difficulty: "中等",
        },
        {
          id: 103,
          title: "合并K个排序链表",
          tags: ["链表", "分治", "堆"],
          difficulty: "困难",
        },
      ];

      // 添加趋势数据
      statistics.trendData = {
        week: [
          { date: "周一", solved: 3, submitted: 5 },
          { date: "周二", solved: 2, submitted: 4 },
          { date: "周三", solved: 5, submitted: 8 },
          { date: "周四", solved: 4, submitted: 6 },
          { date: "周五", solved: 6, submitted: 10 },
          { date: "周六", solved: 8, submitted: 12 },
          { date: "周日", solved: 5, submitted: 7 },
        ],
        month: [
          { date: "第1周", solved: 12, submitted: 20 },
          { date: "第2周", solved: 15, submitted: 25 },
          { date: "第3周", solved: 8, submitted: 15 },
          { date: "第4周", solved: 7, submitted: 12 },
        ],
        year: [
          { date: "1月", solved: 30, submitted: 45 },
          { date: "2月", solved: 25, submitted: 40 },
          { date: "3月", solved: 35, submitted: 50 },
          { date: "4月", solved: 28, submitted: 42 },
          { date: "5月", solved: 32, submitted: 48 },
          { date: "6月", solved: 40, submitted: 60 },
        ],
      };

      // 添加难度分布数据
      statistics.difficultyDistribution = [
        {
          value: 20,
          name: "简单",
          itemStyle: { color: getDifficultyColor("easy") },
        },
        {
          value: 15,
          name: "中等",
          itemStyle: { color: getDifficultyColor("medium") },
        },
        {
          value: 7,
          name: "困难",
          itemStyle: { color: getDifficultyColor("hard") },
        },
      ];

      loading.value = false;

      // 初始化图表
      setTimeout(() => {
        initTrendChart();
        initDifficultyChart();
      }, 100);
    }, 1000);
  } catch (error) {
    console.error("获取用户统计数据失败", error);
    Message.error("获取用户统计数据失败");
    loading.value = false;
  }
};

// 初始化趋势图表
const initTrendChart = () => {
  if (!trendChartRef.value) return;

  trendChart = echarts.init(trendChartRef.value, getChartTheme());
  updateTrendChart();

  // 监听窗口大小变化，调整图表大小
  window.addEventListener("resize", () => {
    trendChart && trendChart.resize();
  });
};

// 更新趋势图表数据
const updateTrendChart = () => {
  if (!trendChart) return;

  const data = statistics.trendData[activeChartTab.value];
  const xAxisData = data.map((item) => item.date);
  const solvedData = data.map((item) => item.solved);
  const submittedData = data.map((item) => item.submitted);

  const option = {
    tooltip: {
      trigger: "axis",
      axisPointer: {
        type: "shadow",
      },
    },
    legend: {
      data: ["已解决", "已提交"],
    },
    grid: {
      left: "3%",
      right: "4%",
      bottom: "3%",
      containLabel: true,
    },
    xAxis: {
      type: "category",
      data: xAxisData,
    },
    yAxis: {
      type: "value",
    },
    series: [
      {
        name: "已解决",
        type: "bar",
        stack: "total",
        emphasis: {
          focus: "series",
        },
        data: solvedData,
        itemStyle: {
          color: getChartColor("success"),
        },
      },
      {
        name: "已提交",
        type: "bar",
        stack: "total",
        emphasis: {
          focus: "series",
        },
        data: submittedData,
        itemStyle: {
          color: getChartColor("primary"),
        },
      },
    ],
  };

  trendChart.setOption(option);
};

// 初始化难度分布图表
const initDifficultyChart = () => {
  if (!difficultyChartRef.value) return;

  difficultyChart = echarts.init(difficultyChartRef.value, getChartTheme());
  updateDifficultyChart();

  // 监听窗口大小变化，调整图表大小
  window.addEventListener("resize", () => {
    difficultyChart && difficultyChart.resize();
  });
};

// 更新难度分布图表
const updateDifficultyChart = () => {
  if (!difficultyChart) return;

  const option = {
    tooltip: {
      trigger: "item",
      formatter: "{a} <br/>{b}: {c} ({d}%)",
    },
    legend: {
      orient: "horizontal",
      bottom: "bottom",
      data: ["简单", "中等", "困难"],
    },
    series: [
      {
        name: "难度分布",
        type: "pie",
        radius: ["40%", "70%"],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: "var(--color-bg-1)",
          borderWidth: 2,
        },
        label: {
          show: false,
          position: "center",
        },
        emphasis: {
          label: {
            show: true,
            fontSize: "18",
            fontWeight: "bold",
          },
        },
        labelLine: {
          show: false,
        },
        data: statistics.difficultyDistribution,
      },
    ],
  };

  difficultyChart.setOption(option);
};

// 根据当前主题获取图表主题
const getChartTheme = () => {
  const htmlElement = document.documentElement;
  const theme = htmlElement.getAttribute("data-theme") || "light";
  return theme === "dark" ? "dark" : null;
};

// 监听主题变化，更新图表主题
const updateChartsTheme = () => {
  const theme = getChartTheme();

  if (trendChart) {
    trendChart.dispose();
    trendChart = echarts.init(trendChartRef.value, theme);
    updateTrendChart();
  }

  if (difficultyChart) {
    difficultyChart.dispose();
    difficultyChart = echarts.init(difficultyChartRef.value, theme);
    updateDifficultyChart();
  }
};

// 格式化时间
const formatTime = (timeString: string) => {
  return moment(timeString).format("YYYY-MM-DD HH:mm");
};

// 根据活动类型获取颜色
const getActivityColor = (type: string) => {
  const colorMap: Record<string, string> = {
    solve: "green",
    submit: "blue",
    comment: "orange",
    other: "gray",
  };
  return colorMap[type] || "blue";
};

// 获取进度条颜色
const getProgressColors = () => {
  const primaryColor = getComputedStyle(document.documentElement)
    .getPropertyValue("--primary-color")
    .trim();
  const successColor = getComputedStyle(document.documentElement)
    .getPropertyValue("--success-color")
    .trim();
  return {
    "0%": primaryColor || "#3C7EFF",
    "100%": successColor || "#00B42A",
  };
};

// 获取难度颜色
const getDifficultyColor = (difficulty: string) => {
  const colorMap: Record<string, string> = {
    easy:
      getComputedStyle(document.documentElement)
        .getPropertyValue("--success-color")
        .trim() || "#00B42A",
    medium:
      getComputedStyle(document.documentElement)
        .getPropertyValue("--warning-color")
        .trim() || "#F7BA1E",
    hard:
      getComputedStyle(document.documentElement)
        .getPropertyValue("--danger-color")
        .trim() || "#F53F3F",
  };
  return colorMap[difficulty] || "#86909C";
};

// 获取图表颜色
const getChartColor = (type: string) => {
  const colorMap: Record<string, string> = {
    primary:
      getComputedStyle(document.documentElement)
        .getPropertyValue("--primary-color")
        .trim() || "#165DFF",
    success:
      getComputedStyle(document.documentElement)
        .getPropertyValue("--success-color")
        .trim() || "#00B42A",
    warning:
      getComputedStyle(document.documentElement)
        .getPropertyValue("--warning-color")
        .trim() || "#FF7D00",
    danger:
      getComputedStyle(document.documentElement)
        .getPropertyValue("--danger-color")
        .trim() || "#F53F3F",
    info:
      getComputedStyle(document.documentElement)
        .getPropertyValue("--info-color")
        .trim() || "#86909C",
  };
  return colorMap[type] || "#86909C";
};

// 监听图表选项卡变化
watch(activeChartTab, () => {
  updateTrendChart();
});

// 组件挂载时获取数据
onMounted(() => {
  fetchUserStatistics();

  // 监听主题变化
  window.addEventListener("themeChange", () => {
    updateChartsTheme();
  });
});

// 监听主题变化的另一种方式 - 使用MutationObserver
const observeThemeChange = () => {
  const observer = new MutationObserver((mutations) => {
    mutations.forEach((mutation) => {
      if (
        mutation.type === "attributes" &&
        mutation.attributeName === "data-theme"
      ) {
        updateChartsTheme();
        // 重新获取难度分布数据以更新颜色
        if (statistics.difficultyDistribution.length > 0) {
          statistics.difficultyDistribution = [
            {
              value: statistics.difficultyDistribution[0].value,
              name: "简单",
              itemStyle: { color: getDifficultyColor("easy") },
            },
            {
              value: statistics.difficultyDistribution[1].value,
              name: "中等",
              itemStyle: { color: getDifficultyColor("medium") },
            },
            {
              value: statistics.difficultyDistribution[2].value,
              name: "困难",
              itemStyle: { color: getDifficultyColor("hard") },
            },
          ];
          updateDifficultyChart();
        }
      }
    });
  });

  observer.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ["data-theme"],
  });
};

// 在组件挂载时启动主题监听
onMounted(() => {
  observeThemeChange();
});
</script>

<style scoped>
.user-statistics {
  margin-bottom: 24px;
}

.statistics-card {
  background-color: var(--bg-color);
  border-radius: 8px;
  box-shadow: var(--box-shadow);
  transition: box-shadow 0.3s;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-color-primary);
}

.stat-overview {
  margin-bottom: 24px;
}

.suffix-text {
  font-size: 14px;
  margin-left: 4px;
  color: var(--text-color-secondary);
}

.section-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 16px;
  color: var(--text-color-primary);
}

.progress-section {
  margin-bottom: 24px;
}

.chart-section {
  margin-bottom: 24px;
}

.chart-tabs {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}

.chart-container {
  height: 300px;
  margin-bottom: 16px;
}

.trend-chart,
.difficulty-chart {
  width: 100%;
  height: 100%;
}

.difficulty-distribution {
  margin-bottom: 24px;
}

.recent-activity {
  margin-bottom: 24px;
}

.activity-item {
  display: flex;
  flex-direction: column;
}

.activity-time {
  font-size: 12px;
  color: var(--text-color-secondary);
  margin-bottom: 4px;
}

.activity-content {
  color: var(--text-color-primary);
}

.question-link {
  color: var(--primary-color);
  text-decoration: none;
}

.question-link:hover {
  text-decoration: underline;
}

.question-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 4px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .stat-overview > div {
    margin-bottom: 16px;
  }
}
</style>
