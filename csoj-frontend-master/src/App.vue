<template>
  <div id="app" class="app-container">
    <template v-if="route.path.startsWith('/user') && !isLoginOrRegister">
      <router-view />
    </template>
    <template v-else>
      <BasicLayout />
    </template>
    <!-- 添加全局聊天机器人组件 -->
    <ChatBot />
  </div>
</template>

<style>
@import "./assets/styles/theme.css";
@import "./assets/styles/global.css";

#app {
  min-height: 100vh;
  transition: background-color 0.3s, color 0.3s;
}

/* 响应式基础样式 */
@media (max-width: 768px) {
  .mobile-hidden {
    display: none !important;
  }

  .mobile-block {
    display: block !important;
  }

  .mobile-flex {
    display: flex !important;
  }
}
</style>
<script setup lang="ts">
import BasicLayout from "@/layouts/BasicLayout";
import { onMounted, computed } from "vue";
import { useRoute } from "vue-router";
import ChatBot from "@/components/ChatBot.vue";
import { ThemeManager } from "@/utils/themeManager";

const route = useRoute();

/**
 * 判断当前路由是否为登录或注册页面
 */
const isLoginOrRegister = computed(() => {
  return route.path === "/user/login" || route.path === "/user/register";
});

/**
 * 全局初始化函数，有全局单次调用的代码，都可以写到这里
 */
const doInit = () => {
  console.log("hello 欢迎来到我的项目");

  // 初始化主题管理器
  ThemeManager.init();

  // 添加响应式设计的视口元标签
  const viewportMeta = document.createElement("meta");
  viewportMeta.name = "viewport";
  viewportMeta.content =
    "width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no";
  document.head.appendChild(viewportMeta);

  // 导入 store
  import("@/store").then(({ default: store }) => {
    // 初始化令牌，确保在页面刷新后能恢复登录状态
    store.dispatch("user/initToken").then(() => {
      // 获取用户信息
      store.dispatch("user/getLoginUser");
    });
  });
};

onMounted(() => {
  doInit();
});
</script>
