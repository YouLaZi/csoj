<template>
  <div id="homeView">
    <a-row :gutter="24">
      <!-- 左侧边栏 -->
      <a-col :xs="24" :sm="24" :md="6" :lg="5" :xl="4">
        <div class="sidebar-section">
          <SystemAnnouncement class="sidebar-card" />
        </div>

        <!-- 排行榜预览 -->
        <div class="sidebar-section">
          <div class="card leaderboard-card">
            <div class="card-header">
              <h3 class="card-title">{{ $t("home.pointsLeaderboard") }}</h3>
              <a-link
                class="card-extra"
                @click="$router.push('/points/leaderboard')"
              >
                {{ $t("home.viewAll") }}
              </a-link>
            </div>
            <div class="card-body">
              <a-empty
                v-if="!leaderboard || leaderboard.length === 0"
                :description="$t('home.noData')"
              />
              <div v-else class="leaderboard-list">
                <div
                  v-for="(item, index) in leaderboard"
                  :key="index"
                  class="leaderboard-item"
                >
                  <div class="rank-badge" :class="{ 'top-rank': index < 3 }">
                    {{ index + 1 }}
                  </div>
                  <div class="user-info">
                    <a-link
                      class="user-name"
                      @click="$router.push(`/user/profile?id=${item.id}`)"
                    >
                      {{ item.userName }}
                    </a-link>
                    <span class="user-points"
                      >{{ item.totalPoints }} {{ $t("user.points") }}</span
                    >
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </a-col>

      <!-- 中间内容区 -->
      <a-col :xs="24" :sm="24" :md="12" :lg="14" :xl="16">
        <!-- 欢迎信息 -->
        <div class="welcome-section">
          <div class="welcome-content">
            <h1 class="welcome-title">{{ $t("home.welcome") }}</h1>
            <p class="welcome-description">
              {{ $t("home.welcomeDesc") }}
            </p>
            <div class="welcome-actions">
              <a-button
                type="primary"
                size="large"
                @click="$router.push('/questions')"
              >
                <template #icon><icon-code /></template>
                {{ $t("home.startPractice") }}
              </a-button>
              <a-button size="large" @click="$router.push('/posts')">
                <template #icon><icon-message /></template>
                {{ $t("home.browseDiscussion") }}
              </a-button>
            </div>
          </div>
        </div>

        <!-- 热门题目 -->
        <div class="section">
          <div class="section-header">
            <h2 class="section-title">{{ $t("home.hotQuestions") }}</h2>
            <a-link class="section-extra" @click="$router.push('/questions')">
              {{ $t("home.viewMore") }}
            </a-link>
          </div>

          <a-empty
            v-if="!hotQuestions || hotQuestions.length === 0"
            :description="$t('home.noData')"
          />
          <div v-else class="questions-grid">
            <div
              v-for="item in hotQuestions"
              :key="item.id"
              class="question-card"
              @click="$router.push(`/view/question/${item.id}`)"
            >
              <div class="question-header">
                <h4 class="question-title">{{ item.title }}</h4>
                <span
                  class="accept-rate"
                  :class="getAcceptRateClass(item.acceptRate)"
                >
                  {{ item.acceptRate }}%
                </span>
              </div>
              <div class="question-tags">
                <span
                  v-for="(tag, tagIndex) in item.tags"
                  :key="tagIndex"
                  class="tag"
                >
                  {{ tag }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </a-col>

      <!-- 右侧边栏 -->
      <a-col :xs="24" :sm="24" :md="6" :lg="5" :xl="4">
        <!-- 用户信息卡片 -->
        <div class="sidebar-section">
          <div v-if="userInfo" class="card user-card">
            <div class="user-avatar-section">
              <a-avatar
                v-if="userInfo.userAvatar"
                :size="72"
                :image-url="userInfo.userAvatar"
                class="user-avatar"
              />
              <a-avatar v-else :size="72" class="user-avatar">
                {{ userInfo.userName ? userInfo.userName.charAt(0) : "U" }}
              </a-avatar>
              <div class="user-details">
                <div class="user-name">
                  {{ userInfo.userName || $t("user.notLoggedIn") }}
                </div>
                <div class="user-role">
                  {{ userInfo.userRole || $t("user.guest") }}
                </div>
              </div>
            </div>
            <div class="user-stats">
              <div class="stat-item">
                <div class="stat-value">{{ userInfo.solvedCount || 0 }}</div>
                <div class="stat-label">{{ $t("user.solved") }}</div>
              </div>
              <div class="stat-divider"></div>
              <div class="stat-item">
                <div class="stat-value">
                  {{ userInfo.submissionCount || 0 }}
                </div>
                <div class="stat-label">{{ $t("user.submissions") }}</div>
              </div>
              <div class="stat-divider"></div>
              <div class="stat-item">
                <div class="stat-value">{{ userInfo.points || 0 }}</div>
                <div class="stat-label">{{ $t("user.points") }}</div>
              </div>
            </div>
          </div>

          <div v-else class="card login-prompt-card">
            <div class="login-prompt">
              <div class="login-icon">
                <icon-user />
              </div>
              <h4 class="login-title">{{ $t("home.loginToTrack") }}</h4>
              <p class="login-description">{{ $t("home.trackProgress") }}</p>
              <div class="login-actions">
                <a-button type="primary" @click="$router.push('/user/login')">
                  {{ $t("user.login") }}
                </a-button>
                <a-button @click="$router.push('/user/register')">
                  {{ $t("user.register") }}
                </a-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 签到日历 -->
        <div class="sidebar-section">
          <CheckinCalendar class="sidebar-card" />
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from "vue";
import { IconCode, IconMessage, IconUser } from "@arco-design/web-vue/es/icon";
import { Message } from "@arco-design/web-vue";
import CheckinCalendar from "@/components/CheckinCalendar.vue";
import SystemAnnouncement from "@/components/SystemAnnouncement.vue";
import UserService from "@/services/UserService";
import QuestionService from "@/services/QuestionService";
import PointsService from "@/services/PointsService";
import { setupResizeObserverWorkaround } from "@/utils/resizeObserverHelper";
import { useUserStore } from "@/store/useUserStore";

const userStore = useUserStore();
const userInfo = ref<any>(null);
const hotQuestions = ref<any[]>([]);
const leaderboard = ref<any[]>([]);

const loadUserInfo = async () => {
  try {
    if (!userStore.isLoggedIn) {
      userInfo.value = null;
      return;
    }
    const res = await UserService.getCurrentUser();
    if (res.code === 0 && res.data) {
      userInfo.value = res.data;
    } else {
      userInfo.value = null;
    }
  } catch (error) {
    console.error("加载用户信息失败：", error);
    userInfo.value = null;
  }
};

const reloadAllData = () => {
  loadUserInfo();
  loadHotQuestions();
  loadLeaderboard();
};

const loadHotQuestions = async () => {
  try {
    const res = await QuestionService.getHotQuestions(5);
    if (res.code === 0 && res.data && res.data.records) {
      hotQuestions.value = res.data.records.map((item) => ({
        id: item.id,
        title: item.title,
        tags:
          item.tags && typeof item.tags === "string"
            ? item.tags.split(",")
            : [],
        acceptRate:
          item.acceptedNum && item.submitNum
            ? Math.round((item.acceptedNum / item.submitNum) * 100)
            : 0,
      }));
    } else {
      hotQuestions.value = [
        { id: 1, title: "两数之和", tags: ["数组", "哈希表"], acceptRate: 78 },
        {
          id: 2,
          title: "合并两个有序链表",
          tags: ["链表", "递归"],
          acceptRate: 65,
        },
        {
          id: 3,
          title: "最长回文子串",
          tags: ["字符串", "动态规划"],
          acceptRate: 42,
        },
        {
          id: 4,
          title: "二叉树的层序遍历",
          tags: ["树", "BFS"],
          acceptRate: 56,
        },
        {
          id: 5,
          title: "排序算法比较",
          tags: ["排序", "算法"],
          acceptRate: 61,
        },
      ];
    }
  } catch (error) {
    Message.error("加载热门题目失败：" + error.message);
  }
};

const loadLeaderboard = async () => {
  try {
    const res = await PointsService.getLeaderboard("all", 5);
    if (res.code === 0) {
      leaderboard.value = res.data.map((user) => ({
        id: user.userId,
        userName: user.userName || `用户${user.userId.slice(-4)}`,
        totalPoints: user.totalPoints,
      }));
    } else {
      leaderboard.value = [
        { id: 1, userName: "张三", totalPoints: 980 },
        { id: 2, userName: "李四", totalPoints: 875 },
        { id: 3, userName: "王五", totalPoints: 820 },
        { id: 4, userName: "赵六", totalPoints: 760 },
        { id: 5, userName: "钱七", totalPoints: 695 },
      ];
    }
  } catch (error) {
    Message.error("加载排行榜数据失败：" + error.message);
  }
};

const getAcceptRateClass = (rate: number) => {
  if (rate >= 70) return "rate-high";
  if (rate >= 40) return "rate-medium";
  return "rate-low";
};

let cleanupResizeObserverWorkaround: (() => void) | null = null;

onMounted(() => {
  reloadAllData();

  watch(
    () => userStore.isLoggedIn,
    (newValue, oldValue) => {
      if (newValue !== oldValue) {
        reloadAllData();
      }
    },
    { immediate: false }
  );

  watch(
    () => userStore.loginUser,
    (newUser, oldUser) => {
      if (newUser?.id !== oldUser?.id) {
        reloadAllData();
      }
    },
    { immediate: false, deep: true }
  );

  cleanupResizeObserverWorkaround = setupResizeObserverWorkaround(200);
});

onUnmounted(() => {
  if (cleanupResizeObserverWorkaround) {
    cleanupResizeObserverWorkaround();
    cleanupResizeObserverWorkaround = null;
  }
});
</script>

<style scoped>
/* ========================================
   首页布局 - 简约大方
   ======================================== */

#homeView {
  padding: var(--spacing-xl);
  max-width: var(--content-max-width);
  margin: 0 auto;
}

