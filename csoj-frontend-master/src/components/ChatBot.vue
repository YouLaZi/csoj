<template>
  <div
    v-show="visible"
    class="chat-bot-container"
    :class="{ minimized: minimized }"
    :style="{
      left: position.x + 'px',
      top: position.y + 'px',
      width: currentSize.width + 'px',
    }"
    @mousedown="startDrag"
  >
    <div class="chat-bot-header">
      <div class="chat-bot-title">
        <icon-robot class="float-button-icon" />
        <span>{{ t("chatbot.title") }}</span>
      </div>
      <div class="chat-bot-actions">
        <a-button type="text" size="mini" @click="toggleMinimize">
          <icon-minus v-if="!minimized" />
          <icon-expand v-else />
        </a-button>
        <a-button type="text" size="mini" @click="hideBot">
          <icon-close />
        </a-button>
      </div>
    </div>
    <div
      v-show="!minimized"
      class="chat-bot-body"
      :style="{ height: currentSize.height + 'px' }"
    >
      <div class="chat-messages" ref="messagesContainer">
        <div
          v-for="(message, index) in messages"
          :key="index"
          class="message"
          :class="message.type"
        >
          <div class="avatar" v-if="message.type === 'bot'">
            <icon-robot />
          </div>
          <div class="avatar user-avatar" v-if="message.type === 'user'">
            <icon-user />
          </div>
          <div class="message-content">
            <!-- 根据内容类型显示不同的渲染方�?-->
            <div v-if="message.contentType === 'code'" class="code-block">
              <div class="code-header">
                <span>{{ message.language || "java" }}</span>
                <a-button
                  type="text"
                  size="mini"
                  @click="copyCode(message.content)"
                >
                  <icon-copy />
                </a-button>
              </div>
              <pre><code>{{ message.content }}</code></pre>
              <!-- 代码分析结果 -->
              <div v-if="message.codeAnalysis" class="code-analysis">
                <div
                  v-if="
                    message.codeAnalysis.errors &&
                    message.codeAnalysis.errors.length > 0
                  "
                  class="errors"
                >
                  <div class="analysis-title">
                    {{ t("chatbot.errorAnalysis") }}
                  </div>
                  <ul>
                    <li
                      v-for="(error, idx) in message.codeAnalysis.errors"
                      :key="idx"
                    >
                      {{ error }}
                    </li>
                  </ul>
                </div>
                <div
                  v-if="
                    message.codeAnalysis.suggestions &&
                    message.codeAnalysis.suggestions.length > 0
                  "
                  class="suggestions"
                >
                  <div class="analysis-title">
                    {{ t("chatbot.suggestions") }}
                  </div>
                  <ul>
                    <li
                      v-for="(suggestion, idx) in message.codeAnalysis
                        .suggestions"
                      :key="idx"
                    >
                      {{ suggestion }}
                    </li>
                  </ul>
                </div>
              </div>
            </div>
            <div v-else-if="message.contentType === 'math'" class="math-block">
              <!-- 数学公式渲染 -->
              <div>{{ message.content }}</div>
            </div>
            <div v-else v-html="formatMessage(message.content)"></div>
          </div>
        </div>
      </div>
      <div class="chat-toolbar">
        <a-button
          v-for="(button, index) in toolbarButtons"
          :key="index"
          type="text"
          size="small"
          @click="button.action"
        >
          <component :is="button.icon" />
          <span class="toolbar-text">{{ button.text }}</span>
        </a-button>
      </div>

      <div class="chat-input">
        <a-input
          v-model="inputMessage"
          :placeholder="t('chatbot.inputPlaceholder')"
          allow-clear
          @keyup.enter="sendMessage"
        >
          <template #append>
            <a-button type="primary" @click="sendMessage">
              <icon-send />
            </a-button>
          </template>
        </a-input>
      </div>

      <!-- 学习进度面板 -->
      <div class="learning-progress" v-if="!minimized">
        <div class="progress-header">{{ t("chatbot.learningProgress") }}</div>
        <div class="progress-stats">
          <div class="progress-item">
            <div class="progress-label">{{ t("chatbot.solvedProblems") }}</div>
            <div class="progress-value">
              {{ learningProgress.solvedProblems }}/
              {{ learningProgress.totalProblems }}
            </div>
          </div>
          <div class="progress-item">
            <div class="progress-label">{{ t("chatbot.recentTopics") }}</div>
            <div class="progress-tags">
              <a-tag
                v-for="(topic, index) in learningProgress.recentTopics"
                :key="index"
                size="small"
                >{{ topic }}</a-tag
              >
            </div>
          </div>
          <div class="progress-item">
            <div class="progress-label">
              {{ t("chatbot.recommendedTopics") }}
            </div>
            <div class="progress-tags">
              <a-tag
                v-for="(topic, index) in learningProgress.recommendedTopics"
                :key="index"
                size="small"
                color="arcoblue"
                >{{ topic }}</a-tag
              >
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 调整大小的手�?-->
    <div v-show="!minimized" class="resize-handle" @mousedown="startResize">
      <icon-drag-dot-vertical />
    </div>
  </div>

  <!-- 可爱吉祥物悬浮按钮，当聊天机器人隐藏时显�?-->
  <div v-if="!visible" class="chat-bot-mascot-container" @click="showBot">
    <CuteMascot
      ref="mascotRef"
      :mood="mascotMood"
      @click="showBot"
      @moodChange="onMascotMoodChange"
      @askExplain="onAskExplain"
      @askQuestion="onAskQuestion"
    />
    <div class="mascot-hint">
      <span>点击我聊�?</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick, watch } from "vue";
