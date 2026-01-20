<template>
  <a-row id="globalHeader" align="center" :wrap="false">
    <a-col flex="auto">
      <!-- 移动端菜单按钮 -->
      <div class="mobile-menu-button mobile-block" @click="toggleMobileMenu">
        <icon-menu />
      </div>

      <!-- 桌面端菜单 -->
      <a-menu
        mode="horizontal"
        :selected-keys="selectedKeys"
        @menu-item-click="doMenuClick"
        class="desktop-menu"
      >
        <a-menu-item
          key="0"
          :style="{ padding: 0, marginRight: '28px' }"
          disabled
        >
          <div class="title-bar">CodeSmart</div>
        </a-menu-item>
        <template v-for="item in visibleRoutes" :key="item.path">
          <a-sub-menu v-if="hasVisibleChildren(item)" :key="item.path">
            <template #title>{{ item.name }}</template>
            <a-menu-item
              v-for="child in getChildRoutes(item)"
              :key="item.path + '/' + child.path"
            >
              {{ child.name }}
            </a-menu-item>
          </a-sub-menu>
          <a-menu-item v-else :key="item.path">
            {{ item.name }}
          </a-menu-item>
        </template>
      </a-menu>

      <!-- 移动端菜单抽屉 -->
      <a-drawer
        :visible="mobileMenuVisible"
        placement="left"
        @cancel="mobileMenuVisible = false"
        :footer="false"
        :width="250"
      >
        <template #title>
          <div class="drawer-title">CodeSmart</div>
        </template>
        <a-menu
          :selected-keys="selectedKeys"
          @menu-item-click="
            (key) => {
              doMenuClick(key);
              mobileMenuVisible = false;
            }
          "
        >
          <template v-for="item in visibleRoutes" :key="item.path">
            <a-sub-menu v-if="hasVisibleChildren(item)" :key="item.path">
              <template #title>{{ item.name }}</template>
              <a-menu-item
                v-for="child in getChildRoutes(item)"
                :key="item.path + '/' + child.path"
              >
                {{ child.name }}
              </a-menu-item>
            </a-sub-menu>
            <a-menu-item v-else :key="item.path">
              {{ item.name }}
            </a-menu-item>
          </template>
        </a-menu>
      </a-drawer>
    </a-col>

    <a-col flex="auto" class="header-actions">
      <!-- 排行榜入口 -->
      <div class="leaderboard-entry">
        <a-tooltip content="积分排行榜">
          <a-button
            class="leaderboard-button"
            type="outline"
            shape="round"
            @click="router.push('/points/leaderboard')"
          >
            <template #icon>
              <icon-trophy />
            </template>
            <span class="desktop-only">排行榜</span>
          </a-button>
        </a-tooltip>
      </div>

      <!-- 主题切换按钮 -->
      <ThemeToggle />

      <!-- 用户操作区 -->
      <div v-if="!isLogin" class="user-actions">
        <a-button type="text" @click="showLoginModal">登录</a-button>
        <a-button type="text" @click="showRegisterModal">注册</a-button>
      </div>
      <a-dropdown v-else trigger="hover">
        <div class="user-info">
          <a-avatar
            :size="32"
            v-if="store.state.user?.loginUser?.userAvatar"
            :image-url="store.state.user.loginUser.userAvatar"
          ></a-avatar>
          <a-avatar :size="32" v-else>{{ userNameFirst }}</a-avatar>
          <span class="user-name desktop-only">{{
            store.state.user?.loginUser?.userName
          }}</span>
        </div>
        <template #content>
          <a-doption @click="toUserProfile" class="custom-doption">
            <template #icon>
              <icon-user />
            </template>
            个人中心
          </a-doption>
          <a-doption
            @click="router.push('/permission-example')"
            class="custom-doption"
          >
            <template #icon>
              <icon-lock />
            </template>
            权限示例
          </a-doption>
          <a-doption @click="toPreferences" class="custom-doption">
            <template #icon>
              <icon-settings />
            </template>
            个性化设置
          </a-doption>
          <a-doption @click="logout" class="custom-doption">
            <template #icon>
              <icon-export />
            </template>
            退出登录
          </a-doption>
        </template>
      </a-dropdown>
    </a-col>

    <!-- 登录注册模态框 -->
    <LoginRegisterModal ref="loginModalRef" />
  </a-row>
