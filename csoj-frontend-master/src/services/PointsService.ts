import axios from "axios";
import { Message } from "@arco-design/web-vue";

/**
 * 积分服务类
 */
export class PointsService {
  /**
   * 获取积分排行榜
   * @param timeRange 时间范围，可选值：day, week, month, all
   * @param limit 限制数量
   * @returns 积分排行榜数据
   */
  public static async getLeaderboard(timeRange = "all", limit = 5) {
    try {
      // 直接使用静态方法调用API，避免实例化和修改headers
      // 使用axios直接调用API，避免使用生成的服务类
      const response = await axios.get(`/api/leaderboard/points`, {
        params: { timeRange },
        headers: { "Content-Type": "application/json" },
      });

      const res = response.data;
      if (res.code === 0) {
        // 如果有数据，返回前limit条
        const users = res.data || [];
        return {
          code: 0,
          message: "获取排行榜成功",
          data: users.slice(0, limit),
        };
      }
      return res;
    } catch (error) {
      // 静默处理错误，返回空数据
      return {
        code: -1,
        message: error instanceof Error ? error.message : "获取积分排行榜失败",
        data: [],
      };
    }
  }
}

export default PointsService;
