<template>
  <div class="checkin-calendar">
    <!-- 头部区域 -->
    <div class="calendar-header">
      <div class="header-left">
        <div class="checkin-icon" :class="{ checked: hasCheckedToday }">
          <span v-if="hasCheckedToday">✓</span>
          <span v-else class="icon-calendar">📅</span>
        </div>
        <div class="header-info">
          <h3 class="title">{{ t("home.dailyCheckin") }}</h3>
          <p class="status" :class="{ checked: hasCheckedToday }">
            {{
              hasCheckedToday
                ? t("home.alreadyCheckedIn")
                : t("home.checkinNow")
            }}
          </p>
        </div>
      </div>
      <a-button
        type="primary"
        size="small"
        :disabled="hasCheckedToday"
        :loading="isChecking"
        @click="handleCheckin"
        class="checkin-btn"
      >
        {{ hasCheckedToday ? t("points.checked") : t("points.checkin") }}
      </a-button>
    </div>

    <!-- 日历网格 -->
    <div class="calendar-body">
      <!-- 月份导航 -->
      <div class="month-nav">
        <button class="nav-btn" @click="prevMonth" :disabled="!canPrevMonth">
          <span>‹</span>
        </button>
        <span class="month-title">{{ currentMonthTitle }}</span>
        <button class="nav-btn" @click="nextMonth" :disabled="!canNextMonth">
          <span>›</span>
        </button>
      </div>

      <!-- 星期标题 -->
      <div class="weekdays">
        <span v-for="day in weekDays" :key="day" class="weekday">{{
          day
        }}</span>
      </div>

      <!-- 日期网格 -->
      <div class="days-grid">
        <div
          v-for="(day, index) in calendarDays"
          :key="index"
          class="day-cell"
          :class="{
            empty: !day.date,
            checked: day.checked,
            today: day.isToday,
            otherMonth: day.otherMonth,
          }"
        >
          <span v-if="day.date" class="day-number">{{ day.date }}</span>
          <span v-if="day.checked" class="check-dot"></span>
        </div>
      </div>
    </div>

    <!-- 统计信息 -->
    <div class="calendar-stats">
      <div class="stat-item">
        <span class="stat-icon">🔥</span>
        <span class="stat-value">{{ consecutiveDays }}</span>
        <span class="stat-label">{{ t("home.consecutiveDays") }}</span>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <span class="stat-icon">📊</span>
        <span class="stat-value">{{ monthlyCheckins }}</span>
        <span class="stat-label">{{ t("home.monthlyCheckins") }}</span>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <span class="stat-icon">💎</span>
        <span class="stat-value">{{ totalPoints }}</span>
        <span class="stat-label">{{ t("home.totalPointsLabel") }}</span>
      </div>
    </div>

    <!-- 签到成功庆祝动画 -->
    <transition name="celebrate">
      <div v-if="showCelebration" class="celebration">
        <div class="celebration-content">
          <div class="celebrate-icon">🎉</div>
          <div class="celebrate-text">
            {{ t("checkin.earnedPoints", { points: earnedPoints }) }}
          </div>
          <div class="celebrate-sub">{{ t("checkin.keepGoing") }}</div>
        </div>
        <div class="confetti">
          <span
            v-for="i in 20"
            :key="i"
            class="confetti-piece"
            :style="confettiStyle(i)"
          ></span>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { Message } from "@arco-design/web-vue";
import { useI18n } from "vue-i18n";
import CheckinService from "@/services/CheckinService";

const { t } = useI18n();

// 状态
const checkinDays = ref<Date[]>([]);
const consecutiveDays = ref(0);
const monthlyCheckins = ref(0);
const totalPoints = ref(0);
const isChecking = ref(false);
const showCelebration = ref(false);
const earnedPoints = ref(0);
const viewDate = ref(new Date());

// 星期标题
const weekDays = computed(() => [
  t("checkin.sun"),
  t("checkin.mon"),
  t("checkin.tue"),
  t("checkin.wed"),
  t("checkin.thu"),
  t("checkin.fri"),
  t("checkin.sat"),
]);

