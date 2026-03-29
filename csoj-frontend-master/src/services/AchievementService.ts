import axios from "axios";

const API_BASE = "/api";

/**
 * 成就系统服务
 */
class AchievementService {
  /**
   * 获取用户所有成就
   */
  async getUserAchievements() {
    const response = await axios.get(`${API_BASE}/achievement`);
    return response.data;
  }

  /**
   * 获取指定分类的成就
   * @param category 分类
   */
  async getAchievementsByCategory(category: string) {
    const response = await axios.get(`${API_BASE}/achievement/category/${category}`);
    return response.data;
  }

  /**
   * 获取所有成就定义
   */
  async getAllAchievements() {
    const response = await axios.get(`${API_BASE}/achievement/definitions`);
    return response.data;
  }

  /**
   * 获取未读成就数量
   */
  async getUnreadCount() {
    const response = await axios.get(`${API_BASE}/achievement/unread/count`);
    return response.data;
  }

  /**
   * 标记成就为已读
   * @param achievementCode 成就代码
   */
  async markAsRead(achievementCode: string) {
    const response = await axios.post(
      `${API_BASE}/achievement/read/${achievementCode}`
    );
    return response.data;
  }
}

export default new AchievementService();