/* ========================================
   欢迎区域
   ======================================== */

.welcome-section {
  background: linear-gradient(
    135deg,
    var(--primary-color) 0%,
    var(--primary-hover-color) 100%
  );
  border-radius: var(--radius-xl);
  padding: var(--spacing-3xl) var(--spacing-2xl);
  margin-bottom: var(--spacing-xl);
  position: relative;
  overflow: hidden;
}

.welcome-section::before {
  content: "";
  position: absolute;
  top: 0;
  right: 0;
  width: 300px;
  height: 300px;
  background: radial-gradient(
    circle,
    rgba(255, 255, 255, 0.1) 0%,
    transparent 70%
  );
  pointer-events: none;
}

.welcome-content {
  position: relative;
  z-index: 1;
  max-width: 600px;
}

.welcome-title {
  font-family: var(--font-family-serif);
  font-size: var(--font-size-4xl);
  font-weight: var(--font-weight-bold);
  color: #ffffff;
  margin: 0 0 var(--spacing-md) 0;
  line-height: var(--line-height-tight);
  letter-spacing: 1px;
}

.welcome-description {
  font-size: var(--font-size-lg);
  color: rgba(255, 255, 255, 0.9);
  margin: 0 0 var(--spacing-xl) 0;
  line-height: var(--line-height-relaxed);
}

