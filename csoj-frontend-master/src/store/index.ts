import { createStore } from "vuex";
import user from "./user";

// 创建并导出store实例作为命名导出
export const store = createStore({
  mutations: {},
  actions: {},
  modules: {
    user,
  },
});

// 同时保留默认导出以兼容现有代码
export default store;
