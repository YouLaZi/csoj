<template>
  <header id="globalHeader">
    <div class="header-inner">
      <!-- 左侧：Logo 和导航 -->
      <div class="header-left">
        <!-- 移动端菜单按钮 -->
        <button class="mobile-menu-btn" @click="toggleMobileMenu">
          <icon-menu />
        </button>

        <!-- Logo -->
        <div class="logo" @click="router.push('/')">
          <span class="logo-text">CodeSmart</span>
        </div>

        <!-- 桌面端导航菜单 -->
        <nav class="desktop-nav">
          <div
            v-for="item in visibleRoutes"
            :key="item.path"
            :class="[
              'nav-item',
              {
                'has-dropdown': hasVisibleChildren(item),
                active: isActiveRoute(item.path),
              },
            ]"
          >
            <template v-if="hasVisibleChildren(item)">
              <span class="nav-link">
                {{ $t(item.name) }}
                <icon-down class="dropdown-arrow" />
              </span>
              <div class="dropdown-menu">
                <a
                  v-for="child in getChildRoutes(item)"
                  :key="item.path + '/' + child.path"
                  class="dropdown-item"
                  @click="navigateTo(item.path + '/' + child.path)"
                >
                  {{ $t(child.name) }}
                </a>
              </div>
            </template>
            <template v-else>
              <span class="nav-link" @click="navigateTo(item.path)">{{
                $t(item.name)
              }}</span>
            </template>
          </div>
        </nav>
      </div>

      <!-- 右侧：操作区 -->
      <div class="header-right">
        <!-- 排行榜入口 -->
        <button
          class="icon-btn leaderboard-btn"
          @click="router.push('/points/leaderboard')"
        >
          <icon-trophy />
          <span class="btn-text">{{ $t("points.leaderboard") }}</span>
        </button>

        <!-- 主题切换 -->
        <ThemeToggle />

        <!-- 语言切换 -->
        <LocaleToggle />

        <!-- 用户区域 -->
        <div v-if="!isLogin" class="auth-buttons">
          <a-button type="text" class="login-btn" @click="showLoginModal">
            {{ $t("user.login") }}
          </a-button>
          <a-button
            type="primary"
            class="register-btn"
            @click="showRegisterModal"
          >
            {{ $t("user.register") }}
          </a-button>
        </div>

        <a-dropdown v-else trigger="hover" class="user-dropdown">
          <div class="user-trigger">
            <a-avatar :size="32" v-if="userAvatar" :image-url="userAvatar" />
            <a-avatar :size="32" v-else class="avatar-placeholder">
              {{ userNameFirst }}
            </a-avatar>
            <span class="user-name">{{ userName }}</span>
            <icon-down class="dropdown-icon" />
          </div>
          <template #content>
            <a-doption @click="toUserProfile">
              <template #icon><icon-user /></template>
              {{ $t("user.profile") }}
            </a-doption>
            <a-doption @click="router.push('/permission-example')">
              <template #icon><icon-lock /></template>
              {{ $t("nav.permissionExample") }}
            </a-doption>
            <a-doption @click="toPreferences">
              <template #icon><icon-settings /></template>
              {{ $t("user.preferences") }}
            </a-doption>
            <a-doption @click="logout" class="logout-option">
              <template #icon><icon-export /></template>
              {{ $t("user.logout") }}
            </a-doption>
          </template>
        </a-dropdown>
      </div>
    </div>

    <!-- 移动端菜单抽屉 -->
    <a-drawer
      :visible="mobileMenuVisible"
      placement="left"
      @cancel="mobileMenuVisible = false"
      :footer="false"
      :width="280"
      class="mobile-drawer"
    >
      <template #title>
        <div class="drawer-logo">CodeSmart</div>
      </template>
      <nav class="mobile-nav">
        <template v-for="item in visibleRoutes" :key="item.path">
          <div
            v-if="hasVisibleChildren(item)"
            :key="item.path"
            class="mobile-nav-group"
          >
            <div class="mobile-nav-title">{{ $t(item.name) }}</div>
            <a
              v-for="child in getChildRoutes(item)"
              :key="item.path + '/' + child.path"
              class="mobile-nav-item"
              @click="navigateToAndClose(item.path + '/' + child.path)"
            >
              {{ $t(child.name) }}
            </a>
          </div>
          <a
            v-else
            :key="item.path"
            :class="['mobile-nav-item', { active: isActiveRoute(item.path) }]"
            @click="navigateToAndClose(item.path)"
          >
            {{ $t(item.name) }}
          </a>
        </template>
      </nav>
    </a-drawer>

    <!-- 登录注册模态框 -->
    <LoginRegisterModal ref="loginModalRef" />
  </header>