.welcome-actions {
  display: flex;
  gap: var(--spacing-md);
}

.welcome-actions .arco-btn {
  height: 44px;
  padding: 0 var(--spacing-xl);
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-medium);
  border-radius: var(--radius-md);
  transition: all var(--transition-base);
}

.welcome-actions .arco-btn-primary {
  background: #ffffff;
  color: var(--primary-color);
  border: none;
}

.welcome-actions .arco-btn-primary:hover {
  background: rgba(255, 255, 255, 0.95);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.welcome-actions .arco-btn:not(.arco-btn-primary) {
  background: transparent;
  color: #ffffff;
  border: 1px solid rgba(255, 255, 255, 0.4);
}

.welcome-actions .arco-btn:not(.arco-btn-primary):hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.6);
}

/* ========================================
   区块和卡片
   ======================================== */

.section {
  margin-bottom: var(--spacing-xl);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
}

.section-title {
  font-family: var(--font-family-serif);
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-semibold);
  color: var(--text-color-primary);
  margin: 0;
}

.section-extra {
  font-size: var(--font-size-sm);
  color: var(--text-color-secondary);
}

.section-extra:hover {
  color: var(--primary-color);
}

.card {
  background: var(--bg-color-secondary);
  border: 1px solid var(--border-color-light);
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: box-shadow var(--transition-base);
}

.card:hover {
  box-shadow: var(--shadow-md);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.card-extra {
  font-size: var(--font-size-sm);
  color: var(--text-color-secondary);
}

.card-body {
  padding: var(--spacing-md);
}

/* ========================================
   侧边栏
   ======================================== */

.sidebar-section {
  margin-bottom: var(--spacing-lg);
}

.sidebar-card {
  border-radius: var(--radius-lg);
  overflow: hidden;
}

/* ========================================
   排行榜
   ======================================== */

.leaderboard-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.leaderboard-item {
  display: flex;
  align-items: center;
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--radius-md);
  transition: background-color var(--transition-fast);
}

.leaderboard-item:hover {
  background-color: var(--bg-color-tertiary);
}

.rank-badge {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-full);
  background-color: var(--bg-color-tertiary);
  color: var(--text-color-secondary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  margin-right: var(--spacing-md);
  flex-shrink: 0;
}

.rank-badge.top-rank {
  background: linear-gradient(
    135deg,
    var(--primary-color),
    var(--primary-hover-color)
  );
  color: #ffffff;
}