// 当前月份标题
const currentMonthTitle = computed(() => {
  const year = viewDate.value.getFullYear();
  const month = viewDate.value.getMonth() + 1;
  return `${year} ${t("calendar.year")} ${month} ${t("calendar.month")}`;
});

// 是否可以切换月份
const canPrevMonth = computed(() => {
  const now = new Date();
  const viewYear = viewDate.value.getFullYear();
  const viewMonth = viewDate.value.getMonth();
  return viewYear > 2020 || (viewYear === 2020 && viewMonth > 0);
});

const canNextMonth = computed(() => {
  const now = new Date();
  const viewYear = viewDate.value.getFullYear();
  const viewMonth = viewDate.value.getMonth();
  const nowYear = now.getFullYear();
  const nowMonth = now.getMonth();
  return viewYear < nowYear || (viewYear === nowYear && viewMonth < nowMonth);
});

// 日历天数
const calendarDays = computed(() => {
  const year = viewDate.value.getFullYear();
  const month = viewDate.value.getMonth();
  const today = new Date();

  // 获取当月第一天
  const firstDay = new Date(year, month, 1);
  // 获取当月最后一天
  const lastDay = new Date(year, month + 1, 0);

  // 获取当月第一天是星期几（0-6）
  const firstDayOfWeek = firstDay.getDay();

  const days: Array<{
    date: number | null;
    checked: boolean;
    isToday: boolean;
    otherMonth: boolean;
  }> = [];

  // 填充前面的空白
  for (let i = 0; i < firstDayOfWeek; i++) {
    days.push({
      date: null,
      checked: false,
      isToday: false,
      otherMonth: false,
    });
  }

  // 填充当月日期
  for (let date = 1; date <= lastDay.getDate(); date++) {
    const currentDate = new Date(year, month, date);
    const isChecked = checkinDays.value.some((d) => {
      return (
        d.getFullYear() === year &&
        d.getMonth() === month &&
        d.getDate() === date
      );
    });

    const isToday =
      today.getFullYear() === year &&
      today.getMonth() === month &&
      today.getDate() === date;

    days.push({
      date,
      checked: isChecked,
      isToday,
      otherMonth: false,
    });
  }

  return days;
});

// 今天是否已签到
const hasCheckedToday = computed(() => {
  const today = new Date();
  return checkinDays.value.some((d) => {
    return (
      d.getFullYear() === today.getFullYear() &&
      d.getMonth() === today.getMonth() &&
      d.getDate() === today.getDate()
    );
  });
});

// 上一月
const prevMonth = () => {
  if (!canPrevMonth.value) return;
  viewDate.value = new Date(
    viewDate.value.getFullYear(),
    viewDate.value.getMonth() - 1,
    1
  );
};

// 下一月
const nextMonth = () => {
  if (!canNextMonth.value) return;
  viewDate.value = new Date(
    viewDate.value.getFullYear(),
    viewDate.value.getMonth() + 1,
    1
  );
};

// 彩纸样式
const confettiStyle = (index: number) => {
  const colors = [
    "#ff6b6b",
    "#4ecdc4",
    "#45b7d1",
    "#96ceb4",
    "#ffeaa7",
    "#dfe6e9",
    "#fd79a8",
  ];
  const left = Math.random() * 100;
  const delay = Math.random() * 0.5;
  const duration = 1 + Math.random() * 0.5;
  const color = colors[index % colors.length];

  return {
    left: `${left}%`,
    animationDelay: `${delay}s`,
    animationDuration: `${duration}s`,
    backgroundColor: color,
  };
};

// 签到操作
const handleCheckin = async () => {
  if (hasCheckedToday.value || isChecking.value) return;

  isChecking.value = true;
  try {
    const res = await CheckinService.checkin();

    if (res.success) {
      earnedPoints.value = res.data?.points || 15;

      // 显示庆祝动画
      showCelebration.value = true;
      setTimeout(() => {
        showCelebration.value = false;
      }, 2000);

      // 重新加载数据
      await loadCheckinData();

      Message.success(t("points.checkinSuccess"));
    } else {
      Message.error(res.message || t("message.failed"));
    }
  } catch (error) {
    console.error("签到错误:", error);
    Message.error(t("points.checkinFailed"));
  } finally {
    isChecking.value = false;
  }
};

