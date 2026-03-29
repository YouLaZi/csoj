<template>
  <Teleport to="body">
    <transition name="celebration">
      <div v-if="isVisible" class="celebration-overlay" @click="close">
        <!-- 烟花效果 -->
        <div class="fireworks">
          <div
            v-for="firework in fireworks"
            :key="firework.id"
            class="firework"
            :style="{
              left: firework.x + '%',
              top: firework.y + '%',
              '--delay': firework.delay + 's',
            }"
          >
            <div
              v-for="i in 12"
              :key="i"
              class="spark"
              :style="{
                '--angle': i * 30 + 'deg',
                '--color': firework.colors[i % firework.colors.length],
              }"
            ></div>
          </div>
        </div>

        <!-- 五彩纸屑 -->
        <div class="confetti-container">
          <div
            v-for="confetti in confettis"
            :key="confetti.id"
            class="confetti"
            :style="{
              left: confetti.x + '%',
              '--delay': confetti.delay + 's',
              '--duration': confetti.duration + 's',
              '--color': confetti.color,
              '--rotation': confetti.rotation + 'deg',
            }"
          ></div>
        </div>

        <!-- 中心消息 -->
        <div class="celebration-message">
          <div class="emoji">{{ emoji }}</div>
          <div class="title">{{ title }}</div>
          <div class="subtitle">{{ subtitle }}</div>
        </div>

        <!-- 星星闪烁 -->
        <div class="stars">
          <div
            v-for="i in 20"
            :key="i"
            class="star"
            :style="getStarStyle(i)"
          ></div>
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";

// Props
const props = defineProps<{
  type?: "success" | "level-up" | "achievement" | "streak" | "custom";
  title?: string;
  subtitle?: string;
  emoji?: string;
  duration?: number;
}>();

// Emits
const emit = defineEmits<{
  (e: "close"): void;
}>();

const isVisible = ref(false);

// 预设配置
const presets = {
  success: {
    title: "太棒了！",
    subtitle: "代码通过测试！",
    emoji: "🎉",
  },
  "level-up": {
    title: "升级了！",
    subtitle: "你的等级提升了！",
    emoji: "⭐",
  },
  achievement: {
    title: "成就解锁！",
    subtitle: "你获得了一个新成就！",
    emoji: "🏆",
  },
  streak: {
    title: "连续签到！",
    subtitle: "继续保持！",
    emoji: "🔥",
  },
  custom: {
    title: "",
    subtitle: "",
    emoji: "✨",
  },
};

const type = computed(() => props.type || "success");
const preset = computed(() => presets[type.value]);

const title = computed(() => props.title || preset.value.title);
const subtitle = computed(() => props.subtitle || preset.value.subtitle);
const emoji = computed(() => props.emoji || preset.value.emoji);

// 烟花数据
const fireworks = computed(() => [
  { id: 1, x: 20, y: 30, delay: 0, colors: ["#ff6b6b", "#ffd93d", "#6bcb77"] },
  {
    id: 2,
    x: 80,
    y: 25,
    delay: 0.2,
    colors: ["#4d96ff", "#6bcb77", "#ff6b6b"],
  },
  {
    id: 3,
    x: 50,
    y: 20,
    delay: 0.4,
    colors: ["#ffd93d", "#4d96ff", "#ff6b6b"],
  },
  {
    id: 4,
    x: 30,
    y: 40,
    delay: 0.6,
    colors: ["#6bcb77", "#ffd93d", "#4d96ff"],
  },
  {
    id: 5,
    x: 70,
    y: 35,
    delay: 0.8,
    colors: ["#ff6b6b", "#4d96ff", "#6bcb77"],
  },
]);

// 五彩纸屑数据
const confettiColors = [
  "#ff6b6b",
  "#ffd93d",
  "#6bcb77",
  "#4d96ff",
  "#9b59b6",
  "#e74c3c",
];
const confettis = computed(() => {
  return Array.from({ length: 50 }, (_, i) => ({
    id: i,
    x: Math.random() * 100,
    delay: Math.random() * 0.5,
    duration: 2 + Math.random() * 2,
    color: confettiColors[Math.floor(Math.random() * confettiColors.length)],
    rotation: Math.random() * 360,
  }));
});

