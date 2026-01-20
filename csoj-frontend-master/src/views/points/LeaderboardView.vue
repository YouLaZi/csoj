<template>
  <div class="leaderboard-view">
    <a-row :gutter="16">
      <a-col :span="24">
        <a-card class="intro-card" :bordered="false">
          <template #title>
            <div class="card-title">
              <a-typography-title :heading="3">积分排行榜</a-typography-title>
              <a-tag color="#165dff">实时更新</a-tag>
            </div>
          </template>
          <a-typography-paragraph>
            在CodeSmart平台，您可以通过多种方式获取积分：
            <a-row :gutter="16" class="points-rules">
              <a-col :xs="24" :sm="12" :md="6">
                <a-card class="rule-card" :bordered="false">
                  <template #title>
                    <div class="rule-title"><icon-calendar /> 每日签到</div>
                  </template>
                  <div class="rule-points">+15积分</div>
                </a-card>
              </a-col>
              <a-col :xs="24" :sm="12" :md="6">
                <a-card class="rule-card" :bordered="false">
                  <template #title>
                    <div class="rule-title"><icon-code-block /> 提交题目</div>
                  </template>
                  <div class="rule-points">+5积分</div>
                </a-card>
              </a-col>
              <a-col :xs="24" :sm="12" :md="6">
                <a-card class="rule-card" :bordered="false">
                  <template #title>
                    <div class="rule-title"><icon-check-circle /> 题目通过</div>
                  </template>
                  <div class="rule-points">+20积分</div>
                </a-card>
              </a-col>
              <a-col :xs="24" :sm="12" :md="6">
                <a-card class="rule-card" :bordered="false">
                  <template #title>
                    <div class="rule-title"><icon-file /> 发布题解</div>
                  </template>
                  <div class="rule-points">+30积分</div>
                </a-card>
              </a-col>
            </a-row>
            <div class="motivation-text">
              积分越高，排名越靠前。快来参与吧！
            </div>
          </a-typography-paragraph>
        </a-card>
      </a-col>
    </a-row>

    <a-row>
      <a-col :span="24">
        <PointsLeaderboard />
      </a-col>
    </a-row>

    <a-row :gutter="16" class="info-section">
      <a-col :span="24">
        <a-alert type="info">
          <template #icon>
            <icon-info-circle />
          </template>
          <template #title>关于积分排行榜</template>
          <template #default>
            <p>
              排行榜数据每小时更新一次。您可以选择查看日榜、周榜、月榜或总榜。
            </p>
            <p>如果您有任何问题或建议，请联系我们的支持团队。</p>
          </template>
        </a-alert>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import {
  IconCalendar,
  IconCodeBlock,
  IconCheckCircle,
  IconFile,
  IconInfoCircle,
} from "@arco-design/web-vue/es/icon";
import PointsLeaderboard from "@/components/PointsLeaderboard.vue";

onMounted(() => {
  document.title = "积分排行榜 - CodeSmart";
});
</script>

<style scoped>
.leaderboard-view {
  padding: 24px;
  /* max-width: 1200px; */ /* 移除最大宽度限制，允许表格更宽 */
  margin: 0 auto;
}

.intro-card {
  margin-bottom: 28px;
  border-radius: 12px;
  background-color: var(--color-bg-2);
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.intro-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  transform: translateY(-4px);
}

.card-title {
  display: flex;
  align-items: center;
  gap: 16px;
}

.points-rules {
  margin-top: 24px;
  margin-bottom: 24px;
}

.rule-card {
  margin-bottom: 20px;
  border-radius: 12px;
  background-color: var(--color-fill-2);
  transition: all 0.3s ease;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.rule-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.rule-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
  color: var(--color-text-1);
  font-size: 16px;
}

.rule-points {
  font-size: 22px;
  font-weight: bold;
  color: #ff7d00;
  text-align: center;
  padding: 16px 0;
  margin-top: auto;
}

.motivation-text {
  margin-top: 24px;
  font-weight: 600;
  text-align: center;
  font-size: 18px;
  color: var(--color-text-1);
  background: linear-gradient(
    90deg,
    var(--color-primary-light-4),
    var(--color-primary),
    var(--color-primary-light-4)
  );
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
  animation: shine 3s infinite linear;
}

.info-section {
  margin-top: 28px;
  margin-bottom: 28px;
  animation: fadeIn 0.8s ease-in-out;
}

/* 深色模式适配 */
:root[data-theme="dark"] .intro-card {
  background-color: var(--color-bg-2);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.25);
}

:root[data-theme="dark"] .rule-card {
  background-color: var(--color-fill-2);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.25);
}

:root[data-theme="dark"] .rule-points {
  color: #ff9500;
}

:root[data-theme="dark"] .motivation-text {
  background: linear-gradient(
    90deg,
    var(--color-primary-light-3),
    var(--color-primary),
    var(--color-primary-light-3)
  );
  background-clip: text;
  -webkit-background-clip: text;
}

/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes shine {
  0% {
    background-position: 0%;
  }
  100% {
    background-position: 400%;
  }
}

/* 响应式调整 */
@media (max-width: 768px) {
  .leaderboard-view {
    padding: 16px;
  }

  .card-title {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .rule-points {
    font-size: 18px;
    padding: 12px 0;
  }

  .motivation-text {
    font-size: 16px;
  }

  .rule-title {
    font-size: 14px;
  }
}
</style>
