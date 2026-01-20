<template>
  <div id="addQuestionView">
    <a-row :gutter="[24, 24]" class="container">
      <a-col :span="24">
        <div class="page-header">
          <h2 class="page-title">创建题目</h2>
          <p class="page-description">
            在此页面创建新的编程题目，包括题目内容、答案和测试用例
          </p>
        </div>
        <a-card class="form-card" :bordered="false">
          <a-form :model="form" label-align="left" layout="vertical">
            <div class="form-section">
              <h3 class="section-title">基本信息</h3>
              <a-form-item field="title" label="标题">
                <a-input
                  v-model="form.title"
                  placeholder="请输入标题"
                  size="large"
                />
              </a-form-item>
              <a-form-item field="tags" label="标签">
                <TagSelect
                  v-model="form.tags"
                  category="题目"
                  placeholder="请选择标签"
                />
              </a-form-item>
            </div>
            <div class="form-section">
              <h3 class="section-title">题目详情</h3>
              <a-form-item field="content" label="题目内容">
                <MdEditor
                  :value="form.content"
                  :handle-change="onContentChange"
                />
              </a-form-item>
              <a-form-item field="answer" label="答案">
                <MdEditor
                  :value="form.answer"
                  :handle-change="onAnswerChange"
                />
              </a-form-item>
            </div>
            <div class="form-section">
              <h3 class="section-title">判题配置</h3>
              <a-form-item :content-flex="false" :merge-props="false">
                <a-row :gutter="[16, 16]">
                  <a-col :span="8">
                    <a-form-item field="judgeConfig.timeLimit" label="时间限制">
                      <a-input-number
                        v-model="form.judgeConfig.timeLimit"
                        placeholder="请输入时间限制"
                        mode="button"
                        min="0"
                        size="large"
                        class="full-width"
                      />
                    </a-form-item>
                  </a-col>
                  <a-col :span="8">
                    <a-form-item
                      field="judgeConfig.memoryLimit"
                      label="内存限制"
                    >
                      <a-input-number
                        v-model="form.judgeConfig.memoryLimit"
                        placeholder="请输入内存限制"
                        mode="button"
                        min="0"
                        size="large"
                        class="full-width"
                      />
                    </a-form-item>
                  </a-col>
                  <a-col :span="8">
                    <a-form-item
                      field="judgeConfig.stackLimit"
                      label="堆栈限制"
                    >
                      <a-input-number
                        v-model="form.judgeConfig.stackLimit"
                        placeholder="请输入堆栈限制"
                        mode="button"
                        min="0"
                        size="large"
                        class="full-width"
                      />
                    </a-form-item>
                  </a-col>
                </a-row>
              </a-form-item>
            </div>

            <div class="form-section">
              <h3 class="section-title">测试用例配置</h3>
              <a-form-item :content-flex="false" :merge-props="false">
                <div class="test-cases-container">
                  <a-form-item
                    v-for="(judgeCaseItem, index) of form.judgeCase"
                    :key="index"
                    no-style
                  >
                    <div class="test-case-card">
                      <div class="test-case-header">
                        <span class="test-case-number"
                          >测试用例 #{{ index + 1 }}</span
                        >
                        <a-button
                          status="danger"
                          @click="handleDelete(index)"
                          shape="circle"
                          size="small"
                          class="delete-button"
                        >
                          <template #icon><icon-delete /></template>
                        </a-button>
                      </div>
                      <a-space direction="vertical" style="width: 100%">
                        <a-form-item
                          :field="`form.judgeCase[${index}].input`"
                          label="输入"
                          :key="index"
                        >
                          <a-textarea
                            v-model="judgeCaseItem.input"
                            placeholder="请输入测试输入用例"
                            allow-clear
                            :auto-size="{ minRows: 3, maxRows: 6 }"
                          />
                        </a-form-item>
                        <a-form-item
                          :field="`form.judgeCase[${index}].output`"
                          label="期望输出"
                          :key="index"
                        >
                          <a-textarea
                            v-model="judgeCaseItem.output"
                            placeholder="请输入测试输出用例"
                            allow-clear
                            :auto-size="{ minRows: 3, maxRows: 6 }"
                          />
                        </a-form-item>
                      </a-space>
                    </div>
                  </a-form-item>
                </div>
                <div class="action-button-container">
                  <a-button
                    @click="handleAdd"
                    type="outline"
                    status="success"
                    size="large"
                    class="add-case-button"
                  >
                    <template #icon><icon-plus /></template>
                    新增测试用例
                  </a-button>
                </div>
              </a-form-item>
            </div>

            <div class="form-footer">
              <a-form-item>
                <a-button
                  type="primary"
                  size="large"
                  class="submit-button"
                  @click="doSubmit"
                >
                  提交
                </a-button>
              </a-form-item>
            </div>
          </a-form>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { IconPlus, IconDelete } from "@arco-design/web-vue/es/icon";
import MdEditor from "@/components/MdEditor.vue";
import TagSelect from "@/components/TagSelect.vue";
import { QuestionControllerService } from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();
// 如果页面地址包含 update，视为更新页面
const updatePage = route.path.includes("update");

let form = ref({
  title: "",
  tags: [],
  answer: "",
  content: "",
  judgeConfig: {
    memoryLimit: 1000,
    stackLimit: 1000,
    timeLimit: 1000,
  },
  judgeCase: [
    {
      input: "",
      output: "",
    },
  ],
});

/**
 * 根据题目 id 获取老的数据
 */
