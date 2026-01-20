<template>
  <div id="homeView">
    <a-row :gutter="16">
      <!-- 左侧边栏：系统公告 和 排行榜 -->
      <a-col :xs="24" :sm="24" :md="6" :lg="5" :xl="4">
        <SystemAnnouncement class="scaled-component" />
        <!-- 排行榜预览 -->
        <a-card
          title="积分排行榜"
          :bordered="false"
          class="leaderboard-card scaled-component"
        >
          <template #extra>
            <a-link @click="$router.push('/points/leaderboard')"
              >查看完整榜单</a-link
            >
          </template>
          <a-empty
            v-if="!leaderboard || leaderboard.length === 0"
            description="暂无数据"
          />
          <a-list v-else :max-height="300">
            <a-list-item v-for="(item, index) in leaderboard" :key="index">
              <a-list-item-meta>
                <template #avatar>
                  <div class="rank-number" :class="{ 'top-rank': index < 3 }">
                    {{ index + 1 }}
                  </div>
                </template>
                <template #title>
                  <a-link @click="$router.push(`/user/profile?id=${item.id}`)">
                    {{ item.userName }}
                  </a-link>
                </template>
                <template #description>
                  <div>积分: {{ item.totalPoints }}</div>
                </template>
              </a-list-item-meta>
            </a-list-item>
          </a-list>
        </a-card>
      </a-col>

      <!-- 中间内容区: 欢迎信息 和 热门题目 -->
      <a-col :xs="24" :sm="24" :md="12" :lg="14" :xl="16">
        <!-- 欢迎信息 -->
        <a-card class="welcome-card" :bordered="false">
          <template #title>
            <div class="welcome-title">
              <icon-home />
              <span style="margin-left: 8px">欢迎来到在线判题系统</span>
            </div>
          </template>
          <div class="welcome-content">
            <p>
              这是一个功能强大的在线编程学习平台，您可以在这里练习编程题目、参与讨论、提高编程技能。
            </p>
            <a-space>
              <a-button type="primary" @click="$router.push('/questions')"
                >开始刷题</a-button
              >
              <a-button @click="$router.push('/posts')">浏览讨论</a-button>
            </a-space>
          </div>
        </a-card>

        <!-- 热门题目预览 -->
        <a-card title="热门题目" :bordered="false" class="hot-questions-card">
          <template #extra>
            <a-link @click="$router.push('/questions')">查看更多</a-link>
          </template>
          <a-empty
            v-if="!hotQuestions || hotQuestions.length === 0"
            description="暂无数据"
          />
          <a-list v-else :max-height="400">
            <a-list-item v-for="(item, index) in hotQuestions" :key="index">
              <a-list-item-meta>
                <template #title>
                  <a-link @click="$router.push(`/view/question/${item.id}`)">
                    {{ item.title }}
                  </a-link>
                </template>
                <template #description>
                  <div class="question-tags">
                    <a-tag
                      v-for="(tag, tagIndex) in item.tags"
                      :key="tagIndex"
                      color="#165DFF"
                      bordered
                    >
                      {{ tag }}
                    </a-tag>
                    <a-tag color="#00B42A" bordered>
                      通过率: {{ item.acceptRate }}%
                    </a-tag>
                  </div>
                </template>
              </a-list-item-meta>
            </a-list-item>
          </a-list>
        </a-card>
      </a-col>

      <!-- 右侧边栏：签到日历 和 用户信息卡片 -->
      <a-col :xs="24" :sm="24" :md="6" :lg="5" :xl="4">
        <!-- 用户信息卡片 -->
        <a-card
          v-if="userInfo"
          class="user-info-card scaled-component"
          :bordered="false"
        >
          <div class="user-info-header">
            <a-avatar
              v-if="userInfo.userAvatar"
              :size="64"
              :image-url="userInfo.userAvatar"
            >
            </a-avatar>
            <a-avatar v-else :size="64">
              {{ userInfo.userName ? userInfo.userName.charAt(0) : "U" }}
            </a-avatar>
            <div class="user-info-details">
              <div class="user-name">{{ userInfo.userName || "未登录" }}</div>
              <div class="user-role">{{ userInfo.userRole || "访客" }}</div>
            </div>
          </div>
          <div class="user-stats">
            <div class="stat-item">
              <div class="stat-value">{{ userInfo.solvedCount || 0 }}</div>
              <div class="stat-label">已解决</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ userInfo.submissionCount || 0 }}</div>
              <div class="stat-label">提交数</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ userInfo.points || 0 }}</div>
              <div class="stat-label">积分</div>
            </div>
          </div>
        </a-card>
        <a-card v-else class="login-card scaled-component" :bordered="false">
          <div class="login-prompt">
            <icon-user />
            <p>登录以跟踪您的学习进度</p>
            <a-space>
              <a-button type="primary" @click="$router.push('/user/login')"
                >登录</a-button
              >
              <a-button @click="$router.push('/user/register')">注册</a-button>
            </a-space>
          </div>
        </a-card>
        <CheckinCalendar class="scaled-component" />
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from "vue";
import { IconHome, IconUser } from "@arco-design/web-vue/es/icon";
import { Message } from "@arco-design/web-vue";
import CheckinCalendar from "@/components/CheckinCalendar.vue";
import SystemAnnouncement from "@/components/SystemAnnouncement.vue";
import UserService from "@/services/UserService";
import QuestionService from "@/services/QuestionService";
import PointsService from "@/services/PointsService";
import { setupResizeObserverWorkaround } from "@/utils/resizeObserverHelper";
import { useUserStore } from "@/store/useUserStore";

