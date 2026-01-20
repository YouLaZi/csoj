import axios from "axios";
import { ref, computed } from "vue";
import store from "@/store";
import ACCESS_ENUM from "@/access/accessEnum";

// 用户权限缓存
const userPermissions = ref<string[]>([]);

// 是否已加载权限
const permissionsLoaded = ref<boolean>(false);
// 权限缓存过期时间（毫秒），例如 5 分钟
const CACHE_EXPIRATION_MS = 5 * 60 * 1000;
// 上次加载权限的时间戳
let lastPermissionsFetchTime = 0;

/**
 * 获取当前用户权限
 * @returns Promise<string[]> 权限列表
 */
export const fetchUserPermissions = async (): Promise<string[]> => {
  try {
    // 检查用户是否登录
    const loginUser = store.state.user.loginUser;
    if (!loginUser || loginUser.userRole === ACCESS_ENUM.NOT_LOGIN) {
      userPermissions.value = [];
      permissionsLoaded.value = true;
      return [];
    }

    // 检查缓存是否有效且未过期
    const currentTime = Date.now();
    if (
      permissionsLoaded.value &&
      userPermissions.value.length > 0 &&
      currentTime - lastPermissionsFetchTime < CACHE_EXPIRATION_MS
    ) {
      console.log("从缓存返回用户权限");
      return userPermissions.value;
    }
    console.log("权限缓存已过期或无效，重新获取");

    try {
      // 尝试调用后端API获取用户权限
      console.log("正在获取用户权限...");
      const response = await axios.get("/api/user/permission/current");
      if (response.data.code === 0 && response.data.data) {
        console.log("成功获取用户权限:", response.data.data);
        userPermissions.value = response.data.data.permissions || [];
        permissionsLoaded.value = true;
        lastPermissionsFetchTime = Date.now(); // 更新加载时间戳
        return userPermissions.value;
      } else {
        console.warn("获取用户权限API返回异常结果:", response.data);
      }
    } catch (apiError) {
      console.warn("获取用户权限API不可用，使用角色权限映射:", apiError);
      // 如果API不存在或不可用，根据用户角色分配基本权限
      if (loginUser.userRole === ACCESS_ENUM.ADMIN) {
        console.log("使用管理员默认权限");
        userPermissions.value = [
          "admin:access",
          "user:read",
          "user:edit",
          "user:delete",
          "question:read",
          "question:edit",
          "question:delete",
          "submit:read",
          "submit:create",
          "post:read",
          "post:edit",
          "post:delete",
          "points:read",
          "points:edit",
        ];
      } else if (loginUser.userRole === ACCESS_ENUM.USER) {
        console.log("使用普通用户默认权限");
        userPermissions.value = [
          "user:read",
          "user:edit",
          "question:read",
          "submit:create",
          "post:read",
          "post:edit",
          "points:read",
        ];
      }
      permissionsLoaded.value = true;
      lastPermissionsFetchTime = Date.now(); // 更新加载时间戳（即使是回退方案）
      return userPermissions.value;
    }

    return [];
  } catch (error) {
    console.error("获取用户权限失败:", error);
    return [];
  }
};

/**
 * 检查用户是否拥有指定权限
 * @param permission 单个权限或权限数组
 * @param logic 权限检查逻辑，'ANY'表示满足任一权限即可，'ALL'表示必须满足所有权限
 * @returns boolean 是否拥有权限
 */
export const hasPermission = (
  permission: string | string[],
  logic: "ANY" | "ALL" = "ANY"
): boolean => {
  // 如果用户未登录，直接返回false
  const loginUser = store.state.user.loginUser;
  if (!loginUser || loginUser.userRole === ACCESS_ENUM.NOT_LOGIN) {
    return false;
  }

  // 管理员拥有所有权限
  if (loginUser.userRole === ACCESS_ENUM.ADMIN) {
    return true;
  }

  // 如果没有获取到权限列表，返回false
  if (userPermissions.value.length === 0) {
    return false;
  }

  // 将单个权限字符串转换为数组
  const requiredPermissions = Array.isArray(permission)
    ? permission
    : [permission];

  // 根据逻辑检查权限
  if (logic === "ANY") {
    // 满足任一权限即可
    return requiredPermissions.some((perm) =>
      userPermissions.value.includes(perm)
    );
  } else {
    // 必须满足所有权限
    return requiredPermissions.every((perm) =>
      userPermissions.value.includes(perm)
    );
  }
};

/**
 * 清除权限缓存
 */
export const clearPermissionCache = (): void => {
  userPermissions.value = [];
  permissionsLoaded.value = false;
  lastPermissionsFetchTime = 0; // 重置时间戳，使缓存立即失效
  console.log("权限缓存已清除");
};

/**
 * 检查用户是否有权限访问路由
 * @param routeAccess 路由所需权限
 * @returns boolean 是否有权限
 */
export const checkRoutePermission = (routeAccess: string): boolean => {
  const loginUser = store.state.user.loginUser;

  // 不需要权限的路由，所有人都可以访问
  if (routeAccess === ACCESS_ENUM.NOT_LOGIN) {
    return true;
  }

  // 需要用户权限的路由，检查是否登录
  if (routeAccess === ACCESS_ENUM.USER) {
    return loginUser && loginUser.userRole !== ACCESS_ENUM.NOT_LOGIN;
  }

  // 需要管理员权限的路由，检查是否是管理员
  if (routeAccess === ACCESS_ENUM.ADMIN) {
    return loginUser && loginUser.userRole === ACCESS_ENUM.ADMIN;
  }

  return false;
};

export default {
  fetchUserPermissions,
  hasPermission,
  clearPermissionCache,
  checkRoutePermission,
};
