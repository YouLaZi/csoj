<template>
  <div id="basicLayout">
    <a-layout style="min-height: 100vh">
      <a-layout-header class="layout-header">
        <GlobalHeader ref="headerRef" />
      </a-layout-header>
      <a-layout-content class="layout-content">
        <router-view />
      </a-layout-content>
    </a-layout>
  </div>
</template>

<script setup>
import GlobalHeader from "@/components/GlobalHeader";
import { ref, onMounted } from "vue";
import { useRoute } from "vue-router";

const route = useRoute();
const headerRef = ref(null);

onMounted(() => {
  if (route.query.showLogin === "true" && headerRef.value) {
    headerRef.value.showLoginModal();
  }

  if (route.query.showRegister === "true" && headerRef.value) {
    headerRef.value.showRegisterModal();
  }
});
</script>

<style scoped>
/* ========================================
   基础布局 - 简约大方
   ======================================== */

#basicLayout {
  min-height: 100vh;
  background-color: var(--bg-color);
}

.layout-header {
  position: sticky;
  top: 0;
  z-index: 100;
  padding: 0;
  height: auto;
  line-height: normal;
  background: transparent;
}

.layout-content {
  background: var(--bg-color);
  min-height: calc(100vh - var(--header-height));
}

/* ========================================
   深色模式
   ======================================== */

[data-theme="dark"] #basicLayout {
  background-color: var(--bg-color);
}

[data-theme="dark"] .layout-content {
  background-color: var(--bg-color);
}
</style>
