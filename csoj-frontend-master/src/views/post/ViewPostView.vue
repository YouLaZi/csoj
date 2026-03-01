<template>
  <div id="viewPostView">
    <!-- 错误状态 -->
    <div v-if="loadError" class="error-state">
      <div class="error-icon">
        <icon-close-circle />
      </div>
      <h2 class="error-title">{{ errorTitle }}</h2>
      <p class="error-message">{{ errorMessage }}</p>
      <a-button type="primary" @click="goBack">{{
        t("error.backHome")
      }}</a-button>
    </div>

    <!-- 加载状态 -->
    <a-spin :loading="loading" class="loading-container">
      <div v-if="post" class="post-container">
        <!-- 帖子主体 -->
        <article class="post-main">
          <!-- 帖子头部 -->
          <header class="post-header">
            <h1 class="post-title">{{ post.title }}</h1>
            <div class="post-meta">
              <div class="author-info">
                <a-avatar :size="40" :image-url="post.user?.userAvatar">
                  <icon-user />
                </a-avatar>
                <div class="author-details">
                  <span class="author-name">{{
                    post.user?.userName || t("user.guest")
                  }}</span>
                  <span class="post-time">{{
                    formatTime(post.createTime)
                  }}</span>
                </div>
              </div>
              <div class="post-actions">
                <button
                  class="action-btn"
                  :class="{ active: post.hasThumb }"
                  @click="doThumb"
                >
                  <icon-thumb-up />
                  <span>{{ post.thumbNum || 0 }}</span>
                </button>
                <button
                  class="action-btn"
                  :class="{ active: post.hasFavour }"
                  @click="doFavour"
                >
                  <icon-star />
                  <span>{{ post.favourNum || 0 }}</span>
                </button>
              </div>
            </div>
          </header>

          <!-- 帖子内容 -->
          <div class="post-content">
            <MdViewer :value="post.content || ''" />
          </div>
        </article>

        <!-- 评论区 -->
        <section class="comments-section">
          <div class="section-header">
            <h3 class="section-title">{{ t("viewPost.comments") }}</h3>
            <span class="comment-count">{{ comments.length }}</span>
          </div>

          <!-- 发表评论 -->
          <div class="comment-form">
            <a-textarea
              v-model="commentContent"
              :placeholder="t('viewPost.writeComment')"
              :auto-size="{ minRows: 3, maxRows: 6 }"
            />
            <div class="form-actions">
              <a-button
                type="primary"
                @click="submitComment"
                :disabled="!commentContent.trim()"
              >
                {{ t("viewPost.submitComment") }}
              </a-button>
            </div>
          </div>

          <!-- 评论列表 -->
          <div class="comments-list">
            <div v-if="comments.length === 0" class="empty-comments">
              <icon-message class="empty-icon" />
              <span>{{ t("viewPost.noComments") }}</span>
            </div>
            <div v-else class="comment-items">
              <div v-for="item in comments" :key="item.id" class="comment-item">
                <a-avatar :size="36" class="comment-avatar">
                  <icon-user />
                </a-avatar>
                <div class="comment-body">
                  <div class="comment-header">
                    <span class="comment-author">{{
                      item.userVO?.userName || t("user.guest")
                    }}</span>
                    <span class="comment-time">{{
                      formatTime(item.createTime)
                    }}</span>
                  </div>
                  <p class="comment-text">{{ item.content }}</p>
                </div>
              </div>
            </div>
          </div>
        </section>
      </div>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import {
  onMounted,
  ref,
  withDefaults,
  defineProps,
  onBeforeUnmount,
} from "vue";
import { setupResizeObserverWorkaround } from "../../utils/resizeObserverHelper";
import { useRoute, useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import message from "@arco-design/web-vue/es/message";
import MdViewer from "@/components/MdViewer.vue";
import {
  PostControllerService,
  PostFavourControllerService,
  PostFavourAddRequest,
  PostThumbControllerService,
  PostThumbAddRequest,
  PostVO,
  CommentControllerService,
} from "../../../generated";
import moment from "moment";
import {
  IconThumbUp,
  IconStar,
  IconUser,
  IconCloseCircle,
  IconMessage,
} from "@arco-design/web-vue/es/icon";

const { t } = useI18n();

interface Props {
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  id: () => "",
});

const route = useRoute();
const router = useRouter();

