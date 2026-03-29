<template>
  <div class="chatbot-wrapper">
    <!-- Floating mascot button -->
    <transition name="mascot">
      <div
        v-if="!isOpen"
        class="mascot-button"
        :class="{ 'has-notification': hasNewMessage }"
        @click="openChat"
      >
        <div class="mascot-icon">
          <icon-mascot />
        </div>
        <div v-if="hasNewMessage" class="notification-dot"></div>
        <div class="mascot-tooltip">
          <span>{{ t("chatbot.openChat") }}</span>
        </div>
      </div>
    </transition>

    <!-- Chat window -->
    <transition name="chat-window">
      <div v-if="isOpen" class="chat-window" :style="windowStyle">
        <!-- Header -->
        <div class="chat-header" @mousedown="startDrag">
          <div class="header-title">
            <icon-sparkle class="title-icon" />
            <span>{{ t("chatbot.title") }}</span>
          </div>
          <div class="header-actions">
            <button class="action-btn" @click="clearChat" :title="t('chatbot.clearHistory')">
              <icon-clear />
            </button>
            <button class="action-btn" @click="minimizeChat" :title="t('chatbot.minimize')">
              <icon-minimize />
            </button>
            <button class="action-btn" @click="closeChat" :title="t('chatbot.close')">
              <icon-close />
            </button>
          </div>
        </div>

        <!-- Messages area -->
        <div ref="messagesContainer" class="messages-area">
          <div v-if="messages.length === 0" class="empty-state">
            <icon-mascot class="empty-icon" />
            <p class="empty-title">{{ t("chatbot.welcomeTitle") }}</p>
            <p class="empty-desc">{{ t("chatbot.welcomeDesc") }}</p>
            <div class="quick-actions">
              <button
                v-for="action in quickActions"
                :key="action.text"
                class="quick-action-btn"
                @click="handleQuickAction(action.text)"
              >
                {{ action.text }}
              </button>
            </div>
          </div>
          <chat-message
            v-for="msg in messages"
            :key="msg.id"
            :message="msg"
          />
          <div v-if="isStreaming && !currentStreamContent" class="typing-indicator">
            <span></span><span></span><span></span>
          </div>
        </div>

        <!-- Input area -->
        <chat-input
          ref="chatInputRef"
          :is-streaming="isStreaming"
          :placeholder="t('chatbot.inputPlaceholder')"
          @send="handleSend"
          @stop="handleStop"
        />
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, nextTick, watch } from "vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { useUserStore } from "@/store/useUserStore";
import chatBotService from "@/services/ChatBotService";
import ChatMessage from "./ChatMessage.vue";
import ChatInput from "./ChatInput.vue";
import {
  iconMascot,
  iconClose,
  iconClear,
  iconMinimize,
  iconSparkle,
} from "./icons";
import type { ChatMessage as ChatMessageType } from "@/types/chat";

const { t } = useI18n();
const route = useRoute();
const userStore = useUserStore();

// State
const isOpen = ref(false);
const isStreaming = ref(false);
const hasNewMessage = ref(false);
const currentStreamContent = ref("");
const messagesContainer = ref<HTMLElement | null>(null);
const chatInputRef = ref<InstanceType<typeof ChatInput> | null>(null);

const messages = ref<ChatMessageType[]>([]);

// Current question context
const currentQuestionId = computed(() => {
  const id = route.params.id;
  return id ? Number(id) : undefined;
});

// Window position and size
const windowStyle = reactive({
  right: "20px",
  bottom: "20px",
  width: "380px",
  height: "520px",
});

// Dragging state
let isDragging = false;
let dragStartX = 0;
let dragStartY = 0;
let dragStartRight = 0;
let dragStartBottom = 0;

// Quick actions
const quickActions = computed(() => [
  { text: t("chatbot.quickAction1") },
  { text: t("chatbot.quickAction2") },
  { text: t("chatbot.quickAction3") },
]);

// Generate unique ID
function generateId(): string {
  return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
}

// Open chat window
function openChat(): void {
  isOpen.value = true;
  hasNewMessage.value = false;
  nextTick(() => {
    chatInputRef.value?.focus();
    scrollToBottom();
  });
}

