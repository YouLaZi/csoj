<template>
  <div class="home">
    管理员可见
    <img alt="Vue logo" src="../assets/logo.png" />
    <a-sub-menu key="questionManagement">
      <template #title>题目管理</template>
      <a-menu-item key="题目列表（管理）">
        <router-link to="/manage/question">题目列表</router-link>
      </a-menu-item>
      <a-menu-item key="批量导入题目">
        <router-link to="/manage/question/batch-import"
          >批量导入题目</router-link
        >
      </a-menu-item>
    </a-sub-menu>
    <a-menu-item key="帖子管理">
      <router-link to="/admin/posts">帖子管理</router-link>
    </a-menu-item>
    <a-menu-item key="标签管理">
      <router-link to="/tags">标签管理</router-link>
    </a-menu-item>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";

export default defineComponent({
  name: "HomeView",
  components: {},
});
</script>
