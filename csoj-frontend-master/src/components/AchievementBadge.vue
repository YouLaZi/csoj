<template>
  <div class="achievement-system">
    <!-- 成就徽章展示 -->
    <div class="achievement-badges">
      <div
        v-for="achievement in displayAchievements"
        :key="achievement.id"
        class="achievement-badge"
        :class="{
          'is-unlocked': achievement.unlocked,
          'is-new': achievement.isNew,
          'is-locked': !achievement.unlocked,
        }"
        @mouseenter="showTooltip(achievement)"
        @mouseleave="hideTooltip"
      >
        <div class="badge-icon" :style="{ background: achievement.gradient }">
          <span class="icon-emoji">{{ achievement.icon }}</span>
          <div
            v-if="achievement.unlocked && achievement.progress < 100"
            class="progress-ring"
          >
            <svg viewBox="0 0 36 36">
              <circle class="ring-bg" cx="18" cy="18" r="16" />
              <circle
                class="ring-progress"
                cx="18"
                cy="18"
                r="16"
                :style="{ strokeDashoffset: 100 - achievement.progress }"
              />
            </svg>
          </div>
        </div>
        <div v-if="achievement.isNew" class="new-indicator">NEW</div>
      </div>
    </div>

    <!-- 工具提示 -->
    <transition name="tooltip">
      <div
        v-if="tooltipVisible"
        class="achievement-tooltip"
        :style="tooltipStyle"
      >
        <div class="tooltip-header">
          <span class="tooltip-icon">{{ tooltipData?.icon }}</span>
          <span class="tooltip-title">{{ tooltipData?.title }}</span>
        </div>
        <div class="tooltip-description">{{ tooltipData?.description }}</div>
        <div v-if="tooltipData?.unlocked" class="tooltip-date">
          解锁于: {{ tooltipData?.unlockedDate }}
        </div>
        <div v-else class="tooltip-progress">
          <div class="progress-bar">
            <div
              class="progress-fill"
              :style="{ width: tooltipData?.progress + '%' }"
            ></div>
          </div>
          <span class="progress-text">{{ tooltipData?.progress }}%</span>
        </div>
      </div>
    </transition>

    <!-- 成就解锁通知 -->
    <Teleport to="body">
      <transition name="notification">
        <div v-if="notificationVisible" class="achievement-notification">
          <div class="notification-content">
            <div
              class="notification-icon"
              :style="{ background: notificationData?.gradient }"
            >
              {{ notificationData?.icon }}
            </div>
            <div class="notification-text">
              <div class="notification-title">成就解锁!</div>
              <div class="notification-name">{{ notificationData?.title }}</div>
            </div>
          </div>
          <div class="notification-close" @click="hideNotification">×</div>
        </div>
      </transition>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";

// 成就类型
interface Achievement {
  id: string;
  title: string;
  description: string;
  icon: string;
  gradient: string;
  unlocked: boolean;
  isNew?: boolean;
  progress: number;
  unlockedDate?: string;
  requirement: number;
  currentValue: number;
}

