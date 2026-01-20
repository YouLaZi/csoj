<template>
  <div class="batch-import-questions">
    <a-page-header title="批量导入题目" @back="() => $router.go(-1)">
      <template #extra>
        <a-button type="primary" @click="handleImport" :loading="loading">
          开始导入
        </a-button>
      </template>
    </a-page-header>

    <a-card>
      <a-form :model="formState" layout="vertical">
        <a-form-item
          label="题目文件 (支持.json格式)"
          name="fileList"
          :rules="[{ required: true, message: '请上传题目文件!' }]"
        >
          <a-upload
            v-model:file-list="formState.fileList"
            name="file"
            :before-upload="beforeUpload"
            @remove="handleRemove"
            accept=".json"
          >
            <a-button>
              <icon-upload />
              选择文件
            </a-button>
          </a-upload>
          <div
            v-if="formState.fileList && formState.fileList.length > 0"
            style="margin-top: 8px"
          >
            <p>文件预览 (最多显示前5条记录):</p>
            <a-textarea :value="filePreview" :rows="10" read-only />
          </div>
        </a-form-item>

        <a-form-item label="导入说明">
          <p>
            请上传JSON格式的文件，文件内容应为一个包含题目对象的数组。每个题目对象应包含以下字段：
          </p>
          <ul>
            <li><code>title</code>: 题目名称 (字符串, 必填)</li>
            <li><code>content</code>: 题目描述 (字符串, 必填, Markdown格式)</li>
            <li>
              <code>tags</code>: 题目标签 (字符串数组, 可选, 例如: ["数组",
              "动态规划"])
            </li>
            <li><code>answer</code>: 题目答案 (字符串, 可选, Markdown格式)</li>
            <li>
              <code>judgeConfig</code>: 判题配置 (对象, 必填, 包含
              <code>timeLimit</code> 和 <code>memoryLimit</code>, 单位为ms和MB)
            </li>
            <li>
              <code>judgeCase</code>: 判题用例 (对象数组, 必填, 每个对象包含
              <code>input</code> 和 <code>output</code>)
            </li>
          </ul>
          <p>示例:</p>
          <pre>
[
  {
    "title": "A+B Problem",
    "content": "计算两个整数的和。",
    "tags": ["入门"],
    "answer": "无",
    "judgeConfig": {
      "timeLimit": 1000,
      "memoryLimit": 256
    },
    "judgeCase": [
      {
        "input": "1 2",
        "output": "3"
      },
      {
        "input": "10 20",
        "output": "30"
      }
    ]
  }
  // ...更多题目
]
          </pre>
        </a-form-item>
      </a-form>
    </a-card>

    <a-modal
      v-model:visible="importResultVisible"
      title="导入结果"
      @ok="importResultVisible = false"
      :footer="null"
      width="80%"
    >
      <a-alert
        v-if="importSummary.totalCount > 0"
        type="info"
        style="margin-bottom: 16px"
      >
        <template #message>
          共处理 {{ importSummary.totalCount }} 条题目，成功导入
          {{ importSummary.successCount }} 条，失败
          {{ importSummary.failCount }} 条。
        </template>
      </a-alert>
      <a-table
        :columns="resultColumns"
        :data-source="importResults"
        row-key="index"
        :pagination="{ pageSize: 10 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.success ? 'green' : 'red'">
              {{ record.success ? "成功" : "失败" }}
            </a-tag>
          </template>
          <template v-if="column.key === 'title'">
            {{ record.question?.title || "N/A" }}
          </template>
        </template>
      </a-table>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import { IconUpload } from "@arco-design/web-vue/es/icon";
import { Message, Modal } from "@arco-design/web-vue";
import type { UploadProps, UploadFile } from "ant-design-vue";
import { QuestionControllerService } from "../../../../generated";
import type {
  QuestionAddRequest,
  QuestionBatchAddRequest,
} from "../../../../generated";

interface FormState {
  fileList: UploadFile[];
}

const formState = reactive<FormState>({
  fileList: [],
});

const loading = ref(false);
const filePreview = ref("");
const importResultVisible = ref(false);
const importResults = ref<any[]>([]);
const importSummary = reactive({
  totalCount: 0,
  successCount: 0,
  failCount: 0,
});

const resultColumns = [
  {
    title: "序号",
    dataIndex: "index",
    key: "index",
    customRender: ({ index }: { index: number }) => index + 1,
  },
  { title: "题目名称", dataIndex: "title", key: "title" },
  { title: "状态", key: "status", width: 100 },
  { title: "信息", dataIndex: "message", key: "message" },
];

const beforeUpload: UploadProps["beforeUpload"] = (file) => {
  formState.fileList = [file as UploadFile];
  const reader = new FileReader();
  reader.onload = (e) => {
    try {
      const content = e.target?.result as string;
      const jsonData = JSON.parse(content);
      filePreview.value = JSON.stringify(jsonData.slice(0, 5), null, 2); // 预览前5条
    } catch (error) {
      Message.error("文件格式错误，请确保是有效的JSON文件");
      filePreview.value = "无法预览文件内容，请检查文件格式。";
    }
  };
  reader.readAsText(file);
  return false; // 阻止自动上传
};