</template>

<script setup lang="ts">
import { routes } from "../router/routes";
import { useRouter } from "vue-router";
import { computed, ref, onMounted } from "vue";
import { useStore } from "vuex";
import { useI18n } from "vue-i18n";
import checkAccess from "@/access/checkAccess";
import ACCESS_ENUM from "@/access/accessEnum";
import {
  IconUser,
  IconExport,
  IconSettings,
  IconMenu,
  IconTrophy,
  IconLock,
  IconDown,
} from "@arco-design/web-vue/es/icon";
import message from "@arco-design/web-vue/es/message";
import { UserControllerService } from "../../generated";
import LoginRegisterModal from "@/components/LoginRegisterModal.vue";
import ThemeToggle from "@/components/ThemeToggle.vue";
import LocaleToggle from "@/components/LocaleToggle.vue";

const router = useRouter();
const store = useStore();
const { t } = useI18n();
const loginModalRef = ref(null);
const mobileMenuVisible = ref(false);

const isLogin = computed(() => {
  const user = store.state.user.loginUser;
  return user && user.userRole !== ACCESS_ENUM.NOT_LOGIN;
});

const userNameFirst = computed(() => {
  const userName = store.state.user?.loginUser?.userName;
  return userName ? userName.charAt(0).toUpperCase() : "?";
});

const userName = computed(() => {
  return store.state.user?.loginUser?.userName || "";
});

const userAvatar = computed(() => {
  return store.state.user?.loginUser?.userAvatar || "";
});

const visibleRoutes = computed(() => {
  return routes.filter((item) => {
    if (item.meta?.hideInMenu) return false;
    if (!checkAccess(store.state.user.loginUser, item?.meta?.access as string))
      return false;
    return true;
  });
});

const getChildRoutes = (route: any) => {
  if (!route.children) return [];
  return route.children.filter((child: any) => {
    if (child.meta?.hideInMenu) return false;
    if (!checkAccess(store.state.user.loginUser, child?.meta?.access as string))
      return false;
    return true;
  });
};

const hasVisibleChildren = (route: any) => {
  return getChildRoutes(route).length > 0;
};

const isActiveRoute = (path: string) => {
  const currentPath = router.currentRoute.value.path;
  return currentPath === path || currentPath.startsWith(path + "/");
};

const navigateTo = (path: string) => {
  router.push(path);
};

const navigateToAndClose = (path: string) => {
  router.push(path);
  mobileMenuVisible.value = false;
};

const toggleMobileMenu = () => {
  mobileMenuVisible.value = !mobileMenuVisible.value;
};

const showLoginModal = () => {
  loginModalRef.value.open("login");
};

const showRegisterModal = () => {
  loginModalRef.value.open("register");
};

defineExpose({ showLoginModal, showRegisterModal });

const toUserProfile = () => router.push("/user/profile");
const toPreferences = () => router.push("/user/preferences");

const logout = async () => {
  try {
    const res = await UserControllerService.userLogoutUsingPost();
    if (res.code === 0) {
      message.success(t("user.logoutSuccess"));
    } else {
      message.warning(res.message || t("message.failed"));
    }
  } catch (error) {
    message.error(t("error.networkError"));
    console.error("Logout error:", error);
  } finally {
    await store.dispatch("user/setToken", null);
    await store.dispatch("user/getLoginUser");
    router.push("/");
  }
};

onMounted(() => {
  store.dispatch("user/getLoginUser");
});
</script>

<style scoped>
/* ========================================
   头部导航 - 简约大方
   ======================================== */

#globalHeader {
  position: sticky;
  top: 0;
  z-index: 100;
  width: 100%;
  height: var(--header-height);
  background-color: var(--bg-color-secondary);
  border-bottom: 1px solid var(--border-color-light);
  transition: all var(--transition-base);
}

