<template>
  <div>
    <!-- 如果用户有权限，则显示插槽内容 -->
    <slot v-if="hasPermission"></slot>
    <!-- 如果用户没有权限且提供了fallback插槽，则显示fallback内容 -->
    <slot v-else name="fallback">
      <!-- 默认的无权限提示 -->
      <div v-if="showTip" class="no-permission-tip">
        <a-alert type="warning" :message="tipMessage" banner />
      </div>
    </slot>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from "vue";
import { useStore } from "vuex";
// import { Message } from "@arco-design/web-vue"; // Message seems unused if default tip is an Alert
import ACCESS_ENUM from "@/access/accessEnum";
import {
  fetchUserPermissions as serviceFetchUserPermissions,
  hasPermission as serviceHasPermission,
} from "../services/PermissionService";

const store = useStore();

// 组件属性
const props = defineProps({
  // 所需权限，可以是单个权限字符串或权限数组
  permission: {
    type: [String, Array],
    required: true,
  },
  // 权限检查逻辑，'ANY'表示满足任一权限即可，'ALL'表示必须满足所有权限
  logic: {
    type: String,
    default: "ANY",
    validator: (value: string) => ["ANY", "ALL"].includes(value),
  },
  // 是否显示无权限提示
  showTip: {
    type: Boolean,
    default: true,
  },
  // 无权限提示信息
  tipMessage: {
    type: String,
    default: "您没有权限执行此操作",
  },
});

// 判断用户是否拥有所需权限
const hasPermission = computed(() => {
  return serviceHasPermission(
    props.permission as string | string[],
    props.logic as "ANY" | "ALL"
  );
});

// 组件挂载时确保用户权限已加载
onMounted(async () => {
  // 调用服务中的权限获取函数，它内部有缓存逻辑
  await serviceFetchUserPermissions();
});
</script>

<style scoped>
.no-permission-tip {
  padding: 8px 0;
}
</style>
