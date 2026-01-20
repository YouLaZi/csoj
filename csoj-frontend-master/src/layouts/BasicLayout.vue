<template>
  <div id="basicLayout">
    <a-layout style="min-height: 100vh">
      <a-layout-header class="header">
        <GlobalHeader ref="headerRef" />
      </a-layout-header>
      <a-layout-content class="content">
        <router-view />
      </a-layout-content>
      <!-- <a-layout-footer class="footer">
        <a href="https://github.com/YouLaZi/" target="_blank"> by Pyrrhus </a>
      </a-layout-footer> -->
    </a-layout>
  </div>
</template>

<style scoped>
#basicLayout {
}

#basicLayout .header {
  margin-bottom: 16px;
  box-shadow: var(--box-shadow);
}

#basicLayout .content {
  background: var(--bg-color);
  margin-bottom: 16px;
  padding: 20px;
  border-radius: 8px;
  box-shadow: var(--box-shadow);
}

#basicLayout .footer {
  background: #efefef;
  padding: 16px;
  position: sticky;
  bottom: 0;
  left: 0;
  right: 0;
  text-align: center;
}
</style>
<script setup>
import GlobalHeader from "@/components/GlobalHeader";
import { ref, onMounted } from "vue";
import { useRoute } from "vue-router";

const route = useRoute();
const headerRef = ref(null);

// 监听路由参数，显示登录或注册模态框
onMounted(() => {
  // 检查URL参数是否需要显示登录模态框
  if (route.query.showLogin === "true" && headerRef.value) {
    headerRef.value.showLoginModal();
  }

  // 检查URL参数是否需要显示注册模态框
  if (route.query.showRegister === "true" && headerRef.value) {
    headerRef.value.showRegisterModal();
  }
});
</script>
