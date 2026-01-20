<template>
  <GlobalHeader />
  <div class="user-preferences-view">
    <UserPreferences />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { IconSettings } from "@arco-design/web-vue/es/icon";
import UserPreferences from "@/components/UserPreferences.vue";
import GlobalHeader from "@/components/GlobalHeader.vue";
import UserStatistics from "@/components/UserStatistics.vue";
import { useStore } from "vuex";
import { useRouter } from "vue-router";
import { Message } from "@arco-design/web-vue";
import ACCESS_ENUM from "@/access/accessEnum";

const store = useStore();
const router = useRouter();

// 检查用户是否登录
onMounted(() => {
  const { loginUser } = store.state.user;
  if (!loginUser || loginUser.userRole === ACCESS_ENUM.NOT_LOGIN) {
    Message.warning("请先登录");
    router.push({
      path: "/",
      query: {
        showLogin: "true",
      },
    });
  }
});
</script>

<style scoped>
.user-preferences-view {
  width: 100%;
  margin: 0 auto;
}

.preferences-card {
  background-color: var(--bg-color);
  border-radius: 8px;
  box-shadow: var(--box-shadow);
  transition: box-shadow 0.3s;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-color-primary);
  font-size: 18px;
  font-weight: 500;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .user-preferences-view {
    padding: 10px;
  }
}
</style>
