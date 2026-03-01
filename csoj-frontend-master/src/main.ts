import { createApp } from "vue";
import App from "./App.vue";
import ArcoVue from "@arco-design/web-vue";
import "@arco-design/web-vue/dist/arco.css";
import router from "./router";
import store from "./store";
import i18n from "./locales";
import "@/plugins/axios";
import "@/access";
import "bytemd/dist/index.css";
import { registerDirectives } from "./directives";
import { LocaleManager } from "./utils/localeManager";

const app = createApp(App);

// 注册全局指令
registerDirectives(app);

// 初始化语言设置
LocaleManager.init();

// 应用启动时初始化 token
store.dispatch("user/initToken");

app.use(ArcoVue).use(store).use(router).use(i18n).mount("#app");

// 导入错误处理和日志服务
import ErrorService from "./services/ErrorService";
import LogService, { LogLevel } from "./services/LogService";

// 初始化日志服务
LogService.init({
  enableConsole: true,
  enableRemoteLogging: process.env.NODE_ENV === "production",
  minLevel:
    process.env.NODE_ENV === "production" ? LogLevel.INFO : LogLevel.DEBUG,
  // 如果有日志服务器，可以配置远程日志端点
  // remoteLogEndpoint: '/api/logs'
});

// 初始化全局错误处理
ErrorService.init();

// 记录应用启动日志
LogService.info("应用程序已启动", {
  version: process.env.VUE_APP_VERSION || "1.0.0",
});