const post = ref<PostVO>();
const comments = ref([]);
const commentContent = ref("");
const loading = ref(false);
const loadError = ref(false);
const errorTitle = ref("加载失败");
const errorMessage = ref("");

const formatTime = (time: any) => {
  return moment(time).format("YYYY年MM月DD日 HH:mm");
};

const goBack = () => {
  router.push("/posts");
};

const loadData = async () => {
  const postId = props.id || route.params.id;
  if (!postId) {
    loadError.value = true;
    errorTitle.value = "参数错误";
    errorMessage.value = "帖子ID无效，请返回帖子列表重新选择";
    return;
  }

  loadError.value = false;
  loading.value = true;

  try {
    const res = await PostControllerService.getPostVoByIdUsingGet(
      postId.toString()
    );
    if (res.code === 0) {
      post.value = res.data;
      loadComments();
    } else {
      loadError.value = true;
      if (res.code === 40400) {
        errorTitle.value = "帖子不存在";
        errorMessage.value = "您访问的帖子不存在或已被删除";
      } else {
        errorTitle.value = "加载失败";
        errorMessage.value = res.message || "请求数据不存在";
      }
    }
  } catch (error) {
    console.error("加载帖子详情失败", error);
    loadError.value = true;
    errorTitle.value = "加载失败";
    errorMessage.value = "网络异常，请稍后重试";
  } finally {
    loading.value = false;
  }
};

const loadComments = async () => {
  const postId = props.id || route.params.id;
  if (!postId) return;

  try {
    const res = await CommentControllerService.listCommentsUsingGet(
      undefined,
      1,
      parseInt(postId.toString()),
      "帖子",
      10,
      undefined,
      undefined,
      "createTime",
      "desc"
    );
    if (res.code === 0) {
      comments.value = res.data?.records || [];
    } else {
      comments.value = [];
    }
  } catch (error) {
    console.error("加载评论失败", error);
    comments.value = [];
  }
};

const submitComment = async () => {
  if (!commentContent.value.trim()) {
    message.warning("评论内容不能为空");
    return;
  }

  const postId = props.id || route.params.id;
  if (!postId) {
    message.error("帖子ID无效");
    return;
  }

  try {
    const res = await CommentControllerService.addCommentUsingPost({
      objectId: parseInt(postId.toString()),
      content: commentContent.value,
      objectType: "帖子",
    });
    if (res.code === 0) {
      message.success("评论成功");
      commentContent.value = "";
      loadComments();
    } else {
      message.error("评论失败，" + res.message);
    }
  } catch (error) {
    console.error("提交评论失败", error);
    message.error("提交评论失败，请稍后重试");
  }
};

const doThumb = async () => {
  const res = await PostThumbControllerService.doThumbUsingPost({
    postId: post.value?.id,
  });
  if (res.code === 0) {
    message.success(post.value?.hasThumb ? "取消点赞成功" : "点赞成功");
    loadData();
  } else {
    message.error("操作失败，" + res.message);
  }
};

const doFavour = async () => {
  const res = await PostFavourControllerService.doPostFavourUsingPost({
    postId: post.value?.id,
  });
  if (res.code === 0) {
    message.success(post.value?.hasFavour ? "取消收藏成功" : "收藏成功");
    loadData();
  } else {
    message.error("操作失败，" + res.message);
  }
};

let cleanupResizeObserverHandler: (() => void) | null = null;

onMounted(() => {
  const postId = props.id || route.params.id;
  if (postId) {
    loadData();
  } else {
    message.error("帖子ID无效");
  }
  cleanupResizeObserverHandler = setupResizeObserverWorkaround();
});

onBeforeUnmount(() => {
  if (cleanupResizeObserverHandler) {
    cleanupResizeObserverHandler();
  }
});
</script>

<style scoped>
/* ========================================
   帖子详情页面 - 简约大方
   ======================================== */

#viewPostView {
  min-height: 100vh;
  background: var(--bg-color);
  padding: var(--spacing-xl);
}

.loading-container {
  min-height: 60vh;
}

.post-container {
  max-width: 900px;
  margin: 0 auto;
}

/* ========================================
   错误状态
   ======================================== */

.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  text-align: center;
}

.error-icon {
  font-size: 64px;
  color: var(--danger-color);
  margin-bottom: var(--spacing-lg);
}

