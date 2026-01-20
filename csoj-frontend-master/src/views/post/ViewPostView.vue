<template>
  <div id="viewPostView">
    <!-- 错误状态显示 -->
    <a-result
      v-if="loadError"
      status="error"
      :title="errorTitle"
      :subtitle="errorMessage"
    >
      <template #extra>
        <a-button type="primary" @click="goBack">返回帖子列表</a-button>
      </template>
    </a-result>

    <!-- 加载状态显示 -->
    <a-spin :loading="loading" tip="加载中..." class="loading-container">
      <div v-if="post" class="post-container">
        <!-- 帖子主体内容 -->
        <div class="post-main">
          <!-- 帖子标题区域 -->
          <div class="post-header">
            <h1 class="post-title">{{ post.title }}</h1>
            <div class="post-meta">
              <div class="author-info">
                <a-avatar
                  :size="36"
                  :image-url="
                    post.user?.userAvatar?.replace(/`/g, '').trim() || undefined
                  "
                  class="author-avatar"
                >
                  <IconUser />
                </a-avatar>
                <div class="author-details">
                  <span class="author-name">{{
                    post.user?.userName || "匿名"
                  }}</span>
                  <span class="post-time">{{
                    moment(post.createTime).format("YYYY年MM月DD日 HH:mm")
                  }}</span>
                </div>
              </div>
              <div class="post-actions">
                <a-space size="medium">
                  <div class="action-item" @click="doThumb">
                    <icon-thumb-up
                      :class="['action-icon', { active: post.hasThumb }]"
                    />
                    <span class="action-count">{{ post.thumbNum || 0 }}</span>
                  </div>
                  <div class="action-item" @click="doFavour">
                    <icon-star
                      :class="['action-icon', { active: post.hasFavour }]"
                    />
                    <span class="action-count">{{ post.favourNum || 0 }}</span>
                  </div>
                </a-space>
              </div>
            </div>
          </div>

          <!-- 帖子内容 -->
          <div class="post-content">
            <MdViewer :value="post.content || ''" />
          </div>
        </div>

        <!-- 评论区域 -->
        <div v-if="!loadError" class="comments-section">
          <!-- 发表评论 -->
          <div class="comment-form">
            <h3 class="section-title">发表评论</h3>
            <div class="comment-input-area">
              <a-textarea
                v-model="commentContent"
                placeholder="写下你的想法..."
                :auto-size="{ minRows: 4, maxRows: 8 }"
                class="comment-textarea"
              />
              <div class="comment-actions">
                <a-button
                  type="primary"
                  @click="submitComment"
                  class="submit-btn"
                >
                  发布评论
                </a-button>
              </div>
            </div>
          </div>

          <!-- 评论列表 -->
          <div class="comments-list">
            <h3 class="section-title">
              评论 <span class="comment-count">({{ comments.length }})</span>
            </h3>

            <a-empty
              v-if="!comments || comments.length === 0"
              description="还没有评论，快来抢沙发吧~"
              class="empty-comments"
            />

            <div v-else class="comment-items">
              <div v-for="item in comments" :key="item.id" class="comment-item">
                <a-avatar class="comment-avatar">
                  <IconUser />
                </a-avatar>
                <div class="comment-content">
                  <div class="comment-header">
                    <span class="comment-author">{{
                      item.userVO?.userName || "匿名用户"
                    }}</span>
                    <span class="comment-time">{{
                      moment(item.createTime).format("MM-DD HH:mm")
                    }}</span>
                  </div>
                  <div class="comment-text">{{ item.content }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
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
import { IconThumbUp, IconStar, IconUser } from "@arco-design/web-vue/es/icon";

interface Props {
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  id: () => "",
});

// 使用路由获取参数
const route = useRoute();
const router = useRouter();

const post = ref<PostVO>();
const comments = ref([]);
const commentContent = ref("");
const loading = ref(false);
const loadError = ref(false);
const errorTitle = ref("加载失败");
const errorMessage = ref("");

// 返回帖子列表页
const goBack = () => {
  router.push("/view/posts");
};

const loadData = async () => {
  // 从props或路由参数获取ID
  const postId = props.id || route.params.id;
  // 验证ID是否有效
  if (!postId) {
    loadError.value = true;
    errorTitle.value = "参数错误";
    errorMessage.value = "帖子ID无效，请返回帖子列表重新选择";
    return;
  }

  // 重置错误状态
  loadError.value = false;
  loading.value = true;

  try {
    // 使用字符串类型的ID发送请求，避免JS处理大数值时可能出现的精度问题
    const res = await PostControllerService.getPostVoByIdUsingGet(
      postId.toString()
    );
    if (res.code === 0) {
      post.value = res.data;
      // 加载评论数据
      loadComments();
    } else {
      loadError.value = true;
      // 根据错误码设置更具体的错误信息
      if (res.code === 40400) {
        // NOT_FOUND_ERROR
        errorTitle.value = "帖子不存在";
        errorMessage.value =
          "您访问的帖子不存在或已被删除，请返回帖子列表查看其他内容";
      } else {
        errorTitle.value = "加载失败";
        errorMessage.value = res.message || "请求数据不存在";
      }
      console.error("加载帖子详情失败", res.message);
    }
  } catch (error) {
    console.error("加载帖子详情失败", error);
    loadError.value = true;
    errorTitle.value = "加载失败";
    errorMessage.value = "请求数据不存在或网络异常，请稍后重试";
  } finally {
    loading.value = false;
  }
};

// 加载评论数据的方法
const loadComments = async () => {
  // 从props或路由参数获取ID
  const postId = props.id || route.params.id;
  if (!postId) {
    return;
  }
  try {
    // 使用正确的方法和参数获取评论数据
    const res = await CommentControllerService.listCommentsUsingGet(
      undefined, // content
      1, // current
      parseInt(postId.toString()), // objectId
      "帖子", // objectType
      10, // pageSize
      undefined, // parentId
      undefined, // rootId
      "createTime", // sortField
      "desc" // sortOrder
    );
    if (res.code === 0) {
      comments.value = res.data?.records || [];
    } else {
      console.error("加载评论失败", res.message);
      comments.value = [];
    }
  } catch (error) {
    console.error("加载评论失败", error);
    comments.value = [];
  }
};

// 提交评论
const submitComment = async () => {
  if (!commentContent.value.trim()) {
    message.warning("评论内容不能为空");
    return;
  }

  // 从props或路由参数获取ID
  const postId = props.id || route.params.id;
  if (!postId) {
    message.error("帖子ID无效");
    return;
  }

  try {
    const commentAddRequest = {
      objectId: parseInt(postId.toString()),
      content: commentContent.value,
      objectType: "帖子",
    };
    const res = await CommentControllerService.addCommentUsingPost(
      commentAddRequest
    );
    if (res.code === 0) {
      message.success("评论成功");
      commentContent.value = "";
      // 重新加载评论
      loadComments();
    } else {
      message.error("评论失败，" + res.message);
    }
  } catch (error) {
    console.error("提交评论失败", error);
    message.error("提交评论失败，请稍后重试");
  }
};

// 点赞/取消点赞
const doThumb = async () => {
  const postThumbAddRequest: PostThumbAddRequest = {
    postId: post.value?.id,
  };
  const res = await PostThumbControllerService.doThumbUsingPost(
    postThumbAddRequest
  );
  if (res.code === 0) {
    message.success(post.value?.hasThumb ? "取消点赞成功" : "点赞成功");
    // 重新加载数据
    loadData();
  } else {
    message.error("操作失败，" + res.message);
  }
};

// 收藏/取消收藏
const doFavour = async () => {
  const postFavourAddRequest: PostFavourAddRequest = {
    postId: post.value?.id,
  };
  const res = await PostFavourControllerService.doPostFavourUsingPost(
    postFavourAddRequest
  );
  if (res.code === 0) {
    message.success(post.value?.hasFavour ? "取消收藏成功" : "收藏成功");
    // 重新加载数据
    loadData();
  } else {
    message.error("操作失败，" + res.message);
  }
};

// 初始化ResizeObserver错误处理器
let cleanupResizeObserverHandler: (() => void) | null = null;

onMounted(() => {
  // 优先从路由参数获取id
  const postId = props.id || route.params.id;
  if (postId) {
    // 由于props是只读的，这里不能直接修改props.id
    // 但可以使用postId进行API调用
    loadData();
  } else {
    message.error("帖子ID无效");
  }
  // 启动ResizeObserver错误处理的变通方案
  cleanupResizeObserverHandler = setupResizeObserverWorkaround();
});

onBeforeUnmount(() => {
  // 清理ResizeObserver错误处理
  if (cleanupResizeObserverHandler) {
    cleanupResizeObserverHandler();
  }
});
</script>

<style scoped>
#viewPostView {
  min-height: 100vh;
  background-color: var(--color-fill-1);
  padding: 24px;
}

.loading-container {
  min-height: 60vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.post-container {
  min-width: 70vw;
  margin: 0 auto;
}

.post-main {
  width: 100%;
  background: var(--color-bg-1);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  margin-bottom: 24px;
  border: 1px solid var(--color-border);
}

.post-header {
  padding: 24px 32px;
  border-bottom: 1px solid var(--color-border);
  background: var(--color-bg-1);
}

.post-title {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 20px 0;
  line-height: 1.3;
  color: #1d2129;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-avatar {
  border: 1px solid #e8e9ea;
}

.author-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.author-name {
  font-size: 16px;
  font-weight: 500;
  color: #1d2129;
}

.post-time {
  font-size: 14px;
  color: #86909c;
}

.post-actions {
  display: flex;
  gap: 16px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 6px;
  background: var(--color-bg-2);
  transition: all 0.2s ease;
  border: 1px solid var(--color-border);
}

.action-item:hover {
  background: var(--color-fill-2);
  border-color: var(--color-border-2);
}

.action-icon {
  font-size: 16px;
  color: #86909c;
  transition: color 0.2s ease;
}

.action-icon.active {
  color: #165dff;
}

.action-count {
  font-size: 14px;
  font-weight: 500;
  color: #4e5969;
}

.post-content {
  padding: 32px;
  font-size: 16px;
  line-height: 1.7;
  color: #1d2129;
}

.comments-section {
  background: var(--color-bg-1);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  border: 1px solid var(--color-border);
}

.comment-form {
  padding: 24px 32px;
  border-bottom: 1px solid var(--color-border);
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1d2129;
  margin: 0 0 16px 0;
}

.comment-count {
  font-weight: 400;
  color: #86909c;
  font-size: 16px;
}

.comment-input-area {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment-textarea {
  border: 1px solid var(--color-border);
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s ease;
  resize: vertical;
}

.comment-textarea:focus {
  border-color: var(--color-primary);
}

.comment-actions {
  display: flex;
  justify-content: flex-end;
}

.submit-btn {
  padding: 8px 20px;
  height: auto;
  font-size: 14px;
  font-weight: 500;
  border-radius: 6px;
}

.comments-list {
  padding: 24px 32px;
}

.empty-comments {
  padding: 40px 0;
}

.comment-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: var(--color-bg-2);
  border-radius: 6px;
  border: 1px solid var(--color-border);
  transition: all 0.2s ease;
}

.comment-item:hover {
  background: var(--color-fill-2);
  border-color: var(--color-border-2);
}

.comment-avatar {
  flex-shrink: 0;
  margin-top: 2px;
}

.comment-content {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.comment-author {
  font-weight: 500;
  color: #1d2129;
  font-size: 14px;
}

.comment-time {
  color: #86909c;
  font-size: 12px;
}

.comment-text {
  color: #4e5969;
  line-height: 1.6;
  font-size: 14px;
  word-wrap: break-word;
}

/* 响应式设计 */
@media (max-width: 768px) {
  #viewPostView {
    padding: 16px;
  }

  .post-header {
    padding: 20px 24px;
  }

  .post-title {
    font-size: 24px;
  }

  .post-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .post-content,
  .comment-form,
  .comments-list {
    padding: 20px 24px;
  }

  .action-item {
    padding: 4px 8px;
  }
}
</style>
