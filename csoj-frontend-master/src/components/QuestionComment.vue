<template>
  <div class="question-comment">
    <a-spin :loading="loading">
      <div v-if="comments.length === 0" class="empty-comments">
        <a-empty description="暂无评论" />
      </div>

      <a-comment
        v-for="comment in comments"
        :key="comment.id"
        :author="comment.userVO?.userName || '匿名用户'"
        :datetime="formatDate(comment.createTime)"
      >
        <template #avatar>
          <a-avatar>
            {{ (comment.userVO?.userName || "匿名")?.charAt(0) }}
          </a-avatar>
        </template>
        <template #content>
          <p>{{ comment.content }}</p>
        </template>
        <template #actions>
          <span>
            <icon-heart
              :style="{
                color:
                  (typeof document !== 'undefined' && document.documentElement
                    ? getComputedStyle(document.documentElement)
                        .getPropertyValue('--danger-color')
                        .trim()
                    : '') || '#f53f3f',
              }"
            />
            {{ comment.likeCount || 0 }}
          </span>
        </template>
      </a-comment>

      <a-pagination
        v-if="comments.length > 0 && total > pageSize"
        :total="total"
        :current="current"
        :page-size="pageSize"
        @change="handlePageChange"
        style="margin-top: 16px; text-align: center"
      />
    </a-spin>

    <a-divider />

    <a-comment>
      <template #avatar>
        <a-avatar>我</a-avatar>
      </template>
      <template #content>
        <a-textarea
          v-model="newComment"
          placeholder="写下你的评论..."
          :rows="4"
        />
        <a-button
          type="primary"
          style="margin-top: 16px"
          :loading="submitting"
          @click="submitComment"
        >
          发表评论
        </a-button>
      </template>
    </a-comment>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { Message } from "@arco-design/web-vue";
import { IconHeart } from "@arco-design/web-vue/es/icon";
import { QuestionCommentControllerService } from "../../generated";
import type { CommentAddRequest } from "../../generated";

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return "";
  const date = new Date(dateString);
  return date.toLocaleString("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
};

interface Comment {
  id: string;
  content: string;
  userId: string;
  objectId: string;
  objectType: string;
  parentId?: string;
  rootId?: string;
  replyUserId?: string;
  likeCount: number;
  createTime: string;
  updateTime: string;
  userVO?: {
    id: string;
    userName: string;
    userAvatar?: string;
  };
  replyUserVO?: {
    id: string;
    userName: string;
    userAvatar?: string;
  };
}

const props = defineProps<{
  questionId: string;
}>();

const comments = ref<Comment[]>([]);
const newComment = ref("");
const loading = ref(false);
const submitting = ref(false);

// 分页相关
const current = ref(1);
const pageSize = ref(10);
const total = ref(0);

const handlePageChange = (page: number) => {
  current.value = page;
  loadComments();
};

const submitComment = async () => {
  if (!newComment.value.trim()) {
    Message.warning("评论内容不能为空");
    return;
  }

  submitting.value = true;
  try {
    const commentRequest: CommentAddRequest = {
      content: newComment.value,
      objectId: props.questionId,
      objectType: "question",
    };

    const response =
      await QuestionCommentControllerService.addCommentUsingPost1(
        commentRequest
      );

    if (response.code === 0) {
      Message.success("评论发表成功");
      newComment.value = "";
      await loadComments(); // 重新加载评论列表
    } else {
      Message.error(response.message || "评论发表失败");
    }
  } catch (error) {
    console.error("评论发表失败:", error);
    Message.error("评论发表失败");
  } finally {
    submitting.value = false;
  }
};

// 加载评论
const loadComments = async () => {
  loading.value = true;
  try {
    const response =
      await QuestionCommentControllerService.getCommentsByQuestionIdUsingGet(
        props.questionId
      );

    if (response.code === 0) {
      comments.value = response.data || [];
      total.value = comments.value.length || 0;
    } else {
      comments.value = [];
      total.value = 0;
      if (response.message) {
        Message.error(response.message);
      }
    }
  } catch (error) {
    console.error("获取评论失败:", error);
    Message.error("获取评论失败");
  } finally {
    loading.value = false;
  }
};

// 初始化加载评论
onMounted(() => {
  loadComments();
});
</script>

<style scoped>
.question-comment {
  padding: 16px;
}
</style>
