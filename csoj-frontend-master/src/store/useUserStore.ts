import { store } from "./index";

/**
 * 用户存储访问函数
 * 提供对Vuex用户模块的便捷访问
 * 用于组件中获取和操作用户状态
 */
export function useUserStore() {
  return {
    // 获取登录用户信息
    get loginUser() {
      return store.state.user.loginUser;
    },

    // 获取用户角色
    get userRole() {
      return store.state.user.loginUser?.userRole;
    },

    // 判断用户是否已登录
    get isLoggedIn() {
      return !!store.state.user.loginUser?.userRole;
    },

    // 获取用户令牌
    get token() {
      return store.state.user.token || store.state.user.loginUser?.token;
    },

    // 获取登录用户信息的action
    getLoginUser() {
      return store.dispatch("user/getLoginUser");
    },

    // 更新用户信息的mutation
    updateUser(userData: any) {
      store.commit("user/updateUser", userData);
    },

    // 更新用户角色的mutation
    updateUserRole(role: string) {
      store.commit("user/updateUserRole", role);
    },

    // 设置用户令牌
    setToken(token: string) {
      return store.dispatch("user/setToken", token);
    },

    // 初始化令牌
    initToken() {
      return store.dispatch("user/initToken");
    },
  };
}