// Close chat window
function closeChat(): void {
  isOpen.value = false;
  chatBotService.cancelStreaming();
}

// Minimize chat window
function minimizeChat(): void {
  isOpen.value = false;
}

// Clear chat history
async function clearChat(): Promise<void> {
  messages.value = [];
  await chatBotService.clearHistory(currentQuestionId.value);
}

// Handle send message
async function handleSend(message: string): Promise<void> {
  if (!message.trim() || isStreaming.value) return;

  // Add user message
  const userMsg: ChatMessageType = {
    id: generateId(),
    role: "user",
    content: message.trim(),
    timestamp: Date.now(),
  };
  messages.value.push(userMsg);
  scrollToBottom();

  // Start streaming
  isStreaming.value = true;
  currentStreamContent.value = "";

  // Create assistant message placeholder
  const assistantMsg: ChatMessageType = {
    id: generateId(),
    role: "assistant",
    content: "",
    timestamp: Date.now(),
    isStreaming: true,
  };
  messages.value.push(assistantMsg);

  try {
    await chatBotService.sendMessageStream(
      message.trim(),
      currentQuestionId.value,
      (chunk: string) => {
        // Update streaming content
        currentStreamContent.value += chunk;
        const lastMsg = messages.value[messages.value.length - 1];
        if (lastMsg.role === "assistant") {
          lastMsg.content = currentStreamContent.value;
        }
        scrollToBottom();
      },
      (fullResponse: string) => {
        // Complete streaming
        isStreaming.value = false;
        const lastMsg = messages.value[messages.value.length - 1];
        if (lastMsg.role === "assistant") {
          lastMsg.content = fullResponse;
          lastMsg.isStreaming = false;
        }
        currentStreamContent.value = "";
      },
      (error: Error) => {
        console.error("Chat error:", error);
        isStreaming.value = false;
        const lastMsg = messages.value[messages.value.length - 1];
        if (lastMsg.role === "assistant") {
          lastMsg.content = t("chatbot.errorMessage");
          lastMsg.isStreaming = false;
        }
        currentStreamContent.value = "";
      }
    );
  } catch (error) {
    console.error("Failed to send message:", error);
    isStreaming.value = false;
  }
}

// Handle stop streaming
function handleStop(): void {
  chatBotService.cancelStreaming();
  isStreaming.value = false;
  const lastMsg = messages.value[messages.value.length - 1];
  if (lastMsg?.isStreaming) {
    lastMsg.isStreaming = false;
  }
  currentStreamContent.value = "";
}

// Handle quick action
function handleQuickAction(text: string): void {
  chatInputRef.value?.focus();
  handleSend(text);
}

// Scroll to bottom
function scrollToBottom(): void {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
    }
  });
}

// Dragging functionality
function startDrag(event: MouseEvent): void {
  if ((event.target as HTMLElement).closest(".header-actions")) return;

  isDragging = true;
  dragStartX = event.clientX;
  dragStartY = event.clientY;
  const rect = (event.currentTarget as HTMLElement).getBoundingClientRect();
  dragStartRight = window.innerWidth - rect.right;
  dragStartBottom = window.innerHeight - rect.bottom;

  document.addEventListener("mousemove", onDrag, { passive: true });
  document.addEventListener("mouseup", stopDrag);
}

function onDrag(event: MouseEvent): void {
  if (!isDragging) return;

  const deltaX = event.clientX - dragStartX;
  const deltaY = event.clientY - dragStartY;

  const newRight = Math.max(20, Math.min(dragStartRight - deltaX, window.innerWidth - 400));
  const newBottom = Math.max(20, Math.min(dragStartBottom - deltaY, window.innerHeight - 100));

  windowStyle.right = `${newRight}px`;
  windowStyle.bottom = `${newBottom}px`;
}

function stopDrag(): void {
  isDragging = false;
  document.removeEventListener("mousemove", onDrag);
  document.removeEventListener("mouseup", stopDrag);
}

// Load chat history on question change
async function loadHistory(): Promise<void> {
  if (!userStore.isLoggedIn) return;

  try {
    const history = await chatBotService.getChatHistory(currentQuestionId.value);
    if (history.length > 0) {
      messages.value = history;
      scrollToBottom();
    }
  } catch (error) {
    console.error("Failed to load chat history:", error);
  }
}

