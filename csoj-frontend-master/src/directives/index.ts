import { App } from "vue";
import permissionDirective from "./permission";

/**
 * 注册全局指令
 * @param app Vue应用实例
 */
export function registerDirectives(app: App) {
  // 注册权限指令 v-permission
  app.directive("permission", permissionDirective);
}

export default registerDirectives;