</template>

<script setup lang="ts">
import { routes } from "../router/routes";
import { useRoute, useRouter } from "vue-router";
import { computed, ref, onMounted, defineExpose } from "vue";
import { useStore } from "vuex";
import checkAccess from "@/access/checkAccess";
import ACCESS_ENUM from "@/access/accessEnum";
import { BatchImportQuestion } from "../views/admin/question/BatchImportQuestionView.vue";
import {
  IconUser,
  IconExport,
  IconSettings,
  IconMenu,
  IconTrophy,
  IconLock,
} from "@arco-design/web-vue/es/icon";
import message from "@arco-design/web-vue/es/message";
import { UserControllerService } from "../../generated";
import LoginRegisterModal from "@/components/LoginRegisterModal.vue";
import ThemeToggle from "@/components/ThemeToggle.vue";

const router = useRouter();
const store = useStore();

// 登录注册模态框引用
const loginModalRef = ref(null);

// 判断用户是否登录
const isLogin = computed(() => {
  return (
    store.state.user.loginUser &&
    store.state.user.loginUser.userRole !== ACCESS_ENUM.NOT_LOGIN
  );
});

// 获取用户名首字母（用于头像显示）
const userNameFirst = computed(() => {
  const userName = store.state.user?.loginUser?.userName;
  return userName ? userName.charAt(0).toUpperCase() : "?";
});

// 展示在菜单的路由数组
const visibleRoutes = computed(() => {
  return routes.filter((item, index) => {
    if (item.meta?.hideInMenu) {
      return false;
    }
    // 根据权限过滤菜单
    if (
      !checkAccess(store.state.user.loginUser, item?.meta?.access as string)
    ) {
      return false;
    }
    return true;
  });
});

// 获取路由的子路由
const getChildRoutes = (route: any) => {
  if (!route.children) {
    return [];
  }
  return route.children.filter((child: any) => {
    if (child.meta?.hideInMenu) {
      return false;
    }
    // 根据权限过滤菜单
    if (
      !checkAccess(store.state.user.loginUser, child?.meta?.access as string)
    ) {
      return false;
    }
    return true;
  });
};

// 判断路由是否有可见子路由
const hasVisibleChildren = (route: any) => {
  return getChildRoutes(route).length > 0;
};

// 默认主页
const selectedKeys = ref(["/"]);

// 根据当前路由动态设置选中菜单项
const updateSelectedKeys = (to) => {
  // 只高亮一级菜单（visibleRoutes）
  const match = visibleRoutes.value.find((item) => {
    // 完全匹配或以该路由为前缀（如/profile、/preferences等）
    return to.path === item.path || to.path.startsWith(item.path + "/");
  });
  if (match) {
    selectedKeys.value = [match.path];
  } else {
    selectedKeys.value = [to.path];
  }
};

// 路由跳转后，更新选中的菜单项
router.afterEach((to, from, failure) => {
  updateSelectedKeys(to);
  // 关闭移动端菜单
  mobileMenuVisible.value = false;
});

// 页面加载时初始化选中菜单
onMounted(() => {
  store.dispatch("user/getLoginUser");
  updateSelectedKeys(router.currentRoute.value);
});

// 移动端菜单显示状态
const mobileMenuVisible = ref(false);

// 切换移动端菜单显示状态
const toggleMobileMenu = () => {
  mobileMenuVisible.value = !mobileMenuVisible.value;
};

// 路由跳转后，更新选中的菜单项
router.afterEach((to, from, failure) => {
  selectedKeys.value = [to.path];
  // 关闭移动端菜单
  mobileMenuVisible.value = false;
});

// 页面加载时获取用户信息
onMounted(() => {
  store.dispatch("user/getLoginUser");
});

