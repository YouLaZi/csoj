<template>
  <div
    class="cute-mascot"
    :class="{ 'is-active': isActive, 'is-sleeping': isSleeping }"
    @mouseenter="onMouseEnter"
    @mouseleave="onMouseLeave"
    @click="onClick"
  >
    <!-- 猫咪头像容器 -->
    <div class="mascot-avatar" :class="{ 'is-waving': isWaving }">
      <!-- 发光效果 -->
      <div class="avatar-glow"></div>

      <!-- 头像背景 -->
      <div class="avatar-bg">
        <!-- 猫咪emoji -->
        <div class="cat-emoji" :class="currentMood">
          {{ catEmoji }}
        </div>
      </div>

      <!-- 表情装饰 -->
      <div v-if="isBlushing" class="blush-blush">💕</div>
    </div>

    <!-- 气泡消息 -->
    <transition name="bubble">
      <div v-if="showMessage" class="speech-bubble">
        <span>{{ currentMessage }}</span>
        <div class="bubble-tail"></div>
      </div>
    </transition>

    <!-- 粒子效果 -->
    <div v-if="showParticles" class="particles">
      <div
        v-for="i in 6"
        :key="i"
        class="particle"
        :style="{ '--delay': i * 0.1 + 's', '--angle': (i - 1) * 60 + 'deg' }"
      >
        {{ particleEmoji }}
      </div>
    </div>

    <!-- 睡眠时的 Zzz -->
    <div v-if="isSleeping" class="sleep-zzz">
      <span class="z z1">z</span>
      <span class="z z2">z</span>
      <span class="z z3">Z</span>
    </div>

    <!-- 询问气泡 -->
    <transition name="ask-bubble">
      <div v-if="showAskBubble" class="ask-bubble" @click.stop="onAskClick">
        <div class="ask-icon">🐱</div>
        <div class="ask-content">
          <div class="ask-text">{{ askMessage }}</div>
          <div class="ask-actions">
            <button class="ask-btn primary" @click.stop="onExplainClick">
              解释这个
            </button>
            <button class="ask-btn" @click.stop="onQuestionClick">
              提问...
            </button>
          </div>
        </div>
        <button class="ask-close" @click.stop="hideAskBubble">×</button>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from "vue";

// Props
const props = defineProps<{
  mood?: "happy" | "sad" | "thinking" | "sleeping" | "excited" | "surprised";
}>();

// Emits
const emit = defineEmits<{
  (e: "click"): void;
  (e: "moodChange", mood: string): void;
  (e: "askExplain", text: string, type: string): void;
  (e: "askQuestion", text: string, type: string): void;
}>();

// 状态
const isActive = ref(false);
const isBlinking = ref(false);
const isHappy = ref(true);
const isSad = ref(false);
const isThinking = ref(false);
const isSleeping = ref(false);
const isBlushing = ref(false);
const isWaving = ref(false);
const isSurprised = ref(false);
const shakeAngle = ref(0);
const showMessage = ref(false);
const currentMessage = ref("");
const showParticles = ref(false);
const particleEmoji = ref("💕");

// 询问气泡状态
const showAskBubble = ref(false);
const askMessage = ref("需要帮忙吗？");
const selectedTextInfo = ref<{ text: string; type: string } | null>(null);

// 当前心情
const currentMood = computed(() => {
  if (isSleeping.value) return "sleeping";
  if (isSad.value) return "sad";
  if (isThinking.value) return "thinking";
  if (isSurprised.value) return "surprised";
  if (isHappy.value) return "happy";
  return "neutral";
});

// 猫咪emoji
const catEmoji = computed(() => {
  if (isSleeping.value) return "😴";
  if (isSad.value) return "😿";
  if (isThinking.value) return "🤔";
  if (isSurprised.value) return "🙀";
  if (isHappy.value) return "😺";
  return "🐱";
});

