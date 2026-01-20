import { createRouter, createWebHistory, RouteRecordRaw } from "vue-router";
import { routes } from "@/router/routes";
import NotFoundView from "@/views/error/NotFoundView.vue";

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes: [
    ...routes,
    {
      path: "/:pathMatch(.*)*",
      name: "NotFound",
      component: NotFoundView,
    },
  ],
});

// 处理登录和注册路由，显示模态框而不是跳转页面
router.beforeEach((to, from, next) => {
  // 如果是登录或注册页面，重定向到首页并通过参数指示显示相应模态框
  if (to.path === "/user/login") {
    next({ path: "/", query: { showLogin: "true" } });
  } else if (to.path === "/user/register") {
    next({ path: "/", query: { showRegister: "true" } });
  } else if (
    to.path === "/user/forgot-password" ||
    to.path === "/user/reset-password"
  ) {
    // 允许直接访问忘记密码和重置密码页面
    next();
  } else {
    next();
  }
});

export default router;
