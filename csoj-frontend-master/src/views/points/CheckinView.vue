<template>
  <div class="checkin-view">
    <div class="checkin-grid">
      <!-- 左侧：签到区域 -->
      <div class="checkin-main">
        <!-- 签到卡片 -->
        <div class="checkin-card">
          <div class="checkin-header">
            <h2 class="checkin-title">{{ t("checkin.pageTitle") }}</h2>
            <p class="checkin-subtitle">{{ t("checkin.pageSubtitle") }}</p>
          </div>

          <div class="checkin-content">
            <!-- 已签到状态 -->
            <div v-if="checkedInToday" class="checked-status">
              <div class="status-icon success">
                <icon-check-circle />
              </div>
              <h3 class="status-title">{{ t("checkin.alreadyChecked") }}</h3>
              <p class="status-desc">
                {{ t("checkin.checkinSuccess", { points: 15 }) }}
              </p>
              <div class="points-earned">
                <span class="points-label">{{ t("checkin.totalPoints") }}</span>
                <span class="points-value">+15</span>
                <span class="points-unit">{{ t("points.days") }}</span>
              </div>
            </div>

            <!-- 未签到状态 -->
            <div v-else class="unchecked-status">
              <div class="status-icon">
                <icon-calendar />
              </div>
              <h3 class="status-title">{{ t("checkin.checkinNow") }}</h3>
              <p class="status-desc">{{ t("checkin.pageSubtitle") }}</p>
              <a-button
                type="primary"
                size="large"
                @click="handleCheckin"
                class="checkin-btn"
              >
                <template #icon><icon-check /></template>
                {{ t("checkin.checkinNow") }}
              </a-button>
            </div>
          </div>
        </div>

        <!-- 签到日历 -->
        <div class="calendar-card">
          <div class="card-header">
            <h3 class="card-title">{{ t("checkin.monthlyRecord") }}</h3>
          </div>
          <div class="calendar-wrapper">
            <a-calendar
              :panel-mode="'month'"
              :default-value="new Date()"
              :disabled-date="disabledDate"
              :cell-render="cellRender"
            />
          </div>
        </div>
      </div>

      <!-- 右侧：积分信息 -->
      <div class="checkin-sidebar">
        <!-- 积分统计 -->
        <div class="points-card">
          <div class="points-header">
            <div class="points-icon">
              <icon-gift />
            </div>
            <div class="points-info">
              <span class="points-label">{{ t("checkin.totalPoints") }}</span>
              <span class="points-value">{{ userPoints }}</span>
            </div>
          </div>
          <a-button type="outline" long @click="loadPointsRecords">
            <template #icon><icon-refresh /></template>
            {{ t("form.reset") }}
          </a-button>
        </div>

        <!-- 积分记录 -->
        <div class="records-card">
          <div class="card-header">
            <h3 class="card-title">{{ t("checkin.monthlyRecord") }}</h3>
          </div>
          <div class="records-list">
            <div v-if="pointsRecords.length === 0" class="empty-records">
              <icon-file class="empty-icon" />
              <span>{{ t("home.noData") }}</span>
            </div>
            <div
              v-for="(record, index) in pointsRecords"
              :key="index"
              class="record-item"
            >
              <div class="record-info">
                <span class="record-desc">{{ record.description }}</span>
                <span class="record-time">{{
                  formatTime(record.createTime)
                }}</span>
              </div>
              <span
                class="record-points"
                :class="{ positive: record.points > 0 }"
              >
                {{ record.points > 0 ? "+" : "" }}{{ record.points }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, h } from "vue";
import { Message } from "@arco-design/web-vue";
import { useStore } from "vuex";
import { useI18n } from "vue-i18n";
import moment from "moment";
import {
  IconCalendar,
  IconCheckCircle,
  IconCheck,
  IconGift,
  IconRefresh,
  IconFile,
} from "@arco-design/web-vue/es/icon";

import CheckinService from "@/services/CheckinService";

const { t } = useI18n();

const store = useStore();
const userPoints = ref(0);
const checkedInToday = ref(false);
const checkinRecords = ref([]);
const pointsRecords = ref([]);

const disabledDate = (date: Date) => {
  return date > new Date();
};

const cellRender = (current: Date) => {
  const currentDateStr = moment(current).format("YYYY-MM-DD");
  const isCheckedIn = checkinRecords.value.some(
    (record) => record.checkinDate === currentDateStr
  );

  if (isCheckedIn) {
    return h(
      "div",
      {
        class: "calendar-cell checked",
      },
      [
        h("span", current.getDate().toString()),
        h(IconCheckCircle, { class: "check-icon" }),
      ]
    );
  }
  return null;
};

