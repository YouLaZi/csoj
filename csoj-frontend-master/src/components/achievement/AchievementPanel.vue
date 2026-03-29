<template>
  <div class="achievement-panel">
    <div class="panel-header">
      <h3>🏆 成就</h3>
      <div class="achievement-stats">
        <span>{{ unlockedCount }} / {{ totalCount }}</span>
      </div>
    </div>

    <div class="category-tabs">
      <button
        v-for="cat in categories"
        :key="cat.key"
        :class="{ active: activeCategory === cat.key }"
        @click="activeCategory = cat.key"
      >
        {{ cat.icon }} {{ cat.name }}
      </button>
    </div>

    <div class="achievement-list">
      <div
        v-for="achievement in filteredAchievements"
        :key="achievement.achievementCode"
        class="achievement-item"
        :class="{ unlocked: achievement.isUnlocked, new: achievement.isNew }"
      >
        <div class="achievement-icon" :style="{ background: getGradient(achievement) }">
          {{ achievement.icon || getIcon(achievement) }}
        </div>
        <div class="achievement-info">
          <div class="achievement-name">{{ achievement.achievementName }}</div>
          <div class="achievement-desc">{{ achievement.description }}</div>
          <div v-if="!achievement.isUnlocked" class="achievement-progress">
            <div class="progress-bar">
              <div
                class="progress-fill"
                :style="{ width: getProgressPercent(achievement) + '%' }"
              ></div>
            </div>
            <span class="progress-text">
              {{ achievement.progress || 0 }} / {{ achievement.requirement }}
            </span>
          </div>
        </div>
        <div v-if="achievement.isUnlocked" class="unlocked-badge">✓</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import AchievementService from "@/services/AchievementService";

interface Achievement {
  achievementCode: string;
  achievementName: string;
  description: string;
  category: string;
  icon: string;
  gradient: string;
  requirement: number;
  progress: number;
  isUnlocked: number;
  isNew: number;
}

const achievements = ref<Achievement[]>([]);
const activeCategory = ref("all");

const categories = [
  { key: "all", name: "全部", icon: "📋" },
  { key: "problem", name: "刷题", icon: "📝" },
  { key: "streak", name: "签到", icon: "📅" },
  { key: "social", name: "社交", icon: "💬" },
  { key: "special", name: "特殊", icon: "⭐" },
  { key: "cat", name: "猫咪", icon: "🐱" },
];

const filteredAchievements = computed(() => {
  if (activeCategory.value === "all") {
    return achievements.value;
  }
  return achievements.value.filter((a) => a.category === activeCategory.value);
});

const unlockedCount = computed(
  () => achievements.value.filter((a) => a.isUnlocked).length
);

const totalCount = computed(() => achievements.value.length);

const getGradient = (achievement: Achievement) => {
  return achievement.gradient || "linear-gradient(135deg, #667eea 0%, #764ba2 100%)";
};

const getIcon = (achievement: Achievement) => {
  const categoryIcons: Record<string, string> = {
    problem: "📝",
    streak: "📅",
    social: "💬",
    special: "⭐",
    cat: "🐱",
  };
  return categoryIcons[achievement.category] || "🏅";
};

const getProgressPercent = (achievement: Achievement) => {
  if (!achievement.requirement) return 0;
  return Math.min(((achievement.progress || 0) / achievement.requirement) * 100, 100);
};

const fetchData = async () => {
  try {
    const res = await AchievementService.getUserAchievements();
    if (res.code === 0) {
      // 合并成就定义和用户进度
      const defRes = await AchievementService.getAllAchievements();
      if (defRes.code === 0 && defRes.data) {
        achievements.value = defRes.data.map((def: any) => {
          const userAch = res.data?.find(
            (u: any) => u.achievementCode === def.achievementCode
          );
          return {
            ...def,
            progress: userAch?.progress || 0,
            isUnlocked: userAch?.isUnlocked || 0,
            isNew: userAch?.isNew || 0,
          };
        });
      }
    }
  } catch (e) {
    console.error("获取成就失败", e);
  }
};

onMounted(fetchData);
</script>

<style scoped>
.achievement-panel {
  background: var(--bg-color-secondary);
  border: 1px solid var(--border-color-light);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-card);
  position: relative;
  overflow: hidden;
}

/* 面板顶部装饰 */
.achievement-panel::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: var(--gradient-accent);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md);
}

.panel-header h3 {
  margin: 0;
  font-family: var(--font-family-serif);
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-semibold);
  color: var(--text-color-primary);
}

.achievement-stats {
  font-size: var(--font-size-sm);
  color: var(--text-color-secondary);
  font-weight: var(--font-weight-medium);
  padding: var(--spacing-xs) var(--spacing-sm);
  background: var(--bg-color-tertiary);
  border-radius: var(--radius-full);
}

.category-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  overflow-x: auto;
  padding-bottom: 4px;
}

.category-tabs button {
  padding: 8px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 20px;
  background: white;
  white-space: nowrap;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 13px;
}

.category-tabs button:hover {
  background: #f3f4f6;
}

.category-tabs button.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: transparent;
}

.achievement-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 400px;
  overflow-y: auto;
}

.achievement-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 12px;
  transition: all 0.2s ease;
  position: relative;
}

.achievement-item.unlocked {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
}

.achievement-item.new::after {
  content: "NEW";
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 2px 8px;
  background: #ef4444;
  color: white;
  font-size: 10px;
  border-radius: 10px;
}

.achievement-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.achievement-info {
  flex: 1;
  min-width: 0;
}

.achievement-name {
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.achievement-desc {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 8px;
}

.achievement-progress {
  display: flex;
  align-items: center;
  gap: 8px;
}

.progress-bar {
  flex: 1;
  height: 6px;
  background: #e5e7eb;
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea, #764ba2);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 11px;
  color: #6b7280;
  white-space: nowrap;
}

.unlocked-badge {
  width: 24px;
  height: 24px;
  background: #22c55e;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}

/* 深色模式 */
[data-theme="dark"] .achievement-panel {
  background: #1f2937;
}

[data-theme="dark"] .panel-header h3 {
  color: #e5e7eb;
}

[data-theme="dark"] .category-tabs button {
  background: #374151;
  border-color: #4b5563;
  color: #e5e7eb;
}

[data-theme="dark"] .category-tabs button:hover {
  background: #4b5563;
}

[data-theme="dark"] .achievement-item {
  background: #374151;
}

[data-theme="dark"] .achievement-item.unlocked {
  background: linear-gradient(135deg, #78350f 0%, #92400e 100%);
}

[data-theme="dark"] .achievement-name {
  color: #e5e7eb;
}

[data-theme="dark"] .achievement-desc {
  color: #9ca3af;
}

[data-theme="dark"] .progress-bar {
  background: #4b5563;
}
</style>