// 加载签到数据
const loadCheckinData = async () => {
  try {
    const res = await CheckinService.getUserCheckinRecords();

    if (res && res.code === 0 && res.data?.records) {
      checkinDays.value = res.data.records.map((record: any) => {
        const date = new Date(record.checkinDate);
        return new Date(date.getFullYear(), date.getMonth(), date.getDate());
      });

      consecutiveDays.value = calculateConsecutiveDays();
      monthlyCheckins.value = getCurrentMonthCheckins();
      totalPoints.value = checkinDays.value.length * 15;
    } else {
      resetData();
    }
  } catch (error) {
    console.error("签到数据加载出错:", error);
    resetData();
  }
};

// 重置数据
const resetData = () => {
  checkinDays.value = [];
  consecutiveDays.value = 0;
  monthlyCheckins.value = 0;
  totalPoints.value = 0;
};

// 计算连续签到天数
const calculateConsecutiveDays = () => {
  if (!checkinDays.value.length) return 0;

  const sortedDates = [...checkinDays.value]
    .map((d) => new Date(d.getFullYear(), d.getMonth(), d.getDate()))
    .sort((a, b) => b.getTime() - a.getTime());

  const today = new Date();
  const todayNorm = new Date(
    today.getFullYear(),
    today.getMonth(),
    today.getDate()
  );

  let count = 0;
  let expectedDate = new Date(todayNorm);

  for (const checkinDate of sortedDates) {
    if (checkinDate.getTime() === expectedDate.getTime()) {
      count++;
      expectedDate.setDate(expectedDate.getDate() - 1);
    } else if (checkinDate.getTime() < expectedDate.getTime()) {
      break;
    }
  }

  return count;
};

// 获取当月签到次数
const getCurrentMonthCheckins = () => {
  const now = new Date();
  return checkinDays.value.filter((d) => {
    return (
      d.getMonth() === now.getMonth() && d.getFullYear() === now.getFullYear()
    );
  }).length;
};

onMounted(() => {
  loadCheckinData();
});
</script>

<style scoped>
.checkin-calendar {
  background: var(--bg-color-secondary);
  border: 1px solid var(--border-color-light);
  border-radius: var(--radius-lg);
  overflow: hidden;
  position: relative;
}

/* 头部 */
.calendar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-md);
  border-bottom: 1px solid var(--border-color-light);
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.checkin-icon {
  width: 42px;
  height: 42px;
  border-radius: var(--radius-lg);
  background: linear-gradient(
    135deg,
    var(--primary-lighter-color),
    var(--primary-light-color)
  );
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  transition: all var(--transition-base);
}

.checkin-icon.checked {
  background: linear-gradient(135deg, var(--success-color), #6bb37e);
  color: white;
  box-shadow: 0 4px 12px rgba(90, 154, 110, 0.3);
}

.header-info .title {
  margin: 0;
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-semibold);
  color: var(--text-color-primary);
}

.header-info .status {
  margin: 2px 0 0;
  font-size: var(--font-size-xs);
  color: var(--text-color-tertiary);
}

.header-info .status.checked {
  color: var(--success-color);
}

.checkin-btn {
  flex-shrink: 0;
}

/* 日历主体 */
.calendar-body {
  padding: var(--spacing-sm) var(--spacing-md);
}

/* 月份导航 */
.month-nav {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-sm);
}

.nav-btn {
  width: 28px;
  height: 28px;
  border: none;
  background: var(--bg-color-tertiary);
  border-radius: var(--radius-full);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-color-secondary);
  transition: all var(--transition-fast);
}

.nav-btn:hover:not(:disabled) {
  background: var(--primary-lighter-color);
  color: var(--primary-color);
}

.nav-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.month-title {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--text-color-primary);
  min-width: 100px;
  text-align: center;
}

/* 星期标题 */
.weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 2px;
  margin-bottom: var(--spacing-xs);
}

.weekday {
  text-align: center;
  font-size: var(--font-size-xs);
  color: var(--text-color-tertiary);
  padding: var(--spacing-xs) 0;
  font-weight: var(--font-weight-medium);
}

/* 日期网格 */
.days-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 2px;
}

