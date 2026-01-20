<template>
  <div class="no-auth-view">
    <a-result
      status="403"
      title="无权限访问"
      sub-title="抱歉，您没有权限访问此页面"
    >
      <template #extra>
        <a-space>
          <a-button type="primary" @click="$router.push('/')"
            >返回首页</a-button
          >
          <a-button @click="$router.go(-1)">返回上一页</a-button>
        </a-space>
      </template>
    </a-result>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from "vue";

const router = useRouter();
</script>

<style scoped>
.no-auth-view {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
}
</style>
