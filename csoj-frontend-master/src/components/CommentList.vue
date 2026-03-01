<template>
  <div class="comment-list">
    <!-- 评论输入框 -->
    <a-card title="发表评论">
      <a-textarea
        v-model="commentContent"
        placeholder="请输入评论内容"
        :auto-size="{ minRows: 3, maxRows: 5 }"
      />
      <div style="margin-top: 16px; text-align: right">
        <a-button type="primary" @click="submitComment">提交评论</a-button>
      </div>
    </a-card>

    <!-- 评论列表 -->
    <a-card title="评论列表" style="margin-top: 16px">
      <a-empty
        v-if="!comments || comments.length === 0"
        description="暂无评论"
      />
      <a-list v-else :data="comments">
        <template #item="{ item: comment }">
          <a-list-item>
            <a-comment>
              <template #avatar>
                <a-avatar>
                  {{ comment.userName ? comment.userName.charAt(0) : "U" }}
                </a-avatar>
              </template>
              <template #author>
                {{ comment.userName || "匿名用户" }}
              </template>
              <template #content>
                <div>{{ comment.content }}</div>
              </template>
              <template #datetime>
                <span>{{ formatTime(comment.createTime) }}</span>
              </template>
              <template #actions>
                <span @click="likeComment(comment)">
                  <icon-thumb-up
                    :style="{ color: comment.hasLike ? '#165DFF' : '' }"
                  />
                  {{ comment.likeCount || 0 }}
                </span>
                <span @click="replyToComment(comment)">
                  <icon-message /> 回复
                </span>
              </template>
            </a-comment>

            <!-- 回复区域 -->
            <div v-if="replyToId === comment.id" style="margin-left: 40px">
              <a-textarea
                v-model="replyContent"
                placeholder="请输入回复内容"
                :auto-size="{ minRows: 2, maxRows: 3 }"
              />
              <div style="margin-top: 8px; text-align: right">
                <a-space>
                  <a-button @click="cancelReply">取消</a-button>
                  <a-button type="primary" @click="submitReply(comment)"
                    >回复</a-button
                  >
                </a-space>
              </div>
            </div>

            <!-- 子评论 -->
            <div
              v-if="comment.children && comment.children.length > 0"
              style="margin-left: 40px"
            >
              <a-list :data="comment.children">
                <template #item="{ item: reply }">
                  <a-comment>
                    <template #avatar>
                      <a-avatar>
                        {{ reply.userName ? reply.userName.charAt(0) : "U" }}
                      </a-avatar>
                    </template>
                    <template #author>
                      {{ reply.userName || "匿名用户" }}
                      <span v-if="reply.replyUserName" style="color: #86909c">
                        回复 {{ reply.replyUserName }}
                      </span>
                    </template>
                    <template #content>
                      <div>{{ reply.content }}</div>
                    </template>
                    <template #datetime>
                      <span>{{ formatTime(reply.createTime) }}</span>
                    </template>
                    <template #actions>
                      <span @click="likeComment(reply)">
                        <icon-thumb-up
                          :style="{ color: reply.hasLike ? '#165DFF' : '' }"
                        />
                        {{ reply.likeCount || 0 }}
                      </span>
                      <span @click="replyToComment(reply, comment.id)">
                        <icon-message /> 回复
                      </span>
                    </template>
                  </a-comment>
                </template>
              </a-list>
            </div>
          </a-list-item>
        </template>
      </a-list>

      <!-- 分页 -->
      <div
        v-if="comments && comments.length > 0"
        style="margin-top: 16px; text-align: center"
      >
        <a-pagination
          :total="total"
          :current="current"
          :page-size="pageSize"
          @change="onPageChange"
        />
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { IconThumbUp, IconMessage } from "@arco-design/web-vue/es/icon";
import message from "@arco-design/web-vue/es/message";
import moment from "moment";
import { CommentService, CommentLikeService } from "../services/CommentService";

const props = defineProps({
  objectId: {
    type: Number,
    required: true,
  },
  objectType: {
    type: String,
    default: "post",
  },
});

const emit = defineEmits(["refresh"]);

// 评论列表数据
const comments = ref([]);
const total = ref(0);
const current = ref(1);
const pageSize = ref(10);

// 评论内容
const commentContent = ref("");

// 回复相关
const replyToId = ref(null);
const replyContent = ref("");
const rootId = ref(null);

// 格式化时间
const formatTime = (time) => {
  if (!time) return "";
  return moment(time).format("YYYY-MM-DD HH:mm");
};

// 加载评论数据
const loadComments = async () => {
  try {
    const res = await CommentService.listCommentsByObjectIdUsingGet({
      objectId: props.objectId,
      objectType: props.objectType,
      current: current.value,
      pageSize: pageSize.value,
    });
    if (res.code === 0 && res.data) {
      comments.value = res.data.records || [];
      total.value = res.data.total || 0;
    } else {
      message.error("加载评论失败，" + res.message);
    }
  } catch (error) {
    message.error("加载评论失败：" + error.message);
    comments.value = [];
  }
};

// 提交评论
const submitComment = async () => {
  if (!commentContent.value.trim()) {
    message.warning("评论内容不能为空");
    return;
  }

  try {
    const commentAddRequest = {
      objectId: props.objectId,
      objectType: props.objectType,
      content: commentContent.value,
    };
    const res = await CommentService.addCommentUsingPost(commentAddRequest);
    if (res.code === 0) {
      message.success("评论成功");
      commentContent.value = "";
      loadComments();
      emit("refresh");
    } else {
      message.error("评论失败，" + res.message);
    }
  } catch (error) {
    message.error("评论失败：" + error.message);
  }
};

// 点赞评论
const likeComment = async (comment) => {
  try {
    const res = await CommentLikeService.likeCommentUsingPost(comment.id);
    if (res.code === 0) {
      message.success(comment.hasLike ? "取消点赞成功" : "点赞成功");
      // 更新本地状态，避免重新加载整个列表
      comment.hasLike = !comment.hasLike;
      if (comment.hasLike) {
        comment.likeCount++;
      } else {
        comment.likeCount--;
      }
    } else {
      message.error("操作失败，" + res.message);
    }
  } catch (error) {
    message.error("操作失败：" + error.message);
  }
};

// 回复评论
const replyToComment = (comment, rootCommentId = null) => {
  replyToId.value = comment.id;
  rootId.value = rootCommentId || comment.id;
  replyContent.value = "";
};

// 取消回复
const cancelReply = () => {
  replyToId.value = null;
  replyContent.value = "";
  rootId.value = null;
};

// 提交回复
const submitReply = async (comment) => {
  if (!replyContent.value.trim()) {
    message.warning("回复内容不能为空");
    return;
  }

  try {
    const replyRequest = {
      objectId: props.objectId,
      objectType: props.objectType,
      content: replyContent.value,
      parentId: replyToId.value,
      rootId: rootId.value,
      replyUserId: comment.userId,
    };
    const res = await CommentService.addCommentUsingPost(replyRequest);
    if (res.code === 0) {
      message.success("回复成功");
      cancelReply();
      loadComments();
      emit("refresh");
    } else {
      message.error("回复失败，" + res.message);
    }
  } catch (error) {
    message.error("回复失败：" + error.message);
  }
};

// 分页切换
const onPageChange = (page) => {
  current.value = page;
  loadComments();
};

// 组件挂载时加载评论
onMounted(() => {
  loadComments();
});
</script>

<style scoped>
.comment-list {
  margin-top: 16px;
}
</style>
