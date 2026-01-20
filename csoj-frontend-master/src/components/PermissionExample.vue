<template>
  <div class="permission-example">
    <h2>权限控制示例</h2>

    <!-- 权限模拟切换 -->
    <a-card title="权限模拟" :bordered="false" class="permission-simulator">
      <a-space direction="vertical" size="medium" style="width: 100%">
        <a-alert type="info" show-icon>
          <template #message>当前模拟角色: {{ currentRoleLabel }}</template>
          <template #description>您可以切换不同角色来测试权限控制效果</template>
        </a-alert>

        <a-radio-group
          v-model="currentRole"
          type="button"
          @change="handleRoleChange"
        >
          <a-radio value="guest">访客</a-radio>
          <a-radio value="user">普通用户</a-radio>
          <a-radio value="admin">管理员</a-radio>
        </a-radio-group>
      </a-space>
    </a-card>

    <a-divider>使用PermissionControl组件</a-divider>

    <a-space direction="vertical" size="large" style="width: 100%">
      <!-- 使用PermissionControl组件控制内容显示 -->
      <PermissionControl permission="user:read">
        <a-card title="用户信息" :bordered="false" class="permission-card">
          <template #extra>
            <a-tag color="green">user:read</a-tag>
          </template>
          <p>这个内容只有拥有 user:read 权限的用户才能看到</p>
          <a-descriptions :column="1" size="small">
            <a-descriptions-item label="用户ID">10001</a-descriptions-item>
            <a-descriptions-item label="用户名">张三</a-descriptions-item>
            <a-descriptions-item label="邮箱"
              >zhangsan@example.com</a-descriptions-item
            >
          </a-descriptions>
        </a-card>

        <!-- 自定义无权限时显示的内容 -->
        <template #fallback>
          <a-card
            title="无权限"
            :bordered="false"
            class="permission-card permission-denied"
          >
            <template #extra>
              <a-tag color="red">user:read</a-tag>
            </template>
            <a-result
              status="403"
              title="无权限"
              sub-title="您没有查看用户信息的权限"
            />
          </a-card>
        </template>
      </PermissionControl>

      <!-- 使用PermissionControl组件检查多个权限（满足任一权限即可） -->
      <PermissionControl
        :permission="['question:edit', 'question:delete']"
        logic="ANY"
      >
        <a-card title="题目管理" :bordered="false" class="permission-card">
          <template #extra>
            <a-space>
              <a-tag color="green">question:edit</a-tag>
              <a-tag color="green">question:delete</a-tag>
              <a-tag color="blue">ANY</a-tag>
            </a-space>
          </template>
          <p>这个内容只有拥有题目编辑或删除权限的用户才能看到</p>
          <a-space>
            <a-button type="primary">编辑题目</a-button>
            <a-button type="primary" status="danger">删除题目</a-button>
          </a-space>
        </a-card>

        <template #fallback>
          <a-card
            title="无权限"
            :bordered="false"
            class="permission-card permission-denied"
          >
            <template #extra>
              <a-space>
                <a-tag color="red">question:edit</a-tag>
                <a-tag color="red">question:delete</a-tag>
                <a-tag color="blue">ANY</a-tag>
              </a-space>
            </template>
            <a-result
              status="403"
              title="无权限"
              sub-title="您需要拥有题目编辑或删除权限"
            />
          </a-card>
        </template>
      </PermissionControl>

      <!-- 使用PermissionControl组件检查多个权限（必须满足所有权限） -->
      <PermissionControl :permission="['post:edit', 'post:delete']" logic="ALL">
        <a-card title="帖子高级管理" :bordered="false" class="permission-card">
          <template #extra>
            <a-space>
              <a-tag color="green">post:edit</a-tag>
              <a-tag color="green">post:delete</a-tag>
              <a-tag color="purple">ALL</a-tag>
            </a-space>
          </template>
          <p>这个内容只有同时拥有帖子编辑和删除权限的用户才能看到</p>
          <a-space>
            <a-button type="primary">批量编辑</a-button>
            <a-button type="primary" status="danger">批量删除</a-button>
          </a-space>
        </a-card>

        <template #fallback>
          <a-card
            title="无权限"
            :bordered="false"
            class="permission-card permission-denied"
          >
            <template #extra>
              <a-space>
                <a-tag color="red">post:edit</a-tag>
                <a-tag color="red">post:delete</a-tag>
                <a-tag color="purple">ALL</a-tag>
              </a-space>
            </template>
            <a-result
              status="403"
              title="无权限"
              sub-title="您需要同时拥有帖子编辑和删除权限"
            />
          </a-card>
        </template>
      </PermissionControl>
    </a-space>

    <a-divider>使用v-permission指令</a-divider>

    <a-space direction="vertical" size="large" style="width: 100%">
      <a-card :bordered="false" class="permission-card">
        <template #title>按钮权限控制</template>
        <a-space>
          <!-- 使用v-permission指令控制元素显示 -->
          <a-button type="primary" v-permission="'user:edit'">
            编辑用户信息
            <a-tag size="small" color="green">user:edit</a-tag>
          </a-button>

          <!-- 使用v-permission指令检查多个权限（满足任一权限即可） -->
          <a-button
            type="primary"
            v-permission="['question:create', 'question:edit']"
          >
            创建或编辑题目
            <a-tag size="small" color="green">question:create/edit</a-tag>
          </a-button>

          <!-- 使用v-permission指令检查多个权限（必须满足所有权限） -->
          <a-button
            type="primary"
            status="danger"
            v-permission.all="['admin:access', 'user:delete']"
          >
            删除用户
            <a-tag size="small" color="green">admin:access+user:delete</a-tag>
          </a-button>
        </a-space>
        <p class="permission-note">注意：不满足权限条件的按钮将不会显示</p>
      </a-card>
    </a-space>

    <a-divider>当前权限列表</a-divider>

    <a-space direction="vertical" size="large" style="width: 100%">
      <a-card title="当前用户权限" :bordered="false" class="permission-card">
        <template #extra>
          <a-button type="text" @click="refreshPermissions">
            <template #icon><icon-refresh /></template>
            刷新权限
          </a-button>
        </template>
        <a-empty
          v-if="!userPermissions.length"
          description="未获取到权限或用户未登录"
        />
        <div v-else class="permission-tags">
          <a-tag
            v-for="permission in userPermissions"
            :key="permission"
            color="green"
          >
            {{ permission }}
          </a-tag>
        </div>
      </a-card>
    </a-space>

    <!-- <a-divider>系统架构图</a-divider>

    <SystemArchitectureDiagram /> -->
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { Message } from "@arco-design/web-vue";
import { IconRefresh } from "@arco-design/web-vue/es/icon";
import PermissionControl from "./PermissionControl.vue";
import SystemArchitectureDiagram from "./SystemArchitectureDiagram.vue";
import {
  fetchUserPermissions,
  clearPermissionCache,
  hasPermission,
} from "@/services/PermissionService";
import ACCESS_ENUM from "@/access/accessEnum";
import store from "@/store";

