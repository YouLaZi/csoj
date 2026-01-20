<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <div class="error-code">404</div>
      <h1 class="error-title">页面不存在</h1>
      <p class="error-message">抱歉，您访问的页面不存在或已被移除</p>
      <div class="action-buttons">
        <a-button type="primary" @click="goHome">返回首页</a-button>
        <a-button @click="goBack">返回上一页</a-button>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { useRouter } from "vue-router";
import LogService from "@/services/LogService";

const router = useRouter();

// 记录404错误
LogService.warn("用户访问了不存在的页面", { path: window.location.pathname });

// 返回首页
const goHome = () => {
  router.push("/");
};

// 返回上一页
const goBack = () => {
  router.back();
};
</script>

<style scoped>
.not-found-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 64px);
  padding: 20px;
  background-color: var(--color-bg-1);
}

.not-found-content {
  text-align: center;
  max-width: 500px;
  padding: 40px;
  border-radius: 8px;
  background-color: var(--color-bg-2);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.error-code {
  font-size: 120px;
  font-weight: bold;
  color: var(--color-primary);
  line-height: 1;
  margin-bottom: 20px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.error-title {
  font-size: 28px;
  margin-bottom: 16px;
  color: var(--color-text-1);
}

.error-message {
  font-size: 16px;
  margin-bottom: 30px;
  color: var(--color-text-3);
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
}

/* 深色模式适配 */
:root[data-theme="dark"] .not-found-container {
  background-color: var(--color-bg-1);
}

:root[data-theme="dark"] .not-found-content {
  background-color: var(--color-bg-2);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3);
}
</style>