.user-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.user-info .user-name {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-medium);
  color: var(--text-color-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-points {
  font-size: var(--font-size-xs);
  color: var(--text-color-secondary);
  margin-top: 2px;
}

/* ========================================
   题目卡片网格
   ======================================== */

.questions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--spacing-md);
}

.question-card {
  background: var(--bg-color-secondary);
  border: 1px solid var(--border-color-light);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  cursor: pointer;
  transition: all var(--transition-base);
}

.question-card:hover {
  border-color: var(--primary-light-color);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--spacing-sm);
}

.question-title {
  font-family: var(--font-family-serif);
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-medium);
  color: var(--text-color-primary);
  margin: 0;
  flex: 1;
  margin-right: var(--spacing-sm);
  line-height: var(--line-height-tight);
}

.accept-rate {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  padding: 2px 8px;
  border-radius: var(--radius-sm);
  flex-shrink: 0;
}

.rate-high {
  background-color: rgba(90, 154, 110, 0.1);
  color: var(--success-color);
}

.rate-medium {
  background-color: rgba(196, 149, 74, 0.1);
  color: var(--warning-color);
}

.rate-low {
  background-color: rgba(196, 92, 92, 0.1);
  color: var(--danger-color);
}

.question-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-xs);
}

.tag {
  font-size: var(--font-size-xs);
  color: var(--text-color-secondary);
  background-color: var(--bg-color-tertiary);
  padding: 2px 8px;
  border-radius: var(--radius-sm);
}

/* ========================================
   用户卡片
   ======================================== */

.user-card {
  padding: var(--spacing-lg);
}

.user-avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  margin-bottom: var(--spacing-lg);
}

.user-avatar {
  margin-bottom: var(--spacing-md);
  border: 3px solid var(--primary-light-color);
}

.user-details .user-name {
  font-family: var(--font-family-serif);
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-semibold);
  color: var(--text-color-primary);
  margin-bottom: var(--spacing-xs);
}

.user-details .user-role {
  font-size: var(--font-size-sm);
  color: var(--text-color-secondary);
}

.user-stats {
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding-top: var(--spacing-md);
  border-top: 1px solid var(--border-color-light);
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-family: var(--font-family-serif);
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--primary-color);
  line-height: 1;
}

.stat-label {
  font-size: var(--font-size-xs);
  color: var(--text-color-secondary);
  margin-top: var(--spacing-xs);
}

.stat-divider {
  width: 1px;
  height: 32px;
  background-color: var(--border-color-light);
}

/* ========================================
   登录提示卡片
   ======================================== */

.login-prompt-card {
  padding: var(--spacing-xl);
}

.login-prompt {
  text-align: center;
}

.login-icon {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto var(--spacing-md);
  background-color: var(--primary-lighter-color);
  border-radius: var(--radius-full);
  color: var(--primary-color);
  font-size: 28px;
}

.login-title {
  font-family: var(--font-family-serif);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-color-primary);
  margin: 0 0 var(--spacing-xs) 0;
}

.login-description {
  font-size: var(--font-size-sm);
  color: var(--text-color-secondary);
  margin: 0 0 var(--spacing-lg) 0;
}

.login-actions {
  display: flex;
  gap: var(--spacing-sm);
  justify-content: center;
}

.login-actions .arco-btn {
  flex: 1;
}

/* ========================================
   响应式设计
   ======================================== */

@media (max-width: 992px) {
  #homeView {
    padding: var(--spacing-lg);
  }

  .welcome-section {
    padding: var(--spacing-xl) var(--spacing-lg);
  }

  .welcome-title {
    font-size: var(--font-size-2xl);
  }

  .welcome-description {
    font-size: var(--font-size-base);
  }

  .questions-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  #homeView {
    padding: var(--spacing-md);
  }

  .welcome-section {
    padding: var(--spacing-lg);
    margin-bottom: var(--spacing-lg);
  }

  .welcome-title {
    font-size: var(--font-size-xl);
  }

  .welcome-actions {
    flex-direction: column;
  }

  .welcome-actions .arco-btn {
    width: 100%;
  }

  .sidebar-section {
    margin-bottom: var(--spacing-md);
  }
}

/* ========================================
   深色模式适配
   ======================================== */

[data-theme="dark"] .welcome-section {
  background: linear-gradient(
    135deg,
    var(--primary-color) 0%,
    var(--primary-hover-color) 100%
  );
}

[data-theme="dark"] .question-card:hover {
  border-color: var(--primary-color);
}

[data-theme="dark"] .login-icon {
  background-color: var(--primary-light-color);
}
</style>