.day-cell {
  aspect-ratio: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  position: relative;
  transition: all var(--transition-fast);
}

.day-cell.empty {
  background: transparent;
}

.day-cell:not(.empty):not(.otherMonth) {
  background: var(--bg-color-tertiary);
}

.day-cell.today:not(.empty) {
  background: var(--primary-lighter-color);
  box-shadow: inset 0 0 0 2px var(--primary-color);
}

.day-cell.checked {
  background: linear-gradient(
    135deg,
    var(--primary-color),
    var(--primary-hover-color)
  );
}

.day-cell.checked .day-number {
  color: white;
  font-weight: var(--font-weight-semibold);
}

.day-cell.otherMonth {
  opacity: 0.3;
}

.day-number {
  font-size: var(--font-size-xs);
  color: var(--text-color-primary);
}

.check-dot {
  position: absolute;
  bottom: 3px;
  width: 4px;
  height: 4px;
  background: white;
  border-radius: 50%;
}

/* 统计信息 */
.calendar-stats {
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: var(--spacing-sm) var(--spacing-md);
  border-top: 1px solid var(--border-color-light);
  background: var(--bg-color-tertiary);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.stat-icon {
  font-size: 14px;
}

.stat-value {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--primary-color);
}

.stat-label {
  font-size: 10px;
  color: var(--text-color-tertiary);
}

.stat-divider {
  width: 1px;
  height: 32px;
  background: var(--border-color-light);
}

/* 庆祝动画 */
.celebration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.95);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
}

.celebration-content {
  text-align: center;
  z-index: 2;
}

.celebrate-icon {
  font-size: 48px;
  animation: bounce 0.6s ease infinite;
}

.celebrate-text {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--primary-color);
  margin-top: var(--spacing-xs);
}

.celebrate-sub {
  font-size: var(--font-size-xs);
  color: var(--text-color-secondary);
  margin-top: 4px;
}

@keyframes bounce {
  0%,
  100% {
    transform: translateY(0) scale(1);
  }
  50% {
    transform: translateY(-15px) scale(1.1);
  }
}

/* 彩纸效果 */
.confetti {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  pointer-events: none;
}

.confetti-piece {
  position: absolute;
  top: -10px;
  width: 8px;
  height: 8px;
  border-radius: 2px;
  animation: confetti-fall 1.5s ease forwards;
}

@keyframes confetti-fall {
  0% {
    transform: translateY(0) rotate(0deg);
    opacity: 1;
  }
  100% {
    transform: translateY(300px) rotate(720deg);
    opacity: 0;
  }
}

.celebrate-enter-active,
.celebrate-leave-active {
  transition: all 0.4s ease;
}

.celebrate-enter-from,
.celebrate-leave-to {
  opacity: 0;
  transform: scale(0.9);
}

/* 深色模式 */
[data-theme="dark"] .checkin-calendar {
  background: var(--bg-color-secondary);
  border-color: var(--border-color);
}

[data-theme="dark"] .calendar-header {
  border-bottom-color: var(--border-color);
}

[data-theme="dark"] .checkin-icon {
  background: linear-gradient(
    135deg,
    rgba(79, 111, 143, 0.3),
    rgba(79, 111, 143, 0.2)
  );
}

[data-theme="dark"] .day-cell:not(.empty):not(.otherMonth) {
  background: var(--bg-color-secondary);
}

[data-theme="dark"] .day-cell.today:not(.empty) {
  background: rgba(79, 111, 143, 0.3);
}

[data-theme="dark"] .calendar-stats {
  border-top-color: var(--border-color);
  background: rgba(0, 0, 0, 0.2);
}

[data-theme="dark"] .celebration {
  background: rgba(28, 34, 41, 0.95);
}

[data-theme="dark"] .stat-divider {
  background: var(--border-color);
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .calendar-header {
    flex-wrap: wrap;
    gap: var(--spacing-sm);
  }

  .day-cell {
    aspect-ratio: 1;
  }

  .day-number {
    font-size: 10px;
  }

  .stat-value {
    font-size: var(--font-size-base);
  }

  .month-title {
    font-size: var(--font-size-xs);
    min-width: 80px;
  }
}
</style>
