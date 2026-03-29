<template>
  <div id="viewQuestionView">
    <a-row :gutter="[24, 24]">
      <a-col :md="12" :xs="24">
        <a-tabs default-active-key="question">
          <a-tab-pane key="question" title="题目">
            <a-card v-if="question" :title="question.title">
              <a-descriptions
                title="判题条件"
                :column="{ xs: 1, md: 2, lg: 3 }"
              >
                <a-descriptions-item label="时间限制">
                  {{ question.judgeConfig.timeLimit ?? 0 }}
                </a-descriptions-item>
                <a-descriptions-item label="内存限制">
                  {{ question.judgeConfig.memoryLimit ?? 0 }}
                </a-descriptions-item>
                <a-descriptions-item label="堆栈限制">
                  {{ question.judgeConfig.stackLimit ?? 0 }}
                </a-descriptions-item>
              </a-descriptions>
              <MdViewer :value="question.content || ''" />
              <template #extra>
                <a-space wrap>
                  <a-tag
                    v-for="(tag, index) of question.tags"
                    :key="index"
                    color="green"
                    >{{ tag }}
                  </a-tag>
                </a-space>
              </template>
            </a-card>
          </a-tab-pane>
          <a-tab-pane key="comment" title="评论">
            <QuestionComment :question-id="id" />
          </a-tab-pane>
          <a-tab-pane key="answer" title="答案">
            <QuestionAnswer :question-id="id" />
          </a-tab-pane>
        </a-tabs>
      </a-col>
      <a-col :md="12" :xs="24">
        <a-form :model="form" layout="inline" class="submit-form">
          <a-form-item field="language" label="编程语言">
            <a-select
              v-model="form.language"
              placeholder="选择编程语言"
              class="language-select"
            >
              <a-option>java</a-option>
              <a-option>c</a-option>
              <a-option>cpp</a-option>
              <a-option>go</a-option>
              <a-option>python</a-option>
              <a-option>ruby</a-option>
              <a-option>csharp</a-option>
              <a-option>javascript</a-option>
              <a-option>swift</a-option>
            </a-select>
          </a-form-item>
        </a-form>
        <CodeEditor
          :value="form.code || ''"
          :language="form.language"
          :handle-change="changeCode"
        />
        <a-divider size="0" />
        <div class="submit-actions">
          <a-button type="primary" @click="doSubmit"> 提交代码 </a-button>
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watchEffect, withDefaults } from "vue";
import { useRouter } from "vue-router";
import message from "@arco-design/web-vue/es/message";
import CodeEditor from "@/components/CodeEditor.vue";
import MdViewer from "@/components/MdViewer.vue";
import QuestionComment from "@/components/QuestionComment.vue";
import QuestionAnswer from "@/components/QuestionAnswer.vue";

const router = useRouter();
import {
  QuestionControllerService,
  QuestionSubmitAddRequest,
  QuestionVO,
} from "../../../generated";

interface Props {
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  id: () => "",
});

const question = ref<QuestionVO>();

const loadData = async () => {
  const res = await QuestionControllerService.getQuestionVoByIdUsingGet(
    props.id as any
  );
  if (res.code === 0) {
    question.value = res.data;
  } else {
    message.error("加载失败，" + res.message);
  }
};

const form = ref<QuestionSubmitAddRequest>({
  language: "java",
  code: 'public class Main {\n    public static void main(String[] args) {\n        System.out.println("Hello, World!");\n    }\n}',
});

/**
 * 提交代码
 */
const doSubmit = async () => {
  if (!question.value?.id) {
    return;
  }

  const res = await QuestionControllerService.doQuestionSubmitUsingPost({
    ...form.value,
    questionId: question.value.id,
  });
  if (res.code === 0) {
    message.success("提交成功");
    // 提交成功后跳转到题目提交页面
    router.push(`/question_submit`);
  } else {
    message.error("提交失败," + res.message);
  }
};

/**
 * 页面加载时，请求数据
 */
onMounted(() => {
  loadData();
});

const changeCode = (value: string) => {
  form.value.code = value;
};
</script>

<style>
#viewQuestionView {
  max-width: 1400px;
  margin: 0 auto;
  padding: var(--spacing-md);
}

#viewQuestionView .arco-space-horizontal .arco-space-item {
  margin-bottom: 0 !important;
}

/* 表单和语言选择器 */
.submit-form {
  margin-bottom: var(--spacing-md);
}

.language-select {
  width: 200px;
}

/* 提交按钮区域 */
.submit-actions {
  display: flex;
  justify-content: center;
  padding: var(--spacing-md) 0;
}

.submit-actions .arco-btn {
  min-width: 150px;
}

/* 移动端适配 */
@media (max-width: 768px) {
  #viewQuestionView {
    padding: var(--spacing-sm);
  }

  .language-select {
    width: 100%;
  }

  .submit-form .arco-form-item {
    width: 100%;
  }

  .submit-actions .arco-btn {
    width: 100%;
  }
}
</style>