// 成就数据
const achievements = ref<Achievement[]>([
  {
    id: "first_login",
    title: "初来乍到",
    description: "完成首次登录",
    icon: "👋",
    gradient: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
    unlocked: true,
    progress: 100,
    unlockedDate: "2024-01-15",
    requirement: 1,
    currentValue: 1,
  },
  {
    id: "first_submit",
    title: "初试身手",
    description: "提交第一份代码",
    icon: "💻",
    gradient: "linear-gradient(135deg, #f093fb 0%, #f5576c 100%)",
    unlocked: true,
    progress: 100,
    unlockedDate: "2024-01-16",
    requirement: 1,
    currentValue: 1,
  },
  {
    id: "first_accept",
    title: "一击即中",
    description: "首次通过测试用例",
    icon: "🎯",
    gradient: "linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)",
    unlocked: true,
    progress: 100,
    unlockedDate: "2024-01-16",
    requirement: 1,
    currentValue: 1,
  },
  {
    id: "streak_7",
    title: "坚持不懈",
    description: "连续签到7天",
    icon: "🔥",
    gradient: "linear-gradient(135deg, #fa709a 0%, #fee140 100%)",
    unlocked: false,
    progress: 57,
    requirement: 7,
    currentValue: 4,
  },
  {
    id: "streak_30",
    title: "习惯养成",
    description: "连续签到30天",
    icon: "📅",
    gradient: "linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)",
    unlocked: false,
    progress: 13,
    requirement: 30,
    currentValue: 4,
  },
  {
    id: "solve_10",
    title: "小试牛刀",
    description: "解决10道题目",
    icon: "⚔️",
    gradient: "linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)",
    unlocked: false,
    progress: 30,
    requirement: 10,
    currentValue: 3,
  },
  {
    id: "solve_50",
    title: "初露锋芒",
    description: "解决50道题目",
    icon: "🗡️",
    gradient: "linear-gradient(135deg, #d299c2 0%, #fef9d7 100%)",
    unlocked: false,
    progress: 6,
    requirement: 50,
    currentValue: 3,
  },
  {
    id: "solve_100",
    title: "百题斩将",
    description: "解决100道题目",
    icon: "🏆",
    gradient: "linear-gradient(135deg, #f6d365 0%, #fda085 100%)",
    unlocked: false,
    progress: 3,
    requirement: 100,
    currentValue: 3,
  },
  {
    id: "solve_500",
    title: "算法大师",
    description: "解决500道题目",
    icon: "👑",
    gradient: "linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)",
    unlocked: false,
    progress: 0.6,
    requirement: 500,
    currentValue: 3,
  },
  {
    id: "perfect_score",
    title: "完美答卷",
    description: "一次通过所有测试用例",
    icon: "💯",
    gradient: "linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%)",
    unlocked: false,
    progress: 0,
    requirement: 1,
    currentValue: 0,
  },
  {
    id: "help_others",
    title: "热心助人",
    description: "发布10个题解",
    icon: "🤝",
    gradient: "linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%)",
    unlocked: false,
    progress: 20,
    requirement: 10,
    currentValue: 2,
  },
  {
    id: "night_owl",
    title: "夜猫子",
    description: "在凌晨12点后提交代码",
    icon: "🦉",
    gradient: "linear-gradient(135deg, #2c3e50 0%, #4ca1af 100%)",
    unlocked: false,
    progress: 0,
    requirement: 1,
    currentValue: 0,
  },
]);

// 显示的成就（只显示前8个或已解锁的）
const displayAchievements = computed(() => {
  const unlocked = achievements.value.filter((a) => a.unlocked);
  const locked = achievements.value.filter((a) => !a.unlocked);
  return [...unlocked, ...locked].slice(0, 8);
});

// 工具提示
const tooltipVisible = ref(false);
const tooltipData = ref<Achievement | null>(null);
const tooltipStyle = ref({});

const showTooltip = (achievement: Achievement) => {
  tooltipData.value = achievement;
  tooltipVisible.value = true;
};

const hideTooltip = () => {
  tooltipVisible.value = false;
};

// 通知
const notificationVisible = ref(false);
const notificationData = ref<Achievement | null>(null);

const showNotification = (achievement: Achievement) => {
  notificationData.value = achievement;
  notificationVisible.value = true;

  // 3秒后自动关闭
  setTimeout(() => {
    hideNotification();
  }, 3000);
};

const hideNotification = () => {
  notificationVisible.value = false;
};

// 解锁成就
const unlockAchievement = (id: string) => {
  const achievement = achievements.value.find((a) => a.id === id);
  if (achievement && !achievement.unlocked) {
    achievement.unlocked = true;
    achievement.isNew = true;
    achievement.progress = 100;
    achievement.unlockedDate = new Date().toLocaleDateString("zh-CN");
    showNotification(achievement);
  }
};

// 更新进度
const updateProgress = (id: string, value: number) => {
  const achievement = achievements.value.find((a) => a.id === id);
  if (achievement) {
    achievement.currentValue = value;
    achievement.progress = Math.min(
      100,
      Math.round((value / achievement.requirement) * 100)
    );
    if (achievement.progress >= 100 && !achievement.unlocked) {
      unlockAchievement(id);
    }
  }
};

// 暴露方法
defineExpose({
  unlockAchievement,
  updateProgress,
  achievements,
});
</script>

<style scoped>
.achievement-system {
  position: relative;
}

.achievement-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.achievement-badge {
  position: relative;
  width: 50px;
  height: 50px;
  cursor: pointer;
  transition: transform var(--transition-base, 0.25s ease);
}

.achievement-badge:hover {
  transform: scale(1.15) translateY(-5px);
}

.badge-icon {
  width: 100%;
  height: 100%;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  box-shadow: var(--shadow-md, 0 4px 12px rgba(44, 62, 80, 0.06));
  transition: all var(--transition-base, 0.25s ease);
}

.badge-icon .icon-emoji {
  font-size: 24px;
}