// 显示登录模态框
const showLoginModal = () => {
  loginModalRef.value.open("login");
};

// 显示注册模态框
const showRegisterModal = () => {
  loginModalRef.value.open("register");
};

// 暴露方法给父组件
defineExpose({
  showLoginModal,
  showRegisterModal,
});

// 菜单点击事件
const doMenuClick = (key: string) => {
  router.push({
    path: key,
  });
};

// 跳转到个人中心
const toUserProfile = () => {
  router.push("/user/profile");
};

// 跳转到个性化设置页面
const toPreferences = () => {
  router.push("/user/preferences");
};

// 退出登录
const logout = async () => {
  try {
    const res = await UserControllerService.userLogoutUsingPost();
    // 不论后端返回什么，都尝试清除前端状态并跳转
    // 后端成功退出
    if (res.code === 0) {
      message.success("退出登录成功");
    } else {
      // 即便后端接口失败，也提示用户，并尝试清理前端状态
      message.warning(
        res.message || "退出操作可能未完全成功，但将尝试清理本地会话"
      );
    }
  } catch (error) {
    // 网络错误等，也提示用户，并尝试清理前端状态
    message.error("退出登录请求失败，请检查网络连接");
    console.error("Logout error:", error);
  } finally {
    // 清除 localStorage 中的 token 和 Vuex 中的 token
    await store.dispatch("user/setToken", null);
    // 重新获取用户信息（会变为未登录状态）
    await store.dispatch("user/getLoginUser");
    // 跳转到首页
    router.push("/");
  }
};
</script>

<style scoped>
/* 全局头部样式 */
#globalHeader {
  position: sticky;
  top: 0;
  z-index: 100;
  width: 100%;
  height: 60px;
  background-color: var(--color-bg-2);
  border-bottom: 1px solid var(--color-border);
  padding: 0px;
  transition: all 0.3s;
}

.title-bar {
  font-style: italic;
  font-size: 22px;
  font-weight: bold;
  background: linear-gradient(
    to right,
    var(--color-primary),
    var(--color-primary-light-3)
  );
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  font-family: "Arial Black", sans-serif;
}

.logo-container {
  display: flex;
  align-items: center;
}

.logo {
  height: 32px;
  margin-right: 12px;
}

.site-name {
  font-size: 22px; /* 调整 Logo 文字大小 */
  font-weight: bold; /* 加粗 Logo 文字 */
  color: var(--color-text-1);
  white-space: nowrap;
  font-family: "Arial", sans-serif; /* 更改字体 */
}

.desktop-menu {
  margin-left: 12px; /* 减小左边距，为右侧留出更多空间 */
  flex-grow: 1; /* 让菜单占据更多空间 */
}

.header-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end; /* 确保内容靠右对齐 */
  margin-left: auto; /* 将操作按钮推到最右边 */
}

.theme-toggle {
  margin-right: 16px;
  cursor: pointer;
}

.leaderboard-entry {
  display: inline-flex;
  align-items: center;
  margin-right: 16px;
}

.leaderboard-button {
  background-color: transparent;
  color: var(--color-text-1);
  border: 1px solid var(--color-border);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
}

.leaderboard-button:hover {
  color: var(--color-primary);
  border-color: var(--color-primary);
  transform: translateY(-2px);
}

.user-actions {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.user-name {
  margin-left: 8px;
  margin-right: 16px;
  color: var(--color-text-1);
}

.drawer-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--color-primary);
}

.custom-doption {
  /* 你可以在这里为 a-doption 添加统一的样式，例如内边距 */
  padding: 5px 12px; /* 调整内边距以确保垂直和水平间距一致 */
}

/* 移动端样式 */
.mobile-menu-button {
  display: none;
  font-size: 24px;
  cursor: pointer;
  color: var(--color-text-1);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .desktop-menu {
    display: none;
  }

  .mobile-menu-button {
    display: flex;
    align-items: center;
  }

  .desktop-only {
    display: none;
  }

  #globalHeader {
    padding: 0 8px;
  }

  .leaderboard-button {
    padding: 0 8px;
  }
}
</style>
