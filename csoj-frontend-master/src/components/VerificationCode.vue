<template>
  <div class="verification-code">
    <canvas
      ref="canvas"
      :width="width"
      :height="height"
      @click="refreshCode"
    ></canvas>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";

const props = defineProps({
  width: {
    type: Number,
    default: 120,
  },
  height: {
    type: Number,
    default: 40,
  },
  length: {
    type: Number,
    default: 4,
  },
});

const canvas = ref<HTMLCanvasElement | null>(null);
const code = ref("");

// 定义验证码中可能出现的字符
const characters = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";

// 生成随机验证码
const generateCode = () => {
  let result = "";
  for (let i = 0; i < props.length; i++) {
    const randomIndex = Math.floor(Math.random() * characters.length);
    result += characters.charAt(randomIndex);
  }
  code.value = result;
  return result;
};

// 绘制验证码
const drawCode = () => {
  if (!canvas.value) return;

  const ctx = canvas.value.getContext("2d");
  if (!ctx) return;

  // 清空画布
  ctx.clearRect(0, 0, props.width, props.height);

  // 设置背景色 - 使用冷色调
  ctx.fillStyle = "#f0f5f9";
  ctx.fillRect(0, 0, props.width, props.height);

  // 生成新的验证码
  const newCode = generateCode();

  // 绘制验证码文字
  ctx.font = "bold 22px serif";
  ctx.textBaseline = "middle";

  // 绘制每个字符
  for (let i = 0; i < newCode.length; i++) {
    // 随机颜色 - 使用冷色调
    const colors = ["#3e4c59", "#2c3e50", "#34495e", "#1a365d", "#2a4365"];
    ctx.fillStyle = colors[Math.floor(Math.random() * colors.length)];

    // 随机旋转角度
    const angle = (Math.random() - 0.5) * 0.3;
    ctx.save();
    ctx.translate(30 * i + 15, props.height / 2);
    ctx.rotate(angle);

    // 绘制字符
    ctx.fillText(newCode.charAt(i), -10, 0);
    ctx.restore();
  }

  // 绘制干扰线
  for (let i = 0; i < 3; i++) {
    ctx.strokeStyle = `rgba(${Math.random() * 100 + 50}, ${
      Math.random() * 100 + 50
    }, ${Math.random() * 100 + 100}, 0.3)`;
    ctx.beginPath();
    ctx.moveTo(Math.random() * props.width, Math.random() * props.height);
    ctx.lineTo(Math.random() * props.width, Math.random() * props.height);
    ctx.stroke();
  }

  // 绘制干扰点
  for (let i = 0; i < 30; i++) {
    ctx.fillStyle = `rgba(${Math.random() * 100 + 50}, ${
      Math.random() * 100 + 50
    }, ${Math.random() * 100 + 100}, 0.3)`;
    ctx.beginPath();
    ctx.arc(
      Math.random() * props.width,
      Math.random() * props.height,
      1,
      0,
      2 * Math.PI
    );
    ctx.fill();
  }
};

// 刷新验证码
const refreshCode = () => {
  drawCode();
};

// 暴露验证码值和刷新方法
defineExpose({
  code,
  refreshCode,
});

onMounted(() => {
  drawCode();
});
</script>

<style scoped>
.verification-code {
  display: inline-block;
  cursor: pointer;
  user-select: none;
}

canvas {
  border-radius: 4px;
  vertical-align: middle;
}
</style>
