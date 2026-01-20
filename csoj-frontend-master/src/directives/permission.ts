import { DirectiveBinding } from "vue";
import { hasPermission } from "@/services/PermissionService";

/**
 * 权限指令
 * 用法：
 * 1. 单个权限：v-permission="'user:read'"
 * 2. 多个权限（满足任一权限）：v-permission="['user:read', 'user:edit']"
 * 3. 多个权限（必须满足所有权限）：v-permission.all="['user:read', 'user:edit']"
 * 4. 自定义无权限处理：v-permission="{ permission: 'user:read', onDenied: () => { ... } }"
 */
export default {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { value, modifiers } = binding;

    // 处理不同类型的指令值
    let permission: string | string[];
    let logic: "ANY" | "ALL" = modifiers.all ? "ALL" : "ANY";
    let onDenied: (() => void) | null = null;

    if (typeof value === "string") {
      permission = value;
    } else if (Array.isArray(value)) {
      permission = value;
    } else if (value && typeof value === "object") {
      permission = value.permission;
      onDenied = value.onDenied || null;
      logic = value.logic || logic;
    } else {
      console.warn("v-permission指令值无效");
      return;
    }

    // 检查权限
    const hasAuth = hasPermission(permission, logic);

    if (!hasAuth) {
      // 如果没有权限，则隐藏元素
      if (el.parentNode) {
        el.parentNode.removeChild(el);
      } else {
        el.style.display = "none";
      }

      // 如果提供了自定义处理函数，则调用
      if (onDenied && typeof onDenied === "function") {
        onDenied();
      }
    }
  },
};