const formatTime = (time: Date) => {
  return moment(time).format("MM-DD HH:mm");
};

const loadCheckinRecords = async () => {
  try {
    const res = await CheckinService.getUserCheckinRecords();
    if (res && res.code === 0 && res.data?.records) {
      const checkinData = res.data.records;
      checkinRecords.value = checkinData.map((record: any) => ({
        checkinDate: moment(record.checkinDate).format("YYYY-MM-DD"),
        points: 15,
        createTime: record.createTime,
      }));

      const today = moment().format("YYYY-MM-DD");
      checkedInToday.value = checkinData.some(
        (record: any) =>
          moment(record.checkinDate).format("YYYY-MM-DD") === today
      );
    } else {
      checkinRecords.value = [];
      checkedInToday.value = false;
    }
  } catch (error) {
    console.error("获取签到记录失败:", error);
    checkinRecords.value = [];
    checkedInToday.value = false;
  }
};

const loadPointsRecords = async () => {
  try {
    const res = await CheckinService.getUserCheckinRecords();
    if (res?.code === 0 && res.data?.records) {
      pointsRecords.value = res.data.records.map((record: any) => ({
        id: record.id,
        points: 15,
        description: "每日签到",
        type: "签到",
        createTime: new Date(record.createTime),
      }));
      userPoints.value = res.data.records.length * 15;
    } else {
      pointsRecords.value = [];
      userPoints.value = 0;
    }
  } catch (error) {
    console.error("获取积分记录失败:", error);
    pointsRecords.value = [];
    userPoints.value = 0;
  }
};

const handleCheckin = async () => {
  if (checkedInToday.value) {
    Message.warning(t("points.alreadyChecked"));
    return;
  }

  try {
    const res = await CheckinService.checkin();
    if (res.code === 0) {
      Message.success(t("checkin.checkinSuccess", { points: 15 }));
      await loadCheckinRecords();
      await loadPointsRecords();
    } else {
      Message.error(
        t("checkin.checkinFailed") + "：" + (res.message || t("message.failed"))
      );
    }
  } catch (error) {
    console.error("签到失败:", error);
    Message.error(t("checkin.checkinFailed"));
  }
};

const resizeObserverErrorHandler = (e: ErrorEvent) => {
  if (
    e.message ===
    "ResizeObserver loop completed with undelivered notifications."
  ) {
    e.stopImmediatePropagation();
    return false;
  }
};

onMounted(() => {
  loadCheckinRecords();
  loadPointsRecords();
  window.addEventListener("error", resizeObserverErrorHandler);
});

onUnmounted(() => {
  window.removeEventListener("error", resizeObserverErrorHandler);
});
</script>

<style scoped>
/* ========================================
   签到页面 - 简约大方
   ======================================== */

.checkin-view {
  padding: var(--spacing-xl);
  max-width: var(--content-max-width);
  margin: 0 auto;
}

.checkin-grid {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: var(--spacing-xl);
}

/* ========================================
   左侧主区域
   ======================================== */

.checkin-main {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xl);
}

/* 签到卡片 */
.checkin-card {
  background: var(--bg-color-secondary);
  border: 1px solid var(--border-color-light);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.checkin-header {
  padding: var(--spacing-xl);
  text-align: center;
  background: linear-gradient(
    135deg,
    var(--primary-color) 0%,
    var(--primary-hover-color) 100%
  );
}

.checkin-title {
  font-family: var(--font-family-serif);
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: #fff;
  margin: 0 0 var(--spacing-xs) 0;
}

.checkin-subtitle {
  font-size: var(--font-size-base);
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
}

.checkin-content {
  padding: var(--spacing-2xl);
}

/* 签到状态 */
.checked-status,
.unchecked-status {
  text-align: center;
}

.status-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto var(--spacing-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-full);
  font-size: 36px;
  background: var(--primary-lighter-color);
  color: var(--primary-color);
}

.status-icon.success {
  background: rgba(90, 154, 110, 0.1);
  color: var(--success-color);
}

.status-title {
  font-family: var(--font-family-serif);
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-semibold);
  color: var(--text-color-primary);
  margin: 0 0 var(--spacing-sm) 0;
}

.status-desc {
  font-size: var(--font-size-base);
  color: var(--text-color-secondary);
  margin: 0 0 var(--spacing-xl) 0;
}