// 用户权限列表
const userPermissions = ref<string[]>([]);

// 当前模拟角色
const currentRole = ref<string>("user");

// 角色对应的权限映射
const rolePermissionsMap = {
  guest: [],
  user: ["user:read"],
  admin: [
    "user:read",
    "user:edit",
    "user:delete",
    "question:create",
    "question:edit",
    "question:delete",
    "post:edit",
    "post:delete",
    "admin:access",
  ],
};

// 角色标签映射
const roleLabelMap = {
  guest: "访客",
  user: "普通用户",
  admin: "管理员",
};

// 当前角色标签
const currentRoleLabel = computed(() => {
  return (
    roleLabelMap[currentRole.value as keyof typeof roleLabelMap] || "未知角色"
  );
});

// 获取用户权限
const loadPermissions = async () => {
  // 模拟获取权限
  userPermissions.value =
    rolePermissionsMap[currentRole.value as keyof typeof rolePermissionsMap] ||
    [];
};

// 刷新权限
const refreshPermissions = async () => {
  clearPermissionCache();
  await loadPermissions();
  Message.success({
    content: `已刷新权限，当前角色: ${currentRoleLabel.value}`,
    duration: 2000,
  });
};

// 处理角色切换
const handleRoleChange = async () => {
  // 模拟用户角色切换
  const role = currentRole.value;

  // 更新权限
  await loadPermissions();

  // 模拟更新用户角色
  const userRole =
    role === "admin"
      ? ACCESS_ENUM.ADMIN
      : role === "user"
      ? ACCESS_ENUM.USER
      : ACCESS_ENUM.NOT_LOGIN;

  // 更新store中的用户角色
  if (store.state.user && store.state.user.loginUser) {
    store.commit("user/updateUserRole", userRole);
  }

  Message.success({
    content: `已切换为${currentRoleLabel.value}角色`,
    duration: 2000,
  });
};

// 组件挂载时获取用户权限
onMounted(() => {
  loadPermissions();
});
</script>

<style scoped>
.permission-example {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.permission-simulator {
  margin-bottom: 20px;
  background-color: var(--bg-color);
  border: 1px solid var(--border-color);
}

.permission-card {
  margin-bottom: 16px;
  background-color: var(--bg-color);
  border: 1px solid var(--border-color);
  transition: all 0.3s;
}

.permission-card:hover {
  box-shadow: var(--box-shadow);
}

.permission-denied {
  border-color: var(--danger-color);
  border-style: dashed;
  opacity: 0.9;
}

.permission-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.permission-note {
  margin-top: 16px;
  font-size: 12px;
  color: var(--text-color-secondary);
}

/* 深色模式适配 */
:root[data-theme="dark"] .permission-card {
  background-color: var(--bg-color-secondary);
}

:root[data-theme="dark"] .permission-denied {
  background-color: rgba(245, 63, 63, 0.05);
}
</style>