.achievement-badge.is-locked .badge-icon {
  filter: grayscale(100%);
  opacity: 0.5;
}

.achievement-badge.is-unlocked .badge-icon {
  box-shadow: 0 4px 15px rgba(79, 111, 143, 0.3);
}

.achievement-badge.is-new .badge-icon {
  animation: badge-glow 1.5s ease infinite;
}

@keyframes badge-glow {
  0%,
  100% {
    box-shadow: 0 0 10px rgba(79, 111, 143, 0.5);
  }
  50% {
    box-shadow: 0 0 25px rgba(79, 111, 143, 0.8);
  }
}

.new-indicator {
  position: absolute;
  top: -5px;
  right: -5px;
  background: var(--danger-color, #c45c5c);
  color: #fff;
  font-size: 8px;
  font-weight: bold;
  padding: 2px 4px;
  border-radius: 4px;
  animation: bounce 0.5s ease infinite alternate;
}

@keyframes bounce {
  from {
    transform: translateY(0);
  }
  to {
    transform: translateY(-3px);
  }
}

/* 进度环 */
.progress-ring {
  position: absolute;
  top: -3px;
  left: -3px;
  width: 56px;
  height: 56px;
}

.progress-ring svg {
  transform: rotate(-90deg);
}

.ring-bg {
  fill: none;
  stroke: rgba(255, 255, 255, 0.3);
  stroke-width: 2;
}

.ring-progress {
  fill: none;
  stroke: #fff;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-dasharray: 100;
  transition: stroke-dashoffset 0.5s ease;
}

/* 工具提示 */
.achievement-tooltip {
  position: fixed;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  background: var(--bg-color-secondary, #fff);
  border-radius: 12px;
  padding: 16px;
  min-width: 250px;
  box-shadow: var(--shadow-lg, 0 8px 24px rgba(44, 62, 80, 0.08));
  border: 1px solid var(--border-color, #e8edf2);
  z-index: 1000;
}

.tooltip-enter-active,
.tooltip-leave-active {
  transition: all 0.3s ease;
}

.tooltip-enter-from,
.tooltip-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(10px);
}

.tooltip-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.tooltip-icon {
  font-size: 24px;
}

.tooltip-title {
  font-weight: 600;
  color: var(--text-color-primary, #2c3e50);
}

.tooltip-description {
  font-size: 13px;
  color: var(--text-color-secondary, #8a9aaa);
  margin-bottom: 8px;
}

.tooltip-date {
  font-size: 12px;
  color: var(--success-color, #5a9a6e);
}

.tooltip-progress {
  display: flex;
  align-items: center;
  gap: 10px;
}

.progress-bar {
  flex: 1;
  height: 6px;
  background: var(--bg-color-tertiary, #f5f7f9);
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: var(--primary-color, #4f6f8f);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 12px;
  color: var(--text-color-secondary, #8a9aaa);
  white-space: nowrap;
}

/* 通知 */
.achievement-notification {
  position: fixed;
  top: 20px;
  right: 20px;
  background: var(--bg-color-secondary, #fff);
  border-radius: 16px;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: var(--shadow-xl, 0 16px 48px rgba(44, 62, 80, 0.1));
  border: 1px solid var(--border-color, #e8edf2);
  z-index: 10000;
}

.notification-enter-active {
  animation: slide-in 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.notification-leave-active {
  animation: slide-out 0.3s ease;
}

@keyframes slide-in {
  from {
    opacity: 0;
    transform: translateX(100px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes slide-out {
  from {
    opacity: 1;
    transform: translateX(0);
  }
  to {
    opacity: 0;
    transform: translateX(100px);
  }
}

.notification-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.notification-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.notification-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.notification-title {
  font-size: 12px;
  color: var(--success-color, #5a9a6e);
  font-weight: 500;
}

.notification-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-color-primary, #2c3e50);
}

.notification-close {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--text-color-secondary, #8a9aaa);
  font-size: 18px;
  transition: all var(--transition-fast, 0.15s ease);
}

.notification-close:hover {
  background: var(--bg-color-tertiary, #f5f7f9);
  color: var(--text-color-primary, #2c3e50);
}

/* 深色模式 */
[data-theme="dark"] .achievement-tooltip,
[data-theme="dark"] .achievement-notification {
  background: var(--bg-color-secondary, #1c2229);
  border-color: var(--border-color, #2a3440);
}

[data-theme="dark"] .tooltip-title,
[data-theme="dark"] .notification-name {
  color: var(--text-color-primary, #e8edf2);
}
</style>