// Watch for question changes
watch(currentQuestionId, () => {
  loadHistory();
});

// Cleanup
onUnmounted(() => {
  chatBotService.cancelStreaming();
  document.removeEventListener("mousemove", onDrag);
  document.removeEventListener("mouseup", stopDrag);
});

// Expose methods
defineExpose({
  openChat,
  closeChat,
});
</script>

<style scoped>
.chatbot-wrapper {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

/* Mascot button */
.mascot-button {
  position: fixed;
  right: 20px;
  bottom: 20px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  box-shadow: 0 4px 20px rgba(245, 158, 11, 0.4);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 1000;
}

.mascot-button:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 30px rgba(245, 158, 11, 0.5);
}

.mascot-button.has-notification {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { box-shadow: 0 4px 20px rgba(245, 158, 11, 0.4); }
  50% { box-shadow: 0 4px 30px rgba(245, 158, 11, 0.6); }
}

.mascot-icon {
  font-size: 28px;
  color: white;
}

.notification-dot {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 12px;
  height: 12px;
  background: #ef4444;
  border-radius: 50%;
  border: 2px solid white;
  animation: dot-pulse 1.5s infinite;
}

@keyframes dot-pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.2); }
}

.mascot-tooltip {
  position: absolute;
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%);
  background: var(--color-bg-2);
  color: var(--color-text-1);
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 12px;
  white-space: nowrap;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.2s;
  margin-bottom: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.mascot-button:hover .mascot-tooltip {
  opacity: 1;
}

/* Chat window */
.chat-window {
  position: fixed;
  display: flex;
  flex-direction: column;
  background: var(--color-bg-1);
  border-radius: 16px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.15);
  z-index: 1001;
  overflow: hidden;
  border: 1px solid var(--color-border-2);
}

/* Header */
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: white;
  cursor: move;
  user-select: none;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 15px;
}

.title-icon {
  font-size: 18px;
}

.header-actions {
  display: flex;
  gap: 4px;
}

.action-btn {
  width: 28px;
  height: 28px;
  border: none;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 6px;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.25);
}

/* Messages area */
.messages-area {
  flex: 1;
  overflow-y: auto;
  background: var(--color-bg-1);
  padding: 8px;
}

.messages-area::-webkit-scrollbar {
  width: 6px;
}

.messages-area::-webkit-scrollbar-thumb {
  background: var(--color-fill-3);
  border-radius: 3px;
}

/* Empty state */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 24px;
  text-align: center;
  color: var(--color-text-3);
}

.empty-icon {
  font-size: 48px;
  color: #fbbf24;
  margin-bottom: 16px;
}

.empty-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-1);
  margin-bottom: 8px;
}

.empty-desc {
  font-size: 13px;
  margin-bottom: 20px;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.quick-action-btn {
  padding: 8px 16px;
  background: var(--color-fill-1);
  border: 1px solid var(--color-border-2);
  border-radius: 20px;
  font-size: 13px;
  color: var(--color-text-1);
  cursor: pointer;
  transition: all 0.2s;
}

.quick-action-btn:hover {
  background: var(--color-fill-2);
  border-color: var(--color-primary-6);
  color: var(--color-primary-6);
}

/* Typing indicator */
.typing-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 16px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: var(--color-fill-3);
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(1) { animation-delay: 0s; }
.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-6px); }
}

/* Transitions */
.mascot-enter-active,
.mascot-leave-active {
  transition: all 0.3s ease;
}

.mascot-enter-from,
.mascot-leave-to {
  opacity: 0;
  transform: scale(0.8);
}

.chat-window-enter-active,
.chat-window-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.chat-window-enter-from,
.chat-window-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

/* Dark mode */
[data-theme="dark"] .chat-window {
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.3);
}

[data-theme="dark"] .mascot-button {
  box-shadow: 0 4px 20px rgba(245, 158, 11, 0.3);
}

/* Responsive */
@media (max-width: 480px) {
  .chat-window {
    width: calc(100vw - 32px) !important;
    height: calc(100vh - 100px) !important;
    right: 16px !important;
    bottom: 16px !important;
  }
}
</style>
