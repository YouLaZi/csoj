import axios from "axios";

const API_BASE = "/api";

/**
 * 数据可视化服务
 */
class StatsService {
  /**
   * 获取热力图数据
   * @param year 年份
   */
  async getHeatmap(year?: number) {
    const currentYear = year || new Date().getFullYear();
    const response = await axios.get(`${API_BASE}/stats/heatmap`, {
      params: { year: currentYear },
    });
    return response.data;
  }

  /**
   * 获取技能雷达数据
   */
  async getSkillRadar() {
    const response = await axios.get(`${API_BASE}/stats/skills`);
    return response.data;
  }

  /**
   * 获取进度曲线
   * @param period 周期 (week/month/year)
   */
  async getProgressCurve(period: string = "month") {
    const response = await axios.get(`${API_BASE}/stats/progress`, {
      params: { period },
    });
    return response.data;
  }

  /**
   * 获取综合统计
   */
  async getComprehensiveStats() {
    const response = await axios.get(`${API_BASE}/stats/comprehensive`);
    return response.data;
  }

  /**
   * 获取排名位置
   */
  async getLeaderboardPosition() {
    const response = await axios.get(`${API_BASE}/stats/rank`);
    return response.data;
  }

  /**
   * 获取周统计
   */
  async getWeeklyStats() {
    const response = await axios.get(`${API_BASE}/stats/weekly`);
    return response.data;
  }

  /**
   * 获取月统计
   */
  async getMonthlyStats() {
    const response = await axios.get(`${API_BASE}/stats/monthly`);
    return response.data;
  }

  /**
   * 刷新统计缓存
   */
  async refreshStats() {
    const response = await axios.post(`${API_BASE}/stats/refresh`);
    return response.data;
  }
}

export default new StatsService();
