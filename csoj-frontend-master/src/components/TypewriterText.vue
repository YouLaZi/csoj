<template>
  <span class="typewriter-text">
    <span class="text-content">{{ displayText }}</span>
    <span class="cursor" :class="{ 'is-blinking': isTyping || blinkCursor }"
      >|</span
    >
  </span>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from "vue";

// Props
const props = defineProps<{
  text: string;
  speed?: number;
  delay?: number;
  loop?: boolean;
  blinkCursor?: boolean;
  deleteSpeed?: number;
  pauseDuration?: number;
}>();

// Emits
const emit = defineEmits<{
  (e: "complete"): void;
  (e: "start"): void;
  (e: "delete"): void;
}>();

const displayText = ref("");
const isTyping = ref(false);

let typingTimer: number | null = null;
let deleteTimer: number | null = null;
let pauseTimer: number | null = null;

const speed = props.speed || 80;
const delay = props.delay || 0;
const deleteSpeed = props.deleteSpeed || 40;
const pauseDuration = props.pauseDuration || 2000;

// 打字效果
const typeText = (text: string, index = 0) => {
  if (index === 0) {
    emit("start");
    isTyping.value = true;
  }

  if (index < text.length) {
    displayText.value = text.slice(0, index + 1);
    typingTimer = window.setTimeout(() => {
      typeText(text, index + 1);
    }, speed);
  } else {
    isTyping.value = false;
    emit("complete");

    // 循环模式
    if (props.loop) {
      pauseTimer = window.setTimeout(() => {
        deleteText(text);
      }, pauseDuration);
    }
  }
};

// 删除效果
const deleteText = (text: string) => {
  emit("delete");
  isTyping.value = true;

  if (displayText.value.length > 0) {
    displayText.value = displayText.value.slice(0, -1);
    deleteTimer = window.setTimeout(() => {
      deleteText(text);
    }, deleteSpeed);
  } else {
    isTyping.value = false;
    typingTimer = window.setTimeout(() => {
      typeText(text);
    }, delay);
  }
};

// 开始动画
const start = () => {
  clearAllTimers();
  displayText.value = "";
  typingTimer = window.setTimeout(() => {
    typeText(props.text);
  }, delay);
};

// 停止动画
const stop = () => {
  clearAllTimers();
  isTyping.value = false;
};

// 清除所有定时器
const clearAllTimers = () => {
  if (typingTimer) {
    clearTimeout(typingTimer);
    typingTimer = null;
  }
  if (deleteTimer) {
    clearTimeout(deleteTimer);
    deleteTimer = null;
  }
  if (pauseTimer) {
    clearTimeout(pauseTimer);
    pauseTimer = null;
  }
};

// 监听文本变化
watch(
  () => props.text,
  () => {
    start();
  }
);

onMounted(() => {
  start();
});

onUnmounted(() => {
  clearAllTimers();
});

// 暴露方法
defineExpose({
  start,
  stop,
});
</script>

<style scoped>
.typewriter-text {
  display: inline-flex;
  align-items: center;
}

.text-content {
  white-space: pre-wrap;
  word-break: break-word;
}

.cursor {
  display: inline-block;
  margin-left: 2px;
  font-weight: 100;
  color: var(--primary-color, #4f6f8f);
}

.cursor.is-blinking {
  animation: cursor-blink 0.8s infinite;
}

@keyframes cursor-blink {
  0%,
  50% {
    opacity: 1;
  }
  51%,
  100% {
    opacity: 0;
  }
}
</style>
