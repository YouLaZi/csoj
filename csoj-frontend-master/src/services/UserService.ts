import { OpenAPI } from "../../generated/core/OpenAPI";
import { request } from "../../generated/core/request";
import { UserControllerService } from "../../generated/services/UserControllerService";
import { Message } from "@arco-design/web-vue";

/**
 * 用户服务类
 */
export class UserService {
  /**
   * 获取当前登录用户信息
   * @returns 用户信息
   */
  public static async getCurrentUser() {
    try {
      const res = await UserControllerService.getLoginUserUsingGet();
      return res;
    } catch (error) {
      console.error("获取当前用户信息失败", error);
      Message.error("获取用户信息失败");
      return {
        code: -1,
        message: "获取用户信息失败",
        data: null,
      };
    }
  }

  /**
   * 获取用户详细信息
   * @param userId 用户ID
   * @returns 用户详细信息
   */
  public static async getUserDetail(userId: number) {
    try {
      const res = await UserControllerService.getUserVoByIdUsingGet(userId);
      return res;
    } catch (error) {
      console.error("获取用户详细信息失败", error);
      Message.error("获取用户详细信息失败");
      return {
        code: -1,
        message: "获取用户详细信息失败",
        data: null,
      };
    }
  }
}

export default UserService;