// 消息列表
const happyMessages = [
  "喵~",
  "喵喵！",
  "摸摸我~",
  "你好呀！",
  "今天也要加油哦！",
  "你是最棒的！",
  "代码写得很棒呢！",
  "我可以帮你喵~",
  "一起来刷题吧！",
  "你真厉害！",
];

const excitedMessages = [
  "喵喵喵！🎉",
  "太棒啦！",
  "你做到了！",
  "完美！✨",
  "喵呜~！",
];

// 定时器
let blinkTimer: number | null = null;
let idleTimer: number | null = null;

// 眨眼动画
const startBlinking = () => {
  blinkTimer = window.setInterval(() => {
    if (!isSleeping.value) {
      isBlinking.value = true;
      setTimeout(() => {
        isBlinking.value = false;
      }, 150);
    }
  }, 2500 + Math.random() * 2500);
};

// 显示消息
const showBubbleMessage = (msg: string, duration = 3000) => {
  currentMessage.value = msg;
  showMessage.value = true;
  setTimeout(() => {
    showMessage.value = false;
  }, duration);
};

// 显示粒子效果
const triggerParticles = (emoji = "💕") => {
  particleEmoji.value = emoji;
  showParticles.value = true;
  setTimeout(() => {
    showParticles.value = false;
  }, 1000);
};

// 摇晃动画
const shake = () => {
  let angle = 0;
  let direction = 1;
  let count = 0;
  const shakeInterval = setInterval(() => {
    angle = direction * 8 * Math.exp(-count * 0.25);
    shakeAngle.value = angle;
    direction *= -1;
    count++;
    if (count > 8) {
      clearInterval(shakeInterval);
      shakeAngle.value = 0;
    }
  }, 50);
};

// 挥手动画
const wave = () => {
  isWaving.value = true;
  setTimeout(() => {
    isWaving.value = false;
  }, 1200);
};

// 设置心情
const setMood = (mood: string) => {
  isHappy.value = false;
  isSad.value = false;
  isThinking.value = false;
  isSleeping.value = false;
  isBlushing.value = false;
  isSurprised.value = false;

  switch (mood) {
    case "happy":
      isHappy.value = true;
      isBlushing.value = true;
      break;
    case "sad":
      isSad.value = true;
      break;
    case "thinking":
      isThinking.value = true;
      break;
    case "sleeping":
      isSleeping.value = true;
      break;
    case "excited":
      isHappy.value = true;
      isBlushing.value = true;
      isSurprised.value = true;
      shake();
      wave();
      triggerParticles("💖");
      break;
    case "surprised":
      isSurprised.value = true;
      break;
  }
  emit("moodChange", mood);
};

// 监听 mood prop
watch(
  () => props.mood,
  (newMood) => {
    if (newMood) {
      setMood(newMood);
    }
  },
  { immediate: true }
);

// 鼠标进入
const onMouseEnter = () => {
  isActive.value = true;
  isBlushing.value = true;
  wave();

  if (Math.random() > 0.4) {
    const msg = happyMessages[Math.floor(Math.random() * happyMessages.length)];
    showBubbleMessage(msg);
  }
};

// 鼠标离开
const onMouseLeave = () => {
  isActive.value = false;
  if (!isHappy.value) {
    isBlushing.value = false;
  }
};

// 点击
const onClick = () => {
  shake();
  setMood("excited");
  const msg =
    excitedMessages[Math.floor(Math.random() * excitedMessages.length)];
  showBubbleMessage(msg, 2000);
  emit("click");

  setTimeout(() => {
    setMood("happy");
  }, 2500);
};

// 询问气泡相关方法
const getAskMessage = (type: string): string => {
  const messages: Record<string, string[]> = {
    code: ["这段代码需要我帮你解释吗？", "要我帮你分析这段代码喵？"],
    error: ["遇到错误了？让我帮你看看！", "这个错误我来帮你分析~"],
    math: ["这个公式需要解释吗？", "要我帮你理解这个数学内容吗？"],
    description: ["这段描述不太清楚？", "需要我帮你理解题意吗？"],
    general: ["需要帮忙吗？", "有什么我可以帮你的喵？"],
  };
  const list = messages[type] || messages.general;
  return list[Math.floor(Math.random() * list.length)];
};

