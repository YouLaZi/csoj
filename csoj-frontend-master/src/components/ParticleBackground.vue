<template>
  <div class="particle-background" ref="containerRef">
    <canvas ref="canvasRef" class="particle-canvas"></canvas>
    <div class="gradient-overlay"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from "vue";

// Props
const props = defineProps<{
  particleCount?: number;
  color?: string;
  speed?: number;
  linkDistance?: number;
  mouseInteraction?: boolean;
}>();

const containerRef = ref<HTMLDivElement | null>(null);
const canvasRef = ref<HTMLCanvasElement | null>(null);

// 配置
const config = {
  particleCount: props.particleCount || 80,
  color: props.color || "rgba(79, 111, 143, 0.6)",
  speed: props.speed || 0.5,
  linkDistance: props.linkDistance || 120,
  mouseInteraction: props.mouseInteraction !== false,
};

// 粒子类
class Particle {
  x: number;
  y: number;
  vx: number;
  vy: number;
  radius: number;
  opacity: number;

  constructor(canvas: HTMLCanvasElement) {
    this.x = Math.random() * canvas.width;
    this.y = Math.random() * canvas.height;
    this.vx = (Math.random() - 0.5) * config.speed;
    this.vy = (Math.random() - 0.5) * config.speed;
    this.radius = Math.random() * 2 + 1;
    this.opacity = Math.random() * 0.5 + 0.3;
  }

  update(canvas: HTMLCanvasElement, mouseX: number, mouseY: number) {
    // 鼠标交互
    if (config.mouseInteraction && mouseX > 0 && mouseY > 0) {
      const dx = mouseX - this.x;
      const dy = mouseY - this.y;
      const dist = Math.sqrt(dx * dx + dy * dy);
      if (dist < 100) {
        const force = (100 - dist) / 100;
        this.vx -= (dx / dist) * force * 0.02;
        this.vy -= (dy / dist) * force * 0.02;
      }
    }

    this.x += this.vx;
    this.y += this.vy;

    // 边界检测
    if (this.x < 0 || this.x > canvas.width) this.vx *= -1;
    if (this.y < 0 || this.y > canvas.height) this.vy *= -1;

    // 限制速度
    const maxSpeed = config.speed * 2;
    this.vx = Math.max(-maxSpeed, Math.min(maxSpeed, this.vx));
    this.vy = Math.max(-maxSpeed, Math.min(maxSpeed, this.vy));
  }

  draw(ctx: CanvasRenderingContext2D) {
    ctx.beginPath();
    ctx.arc(this.x, this.y, this.radius, 0, Math.PI * 2);
    ctx.fillStyle = config.color.replace("0.6", String(this.opacity));
    ctx.fill();
  }
}

let animationId: number | null = null;
let particles: Particle[] = [];
let mouseX = 0;
let mouseY = 0;
let isDarkMode = false;

// 初始化
const init = () => {
  const canvas = canvasRef.value;
  const container = containerRef.value;
  if (!canvas || !container) return;

  const ctx = canvas.getContext("2d");
  if (!ctx) return;

  // 设置画布大小
  canvas.width = container.offsetWidth;
  canvas.height = container.offsetHeight;

  // 创建粒子
  particles = [];
  for (let i = 0; i < config.particleCount; i++) {
    particles.push(new Particle(canvas));
  }
};

// 动画循环
const animate = () => {
  const canvas = canvasRef.value;
  if (!canvas) return;

  const ctx = canvas.getContext("2d");
  if (!ctx) return;

  ctx.clearRect(0, 0, canvas.width, canvas.height);

  // 更新和绘制粒子
  particles.forEach((particle) => {
    particle.update(canvas, mouseX, mouseY);
    particle.draw(ctx);
  });

  // 绘制连线
  ctx.strokeStyle = isDarkMode
    ? "rgba(107, 155, 195, 0.15)"
    : "rgba(79, 111, 143, 0.1)";
  ctx.lineWidth = 0.5;

  for (let i = 0; i < particles.length; i++) {
    for (let j = i + 1; j < particles.length; j++) {
      const dx = particles[i].x - particles[j].x;
      const dy = particles[i].y - particles[j].y;
      const dist = Math.sqrt(dx * dx + dy * dy);

      if (dist < config.linkDistance) {
        const opacity = (1 - dist / config.linkDistance) * 0.5;
        ctx.globalAlpha = opacity;
        ctx.beginPath();
        ctx.moveTo(particles[i].x, particles[i].y);
        ctx.lineTo(particles[j].x, particles[j].y);
        ctx.stroke();
      }
    }
  }
  ctx.globalAlpha = 1;

  animationId = requestAnimationFrame(animate);
};

// 窗口大小变化
const handleResize = () => {
  const canvas = canvasRef.value;
  const container = containerRef.value;
  if (!canvas || !container) return;

  canvas.width = container.offsetWidth;
  canvas.height = container.offsetHeight;

  // 重新初始化粒子位置
  particles.forEach((particle) => {
    if (particle.x > canvas.width) particle.x = Math.random() * canvas.width;
    if (particle.y > canvas.height) particle.y = Math.random() * canvas.height;
  });
};

// 鼠标移动
const handleMouseMove = (e: MouseEvent) => {
  const canvas = canvasRef.value;
  if (!canvas) return;
  const rect = canvas.getBoundingClientRect();
  mouseX = e.clientX - rect.left;
  mouseY = e.clientY - rect.top;
};

// 监听主题变化
const observeThemeChange = () => {
  const observer = new MutationObserver((mutations) => {
    mutations.forEach((mutation) => {
      if (mutation.attributeName === "data-theme") {
        isDarkMode =
          document.documentElement.getAttribute("data-theme") === "dark";
        config.color = isDarkMode
          ? "rgba(107, 155, 195, 0.6)"
          : "rgba(79, 111, 143, 0.6)";
      }
    });
  });

  observer.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ["data-theme"],
  });

  return observer;
};

let themeObserver: MutationObserver | null = null;

onMounted(() => {
  isDarkMode = document.documentElement.getAttribute("data-theme") === "dark";
  if (isDarkMode) {
    config.color = "rgba(107, 155, 195, 0.6)";
  }

  init();
  animate();

  window.addEventListener("resize", handleResize);
  document.addEventListener("mousemove", handleMouseMove);
  themeObserver = observeThemeChange();
});

onUnmounted(() => {
  if (animationId) {
    cancelAnimationFrame(animationId);
  }
  window.removeEventListener("resize", handleResize);
  document.removeEventListener("mousemove", handleMouseMove);
  if (themeObserver) {
    themeObserver.disconnect();
  }
});
</script>

<style scoped>
.particle-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: -1;
  overflow: hidden;
}

.particle-canvas {
  display: block;
  width: 100%;
  height: 100%;
}

.gradient-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(
    ellipse at center,
    transparent 0%,
    var(--bg-color, #fafbfc) 100%
  );
  pointer-events: none;
}

[data-theme="dark"] .gradient-overlay {
  background: radial-gradient(
    ellipse at center,
    transparent 0%,
    var(--bg-color, #14181e) 100%
  );
}
</style>