// 用户状态管理
const userStore = useUserStore();

// 用户信息
const userInfo = ref<any>(null);

// 热门题目
const hotQuestions = ref<any[]>([]);

// 排行榜数据
const leaderboard = ref<any[]>([]);

// 加载用户信息
const loadUserInfo = async () => {
  try {
    // 检查用户是否已登录
    if (!userStore.isLoggedIn) {
      userInfo.value = null;
      return;
    }

    // 调用后端API获取用户信息
    const res = await UserService.getCurrentUser();
    if (res.code === 0 && res.data) {
      userInfo.value = res.data;
    } else {
      console.warn("获取用户信息失败:", res);
      userInfo.value = null;
    }
  } catch (error) {
    console.error("加载用户信息失败：", error);
    userInfo.value = null;
  }
};

// 重新加载所有数据
const reloadAllData = () => {
  loadUserInfo();
  loadHotQuestions();
  loadLeaderboard();
};

// 加载热门题目
const loadHotQuestions = async () => {
  try {
    // 调用后端API获取热门题目
    const res = await QuestionService.getHotQuestions(5);
    if (res.code === 0 && res.data && res.data.records) {
      // 处理API返回的数据格式
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
      // 如果API调用失败或返回错误，使用模拟数据
      hotQuestions.value = [
        {
          id: 1,
          title: "两数之和",
          tags: ["数组", "哈希表"],
          acceptRate: 78,
        },
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
          tags: ["树", "广度优先搜索"],
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

// 加载排行榜数据
const loadLeaderboard = async () => {
  try {
    // 调用后端API获取排行榜数据
    const res = await PointsService.getLeaderboard("all", 5);
    if (res.code === 0) {
      // 处理API返回的数据格式
      leaderboard.value = res.data.map((user) => ({
        id: user.userId,
        userName: user.userName || `用户${user.userId.slice(-4)}`,
        totalPoints: user.totalPoints,
      }));
    } else {
      // 如果API调用失败或返回错误，使用模拟数据
      leaderboard.value = [
        {
          id: 1,
          userName: "张三",
          points: 980,
        },
        {
          id: 2,
          userName: "李四",
          points: 875,
        },
        {
          id: 3,
          userName: "王五",
          points: 820,
        },
        {
          id: 4,
          userName: "赵六",
          points: 760,
        },
        {
          id: 5,
          userName: "钱七",
          points: 695,
        },
      ];
    }
  } catch (error) {
    Message.error("加载排行榜数据失败：" + error.message);
  }
};

let cleanupResizeObserverWorkaround: (() => void) | null = null;

onMounted(() => {
  // 初始化加载数据
  reloadAllData();

  // 监听用户登录状态变化
  watch(
    () => userStore.isLoggedIn,
    (newValue, oldValue) => {
      // 当登录状态发生变化时，重新加载所有数据
      if (newValue !== oldValue) {
        console.log("用户登录状态变化，重新加载数据");
        reloadAllData();
      }
    },
    { immediate: false }
  );

  // 监听用户信息变化
  watch(
    () => userStore.loginUser,
    (newUser, oldUser) => {
      // 当用户信息发生变化时，重新加载用户相关数据
      if (newUser?.id !== oldUser?.id) {
        console.log("用户信息变化，重新加载数据");
        reloadAllData();
      }
    },
    { immediate: false, deep: true }
  );

  // 设置ResizeObserver循环错误的解决方案
  cleanupResizeObserverWorkaround = setupResizeObserverWorkaround(200);
});

onUnmounted(() => {
  // 清理ResizeObserver错误处理器
  if (cleanupResizeObserverWorkaround) {
    cleanupResizeObserverWorkaround();
    cleanupResizeObserverWorkaround = null;
  }
});
</script>

<style scoped>
.scaled-component {
  transform: scale(0.9);
  transform-origin: top left;
  margin-bottom: 16px; /* Add some margin to prevent overlap */
}

#homeView {
  padding: 16px;
}

.welcome-card,
.hot-questions-card,
.user-info-card,
.login-card,
.leaderboard-card {
  margin-bottom: 16px;
}

.welcome-title {
  display: flex;
  align-items: center;
}

.welcome-content {
  margin-bottom: 16px;
}

.user-info-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.user-info-details {
  margin-left: 16px;
}

.user-name {
  font-size: 18px;
  font-weight: bold;
}

.user-role {
  color: #86909c;
  font-size: 14px;
}

.user-stats {
  display: flex;
  justify-content: space-around;
  text-align: center;
  margin-top: 16px;
}

.stat-item {
  padding: 0 8px;
}

.stat-value {
  font-size: 20px;
  font-weight: bold;
  color: #165dff;
}

.stat-label {
  font-size: 12px;
  color: #86909c;
}

.login-prompt {
  text-align: center;
  padding: 16px;
}

.login-prompt p {
  margin: 16px 0;
}

.question-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.rank-number {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: var(--color-fill-1);
  display: flex;
  justify-content: center;
  align-items: center;
  font-weight: bold;
}

.top-rank {
  background-color: #165dff;
  color: white;
}
</style>