const showAskBubbleForSelection = (text: string, type: string) => {
  selectedTextInfo.value = { text, type };
  askMessage.value = getAskMessage(type);
  showAskBubble.value = true;
  setMood("thinking");
};

const hideAskBubble = () => {
  showAskBubble.value = false;
  selectedTextInfo.value = null;
};

const onAskClick = () => {
  // 点击气泡本身的处理
};

const onExplainClick = () => {
  if (selectedTextInfo.value) {
    emit(
      "askExplain",
      selectedTextInfo.value.text,
      selectedTextInfo.value.type
    );
    hideAskBubble();
    setMood("excited");
    showBubbleMessage("好的，让我来解释喵~", 2000);
  }
};

const onQuestionClick = () => {
  if (selectedTextInfo.value) {
    emit(
      "askQuestion",
      selectedTextInfo.value.text,
      selectedTextInfo.value.type
    );
    hideAskBubble();
  }
};

// 空闲检测
const resetIdleTimer = () => {
  if (idleTimer) {
    clearTimeout(idleTimer);
  }
  idleTimer = window.setTimeout(() => {
    if (!isSleeping.value) {
      if (Math.random() > 0.6) {
        setMood("sleeping");
        showBubbleMessage("Zzz...", 5000);
        setTimeout(() => {
          setMood("happy");
        }, 5000);
      } else {
        const msg =
          happyMessages[Math.floor(Math.random() * happyMessages.length)];
        showBubbleMessage(msg);
      }
    }
  }, 30000);
};

// 生命周期
onMounted(() => {
  startBlinking();
  setMood("happy");

  document.addEventListener("mousemove", resetIdleTimer);
  document.addEventListener("keydown", resetIdleTimer);
});

onUnmounted(() => {
  if (blinkTimer) {
    clearInterval(blinkTimer);
  }
  if (idleTimer) {
    clearTimeout(idleTimer);
  }
  document.removeEventListener("mousemove", resetIdleTimer);
  document.removeEventListener("keydown", resetIdleTimer);
});

// 暴露方法给父组件
defineExpose({
  setMood,
  showBubbleMessage,
  triggerParticles,
  shake,
  wave,
  showAskBubbleForSelection,
  hideAskBubble,
});
</script>

<style scoped>
.cute-mascot {
  position: relative;
  width: 70px;
  height: 70px;
  cursor: pointer;
  user-select: none;
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.cute-mascot:hover {
  transform: scale(1.1);
}

.cute-mascot.is-active {
  transform: scale(1.15);
}

/* 头像容器 */
.mascot-avatar {
  position: relative;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.mascot-avatar.is-waving {
  animation: bounce-wave 0.4s ease infinite;
}

@keyframes bounce-wave {
  0%,
  100% {
    transform: rotate(-5deg) scale(1);
  }
  50% {
    transform: rotate(5deg) scale(1.05);
  }
}

/* 发光效果 */
.avatar-glow {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: radial-gradient(
    circle,
    rgba(251, 191, 36, 0.4) 0%,
    rgba(251, 191, 36, 0) 70%
  );
  animation: glow-pulse 2s ease-in-out infinite;
}

@keyframes glow-pulse {
  0%,
  100% {
    transform: scale(1);
    opacity: 0.6;
  }
  50% {
    transform: scale(1.2);
    opacity: 1;
  }
}

/* 头像背景 */
.avatar-bg {
  position: relative;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #fef3c7 0%, #fcd34d 50%, #f59e0b 100%);
  box-shadow: 0 4px 15px rgba(245, 158, 11, 0.4),
    inset 0 2px 10px rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.cute-mascot:hover .avatar-bg {
  box-shadow: 0 6px 25px rgba(245, 158, 11, 0.5),
    inset 0 2px 10px rgba(255, 255, 255, 0.4);
  transform: scale(1.02);
}

/* 猫咪emoji */
.cat-emoji {
  font-size: 32px;
  line-height: 1;
  transition: transform 0.2s ease;
}

.cat-emoji.happy {
  animation: happy-bounce 0.5s ease infinite;
}

@keyframes happy-bounce {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-3px);
  }
}

.cat-emoji.thinking {
  animation: thinking-tilt 1s ease infinite;
}

@keyframes thinking-tilt {
  0%,
  100% {
    transform: rotate(-5deg);
  }
  50% {
    transform: rotate(5deg);
  }
}

.cat-emoji.sleeping {
  animation: sleeping 2s ease infinite;
}

@keyframes sleeping {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(0.95);
  }
}

