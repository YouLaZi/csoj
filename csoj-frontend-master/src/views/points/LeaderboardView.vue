<template>
  <div class="leaderboard-view">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">{{ t("leaderboard.pageTitle") }}</h1>
      <p class="page-subtitle">{{ t("leaderboard.pageSubtitle") }}</p>
    </div>

    <!-- 积分规则卡片 -->
    <div class="rules-section">
      <div class="rules-grid">
        <div class="rule-card">
          <div class="rule-icon">
            <icon-calendar />
          </div>
          <div class="rule-content">
            <div class="rule-title">{{ t("leaderboard.dailyCheckin") }}</div>
            <div class="rule-points">{{ t("leaderboard.dailyPoints") }}</div>
          </div>
        </div>
        <div class="rule-card">
          <div class="rule-icon">
            <icon-code-block />
          </div>
          <div class="rule-content">
            <div class="rule-title">{{ t("leaderboard.submitQuestion") }}</div>
            <div class="rule-points">{{ t("leaderboard.submitPoints") }}</div>
          </div>
        </div>
        <div class="rule-card">
          <div class="rule-icon">
            <icon-check-circle />
          </div>
          <div class="rule-content">
            <div class="rule-title">
              {{ t("leaderboard.questionAccepted") }}
            </div>
            <div class="rule-points">{{ t("leaderboard.acceptedPoints") }}</div>
          </div>
        </div>
        <div class="rule-card">
          <div class="rule-icon">
            <icon-file />
          </div>
          <div class="rule-content">
            <div class="rule-title">{{ t("leaderboard.publishSolution") }}</div>
            <div class="rule-points">{{ t("leaderboard.solutionPoints") }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 排行榜组件 -->
    <PointsLeaderboard />

    <!-- 提示信息 -->
    <div class="info-section">
      <div class="info-card">
        <icon-info-circle class="info-icon" />
        <div class="info-content">
          <div class="info-title">{{ t("leaderboard.aboutTitle") }}</div>
          <div class="info-text">
            {{ t("leaderboard.aboutDesc") }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import { useI18n } from "vue-i18n";
import {
  IconCalendar,
  IconCodeBlock,
  IconCheckCircle,
  IconFile,
  IconInfoCircle,
} from "@arco-design/web-vue/es/icon";
import PointsLeaderboard from "@/components/PointsLeaderboard.vue";

const { t } = useI18n();

onMounted(() => {
  document.title = t("leaderboard.pageTitle") + " - CodeSmart";
});
</script>

<style scoped>
/* ========================================
   积分排行榜页面 - 简约大方
   ======================================== */

.leaderboard-view {
  padding: var(--spacing-xl);
  max-width: var(--content-max-width);
  margin: 0 auto;
}

/* ========================================
   页面头部
   ======================================== */

.page-header {
  text-align: center;
  margin-bottom: var(--spacing-2xl);
}

.page-title {
  font-family: var(--font-family-serif);
  font-size: var(--font-size-4xl);
  font-weight: var(--font-weight-bold);
  color: var(--text-color-primary);
  margin: 0 0 var(--spacing-sm) 0;
}

.page-subtitle {
  font-size: var(--font-size-lg);
  color: var(--text-color-secondary);
  margin: 0;
}

/* ========================================
   积分规则
   ======================================== */

.rules-section {
  margin-bottom: var(--spacing-xl);
}

.rules-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-md);
}

.rule-card {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-lg);
  background: var(--bg-color-secondary);
  border: 1px solid var(--border-color-light);
  border-radius: var(--radius-lg);
  transition: all var(--transition-base);
}

.rule-card:hover {
  border-color: var(--primary-light-color);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.rule-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--primary-lighter-color);
  border-radius: var(--radius-md);
  color: var(--primary-color);
  font-size: 24px;
  flex-shrink: 0;
}

.rule-content {
  flex: 1;
  min-width: 0;
}

.rule-title {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-medium);
  color: var(--text-color-primary);
  margin-bottom: var(--spacing-xs);
}

.rule-points {
  font-family: var(--font-family-serif);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--warning-color);
}

/* ========================================
   提示信息
   ======================================== */

.info-section {
  margin-top: var(--spacing-xl);
}

.info-card {
  display: flex;
  gap: var(--spacing-md);
  padding: var(--spacing-lg);
  background: var(--primary-lighter-color);
  border: 1px solid var(--primary-light-color);
  border-radius: var(--radius-lg);
}

.info-icon {
  font-size: 24px;
  color: var(--primary-color);
  flex-shrink: 0;
  margin-top: 2px;
}

.info-content {
  flex: 1;
}

.info-title {
  font-weight: var(--font-weight-semibold);
  color: var(--text-color-primary);
  margin-bottom: var(--spacing-xs);
}

.info-text {
  font-size: var(--font-size-sm);
  color: var(--text-color-regular);
  line-height: var(--line-height-relaxed);
}

/* ========================================
   响应式设计
   ======================================== */

@media (max-width: 992px) {
  .rules-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 576px) {
  .leaderboard-view {
    padding: var(--spacing-md);
  }

  .page-title {
    font-size: var(--font-size-2xl);
  }

  .rules-grid {
    grid-template-columns: 1fr;
  }

  .rule-card {
    padding: var(--spacing-md);
  }

  .rule-icon {
    width: 40px;
    height: 40px;
    font-size: 20px;
  }
}

/* ========================================
   深色模式
   ======================================== */

[data-theme="dark"] .rule-card {
  background: var(--bg-color-secondary);
  border-color: var(--border-color);
}

[data-theme="dark"] .rule-card:hover {
  border-color: var(--primary-color);
}

[data-theme="dark"] .rule-icon {
  background: var(--primary-light-color);
}

[data-theme="dark"] .info-card {
  background: var(--primary-light-color);
  border-color: var(--primary-color);
}
</style>