.header-inner {
  max-width: var(--content-max-width);
  height: 100%;
  margin: 0 auto;
  padding: 0 var(--spacing-xl);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* ========================================
   左侧区域
   ======================================== */

.header-left {
  display: flex;
  align-items: center;
  height: 100%;
}

.mobile-menu-btn {
  display: none;
  width: 40px;
  height: 40px;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  color: var(--text-color-primary);
  font-size: 20px;
  cursor: pointer;
  border-radius: var(--radius-md);
  transition: background-color var(--transition-fast);
}

.mobile-menu-btn:hover {
  background-color: var(--bg-color-tertiary);
}

/* Logo */
.logo {
  display: flex;
  align-items: center;
  cursor: pointer;
  margin-right: var(--spacing-2xl);
}

.logo-text {
  font-family: var(--font-family-serif);
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  background: linear-gradient(
    135deg,
    var(--primary-color),
    var(--primary-hover-color)
  );
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 1px;
}

/* ========================================
   导航菜单
   ======================================== */

.desktop-nav {
  display: flex;
  align-items: center;
  height: 100%;
  gap: var(--spacing-xs);
}

.nav-item {
  position: relative;
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 var(--spacing-md);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.nav-link {
  display: flex;
  align-items: center;
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-medium);
  color: var(--text-color-regular);
  transition: color var(--transition-fast);
}

.nav-item:hover .nav-link,
.nav-item.active .nav-link {
  color: var(--primary-color);
}

.dropdown-arrow {
  margin-left: var(--spacing-xs);
  font-size: 12px;
  transition: transform var(--transition-fast);
}

/* 下拉菜单 */
.dropdown-menu {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  min-width: 160px;
  padding: var(--spacing-sm) 0;
  background-color: var(--bg-color-secondary);
  border: 1px solid var(--border-color-light);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  opacity: 0;
  visibility: hidden;
  transition: all var(--transition-fast);
}

.nav-item.has-dropdown:hover .dropdown-menu {
  opacity: 1;
  visibility: visible;
}

.nav-item.has-dropdown:hover .dropdown-arrow {
  transform: rotate(180deg);
}

.dropdown-item {
  display: block;
  padding: var(--spacing-sm) var(--spacing-lg);
  font-size: var(--font-size-sm);
  color: var(--text-color-regular);
  transition: all var(--transition-fast);
}

.dropdown-item:hover {
  background-color: var(--bg-color-tertiary);
  color: var(--primary-color);
}

/* ========================================
   右侧区域
   ======================================== */

.header-right {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.icon-btn {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-sm) var(--spacing-md);
  background: transparent;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  color: var(--text-color-regular);
  font-size: var(--font-size-sm);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.icon-btn:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.icon-btn .btn-text {
  display: inline;
}

/* 认证按钮 */
.auth-buttons {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.login-btn {
  color: var(--text-color-regular);
}

.login-btn:hover {
  color: var(--primary-color);
}

.register-btn {
  border-radius: var(--radius-md);
}

/* 用户下拉 */
.user-dropdown {
  margin-left: var(--spacing-sm);
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: background-color var(--transition-fast);
}

.user-trigger:hover {
  background-color: var(--bg-color-tertiary);
}

.avatar-placeholder {
  background-color: var(--primary-light-color);
  color: var(--primary-color);
}

.user-name {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--text-color-primary);
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dropdown-icon {
  font-size: 12px;
  color: var(--text-color-secondary);
}

.logout-option {
  color: var(--danger-color) !important;
}

/* ========================================
   移动端抽屉
   ======================================== */

.mobile-drawer :deep(.arco-drawer-body) {
  padding: 0;
}

.drawer-logo {
  font-family: var(--font-family-serif);
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  color: var(--primary-color);
}

.mobile-nav {
  padding: var(--spacing-md);
}

.mobile-nav-group {
  margin-bottom: var(--spacing-md);
}

.mobile-nav-title {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  color: var(--text-color-secondary);
  text-transform: uppercase;
  letter-spacing: 1px;
  padding: var(--spacing-sm) var(--spacing-md);
}

.mobile-nav-item {
  display: block;
  padding: var(--spacing-md);
  font-size: var(--font-size-base);
  color: var(--text-color-primary);
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
}

.mobile-nav-item:hover,
.mobile-nav-item.active {
  background-color: var(--primary-lighter-color);
  color: var(--primary-color);
}

/* ========================================
   响应式设计
   ======================================== */

@media (max-width: 992px) {
  .header-inner {
    padding: 0 var(--spacing-lg);
  }

  .icon-btn .btn-text {
    display: none;
  }
}

@media (max-width: 768px) {
  .mobile-menu-btn {
    display: flex;
  }

  .desktop-nav {
    display: none;
  }

  .logo {
    margin-right: var(--spacing-md);
  }

  .user-name {
    display: none;
  }

  .header-inner {
    padding: 0 var(--spacing-md);
  }

  .auth-buttons .register-btn {
    padding: 0 var(--spacing-md);
  }
}

/* ========================================
   深色模式
   ======================================== */

[data-theme="dark"] #globalHeader {
  background-color: var(--bg-color-secondary);
  border-bottom-color: var(--border-color);
}

[data-theme="dark"] .dropdown-menu {
  background-color: var(--bg-color-elevated);
  border-color: var(--border-color);
}

[data-theme="dark"] .mobile-nav-item:hover,
[data-theme="dark"] .mobile-nav-item.active {
  background-color: var(--primary-light-color);
}
</style>
