<template>
  <div class="problem-heatmap">
    <div class="heatmap-header">
      <h3>📊 刷题热力图</h3>
      <div class="year-selector">
        <button @click="prevYear">&lt;</button>
        <span>{{ year }}</span>
        <button @click="nextYear">&gt;</button>
      </div>
    </div>

    <div class="heatmap-grid">
      <div class="month-labels">
        <span v-for="month in 12" :key="month" class="month-label">
          {{ monthNames[month - 1] }}
        </span>
      </div>
      <div class="heatmap-cells">
        <div
          v-for="(day, index) in calendarDays"
          :key="index"
          class="heatmap-cell"
          :class="getLevelClass(day)"
          :title="getTooltip(day)"
        ></div>
      </div>
    </div>

    <div class="heatmap-legend">
      <span>少</span>
      <div class="legend-cells">
        <div class="legend-cell level-0"></div>
        <div class="legend-cell level-1"></div>
        <div class="legend-cell level-2"></div>
        <div class="legend-cell level-3"></div>
        <div class="legend-cell level-4"></div>
      </div>
      <span>多</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import StatsService from "@/services/StatsService";

interface HeatmapData {
  date: string;
  solvedCount: number;
}

const year = ref(new Date().getFullYear());
const heatmapData = ref<HeatmapData[]>([]);
const monthNames = ["1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"];

// 生成日历格子
const calendarDays = computed(() => {
  const days: { date: string; count: number }[] = [];
  const startDate = new Date(year.value, 0, 1);
  const endDate = new Date(year.value, 11, 31);

  // 调整到周日开始
  const firstDay = startDate.getDay();
  startDate.setDate(startDate.getDate() - firstDay);

  // 调整到周六结束
  const lastDay = endDate.getDay();
  endDate.setDate(endDate.getDate() + (6 - lastDay));

  // 创建日期映射
  const dataMap = new Map<string, number>();
  heatmapData.value.forEach((d) => {
    const dateStr = d.date.split("T")[0];
    dataMap.set(dateStr, d.solvedCount || 0);
  });

  // 生成所有格子
  const current = new Date(startDate);
  while (current <= endDate) {
    const dateStr = current.toISOString().split("T")[0];
    days.push({
      date: dateStr,
      count: dataMap.get(dateStr) || 0,
    });
    current.setDate(current.getDate() + 1);
  }

  return days;
});

const getLevelClass = (day: { count: number }) => {
  if (day.count === 0) return "level-0";
  if (day.count <= 2) return "level-1";
  if (day.count <= 4) return "level-2";
  if (day.count <= 6) return "level-3";
  return "level-4";
};

const getTooltip = (day: { date: string; count: number }) => {
  return `${day.date}: ${day.count} 题`;
};

const fetchData = async () => {
  try {
    const res = await StatsService.getHeatmap(year.value);
    if (res.code === 0) {
      heatmapData.value = res.data || [];
    }
  } catch (e) {
    console.error("获取热力图数据失败", e);
  }
};

const prevYear = () => {
  year.value--;
  fetchData();
};

const nextYear = () => {
  year.value++;
  fetchData();
};

onMounted(fetchData);
</script>

<style scoped>
.problem-heatmap {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.heatmap-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.heatmap-header h3 {
  margin: 0;
  font-size: 16px;
  color: #374151;
}

.year-selector {
  display: flex;
  align-items: center;
  gap: 8px;
}

.year-selector button {
  padding: 4px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: white;
  cursor: pointer;
  transition: background 0.2s ease;
}

.year-selector button:hover {
  background: #f3f4f6;
}

.year-selector span {
  font-weight: bold;
  color: #374151;
}

.heatmap-grid {
  margin-bottom: 12px;
}

.month-labels {
  display: flex;
  justify-content: space-around;
  margin-bottom: 8px;
}

.month-label {
  font-size: 10px;
  color: #6b7280;
}

.heatmap-cells {
  display: grid;
  grid-template-columns: repeat(53, 1fr);
  gap: 2px;
}

.heatmap-cell {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 2px;
  cursor: pointer;
  transition: transform 0.1s ease;
}

.heatmap-cell:hover {
  transform: scale(1.5);
}

.level-0 { background: #ebedf0; }
.level-1 { background: #9be9a8; }
.level-2 { background: #40c463; }
.level-3 { background: #30a14e; }
.level-4 { background: #216e39; }

.heatmap-legend {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
  font-size: 12px;
  color: #6b7280;
}

.legend-cells {
  display: flex;
  gap: 2px;
}

.legend-cell {
  width: 12px;
  height: 12px;
  border-radius: 2px;
}

/* 深色模式 */
[data-theme="dark"] .problem-heatmap {
  background: #1f2937;
}

[data-theme="dark"] .heatmap-header h3 {
  color: #e5e7eb;
}

[data-theme="dark"] .year-selector button {
  background: #374151;
  border-color: #4b5563;
  color: #e5e7eb;
}

[data-theme="dark"] .year-selector button:hover {
  background: #4b5563;
}

[data-theme="dark"] .level-0 { background: #2d333b; }
[data-theme="dark"] .level-1 { background: #0e4429; }
[data-theme="dark"] .level-2 { background: #006d32; }
[data-theme="dark"] .level-3 { background: #26a641; }
[data-theme="dark"] .level-4 { background: #39d353; }

[data-theme="dark"] .heatmap-legend {
  color: #9ca3af;
}
</style>
