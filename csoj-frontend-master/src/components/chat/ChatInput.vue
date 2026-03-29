<template>
  <div class="chat-input-container">
    <div class="input-wrapper">
      <textarea
        ref="textareaRef"
        v-model="inputText"
        :placeholder="placeholder"
        :disabled="disabled || isStreaming"
        rows="1"
        @keydown="handleKeydown"
        @input="adjustHeight"
      />
      <button
        class="send-btn"
        :class="{ active: canSend, streaming: isStreaming }"
        :disabled="!canSend || isStreaming"
        @click="handleSend"
      >
        <icon-stop v-if="isStreaming" />
        <icon-send v-else />
      </button>
    </div>
    <div class="input-hints">
      <span class="hint-text">Press Enter to send, Shift+Enter for new line</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from "vue";
import { iconSend, iconStop } from "./icons";

const props = defineProps<{
  disabled?: boolean;
  isStreaming?: boolean;
  placeholder?: string;
}>();

const emit = defineEmits<{
  (e: "send", message: string): void;
  (e: "stop"): void;
}>();

const textareaRef = ref<HTMLTextAreaElement | null>(null);
const inputText = ref("");

const canSend = computed(() => {
  return inputText.value.trim().length > 0;
});

watch(inputText, () => {
  nextTick(adjustHeight);
});

function adjustHeight(): void {
  const textarea = textareaRef.value;
  if (textarea) {
    textarea.style.height = "auto";
    textarea.style.height = textarea.scrollHeight + "px";
  }
}

function handleKeydown(event: KeyboardEvent): void {
  if (event.key === "Enter" && !event.shiftKey) {
    event.preventDefault();
    handleSend();
  }
}

function handleSend(): void {
  const message = inputText.value.trim();
  if (message && !props.disabled && !props.isStreaming) {
    emit("send", message);
    inputText.value = "";
    nextTick(() => {
      adjustHeight();
    });
  }
}

function focus(): void {
  textareaRef.value?.focus();
}

defineExpose({ focus });
</script>

<style scoped>
.chat-input-container {
  padding: 12px 16px;
  background: var(--color-bg-2);
  border-top: 1px solid var(--color-border-2);
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  background: var(--color-fill-1);
  border-radius: 16px;
  padding: 8px 12px;
  border: 1px solid var(--color-border-2);
  transition: border-color 0.2s;
}

.input-wrapper:focus-within {
  border-color: rgb(99 102 241);
}

.input-wrapper textarea {
  flex: 1;
  border: none;
  background: transparent;
  resize: none;
  outline: none;
  font-size: 14px;
  line-height: 1.5;
  color: var(--color-text-1);
  max-height: 120px;
  min-height: 24px;
}

.input-wrapper textarea::placeholder {
  color: var(--color-text-3);
}

.input-wrapper textarea:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.send-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: var(--color-fill-3);
  color: var(--color-text-3);
  cursor: not-allowed;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.send-btn.active {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: white;
  cursor: pointer;
}

.send-btn.active:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(99 102 241, 0.3);
}

.send-btn.streaming {
  background: #ef4444;
}

.send-btn.streaming:hover {
  background: #dc2626;
}

.input-hints {
  margin-top: 8px;
  text-align: center;
}

.hint-text {
  font-size: 11px;
  color: var(--color-text-3);
  opacity: 0.7;
}

[data-theme="dark"] .input-wrapper {
  background: var(--color-fill-2);
}
</style>