// 星星样式
const getStarStyle = (index: number) => {
  return {
    left: `${Math.random() * 100}%`,
    top: `${Math.random() * 100}%`,
    "--delay": `${Math.random() * 2}s`,
    "--size": `${Math.random() * 10 + 5}px`,
  };
};

// 显示庆祝效果
const show = () => {
  isVisible.value = true;
  const duration = props.duration || 4000;
  setTimeout(() => {
    close();
  }, duration);
};

// 关闭
const close = () => {
  isVisible.value = false;
  emit("close");
};

// 暴露方法
defineExpose({
  show,
  close,
});
</script>

<style scoped>
.celebration-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  cursor: pointer;
  overflow: hidden;
}

.celebration-enter-active {
  animation: fade-in 0.3s ease;
}

.celebration-leave-active {
  animation: fade-out 0.3s ease;
}

@keyframes fade-in {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes fade-out {
  from {
    opacity: 1;
  }
  to {
    opacity: 0;
  }
}

/* 烟花效果 */
.fireworks {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.firework {
  position: absolute;
  animation: firework-burst 1.5s ease-out forwards;
  animation-delay: var(--delay);
}

@keyframes firework-burst {
  0% {
    transform: scale(0);
    opacity: 1;
  }
  50% {
    transform: scale(1);
    opacity: 1;
  }
  100% {
    transform: scale(1.5);
    opacity: 0;
  }
}

.spark {
  position: absolute;
  width: 4px;
  height: 15px;
  background: var(--color);
  border-radius: 2px;
  transform-origin: center bottom;
  transform: rotate(var(--angle)) translateY(-50px);
  animation: spark-fly 1s ease-out forwards;
  animation-delay: var(--delay);
}

@keyframes spark-fly {
  0% {
    opacity: 1;
    transform: rotate(var(--angle)) translateY(-50px) scaleY(1);
  }
  100% {
    opacity: 0;
    transform: rotate(var(--angle)) translateY(-120px) scaleY(0.5);
  }
}

/* 五彩纸屑 */
.confetti-container {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.confetti {
  position: absolute;
  top: -20px;
  width: 10px;
  height: 10px;
  background: var(--color);
  transform: rotate(var(--rotation));
  animation: confetti-fall var(--duration) ease-in forwards;
  animation-delay: var(--delay);
}

@keyframes confetti-fall {
  0% {
    transform: translateY(0) rotate(var(--rotation));
    opacity: 1;
  }
  100% {
    transform: translateY(100vh) rotate(calc(var(--rotation) + 720deg));
    opacity: 0;
  }
}

/* 中心消息 */
.celebration-message {
  text-align: center;
  z-index: 10;
  animation: message-pop 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes message-pop {
  0% {
    transform: scale(0);
    opacity: 0;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.emoji {
  font-size: 80px;
  margin-bottom: 16px;
  animation: emoji-bounce 0.6s ease infinite;
}

@keyframes emoji-bounce {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-15px);
  }
}

.title {
  font-size: 48px;
  font-weight: bold;
  color: #fff;
  text-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  margin-bottom: 8px;
}

.subtitle {
  font-size: 20px;
  color: rgba(255, 255, 255, 0.8);
}

/* 星星闪烁 */
.stars {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.star {
  position: absolute;
  width: var(--size);
  height: var(--size);
  background: #ffd93d;
  clip-path: polygon(
    50% 0%,
    61% 35%,
    98% 35%,
    68% 57%,
    79% 91%,
    50% 70%,
    21% 91%,
    32% 57%,
    2% 35%,
    39% 35%
  );
  animation: star-twinkle 1.5s ease-in-out infinite;
  animation-delay: var(--delay);
}

@keyframes star-twinkle {
  0%,
  100% {
    opacity: 0.3;
    transform: scale(0.8);
  }
  50% {
    opacity: 1;
    transform: scale(1.2);
  }
}
</style>
