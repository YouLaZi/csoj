<template>
  <Teleport to="body">
    <transition name="easter-egg">
      <div v-if="isActive" class="konami-effect" @click="close">
        <!-- 彩虹背景 -->
        <div class="rainbow-bg"></div>

        <!-- 飘落的表情 -->
        <div class="emoji-rain">
          <div
            v-for="emoji in emojis"
            :key="emoji.id"
            class="emoji-item"
            :style="{
              left: emoji.x + '%',
              '--delay': emoji.delay + 's',
              '--duration': emoji.duration + 's',
              '--size': emoji.size + 'px',
            }"
          >
            {{ emoji.char }}
          </div>
        </div>

        <!-- 彩虹文字 -->
        <div class="rainbow-text">
          <span
            v-for="(char, index) in secretMessage"
            :key="index"
            :style="{ '--i': index }"
          >
            {{ char }}
          </span>
        </div>

        <!-- 小彩蛋提示 -->
        <div class="easter-egg-hint">
          <span>🎮 你发现了彩蛋！</span>
          <span class="sub">Konami Code Activated!</span>
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from "vue";

// Emits
const emit = defineEmits<{
  (e: "activated"): void;
}>();

const isActive = ref(false);

// Konami Code: ↑ ↑ ↓ ↓ ← → ← → B A
const konamiCode = [
  "ArrowUp",
  "ArrowUp",
  "ArrowDown",
  "ArrowDown",
  "ArrowLeft",
  "ArrowRight",
  "ArrowLeft",
  "ArrowRight",
  "KeyB",
  "KeyA",
];

let inputSequence: string[] = [];
const maxSequenceLength = konamiCode.length;

// 表情数据
const emojiChars = [
  "🎮",
  "🕹️",
  "👾",
  "🚀",
  "⭐",
  "🌟",
  "✨",
  "🎊",
  "🎉",
  "💫",
  "🔥",
  "💡",
];
const emojis = Array.from({ length: 60 }, (_, i) => ({
  id: i,
  x: Math.random() * 100,
  delay: Math.random() * 2,
  duration: 3 + Math.random() * 3,
  size: 20 + Math.random() * 30,
  char: emojiChars[Math.floor(Math.random() * emojiChars.length)],
}));

const secretMessage = "HELLO, GAMER!";

// 监听键盘事件
const handleKeydown = (e: KeyboardEvent) => {
  // 添加到序列
  inputSequence.push(e.code);

  // 保持序列长度
  if (inputSequence.length > maxSequenceLength) {
    inputSequence.shift();
  }

  // 检查是否匹配
  if (inputSequence.length === maxSequenceLength) {
    const isMatch = konamiCode.every(
      (code, index) => code === inputSequence[index]
    );
    if (isMatch) {
      activate();
    }
  }
};

// 激活彩蛋
const activate = () => {
  isActive.value = true;
  inputSequence = [];
  emit("activated");

  // 5秒后自动关闭
  setTimeout(() => {
    close();
  }, 5000);
};

// 关闭
const close = () => {
  isActive.value = false;
};

onMounted(() => {
  document.addEventListener("keydown", handleKeydown);
});

onUnmounted(() => {
  document.removeEventListener("keydown", handleKeydown);
});

defineExpose({
  activate,
  close,
});
</script>

<style scoped>
.konami-effect {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 99999;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  overflow: hidden;
}

.easter-egg-enter-active {
  animation: egg-fade-in 0.5s ease;
}

.easter-egg-leave-active {
  animation: egg-fade-out 0.5s ease;
}

@keyframes egg-fade-in {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes egg-fade-out {
  from {
    opacity: 1;
  }
  to {
    opacity: 0;
  }
}

/* 彩虹背景 */
.rainbow-bg {
  position: absolute;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    45deg,
    #ff0000,
    #ff7f00,
    #ffff00,
    #00ff00,
    #0000ff,
    #4b0082,
    #9400d3,
    #ff0000
  );
  background-size: 400% 400%;
  animation: rainbow-shift 3s ease infinite;
  opacity: 0.9;
}

@keyframes rainbow-shift {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

/* 表情雨 */
.emoji-rain {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.emoji-item {
  position: absolute;
  top: -50px;
  font-size: var(--size);
  animation: emoji-fall var(--duration) linear infinite;
  animation-delay: var(--delay);
}

@keyframes emoji-fall {
  0% {
    transform: translateY(-50px) rotate(0deg);
    opacity: 1;
  }
  100% {
    transform: translateY(100vh) rotate(360deg);
    opacity: 0.5;
  }
}

/* 彩虹文字 */
.rainbow-text {
  position: relative;
  z-index: 10;
  font-size: 60px;
  font-weight: bold;
  font-family: "Press Start 2P", cursive, monospace;
  margin-bottom: 30px;
}

.rainbow-text span {
  display: inline-block;
  animation: rainbow-char 0.5s ease forwards;
  animation-delay: calc(var(--i) * 0.1s);
  opacity: 0;
  transform: translateY(20px);
  background: linear-gradient(
    to bottom,
    #ff0000,
    #ff7f00,
    #ffff00,
    #00ff00,
    #0000ff,
    #4b0082,
    #9400d3
  );
  background-size: 100% 700%;
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: rainbow-text-shift 2s linear infinite;
}

@keyframes rainbow-char {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes rainbow-text-shift {
  0% {
    background-position: 0% 0%;
  }
  100% {
    background-position: 0% 700%;
  }
}

/* 彩蛋提示 */
.easter-egg-hint {
  position: relative;
  z-index: 10;
  text-align: center;
  background: rgba(0, 0, 0, 0.7);
  padding: 20px 40px;
  border-radius: 16px;
  backdrop-filter: blur(10px);
  animation: hint-bounce 0.6s ease;
}

.easter-egg-hint span {
  display: block;
  color: #fff;
  font-size: 24px;
  font-weight: bold;
}

.easter-egg-hint .sub {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 8px;
}

@keyframes hint-bounce {
  0% {
    transform: scale(0);
    opacity: 0;
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}
</style>
