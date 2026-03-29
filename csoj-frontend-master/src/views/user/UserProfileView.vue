<template>
  <div id="userProfileView">
    <!-- 页面头部 -->
    <div class="profile-header">
      <div class="user-info-section">
        <div class="avatar-wrapper">
          <a-avatar :size="100" :image-url="user.userAvatar">
            {{ user.userName ? user.userName.charAt(0) : "U" }}
          </a-avatar>
          <div class="avatar-edit" @click="scrollToEdit">
            <icon-edit />
          </div>
        </div>
        <div class="user-details">
          <h1 class="user-name">{{ user.userName || "未设�? }}</h1>
          <p class="user-role">
            {{ user.userRole === "admin" ? "管理�? : "普通用�? }}
          </p>
          <p class="user-account">@{{ user.userAccount || "未设�? }}</p>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon questions">
            <icon-code-block />
          </div>
          <div class="stat-content">
            <span class="stat-value">{{ myQuestions.length }}</span>
            <span class="stat-label">我的题目</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon posts">
            <icon-file />
          </div>
          <div class="stat-content">
            <span class="stat-value">{{ myPosts.length }}</span>
            <span class="stat-label">我的帖子</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon points">
            <icon-gift />
          </div>
          <div class="stat-content">
            <span class="stat-value">{{ userPoints }}</span>
            <span class="stat-label">积分</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="content-grid">
      <!-- 左侧：编辑资�?-->
      <div class="content-main">
        <div class="edit-card">
          <div class="card-header">
            <h3 class="card-title">编辑资料</h3>
          </div>
          <div class="card-body">
            <a-form :model="form" layout="vertical">
              <a-form-item field="userName" label="用户�?>
                <a-input v-model="form.userName" placeholder="请输入用户名" />
              </a-form-item>
              <a-form-item field="userProfile" label="个人简�?>
                <a-textarea
                  v-model="form.userProfile"
                  placeholder="介绍一下自己吧..."
                  :auto-size="{ minRows: 3, maxRows: 5 }"
                />
              </a-form-item>
              <a-form-item field="userAvatar" label="头像地址">
                <a-input
                  v-model="form.userAvatar"
                  placeholder="请输入头像URL地址"
                />
              </a-form-item>
              <a-form-item>
                <a-button type="primary" @click="updateUserInfo">
                  <template #icon><icon-save /></template>
                  保存修改
                </a-button>
              </a-form-item>
            </a-form>
          </div>
        </div>

        <!-- 积分和统�?-->
        <PointsDisplay />
        <UserStatistics />
      </div>

      <!-- 右侧：我的内�?-->
      <div class="content-sidebar">
        <!-- 我的题目 -->
        <div class="list-card">
          <div class="card-header">
            <h3 class="card-title">我的题目</h3>
            <a-link @click="$router.push('/questions')">查看全部</a-link>
          </div>
          <div class="list-content">
            <div v-if="myQuestions.length === 0" class="empty-list">
              <span>暂无题目</span>
            </div>
            <div
              v-for="item in myQuestions.slice(0, 5)"
              :key="item.id"
              class="list-item"
              @click="viewQuestion(item)"
            >
              <span class="item-title">{{ item.title }}</span>
              <icon-right class="item-arrow" />
            </div>
          </div>
        </div>

        <!-- 我的帖子 -->
        <div class="list-card">
          <div class="card-header">
            <h3 class="card-title">我的帖子</h3>
            <a-link @click="$router.push('/posts')">查看全部</a-link>
          </div>
          <div class="list-content">
            <div v-if="myPosts.length === 0" class="empty-list">
              <span>暂无帖子</span>
            </div>
            <div
              v-for="item in myPosts.slice(0, 5)"
              :key="item.id"
              class="list-item"
              @click="viewPost(item)"
            >
              <span class="item-title">{{ item.title }}</span>
              <icon-right class="item-arrow" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, reactive } from "vue";
import {
  UserControllerService,
  UserUpdateMyRequest,
  QuestionControllerService,
  QuestionQueryRequest,
  PostControllerService,
  PostQueryRequest,
} from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import { useStore } from "vuex";
import PointsDisplay from "@/components/PointsDisplay.vue";
import UserStatistics from "@/components/UserStatistics.vue";
import {
  IconEdit,
  IconCodeBlock,
  IconFile,
  IconGift,
  IconSave,
  IconRight,
} from "@arco-design/web-vue/es/icon";

const router = useRouter();
const store = useStore();

const user = ref(store.state.user.loginUser || {});
const userPoints = ref(0);
const myQuestions = ref([]);
const myPosts = ref([]);

const form = reactive<UserUpdateMyRequest>({
  userName: user.value.userName || "",
  userProfile: user.value.userProfile || "",
  userAvatar: user.value.userAvatar || "",
});

const scrollToEdit = () => {
  document.querySelector(".edit-card")?.scrollIntoView({ behavior: "smooth" });
};

const loadUserInfo = async () => {
  await store.dispatch("user/getLoginUser");
  user.value = store.state.user.loginUser;
  form.userName = user.value.userName || "";
  form.userProfile = user.value.userProfile || "";
  form.userAvatar = user.value.userAvatar || "";
  userPoints.value = user.value.points || 0;
};

const loadMyQuestions = async () => {
  const queryRequest: QuestionQueryRequest = {
    current: 1,
    pageSize: 10,
    userId: user.value.id,
  };
  try {
    const res = await QuestionControllerService.listMyQuestionVoByPageUsingPost(
      queryRequest
    );
    if (res.code === 0) {
      myQuestions.value = res.data.records;
    }
  } catch (error) {
    console.error("加载我的题目失败", error);
  }
};

const loadMyPosts = async () => {
  const queryRequest: PostQueryRequest = {
    current: 1,
    pageSize: 10,
  };
  try {
    const res = await PostControllerService.listMyPostVoByPageUsingPost(
      queryRequest
    );
    if (res.code === 0) {
      myPosts.value = res.data.records;
    }
  } catch (error) {
    console.error("加载我的帖子失败", error);
  }
};

const updateUserInfo = async () => {
  try {
    const res = await UserControllerService.updateMyUserUsingPost(form);
    if (res.code === 0) {
      message.success("更新成功");
      loadUserInfo();
    } else {
      message.error("更新失败�? + res.message);
    }
  } catch (error) {
    message.error("更新失败，请稍后重试");
    console.error(error);
  }
};

const viewQuestion = (question: any) => {
  router.push(`/view/question/${question.id}`);
};

const viewPost = (post: any) => {
  if (post && post.id) {
    router.push(`/view/post/${post.id}`);
  } else {
    message.error("帖子ID无效");
  }
};

onMounted(async () => {
  await loadUserInfo();
  loadMyQuestions();
  loadMyPosts();
});
</script>

<style scoped>
/* ========================================
   用户资料页面 - 简约大�?   ======================================== */

#userProfileView {
  padding: var(--spacing-xl);
  max-width: var(--content-max-width);
  margin: 0 auto;
}

/* ========================================
   页面头部
   ======================================== */

.profile-header {
  background: linear-gradient(
    135deg,
    var(--primary-color) 0%,
    var(--primary-hover-color) 100%
  );
  border-radius: var(--radius-xl);
  padding: var(--spacing-2xl);
  margin-bottom: var(--spacing-xl);
}

.user-info-section {
  display: flex;
  align-items: center;
  gap: var(--spacing-xl);
}

.avatar-wrapper {
  position: relative;
  flex-shrink: 0;
}

.avatar-wrapper :deep(.arco-avatar) {
  border: 4px solid rgba(255, 255, 255, 0.3);
  box-shadow: var(--shadow-lg);
}

.avatar-edit {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 32px;
  height: 32px;
  background: var(--bg-color-secondary);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: var(--shadow-md);
  color: var(--text-color-regular);
  transition: all var(--transition-fast);
}

.avatar-edit:hover {
  background: var(--primary-color);
  color: #fff;
}

.user-details {
  color: #fff;
}

.user-name {
  font-family: var(--font-family-serif);
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
  margin: 0 0 var(--spacing-xs) 0;
}

.user-role {
  font-size: var(--font-size-base);
  opacity: 0.9;
  margin: 0 0 var(--spacing-xs) 0;
}

.user-account {
  font-size: var(--font-size-sm);
  opacity: 0.7;
  margin: 0;
}

/* ========================================
   统计卡片
   ======================================== */

.stats-section {
  margin-bottom: var(--spacing-xl);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--spacing-md);
}

.stat-card {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-lg);
  background: var(--bg-color-secondary);
  border: 1px solid var(--border-color-light);
  border-radius: var(--radius-lg);
  transition: all var(--transition-base);
}

.stat-card:hover {
  border-color: var(--primary-light-color);
  box-shadow: var(--shadow-md);
}

.stat-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  font-size: 24px;
}

