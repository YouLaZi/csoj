import { useUserStore } from "@/store/useUserStore";
import { CheckinControllerService } from "../../generated/services/CheckinControllerService"; // 修改导入路径
import { OpenAPI } from "../../generated/core/OpenAPI"; // 修改导入路径

/**
 * 签到服务
 * 处理用户签到相关功能
 */
class CheckinService {
  /**
   * 用户签到
   * @returns 签到结果
   */
  async checkin() {
    try {
      const userStore = useUserStore();
      const token = userStore.token;

      // token 可能为空（如微信扫码登录只用了 Session），仍尝试请求让后端判断
      if (token) {
        OpenAPI.TOKEN = token;
      }
      const response = await CheckinControllerService.doCheckinUsingPost();

      console.log("签到结果:", response);
      // 检查response是否为null或undefined
      if (!response || !response.data) {
        return {
          success: false,
          code: -1,
          message: response?.message || "签到接口返回数据异常",
          data: null,
        };
      }
      console.log("签到结果:", response.data.success);
      // 检查业务逻辑是否成功
      if (response.data.success) {
        return {
          success: true,
          code: 0,
          message: "签到成功",
          data: response.data,
        };
      } else {
        return {
          success: false,
          code: response.data.code,
          message: response.data.message || "签到失败",
          data: null,
        };
      }
    } catch (error: unknown) {
      console.error("签到失败:", error);
      return {
        success: false,
        code: -1,
        message: error instanceof Error ? error.message : "签到失败",
        data: null,
      };
    }
  }

  /**
   * 获取用户签到记录
   * @returns 用户签到记录
   */
  async getUserCheckinRecords() {
    try {
      const userStore = useUserStore();
      const token = userStore.token;

      // token 可能为空（如微信扫码登录只用了 Session），仍尝试请求让后端判断
      if (token) {
        OpenAPI.TOKEN = token;
      }
      // 使用正确的API方法名
      const response = await CheckinControllerService.getCheckinRecordsUsingGet(
        1,
        1000
      );

      console.log("获取签到记录响应:", response);

      // 检查response是否为null或undefined
      if (!response) {
        return {
          code: -1,
          message: "获取签到记录接口返回数据异常",
          data: {
            records: [],
            total: 0,
          },
        };
      }

      // 返回标准化的响应格式
      return {
        code: response.code || 0,
        message: response.message || "获取成功",
        data: response.data || {
          records: [],
          total: 0,
        },
      };
    } catch (error: unknown) {
      console.error("获取签到记录失败:", error);
      return {
        code: -1,
        message: error instanceof Error ? error.message : "获取签到记录失败",
        data: {
          records: [],
          total: 0,
        },
      };
    }
  }
}

export default new CheckinService();