const handleRemove = () => {
  formState.fileList = [];
  filePreview.value = "";
};

const validateQuestion = (question: any): question is QuestionAddRequest => {
  return (
    question &&
    typeof question.title === "string" &&
    typeof question.content === "string" &&
    question.judgeConfig &&
    typeof question.judgeConfig.timeLimit === "number" &&
    typeof question.judgeConfig.memoryLimit === "number" &&
    Array.isArray(question.judgeCase) &&
    question.judgeCase.every(
      (c: any) => typeof c.input === "string" && typeof c.output === "string"
    ) &&
    (question.tags === undefined ||
      (Array.isArray(question.tags) &&
        question.tags.every((t: any) => typeof t === "string"))) &&
    (question.answer === undefined || typeof question.answer === "string")
  );
};

const handleImport = async () => {
  if (formState.fileList.length === 0) {
    Message.error("请先选择要导入的题目文件！");
    return;
  }

  loading.value = true;
  importResults.value = [];
  importSummary.totalCount = 0;
  importSummary.successCount = 0;
  importSummary.failCount = 0;

  const file = formState.fileList[0].originFileObj as File;
  const reader = new FileReader();

  reader.onload = async (e) => {
    try {
      const content = e.target?.result as string;
      const questionsToImport = JSON.parse(content);

      if (!Array.isArray(questionsToImport)) {
        Message.error("文件内容格式错误，应为一个题目对象数组。");
        loading.value = false;
        return;
      }

      importSummary.totalCount = questionsToImport.length;

      // 验证所有题目数据格式
      const validQuestions: QuestionAddRequest[] = [];
      const invalidQuestions: any[] = [];

      for (let i = 0; i < questionsToImport.length; i++) {
        const questionData = questionsToImport[i];
        if (validateQuestion(questionData)) {
          validQuestions.push(questionData as QuestionAddRequest);
        } else {
          invalidQuestions.push({
            index: i,
            question: questionData,
            success: false,
            message: "题目数据格式不符合要求，请检查。",
          });
        }
      }

      // 先添加无效题目到结果列表
      importResults.value = [...invalidQuestions];
      importSummary.failCount = invalidQuestions.length;

      // 如果有有效题目，则调用批量导入接口
      if (validQuestions.length > 0) {
        try {
          // 使用批量导入接口
          const batchRequest: QuestionBatchAddRequest = {
            questionList: validQuestions,
          };

          const res = await QuestionControllerService.batchAddQuestionUsingPost(
            batchRequest
          );

          if (res.code === 0 && res.data) {
            // 处理批量导入结果
            const successList = res.data.successList || [];
            const failList = res.data.failList || [];

            // 更新导入统计信息
            importSummary.successCount = res.data.successCount || 0;
            importSummary.failCount =
              invalidQuestions.length + (res.data.failCount || 0);

            // 处理成功导入的题目
            for (const result of successList) {
              const questionIndex =
                result.index !== undefined ? result.index : -1;
              const question =
                questionIndex >= 0 && questionIndex < validQuestions.length
                  ? validQuestions[questionIndex]
                  : { title: result.title || "未知题目" };

              importResults.value.push({
                index: invalidQuestions.length + importResults.value.length,
                question: question,
                success: true,
                message: `导入成功，题目ID: ${result.questionId}`,
              });
            }

            // 处理导入失败的题目
            for (const result of failList) {
              const questionIndex =
                result.index !== undefined ? result.index : -1;
              const question =
                questionIndex >= 0 && questionIndex < validQuestions.length
                  ? validQuestions[questionIndex]
                  : { title: result.title || "未知题目" };

              importResults.value.push({
                index: invalidQuestions.length + importResults.value.length,
                question: question,
                success: false,
                message: result.message || "导入失败",
              });
            }
          } else {
            // 如果批量导入整体失败，将所有有效题目标记为失败
            for (let i = 0; i < validQuestions.length; i++) {
              importResults.value.push({
                index: invalidQuestions.length + i,
                question: validQuestions[i],
                success: false,
                message: res.message || "批量导入失败",
              });
            }
            importSummary.failCount += validQuestions.length;
          }
        } catch (apiError: any) {
          // 处理API异常
          for (let i = 0; i < validQuestions.length; i++) {
            importResults.value.push({
              index: invalidQuestions.length + i,
              question: validQuestions[i],
              success: false,
              message: apiError.message || "批量导入API请求失败",
            });
          }
          importSummary.failCount += validQuestions.length;
        }
      }
    } catch (parseError) {
      Message.error("解析JSON文件失败，请检查文件内容和格式。");
      console.error("JSON Parse Error:", parseError);
    } finally {
      loading.value = false;
      importResultVisible.value = true;
    }

    reader.onerror = () => {
      Message.error("读取文件失败！");
      loading.value = false;
    };

    reader.readAsText(file);
  };
};
</script>

<style scoped>
.batch-import-questions {
  margin: 20px;
}

pre {
  background-color: var(--color-fill-1);
  padding: 10px;
  border-radius: 4px;
  white-space: pre-wrap; /* Preserve newlines and spaces */
  word-wrap: break-word; /* Break long words */
}
</style>