const loadData = async () => {
  const id = route.query.id;
  if (!id) {
    return;
  }
  const res = await QuestionControllerService.getQuestionByIdUsingGet(
    id as any
  );
  if (res.code === 0) {
    form.value = res.data as any;
    // json 转 js 对象
    if (!form.value.judgeCase) {
      form.value.judgeCase = [
        {
          input: "",
          output: "",
        },
      ];
    } else {
      form.value.judgeCase = JSON.parse(form.value.judgeCase as any);
    }
    if (!form.value.judgeConfig) {
      form.value.judgeConfig = {
        memoryLimit: 1000,
        stackLimit: 1000,
        timeLimit: 1000,
      };
    } else {
      form.value.judgeConfig = JSON.parse(form.value.judgeConfig as any);
    }
    if (!form.value.tags) {
      form.value.tags = [];
    } else {
      form.value.tags = JSON.parse(form.value.tags as any);
    }
  } else {
    message.error("加载失败，" + res.message);
  }
};

onMounted(() => {
  loadData();
});

const doSubmit = async () => {
  console.log(form.value);
  // 区分更新还是创建
  if (updatePage) {
    const res = await QuestionControllerService.updateQuestionUsingPost(
      form.value
    );
    if (res.code === 0) {
      message.success("更新成功");
      // 更新成功后跳转到题目列表页
      router.push("/questions");
    } else {
      message.error("更新失败，" + res.message);
    }
  } else {
    const res = await QuestionControllerService.addQuestionUsingPost(
      form.value
    );
    if (res.code === 0) {
      message.success("创建成功");
      // 创建成功后跳转到题目列表页
      router.push("/questions");
    } else {
      message.error("创建失败，" + res.message);
    }
  }
};

/**
 * 新增判题用例
 */
const handleAdd = () => {
  form.value.judgeCase.push({
    input: "",
    output: "",
  });
};

/**
 * 删除判题用例
 */
const handleDelete = (index: number) => {
  form.value.judgeCase.splice(index, 1);
};

const onContentChange = (value: string) => {
  form.value.content = value;
};

const onAnswerChange = (value: string) => {
  form.value.answer = value;
};
</script>

<style scoped>
#addQuestionView {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  background-color: var(--bg-color-secondary);
  min-height: 100vh;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 32px;
  position: relative;
}

.page-header::after {
  content: "";
  position: absolute;
  bottom: -16px;
  left: 0;
  width: 100px;
  height: 4px;
  background: linear-gradient(
    90deg,
    var(--primary-color),
    var(--success-color)
  );
  border-radius: 2px;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: var(--text-color-primary);
  margin-bottom: 12px;
  letter-spacing: -0.5px;
}

.page-description {
  color: var(--text-color-regular);
  font-size: 16px;
  margin: 0;
  line-height: 1.5;
}

.form-card {
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  margin-bottom: 32px;
  transition: all 0.3s ease;
}

.form-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.form-section {
  margin-bottom: 40px;
  padding-bottom: 32px;
  border-bottom: 1px solid var(--border-color);
  transition: all 0.3s ease;
}

.form-section:hover {
  background-color: color-mix(in srgb, var(--primary-color) 2%, transparent);
}

.form-section:last-child {
  border-bottom: none;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-color-primary);
  margin-bottom: 24px;
  padding-left: 12px;
  border-left: 4px solid var(--primary-color);
  display: flex;
  align-items: center;
}

.section-title::before {
  content: "";
  display: inline-block;
  width: 8px;
  height: 8px;
  margin-right: 8px;
  background-color: var(--primary-color);
  border-radius: 50%;
}

.full-width {
  width: 100%;
}

:deep(.arco-input-size-large) {
  height: 48px;
  border-radius: 8px;
}

:deep(.arco-input-number-mode-button.arco-input-number-size-large) {
  height: 48px;
  border-radius: 8px;
}

:deep(.arco-form-item-label) {
  font-weight: 500;
  color: var(--text-color-primary);
}

.action-button-container {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}

:deep(.arco-btn) {
  border-radius: 8px;
  transition: all 0.3s ease;
}

:deep(.arco-btn:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.form-footer {
  display: flex;
  justify-content: center;
  margin-top: 40px;
  padding-top: 32px;
  border-top: 1px solid var(--border-color);
}

.submit-button {
  min-width: 220px;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 1px;
  border-radius: 8px;
  background: linear-gradient(
    90deg,
    var(--primary-color),
    var(--success-color)
  );
  border: none;
  transition: all 0.3s ease;
}

.submit-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px
    color-mix(in srgb, var(--primary-color) 30%, transparent);
}

.test-cases-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
  margin-bottom: 24px;
}

.test-case-card {
  background-color: var(--bg-color);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
}

.test-case-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  border-color: var(--text-color-placeholder);
}

.test-case-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px dashed var(--border-color);
}

.test-case-number {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-color-primary);
  display: flex;
  align-items: center;
}

.test-case-number::before {
  content: "";
  display: inline-block;
  width: 8px;
  height: 8px;
  margin-right: 8px;
  background-color: var(--primary-color);
  border-radius: 50%;
}

.delete-button {
  transition: all 0.3s ease;
}

.delete-button:hover {
  transform: rotate(90deg);
  background-color: var(--danger-color);
  color: white;
}

.add-case-button {
  min-width: 180px;
  transition: all 0.3s ease;
}

.add-case-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px
    color-mix(in srgb, var(--success-color) 20%, transparent);
}

@media (max-width: 768px) {
  .container {
    padding: 0 12px;
  }

  .page-title {
    font-size: 24px;
  }

  .page-description {
    font-size: 14px;
  }

  .section-title {
    font-size: 18px;
  }

  .submit-button {
    min-width: 180px;
  }
}
</style>