import { Message as ArcoMessage } from "@arco-design/web-vue";
import { useI18n } from "vue-i18n";
import {
  IconRobot,
  IconMinus,
  IconExpand,
  IconClose,
  IconSend,
  IconCopy,
  IconCode,
  IconBook,
  IconBulb,
  IconDragDotVertical,
  IconUser,
} from "@arco-design/web-vue/es/icon";
import { useRoute } from "vue-router";
import QuestionService from "@/services/QuestionService";
import ChatBotService from "@/services/ChatBotService";
import type { ChatMessage, LearningProgress, Question } from "@/types/chat";
import { useUserStore } from "@/store/useUserStore";
import CuteMascot from "@/components/CuteMascot.vue";
import {
  selectionAssistant,
  type SelectionInfo,
} from "@/utils/selectionAssistant";
import { mascotBehavior, type MascotAction } from "@/utils/mascotBehavior";

const { t } = useI18n();

const visible = ref(false);
const minimized = ref(false);
const inputMessage = ref("");
const messagesContainer = ref<HTMLElement | null>(null);
const userStore = useUserStore();

// 吉祥物相关状�?const mascotRef = ref<InstanceType<typeof CuteMascot> | null>(null);
const mascotMood = ref<"happy" | "sad" | "thinking" | "sleeping" | "excited">(
  "happy"
);

// 吉祥物心情变化处�?const onMascotMoodChange = (mood: string) => {
  console.log("Mascot mood changed to:", mood);
};

// 处理选中文本事件
const onSelectionChange = (info: SelectionInfo) => {
  if (mascotRef.value) {
    mascotRef.value.showAskBubbleForSelection(info.text, info.type);
  }
  // 更新行为控制器的上下�?  mascotBehavior.setSelectedText({
    content: info.text,
    type: info.type,
  });
};

// 处理选择清除
const onSelectionCleared = () => {
  if (mascotRef.value) {
    mascotRef.value.hideAskBubble();
  }
  mascotBehavior.clearSelectedText();
};

// 处理"解释这个"点击
const onAskExplain = (text: string, type: string) => {
  showBot();
  // 根据类型构建不同的提�?  let prompt = "";
  if (type === "code") {
    prompt = `请帮我解释这段代码：\n\`\`\`\n${text}\n\`\`\``;
  } else if (type === "error") {
    prompt = `请帮我分析这个错误并提供解决方案：\n${text}`;
  } else if (type === "math") {
    prompt = `请帮我解释这个数学内容：\n${text}`;
  } else {
    prompt = `请帮我解释这段内容：\n${text}`;
  }
  inputMessage.value = prompt;
  // 自动发�?  setTimeout(() => {
    sendMessage();
  }, 500);
};

// 处理"提问"点击
const onAskQuestion = (text: string, type: string) => {
  showBot();
  // 预填充输入框，但不自动发�?  inputMessage.value = `关于"${text.substring(0, 50)}${
    text.length > 50 ? "..." : ""
  }"，我想问：`;
};

// 处理行为控制器触发的动作
const onMascotAction = (action: MascotAction, ruleId: string) => {
  if (mascotRef.value) {
    mascotRef.value.setMood(action.mood);
    if (action.message) {
      mascotRef.value.showBubbleMessage(
        action.message,
        action.duration || 3000
      );
    }
    if (action.particles) {
      mascotRef.value.triggerParticles(action.particles);
    }
  }
};

const position = reactive({
  x: window.innerWidth - 380,
  y: 100,
});

const messages = ref<ChatMessage[]>([]);

// 当前题目信息
const currentQuestion = ref<Question | null>(null);
// 题目信息缓存
const questionCache = new Map<string, Question>();
const route = useRoute();