.error-title {
  font-family: var(--font-family-serif);
  font-size: var(--font-size-2xl);
  color: var(--text-color-primary);
  margin: 0 0 var(--spacing-sm) 0;
}

.error-message {
  font-size: var(--font-size-base);
  color: var(--text-color-secondary);
  margin: 0 0 var(--spacing-xl) 0;
}

/* ========================================
   帖子主体
   ======================================== */

.post-main {
  background: var(--bg-color-secondary);
  border: 1px solid var(--border-color-light);
  border-radius: var(--radius-lg);
  overflow: hidden;
  margin-bottom: var(--spacing-xl);
}

.post-header {
  padding: var(--spacing-2xl);
  border-bottom: 1px solid var(--border-color-light);
}

.post-title {
  font-family: var(--font-family-serif);
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
  color: var(--text-color-primary);
  margin: 0 0 var(--spacing-lg) 0;
  line-height: var(--line-height-tight);
}

.post-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.author-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.author-details {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-medium);
  color: var(--text-color-primary);
}

.post-time {
  font-size: var(--font-size-sm);
  color: var(--text-color-secondary);
}

.post-actions {
  display: flex;
  gap: var(--spacing-sm);
}

.action-btn {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--bg-color-tertiary);
  border: 1px solid var(--border-color-light);
  border-radius: var(--radius-md);
  color: var(--text-color-secondary);
  font-size: var(--font-size-sm);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.action-btn:hover {
  border-color: var(--primary-light-color);
  color: var(--primary-color);
}

.action-btn.active {
  background: var(--primary-lighter-color);
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.post-content {
  padding: var(--spacing-2xl);
}

/* ========================================
   评论区
   ======================================== */

.comments-section {
  background: var(--bg-color-secondary);
  border: 1px solid var(--border-color-light);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.section-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-lg) var(--spacing-2xl);
  border-bottom: 1px solid var(--border-color-light);
}

.section-title {
  font-family: var(--font-family-serif);
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-semibold);
  color: var(--text-color-primary);
  margin: 0;
}

.comment-count {
  font-size: var(--font-size-sm);
  color: var(--text-color-secondary);
}

.comment-form {
  padding: var(--spacing-lg) var(--spacing-2xl);
  border-bottom: 1px solid var(--border-color-light);
}

.comment-form :deep(.arco-textarea) {
  background: var(--bg-color-tertiary);
  border: 1px solid var(--border-color-light);
  border-radius: var(--radius-md);
}

.comment-form :deep(.arco-textarea:focus) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px var(--focus-ring-color);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--spacing-md);
}

.comments-list {
  padding: var(--spacing-lg) var(--spacing-2xl);
}

.empty-comments {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--spacing-2xl);
  color: var(--text-color-placeholder);
}

.empty-comments .empty-icon {
  font-size: 32px;
  margin-bottom: var(--spacing-sm);
}

.comment-items {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.comment-item {
  display: flex;
  gap: var(--spacing-md);
  padding: var(--spacing-md);
  background: var(--bg-color-tertiary);
  border-radius: var(--radius-md);
}

.comment-avatar {
  flex-shrink: 0;
  background: var(--primary-light-color);
  color: var(--primary-color);
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-xs);
}

.comment-author {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--text-color-primary);
}

.comment-time {
  font-size: var(--font-size-xs);
  color: var(--text-color-placeholder);
}

.comment-text {
  font-size: var(--font-size-sm);
  color: var(--text-color-regular);
  margin: 0;
  line-height: var(--line-height-relaxed);
}

/* ========================================
   响应式设计
   ======================================== */

@media (max-width: 768px) {
  #viewPostView {
    padding: var(--spacing-md);
  }

  .post-header,
  .post-content,
  .comment-form,
  .comments-list {
    padding-left: var(--spacing-lg);
    padding-right: var(--spacing-lg);
  }

  .post-title {
    font-size: var(--font-size-2xl);
  }

  .post-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-md);
  }
}

/* ========================================
   深色模式
   ======================================== */

[data-theme="dark"] .post-main,
[data-theme="dark"] .comments-section {
  background: var(--bg-color-secondary);
  border-color: var(--border-color);
}

[data-theme="dark"] .action-btn {
  background: var(--bg-color-tertiary);
  border-color: var(--border-color);
}

[data-theme="dark"] .comment-item {
  background: var(--bg-color-tertiary);
}
</style>
