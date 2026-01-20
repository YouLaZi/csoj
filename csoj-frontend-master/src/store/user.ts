// initial state
import { StoreOptions } from "vuex";
import ACCESS_ENUM from "@/access/accessEnum";
import { UserControllerService } from "../../generated";

export default {
  namespaced: true,
  state: () => ({
    loginUser: {
      userName: "未登录",
    },
    token: localStorage.getItem("auth_token") || undefined,
  }),
  actions: {
    async getLoginUser({ commit, state }) {
      // 从远程请求获取登录信息
      try {
        const res = await UserControllerService.getLoginUserUsingGet();
        if (res.code === 0 && res.data) {
          commit("updateUser", res.data);
        } else {
          // 即便 code === 0 但 data 为空，也视为未登录
          // 或者 res.code !== 0
          commit("updateUser", {
            userName: "未登录",
            userRole: ACCESS_ENUM.NOT_LOGIN,
          });
        }
      } catch (error) {
        console.error("Failed to get login user:", error);
        // 发生任何错误（网络错误、后端错误等），都将用户状态更新为未登录
        commit("updateUser", {
          userName: "未登录",
          userRole: ACCESS_ENUM.NOT_LOGIN,
        });
      }
    },
    async setToken({ commit, state }, token: string | null) {
      // 明确 token 类型可以为 null
      // 导入 OpenAPI 配置
      const { OpenAPI } = await import("../../generated/core/OpenAPI");
      // 设置全局令牌
      OpenAPI.TOKEN = token || undefined; // 如果 token 为 null，则设置 OpenAPI.TOKEN 为 undefined
      // 将令牌保存到本地存储，以便在页面刷新后恢复
      if (token) {
        localStorage.setItem("auth_token", token);
      } else {
        localStorage.removeItem("auth_token"); // 如果 token 为 null，则从 localStorage 中移除
      }
      // 更新状态中的令牌
      commit("updateToken", token);
    },

    // 初始化令牌，在应用启动时调用
    async initToken({ commit, state, dispatch }) {
      // 从本地存储恢复令牌
      const token = localStorage.getItem("auth_token");
      if (token) {
        // 设置令牌
        await dispatch("setToken", token);
      }
    },
  },
  mutations: {
    updateUser(state, payload) {
      state.loginUser = payload;
    },
    updateUserRole(state, role) {
      if (state.loginUser) {
        state.loginUser.userRole = role;
      }
    },
    updateToken(state, token) {
      state.token = token;
    },
  },
} as StoreOptions<any>;