// 算法知识�?const algorithmKnowledge = {
  排序: ["冒泡排序", "快速排�?, "归并排序", "堆排�?, "计数排序"],
  搜索: ["二分查找", "深度优先搜索", "广度优先搜索", "A*算法"],
  数据结构: ["数组", "链表", "�?, "队列", "�?, "�?, "�?, "哈希�?],
  动态规�? ["背包问题", "最长公共子序列", "最短路�?],
  贪心算法: ["活动选择问题", "哈夫曼编�?, "最小生成树"],
};

// 格式化消息内容，支持简单的Markdown语法
function formatMessage(content: string) {
  if (!content) return "";

  // 处理代码�?  content = content.replace(
    /```([\w]*)[\n\r]([\s\S]*?)```/g,
    '<div class="inline-code-block"><div class="code-header">$1</div><pre><code>$2</code></pre></div>'
  );

  // 处理行内代码
  content = content.replace(
    /`([^`]+)`/g,
    '<code class="inline-code">$1</code>'
  );

  // 处理粗体
  content = content.replace(/\*\*([^*]+)\*\*/g, "<strong>$1</strong>");

  // 处理斜体
  content = content.replace(/\*([^*]+)\*/g, "<em>$1</em>");

  // 处理链接
  content = content.replace(
    /\[([^\]]+)\]\(([^)]+)\)/g,
    '<a href="$2" target="_blank">$1</a>'
  );

  // 处理换行
  content = content.replace(/\n/g, "<br>");

  return content;
}

// 复制代码到剪贴板
function copyCode(code: string) {
  navigator.clipboard
    .writeText(code)
    .then(() => {
      ArcoMessage.success(t("chatbot.codeCopied"));
    })
    .catch((err) => {
      console.error("复制失败:", err);
      ArcoMessage.error(t("chatbot.copyFailed"));
    });
}

// 加载聊天历史
async function loadChatHistory(questionId?: number) {
  try {
    // 检查用户是否已登录
    if (!userStore.token || !userStore.isLoggedIn) {
      // 用户未登录，显示提示消息
      messages.value = [
        {
          content: t("chatbot.welcomeGuest"),
          type: "bot",
          contentType: "text",
        },
      ];
      nextTick(() => {
        scrollToBottom();
      });
      return;
    }

    // 用户已登录，尝试获取聊天历史
    const history = await ChatBotService.getChatHistory(questionId);
    if (history && history.length > 0) {
      messages.value = history;
    } else {
      // 如果没有历史记录，或者特定问题的历史为空，则显示默认欢迎消息
      messages.value = [
        {
          content: t("chatbot.welcome"),
          type: "bot",
          contentType: "text",
        },
      ];
    }
    // 确保在消息更新后滚动到底�?    nextTick(() => {
      scrollToBottom();
    });
  } catch (error) {
    console.error("加载聊天历史失败:", error);
    messages.value = [
      {
        content: t("chatbot.loadHistoryFailed"),
        type: "bot",
        contentType: "text",
      },
    ];
    nextTick(() => {
      scrollToBottom();
    });
  }
}

// 获取当前题目信息
async function fetchCurrentQuestionInfo() {
  try {
    // 从路由中获取题目ID
    const questionIdParam = route.params.id;
    if (questionIdParam) {
      const cacheKey = `question_${questionIdParam}`;

      // 检查缓�?      if (questionCache.has(cacheKey)) {
        console.log("从缓存获取题目信�?", questionIdParam);
        currentQuestion.value = questionCache.get(cacheKey)!;
        // 加载与此题目相关的聊天历�?        await loadChatHistory(currentQuestion.value.id);

        // 在加载完历史后，添加关于当前题目的提示信�?        messages.value.push({
          content: t("chatbot.questionContext", {
            title: currentQuestion.value.title,
          }),
          type: "bot",
          contentType: "text",
        });
        // 确保滚动到底�?        nextTick(() => {
          scrollToBottom();
        });
        return;
      }

      // 缓存中没有，从API获取
      console.log("从API获取题目信息:", questionIdParam);
      const result = await QuestionService.getQuestionDetail(
        Number(questionIdParam)
      );
      if (result.code === 0 && result.data) {
        // 存入缓存
        questionCache.set(cacheKey, result.data);
        currentQuestion.value = result.data;
        // 加载与此题目相关的聊天历�?        await loadChatHistory(currentQuestion.value.id);

        // 在加载完历史后，添加关于当前题目的提示信�?        messages.value.push({
          content: t("chatbot.questionContext", { title: result.data.title }),
          type: "bot",
          contentType: "text",
        });
        // 确保滚动到底�?        nextTick(() => {
          scrollToBottom();
        });
      }
    } else {
      // 如果没有 questionIdParam（例如，在非题目页面），确保 currentQuestion �?null
      // 并加载通用历史记录（如果尚未通过 onMounted �?watcher 加载�?      if (currentQuestion.value !== null) {
        currentQuestion.value = null; // 清除当前问题
        await loadChatHistory(); // 加载通用历史
      }
    }
  } catch (error) {
    console.error("获取题目信息失败:", error);
  }
}

// 拖拽相关变量和方�?let isDragging = false;
let dragOffset = { x: 0, y: 0 };

// 添加窗口大小调整相关变量
const isResizing = ref(false);
const initialSize = ref({ width: 380, height: 450 });
const currentSize = reactive({
  width: 380,
  height: 450,
});

function startDrag(event: MouseEvent) {
  // 如果点击的是按钮或调整大小区域，不进行拖�?  if (
    (event.target as HTMLElement).closest(".chat-bot-actions") ||
    (event.target as HTMLElement).closest(".resize-handle")
  ) {
    return;
  }

  isDragging = true;
  dragOffset.x = event.clientX - position.x;
  dragOffset.y = event.clientY - position.y;

  // 添加样式表明正在拖动
  document.body.style.cursor = "grabbing";

  document.addEventListener("mousemove", onDrag, { passive: true });
  document.addEventListener("mouseup", stopDrag);
}

function onDrag(event: MouseEvent) {
  if (isDragging) {
    // 使用requestAnimationFrame优化性能，减少迟滞感
    requestAnimationFrame(() => {
      position.x = event.clientX - dragOffset.x;
      position.y = event.clientY - dragOffset.y;

      // 确保不会拖出屏幕
      const maxX = window.innerWidth - 50;
      const maxY = window.innerHeight - 50;

      position.x = Math.max(0, Math.min(position.x, maxX));
      position.y = Math.max(0, Math.min(position.y, maxY));
    });
  }
}

function stopDrag() {
  isDragging = false;
  document.body.style.cursor = "";
  document.removeEventListener("mousemove", onDrag);
  document.removeEventListener("mouseup", stopDrag);
}

// 调整大小相关方法
function startResize(event: MouseEvent) {
  event.preventDefault();
  event.stopPropagation();

  isResizing.value = true;
  initialSize.value = {
    width: currentSize.width,
    height: currentSize.height,
  };

  const startX = event.clientX;
  const startY = event.clientY;

  document.body.style.cursor = "nwse-resize";

  const onResizeMove = (moveEvent: MouseEvent) => {
    if (isResizing.value) {
      requestAnimationFrame(() => {
        // 计算新的宽度和高�?        const newWidth = initialSize.value.width + (moveEvent.clientX - startX);
        const newHeight =
          initialSize.value.height + (moveEvent.clientY - startY);

        // 设置最小和最大尺寸限�?        currentSize.width = Math.max(
          300,
          Math.min(newWidth, window.innerWidth - position.x - 20)
        );
        currentSize.height = Math.max(
          300,
          Math.min(newHeight, window.innerHeight - position.y - 20)
        );
      });
    }
  };

  const stopResize = () => {
    isResizing.value = false;
    document.body.style.cursor = "";
    document.removeEventListener("mousemove", onResizeMove);
    document.removeEventListener("mouseup", stopResize);
  };

  document.addEventListener("mousemove", onResizeMove, { passive: true });
  document.addEventListener("mouseup", stopResize);
}

function toggleMinimize() {
  minimized.value = !minimized.value;
}

function hideBot() {
  visible.value = false;
}

function showBot() {
  visible.value = true;
}

// 滚动到底部的辅助函数
function scrollToBottom() {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
    }
  });
}

async function sendMessage() {
  if (inputMessage.value.trim() === "") return;

  // 添加用户消息
  messages.value.push({
    content: inputMessage.value,
    type: "user",
    contentType: "text",
  });

  const userQuestion = inputMessage.value;
  inputMessage.value = "";

  // 添加一个临时的思考消�?  const thinkingIndex = messages.value.length;
  messages.value.push({
    content: t("chatbot.thinking"),
    type: "bot",
    contentType: "text",
  });

  // 滚动到底�?  scrollToBottom();

  try {
    // 调用后端API发送消�?    const questionId = currentQuestion.value?.id;
    const botResponseData = await ChatBotService.sendChatMessage(
      userQuestion,
      questionId
    );

    // 检测回复内容是否包含代码块
    if (botResponseData.contentType === "code") {
      // 如果是代码类型，直接更新消息
      messages.value[thinkingIndex] = {
        content: botResponseData.content,
        type: "bot",
        contentType: "code",
        language: botResponseData.language || "java",
        codeAnalysis: botResponseData.codeAnalysis,
      };
    } else {
      // 普通文本回�?      messages.value[thinkingIndex] = {
        content: botResponseData.content,
        type: "bot",
        contentType: botResponseData.contentType || "text",
      };
    }

    // 如果有代码分析结果，添加到消息中
    if (botResponseData.codeAnalysis) {
      messages.value[thinkingIndex].codeAnalysis = botResponseData.codeAnalysis;
    }

    // 更新学习进度
    updateLearningProgress();
  } catch (error) {
    console.error("发送消息失�?", error);
    // 更新为错误消�?    messages.value[thinkingIndex] = {
      content: t("chatbot.networkError"),
      type: "bot",
      contentType: "text",
    };
  }

  // 滚动到底�?  scrollToBottom();
}

onMounted(async () => {
  // 初始化时显示聊天机器�?  visible.value = false;

  // 确保用户登录状态已更新
  try {
    // 尝试更新用户登录状�?    if (userStore.token) {
      await userStore.getLoginUser();
    }
  } catch (error) {
    console.error("更新用户登录状态失�?", error);
  }

  // 首先加载通用聊天历史或默认欢迎消�?  await loadChatHistory();

  // 然后获取当前题目信息（这可能会再次加载特定于题目的历史）
  await fetchCurrentQuestionInfo();

  // 初始化学习进度数�?  await updateLearningProgress();

  // 初始化选择助手
  selectionAssistant.start();
  selectionAssistant.onSelection = onSelectionChange;
  selectionAssistant.onSelectionCleared = onSelectionCleared;

  // 初始化行为控制器
  mascotBehavior.onAction(onMascotAction);

  // 更新用户状�?  mascotBehavior.updateUserState({
    isLoggedIn: userStore.isLoggedIn,
    todayCheckin: false, // 可以从其他地方获�?  });
});

onUnmounted(() => {
  // 清理选择助手
  selectionAssistant.stop();

  // 清理行为控制�?  mascotBehavior.destroy();
});

watch(
  () => route.params.id,
  async (newQuestionId, oldQuestionId) => {
    if (newQuestionId && newQuestionId !== oldQuestionId) {
      // 当题目ID变化时，重新获取题目信息并加载相应的聊天历史
      await fetchCurrentQuestionInfo();
    } else if (!newQuestionId && oldQuestionId) {
      // 如果从题目页面导航到没有题目ID的页面，则清空当前题目信息并加载通用历史
      currentQuestion.value = null;
      await loadChatHistory();
    }
  }
);

// 添加工具栏按�?const toolbarButtons = [
  {
    icon: IconCode,
    text: t("chatbot.codeAnalysis"),
    action: () => {
      inputMessage.value = t("chatbot.codeAnalysisPrompt");
    },
  },
  {
    icon: IconBook,
    text: t("chatbot.algorithmKnowledge"),
    action: () => {
      inputMessage.value = t("chatbot.algorithmPrompt");
    },
  },
  {
    icon: IconBulb,
    text: t("chatbot.solutionHints"),
    action: () => {
      inputMessage.value = t("chatbot.solutionPrompt");
    },
  },
];

// 学习进度跟踪
const learningProgress = reactive<LearningProgress>({
  solvedProblems: 0,
  totalProblems: 0,
  recentTopics: [],
  recommendedTopics: [],
});

// 更新学习进度
async function updateLearningProgress() {
  try {
    // 调用后端API获取用户的学习进�?    const progressData = await ChatBotService.getLearningProgress();

    // 更新学习进度数据
    learningProgress.solvedProblems = progressData.solvedProblems;
    learningProgress.totalProblems = progressData.totalProblems;
    learningProgress.recentTopics = progressData.recentTopics;
    learningProgress.recommendedTopics = progressData.recommendedTopics;
  } catch (error) {
    console.error("获取学习进度失败:", error);
    // 使用默认数据
    learningProgress.solvedProblems = 0;
    learningProgress.totalProblems = 0;
    learningProgress.recentTopics = [];
    learningProgress.recommendedTopics = [];
  }
}
</script>

<style scoped>
.chat-bot-container {
  position: fixed;
  width: 380px;
  background-color: var(--bg-color-secondary);
  border-radius: 20px;
  box-shadow: 0 16px 40px rgba(59, 130, 246, 0.15),
    0 8px 20px rgba(59, 130, 246, 0.08);
  display: flex;
  flex-direction: column;
  z-index: 1000;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Oxygen,
    Ubuntu, Cantarell, "Open Sans", "Helvetica Neue", sans-serif;
  backdrop-filter: blur(20px);
  border: 2px solid var(--border-color);
}

.chat-bot-container.minimized {
  height: 60px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.chat-bot-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: linear-gradient(135deg, #3b82f6, #60a5fa);
  color: #ffffff;
  cursor: move;
  border-top-left-radius: 20px;
  border-top-right-radius: 20px;
  user-select: none;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
  position: relative;
}

.chat-bot-header::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.2),
    transparent
  );
}

.chat-bot-title {
  display: flex;
  align-items: center;
  font-weight: 700;
  font-size: 17px;
  letter-spacing: 0.3px;
  color: #ffffff;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
}

.chat-bot-title span {
  margin-left: 12px;
}

.chat-bot-actions {
  display: flex;
  gap: 6px;
}

.chat-bot-actions .arco-btn {
  padding: 8px;
  border-radius: 10px;
  transition: all 0.2s ease;
  color: #ffffff;
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.chat-bot-actions .arco-btn:hover {
  background-color: rgba(255, 255, 255, 0.25);
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.chat-bot-body {
  display: flex;
  flex-direction: column;
  height: 450px;
  background-color: var(--bg-color-secondary);
  position: relative;
  overflow: hidden;
}

.chat-messages {
  flex: 1;
  padding: 12px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
  scroll-behavior: smooth;
  background-color: var(--bg-color);
  background-image: radial-gradient(
      circle at 20% 20%,
      rgba(59, 130, 246, 0.04) 0%,
      transparent 50%
    ),
    radial-gradient(
      circle at 80% 80%,
      rgba(147, 197, 253, 0.03) 0%,
      transparent 50%
    );
}

.message {
  max-width: calc(100% - 20px);
  padding: 12px 16px;
  border-radius: 16px;
  word-break: break-word;
  word-wrap: break-word;
  overflow-wrap: break-word;
  line-height: 1.6;
  font-size: 14px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  animation: message-fade-in 0.4s ease-out;
  position: relative;
  margin: 8px 0;
}

.message:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}

.message .avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 16px;
  flex-shrink: 0;
  margin-right: 0;
  box-shadow: 0 2px 6px rgba(59, 130, 246, 0.3);
  color: #ffffff;
  border: 2px solid #ffffff;
}

.message .avatar.user-avatar {
  background: linear-gradient(135deg, #10b981, #059669);
  box-shadow: 0 2px 6px rgba(16, 185, 129, 0.3);
  margin-left: 0;
}

.message.user {
  align-self: flex-end;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: #ffffff;
  border-bottom-right-radius: 8px;
  flex-direction: row-reverse;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  border: 1px solid #2563eb;
  max-width: calc(100% - 20px);
}

.message.bot {
  align-self: flex-start;
  background-color: var(--bg-color-secondary);
  color: var(--text-color-primary);
  border-bottom-left-radius: 8px;
  border: 1px solid #e0f2fe;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.08);
  max-width: calc(100% - 20px);
}

.message.user .message-content {
  text-align: right;
}

.message-content {
  flex: 1;
  min-width: 0;
  word-break: break-word;
  word-wrap: break-word;
  overflow-wrap: break-word;
  white-space: pre-wrap;
}

.chat-input {
  padding: 20px 24px;
  border-top: 1px solid var(--border-color);
  background: var(--bg-color-secondary);
  position: relative;
  z-index: 2;
  box-shadow: 0 -4px 12px rgba(59, 130, 246, 0.06);
}

.chat-input .arco-input-wrapper {
  border-radius: 28px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.chat-input .arco-input-wrapper:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: var(--color-primary-light-4);
}

.chat-input .arco-input-wrapper:focus-within {
  box-shadow: 0 4px 16px rgba(var(--color-primary-6), 0.2);
  border-color: var(--color-primary-5);
}

.chat-input .arco-input {
  padding: 12px 20px;
  font-size: 14px;
  background-color: var(--color-bg-1);
}

.chat-input .arco-input-group-append .arco-btn {
  border-radius: 0 28px 28px 0;
  padding: 0 20px;
  height: 44px;
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  border: none;
  box-shadow: none;
}

.chat-input .arco-input-group-append .arco-btn:hover {
  background: linear-gradient(135deg, #2563eb, #1e40af);
  transform: translateY(-1px);
}

.chat-input .arco-btn-primary {
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  border: none;
  border-radius: 12px;
  padding: 12px 20px;
  font-weight: 500;
  transition: all 0.2s ease;
  box-shadow: 0 3px 8px rgba(59, 130, 246, 0.3);
  color: #ffffff;
}

.chat-input .arco-btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 5px 12px rgba(59, 130, 246, 0.4);
  background: linear-gradient(135deg, #2563eb, #1e40af);
}

.chat-bot-float-button {
  position: fixed;
  bottom: 30px;
  right: 30px;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  color: #ffffff;
  border: none;
  cursor: pointer;
  box-shadow: 0 6px 20px rgba(59, 130, 246, 0.4);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  backdrop-filter: blur(10px);
  border: 2px solid rgba(255, 255, 255, 0.1);
}

.float-button-icon {
  font-size: 28px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-bot-float-button:hover {
  transform: translateY(-3px) scale(1.05);
  box-shadow: 0 10px 30px rgba(59, 130, 246, 0.5);
  background: linear-gradient(135deg, #2563eb, #1e40af);
}

/* 可爱吉祥物容器样�?*/
.chat-bot-mascot-container {
  position: fixed;
  bottom: 20px;
  right: 30px;
  cursor: pointer;
  z-index: 1000;
  animation: float-in 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.mascot-hint {
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  background: var(--bg-color-secondary, #fff);
  color: var(--text-color-primary, #2c3e50);
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 11px;
  white-space: nowrap;
  box-shadow: var(--shadow-sm, 0 1px 3px rgba(44, 62, 80, 0.04));
  border: 1px solid var(--border-color, #e8edf2);
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.chat-bot-mascot-container:hover .mascot-hint {
  opacity: 1;
}

[data-theme="dark"] .mascot-hint {
  background: var(--bg-color-secondary, #1c2229);
  color: var(--text-color-primary, #e8edf2);
  border-color: var(--border-color, #2a3440);
}

/* 代码块样�?*/
.code-block {
  background-color: #f8fafc;
  border-radius: 12px;
  margin: 12px 0;
  overflow: hidden;
  font-family: "JetBrains Mono", "Fira Code", "Courier New", Courier, monospace;
  border: 1px solid #e0f2fe;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.06);
  max-width: 100%;
  word-break: break-all;
  overflow-wrap: break-word;
}

.code-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, var(--color-fill-2), var(--color-fill-3));
  padding: 10px 16px;
  font-size: 13px;
  color: var(--color-text-2);
  font-weight: 600;
  border-bottom: 1px solid var(--color-border-2);
}

.code-block pre {
  margin: 0;
  padding: 16px 20px;
  background: linear-gradient(135deg, #f1f5f9, #e2e8f0);
  color: #334155;
  font-size: 13px;
  line-height: 1.6;
  border-radius: 0 0 12px 12px;
  overflow-x: auto;
  white-space: pre-wrap;
  word-break: break-all;
  overflow-wrap: break-word;
  max-width: 100%;
}

.code-block code {
  font-family: "JetBrains Mono", "Fira Code", "Courier New", Courier, monospace;
  white-space: pre;
  color: var(--color-text-1);
}

.inline-code-block {
  background-color: var(--color-fill-1);
  border-radius: 8px;
  margin: 10px 0;
  overflow: hidden;
  border: 1px solid var(--color-border-2);
}

.inline-code {
  background-color: #eff6ff;
  padding: 3px 6px;
  border-radius: 6px;
  font-family: "JetBrains Mono", "Fira Code", "Courier New", Courier, monospace;
  font-size: 0.9em;
  color: #1e40af;
  font-weight: 500;
  border: 1px solid #bfdbfe;
}

/* 代码分析结果样式 */
.code-analysis {
  padding: 16px;
  background: linear-gradient(
    to bottom,
    var(--color-fill-1),
    var(--color-bg-2)
  );
  border-top: 1px solid var(--color-border-2);
}

.analysis-title {
  font-weight: 700;
  margin-bottom: 8px;
  color: var(--color-text-1);
  font-size: 14px;
}

.errors {
  color: var(--color-danger-6);
}

.errors .analysis-title {
  color: var(--color-danger-6);
}

.suggestions {
  color: var(--color-success-6);
  margin-top: 12px;
}

.suggestions .analysis-title {
  color: var(--color-success-6);
}

.code-analysis ul {
  margin: 0;
  padding-left: 20px;
}

.code-analysis li {
  margin-bottom: 6px;
  font-size: 13px;
  line-height: 1.5;
}

/* 数学公式块样�?*/
.math-block {
  background-color: var(--color-fill-1);
  padding: 16px;
  border-radius: 12px;
  margin: 12px 0;
  overflow-x: auto;
  border: 1px solid var(--color-border-2);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
}

/* 工具栏样�?*/
.chat-toolbar {
  display: flex;
  padding: 12px 24px;
  border-top: 1px solid var(--color-border-2);
  background: linear-gradient(to right, var(--color-fill-1), var(--color-bg-2));
  gap: 8px;
  overflow-x: auto;
  scrollbar-width: thin;
}

.chat-toolbar .arco-btn {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 8px;
  background-color: var(--color-bg-1);
  border: 1px solid var(--color-border-2);
  transition: all 0.2s ease;
  white-space: nowrap;
}

.chat-toolbar .arco-btn:hover {
  background-color: var(--color-primary-light-5);
  border-color: var(--color-primary-light-3);
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
}

.toolbar-text {
  margin-left: 6px;
  font-size: 12px;
  font-weight: 500;
}

/* 学习进度面板样式 */
.learning-progress {
  padding: 20px 24px;
  background: linear-gradient(
    135deg,
    var(--color-primary-light-5),
    var(--color-primary-light-4)
  );
  border-top: 1px solid var(--color-border-2);
  border-bottom-left-radius: 20px;
  border-bottom-right-radius: 20px;
  color: var(--color-text-1);
  position: relative;
}

.learning-progress::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(var(--color-primary-6), 0.3),
    transparent
  );
}

.progress-header {
  font-weight: 700;
  font-size: 15px;
  margin-bottom: 12px;
  color: var(--color-text-1);
  display: flex;
  align-items: center;
  gap: 8px;
}

.progress-header::before {
  content: "📊";
  font-size: 16px;
}

.progress-stats {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.progress-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
}

.progress-label {
  font-size: 13px;
  color: var(--color-text-2);
  font-weight: 600;
}

.progress-value {
  font-weight: 700;
  color: var(--color-primary-6);
  font-size: 14px;
}

.progress-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.progress-tags .arco-tag {
  border-radius: 12px;
  font-weight: 500;
  font-size: 11px;
  padding: 4px 8px;
}

/* 调整大小的手�?*/
.resize-handle {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 24px;
  height: 24px;
  cursor: nwse-resize;
  display: flex;
  justify-content: center;
  align-items: center;
  color: var(--color-text-3);
  opacity: 0.4;
  transition: all 0.2s ease;
  z-index: 10;
  background: linear-gradient(135deg, var(--color-bg-2), var(--color-fill-2));
  border-radius: 24px 0 20px 0;
}

.resize-handle:hover {
  opacity: 0.8;
  transform: scale(1.1);
  background: linear-gradient(135deg, var(--color-fill-2), var(--color-fill-3));
}

/* 消息动画 */
@keyframes message-fade-in {
  from {
    opacity: 0;
    transform: translateY(15px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 自定义滚动条 */
.chat-messages::-webkit-scrollbar {
  width: 8px;
}

.chat-messages::-webkit-scrollbar-track {
  background: transparent;
  border-radius: 10px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, var(--color-fill-3), var(--color-fill-4));
  border-radius: 10px;
  border: 2px solid var(--color-bg-1);
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(
    135deg,
    var(--color-fill-4),
    var(--color-primary-light-4)
  );
}

/* 响应式设�?*/
@media (max-width: 768px) {
  .chat-bot-container {
    width: calc(100vw - 40px);
    height: calc(100vh - 80px);
    left: 20px !important;
    top: 40px !important;
    border-radius: 16px;
  }

  .chat-bot-float-button {
    bottom: 20px;
    right: 20px;
    width: 50px;
    height: 50px;
  }

  .chat-messages {
    padding: 16px;
  }

  .message {
    max-width: calc(100% - 16px);
    padding: 10px 12px;
    font-size: 13px;
    gap: 8px;
  }

  .message .avatar {
    width: 28px;
    height: 28px;
    font-size: 14px;
  }

  .chat-input {
    padding: 16px;
  }

  .learning-progress {
    padding: 12px;
  }
}

@media (max-width: 480px) {
  .chat-bot-container {
    width: calc(100vw - 32px);
    left: 16px !important;
    right: 16px;
  }

  .message {
    max-width: calc(100% - 12px);
    padding: 8px 12px;
    font-size: 12px;
    gap: 6px;
  }

  .message .avatar {
    width: 24px;
    height: 24px;
    font-size: 12px;
  }

  .chat-messages {
    padding: 12px;
    gap: 16px;
  }

  .chat-input {
    padding: 16px;
  }
}

/* 深色模式适配 */
[data-theme="dark"] .chat-bot-container {
  background-color: var(--bg-color-secondary) !important;
  border-color: var(--border-color) !important;
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.3), 0 8px 20px rgba(0, 0, 0, 0.2) !important;
}

[data-theme="dark"] .chat-bot-body {
  background-color: var(--bg-color-secondary) !important;
}

[data-theme="dark"] .chat-messages {
  background-color: var(--bg-color) !important;
  background-image: none;
}

[data-theme="dark"] .message.bot {
  background-color: var(--bg-color-tertiary) !important;
  color: var(--text-color-primary) !important;
  border-color: var(--border-color) !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2) !important;
}

[data-theme="dark"] .message.bot .message-content {
  color: var(--text-color-primary) !important;
}

[data-theme="dark"] .chat-input {
  background: linear-gradient(
    to bottom,
    var(--bg-color-secondary),
    var(--bg-color)
  ) !important;
  border-top-color: var(--border-color) !important;
}

[data-theme="dark"] .code-block {
  background-color: var(--bg-color-tertiary) !important;
  border-color: var(--border-color) !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1) !important;
}

[data-theme="dark"] .code-block pre {
  background: linear-gradient(
    135deg,
    var(--bg-color-tertiary),
    var(--bg-color-secondary)
  ) !important;
  color: var(--text-color-primary) !important;
}

[data-theme="dark"] .inline-code {
  background-color: var(--bg-color-tertiary) !important;
  color: var(--primary-color) !important;
  border-color: var(--border-color) !important;
}

[data-theme="dark"] .math-block {
  background-color: var(--bg-color-tertiary) !important;
  border-color: var(--border-color) !important;
}

/* 系统偏好深色模式适配 */
@media (prefers-color-scheme: dark) {
  .chat-bot-container {
    box-shadow: 0 16px 40px rgba(0, 0, 0, 0.3), 0 8px 20px rgba(0, 0, 0, 0.2);
  }

  .message.bot {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  }

  .code-block {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
}
</style>
