<template>
  <div class="question-answer">
    <a-spin :loading="loading">
      <a-alert v-if="!hasPermission" type="warning" show-icon>
        您暂无权限查看本题答案
        <template #description>
          <p>完成题目或获取更高权限后可查看答案</p>
        </template>
      </a-alert>
      <div v-else>
        <a-empty v-if="!answer" description="暂无答案" />
        <MdViewer v-else :value="answer" />
      </div>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import MdViewer from "@/components/MdViewer.vue";
import { Message } from "@arco-design/web-vue";
import { QuestionAnswerControllerService } from "../../generated";

const props = defineProps<{
  questionId: string;
}>();

const answer = ref("");
const hasPermission = ref(false);
const loading = ref(false);

// 检查用户是否有权限查看答案
const checkPermission = async () => {
  loading.value = true;
  try {
    const response =
      await QuestionAnswerControllerService.checkAnswerPermissionUsingGet(
        props.questionId
      );

    if (response.code === 0) {
      hasPermission.value = response.data || false;
    } else {
      hasPermission.value = false;
      Message.error(response.message || "权限检查失败");
    }
  } catch (error) {
    console.error("权限检查失败:", error);
    Message.error("权限检查失败");
  } finally {
    loading.value = false;
  }
};

// 加载答案
const loadAnswer = async () => {
  if (!hasPermission.value) return;

  loading.value = true;

  try {
    const response =
      await QuestionAnswerControllerService.getQuestionAnswerUsingGet(
        props.questionId
      );

    if (response.code === 0 && response.data) {
      answer.value = response.data;
    } else {
      answer.value = "暂无答案";
      Message.error(response.message || "获取答案失败");
    }
  } catch (error) {
    console.error("获取答案失败:", error);
    Message.error("获取答案失败");
  } finally {
    loading.value = false;
  }
};

// 初始化
onMounted(async () => {
  await checkPermission();
  if (hasPermission.value) {
    await loadAnswer();
  }
});
</script>

<style scoped>
.question-answer {
  padding: 16px;
}
</style>