.cat-emoji.sad {
  transform: scale(0.9);
}

.cat-emoji.surprised {
  animation: surprised 0.3s ease;
}

@keyframes surprised {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.3);
  }
  100% {
    transform: scale(1);
  }
}

/* 腮红装饰 */
.blush-blush {
  position: absolute;
  top: -8px;
  right: -8px;
  font-size: 14px;
  animation: blush-float 1.5s ease infinite;
}

@keyframes blush-float {
  0%,
  100% {
    transform: translateY(0) scale(1);
    opacity: 1;
  }
  50% {
    transform: translateY(-5px) scale(1.1);
    opacity: 0.8;
  }
}

/* 气泡消息 */
.speech-bubble {
  position: absolute;
  bottom: 85px;
  left: 50%;
  transform: translateX(-50%);
  background: linear-gradient(135deg, #fff 0%, #fefce8 100%);
  color: #334155;
  padding: 10px 16px;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  border: 2px solid #fef3c7;
  z-index: 10;
}

.bubble-tail {
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 0;
  border-left: 10px solid transparent;
  border-right: 10px solid transparent;
  border-top: 12px solid #fff;
}

.bubble-enter-active,
.bubble-leave-active {
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.bubble-enter-from,
.bubble-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(10px) scale(0.8);
}

/* 粒子效果 */
.particles {
  position: absolute;
  top: -20px;
  left: 50%;
  transform: translateX(-50%);
  width: 100px;
  height: 100px;
  pointer-events: none;
}

.particle {
  position: absolute;
  top: 50%;
  left: 50%;
  font-size: 18px;
  animation: particle-float 1s ease forwards;
  animation-delay: var(--delay);
}

@keyframes particle-float {
  0% {
    opacity: 1;
    transform: translate(-50%, -50%) rotate(var(--angle)) translateY(0)
      scale(0.5);
  }
  100% {
    opacity: 0;
    transform: translate(-50%, -50%) rotate(var(--angle)) translateY(-60px)
      scale(1.2);
  }
}

/* 睡眠 Zzz */
.sleep-zzz {
  position: absolute;
  top: -5px;
  right: -10px;
  z-index: 5;
}

.z {
  position: absolute;
  color: #f59e0b;
  font-weight: bold;
  font-style: italic;
  animation: float-z 2s ease-in-out infinite;
}

.z1 {
  font-size: 12px;
  right: 0;
  top: 0;
  animation-delay: 0s;
}

.z2 {
  font-size: 16px;
  right: 10px;
  top: -15px;
  animation-delay: 0.3s;
}

.z3 {
  font-size: 20px;
  right: 20px;
  top: -35px;
  animation-delay: 0.6s;
}

@keyframes float-z {
  0%,
  100% {
    transform: translateY(0) rotate(-10deg);
    opacity: 0.6;
  }
  50% {
    transform: translateY(-8px) rotate(10deg);
    opacity: 1;
  }
}

/* 睡眠状态摇摆 */
.cute-mascot.is-sleeping .mascot-avatar {
  animation: sleeping-sway 3s ease-in-out infinite;
}

@keyframes sleeping-sway {
  0%,
  100% {
    transform: rotate(-2deg);
  }
  50% {
    transform: rotate(2deg);
  }
}

/* 询问气泡 */
.ask-bubble {
  position: absolute;
  bottom: 100px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: flex-start;
  gap: 10px;
  background: linear-gradient(135deg, #fff 0%, #fefce8 100%);
  padding: 12px 14px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
  border: 2px solid #fef3c7;
  z-index: 100;
  min-width: 200px;
  max-width: 280px;
}

.ask-icon {
  font-size: 24px;
  flex-shrink: 0;
  animation: cat-bounce 1s ease infinite;
}

@keyframes cat-bounce {
  0%,
  100% {
    transform: translateY(0) rotate(-5deg);
  }
  50% {
    transform: translateY(-3px) rotate(5deg);
  }
}

.ask-content {
  flex: 1;
}

.ask-text {
  font-size: 13px;
  color: #334155;
  font-weight: 500;
  margin-bottom: 10px;
}

.ask-actions {
  display: flex;
  gap: 8px;
}

.ask-btn {
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 500;
  border: 1px solid #e2e8f0;
  background: #fff;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s ease;
}

.ask-btn:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
  transform: translateY(-1px);
}

.ask-btn.primary {
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  border-color: #f59e0b;
  color: #fff;
}

.ask-btn.primary:hover {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  border-color: #d97706;
}

.ask-close {
  position: absolute;
  top: 6px;
  right: 8px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: none;
  background: transparent;
  color: #94a3b8;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.ask-close:hover {
  background: #f1f5f9;
  color: #475569;
}

/* 询问气泡动画 */
.ask-bubble-enter-active {
  animation: ask-bubble-in 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.ask-bubble-leave-active {
  animation: ask-bubble-out 0.2s ease;
}

@keyframes ask-bubble-in {
  0% {
    opacity: 0;
    transform: translateX(-50%) translateY(10px) scale(0.9);
  }
  100% {
    opacity: 1;
    transform: translateX(-50%) translateY(0) scale(1);
  }
}

@keyframes ask-bubble-out {
  0% {
    opacity: 1;
    transform: translateX(-50%) translateY(0) scale(1);
  }
  100% {
    opacity: 0;
    transform: translateX(-50%) translateY(10px) scale(0.9);
  }
}

/* 深色模式 */
[data-theme="dark"] .avatar-glow {
  background: radial-gradient(
    circle,
    rgba(217, 119, 6, 0.4) 0%,
    rgba(217, 119, 6, 0) 70%
  );
}

[data-theme="dark"] .avatar-bg {
  background: linear-gradient(135deg, #78350f 0%, #92400e 50%, #b45309 100%);
  box-shadow: 0 4px 15px rgba(180, 83, 9, 0.4),
    inset 0 2px 10px rgba(255, 255, 255, 0.1);
}

[data-theme="dark"] .speech-bubble {
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  color: #e2e8f0;
  border-color: #92400e;
}

[data-theme="dark"] .bubble-tail {
  border-top-color: #1e293b;
}

[data-theme="dark"] .z {
  color: #fbbf24;
}

[data-theme="dark"] .ask-bubble {
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  border-color: #92400e;
}

[data-theme="dark"] .ask-text {
  color: #e2e8f0;
}

[data-theme="dark"] .ask-btn {
  background: #334155;
  border-color: #475569;
  color: #e2e8f0;
}

[data-theme="dark"] .ask-btn:hover {
  background: #475569;
  border-color: #64748b;
}

[data-theme="dark"] .ask-btn.primary {
  background: linear-gradient(135deg, #d97706 0%, #b45309 100%);
  border-color: #b45309;
}

[data-theme="dark"] .ask-btn.primary:hover {
  background: linear-gradient(135deg, #b45309 0%, #92400e 100%);
}

[data-theme="dark"] .ask-close {
  color: #64748b;
}

[data-theme="dark"] .ask-close:hover {
  background: #334155;
  color: #e2e8f0;
}
</style>