.stat-icon.questions {
  background: rgba(79, 111, 143, 0.1);
  color: var(--primary-color);
}

.stat-icon.posts {
  background: rgba(90, 154, 110, 0.1);
  color: var(--success-color);
}

.stat-icon.points {
  background: rgba(196, 149, 74, 0.1);
  color: var(--warning-color);
}

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-family: var(--font-family-serif);
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--text-color-primary);
}

.stat-label {
  font-size: var(--font-size-sm);
  color: var(--text-color-secondary);
}

/* ========================================
   内容区域
   ======================================== */

.content-grid {
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: var(--spacing-xl);
}

.content-main {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.content-sidebar {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

/* 卡片通用样式 */
.edit-card,
.list-card {
  background: var(--bg-color-secondary);
  border: 1px solid var(--border-color-light);
  border-radius: var(--radius-lg);
  overflow: hidden;
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

.card-body {
  padding: var(--spacing-lg);
}

/* 列表样式 */
.list-content {
  padding: var(--spacing-sm);
}

.list-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-md);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background-color var(--transition-fast);
}

.list-item:hover {
  background: var(--bg-color-tertiary);
}

.item-title {
  font-size: var(--font-size-sm);
  color: var(--text-color-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}

.item-arrow {
  font-size: 12px;
  color: var(--text-color-placeholder);
  margin-left: var(--spacing-sm);
}

.empty-list {
  text-align: center;
  padding: var(--spacing-xl);
  color: var(--text-color-placeholder);
  font-size: var(--font-size-sm);
}

/* ========================================
   响应式设�?   ======================================== */

@media (max-width: 992px) {
  .content-grid {
    grid-template-columns: 1fr;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 576px) {
  #userProfileView {
    padding: var(--spacing-md);
  }

  .profile-header {
    padding: var(--spacing-lg);
  }

  .user-info-section {
    flex-direction: column;
    text-align: center;
  }

  .user-name {
    font-size: var(--font-size-2xl);
  }
}

/* ========================================
   深色模式
   ======================================== */

[data-theme="dark"] .stat-card,
[data-theme="dark"] .edit-card,
[data-theme="dark"] .list-card {
  background: var(--bg-color-secondary);
  border-color: var(--border-color);
}

[data-theme="dark"] .stat-card:hover {
  border-color: var(--primary-color);
}

[data-theme="dark"] .list-item:hover {
  background: var(--bg-color-tertiary);
}
</style>