.checkin-btn {
  min-width: 160px;
  height: 48px;
  font-size: var(--font-size-lg);
  border-radius: var(--radius-md);
}

.points-earned {
  display: inline-flex;
  align-items: baseline;
  gap: var(--spacing-xs);
  padding: var(--spacing-md) var(--spacing-xl);
  background: var(--bg-color-tertiary);
  border-radius: var(--radius-lg);
}

.points-earned .points-label {
  font-size: var(--font-size-sm);
  color: var(--text-color-secondary);
}

.points-earned .points-value {
  font-family: var(--font-family-serif);
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
  color: var(--success-color);
}

.points-earned .points-unit {
  font-size: var(--font-size-sm);
  color: var(--text-color-secondary);
}

/* 日历卡片 */
.calendar-card {
  background: var(--bg-color-secondary);
  border: 1px solid var(--border-color-light);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.card-header {
  padding: var(--spacing-md) var(--spacing-lg);
  border-bottom: 1px solid var(--border-color-light);
}

.card-title {
  font-family: var(--font-family-serif);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-color-primary);
  margin: 0;
}

.calendar-wrapper {
  padding: var(--spacing-md);
}

.calendar-wrapper :deep(.arco-calendar) {
  border: none;
}

.calendar-wrapper :deep(.arco-calendar-cell) {
  border-radius: var(--radius-md);
}

.calendar-cell.checked {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(90, 154, 110, 0.1);
  border-radius: var(--radius-md);
  color: var(--success-color);
  font-weight: var(--font-weight-semibold);
}

.calendar-cell .check-icon {
  position: absolute;
  bottom: 2px;
  right: 2px;
  font-size: 10px;
}

/* ========================================
   右侧边栏
   ======================================== */

.checkin-sidebar {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

/* 积分卡片 */
.points-card {
  background: var(--bg-color-secondary);
  border: 1px solid var(--border-color-light);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
}

.points-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}

.points-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--primary-lighter-color);
  border-radius: var(--radius-md);
  color: var(--primary-color);
  font-size: 24px;
}

.points-info {
  display: flex;
  flex-direction: column;
}

.points-info .points-label {
  font-size: var(--font-size-sm);
  color: var(--text-color-secondary);
}

.points-info .points-value {
  font-family: var(--font-family-serif);
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
  color: var(--primary-color);
}

/* 记录卡片 */
.records-card {
  background: var(--bg-color-secondary);
  border: 1px solid var(--border-color-light);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.records-list {
  max-height: 400px;
  overflow-y: auto;
}

.empty-records {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--spacing-xl);
  color: var(--text-color-placeholder);
}

.empty-records .empty-icon {
  font-size: 32px;
  margin-bottom: var(--spacing-sm);
}

.record-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-md) var(--spacing-lg);
  border-bottom: 1px solid var(--border-color-light);
}

.record-item:last-child {
  border-bottom: none;
}

.record-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.record-desc {
  font-size: var(--font-size-sm);
  color: var(--text-color-primary);
}

.record-time {
  font-size: var(--font-size-xs);
  color: var(--text-color-placeholder);
}

.record-points {
  font-weight: var(--font-weight-semibold);
  color: var(--text-color-secondary);
}

.record-points.positive {
  color: var(--success-color);
}

/* ========================================
   响应式设计
   ======================================== */

@media (max-width: 992px) {
  .checkin-grid {
    grid-template-columns: 1fr;
  }

  .checkin-sidebar {
    flex-direction: row;
    flex-wrap: wrap;
  }

  .points-card,
  .records-card {
    flex: 1;
    min-width: 280px;
  }
}

@media (max-width: 576px) {
  .checkin-view {
    padding: var(--spacing-md);
  }

  .checkin-header {
    padding: var(--spacing-lg);
  }

  .checkin-content {
    padding: var(--spacing-lg);
  }

  .status-icon {
    width: 64px;
    height: 64px;
    font-size: 28px;
  }

  .checkin-sidebar {
    flex-direction: column;
  }

  .points-card,
  .records-card {
    min-width: auto;
  }
}

/* ========================================
   深色模式
   ======================================== */

[data-theme="dark"] .checkin-card {
  background: var(--bg-color-secondary);
}

[data-theme="dark"] .calendar-card,
[data-theme="dark"] .points-card,
[data-theme="dark"] .records-card {
  background: var(--bg-color-secondary);
  border-color: var(--border-color);
}

[data-theme="dark"] .calendar-cell.checked {
  background: rgba(90, 154, 110, 0.15);
}
</style>
