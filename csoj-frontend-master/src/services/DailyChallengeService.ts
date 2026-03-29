import axios from "axios";

const API_BASE = "/api";

/**
 * 每日挑战服务
 */
class DailyChallengeService {
  /**
   * 获取今日挑战
   */
  async getTodayChallenge() {
    const response = await axios.get(`${API_BASE}/challenge/today`);
    return response.data;
  }

  /**
   * 完成挑战
   * @param challengeId 挑战ID
   * @param isPerfect 是否完美通关
   */
  async completeChallenge(challengeId: number, isPerfect: boolean = false) {
    const response = await axios.post(`${API_BASE}/challenge/complete`, null, {
      params: { challengeId, isPerfect },
    });
    return response.data;
  }

  /**
   * 获取用户挑战状态
   * @param challengeId 挑战ID
   */
  async getUserChallengeStatus(challengeId: number) {
    const response = await axios.get(`${API_BASE}/challenge/status/${challengeId}`);
    return response.data;
  }

  /**
   * 获取用户连胜信息
   */
  async getUserStreak() {
    const response = await axios.get(`${API_BASE}/challenge/streak`);
    return response.data;
  }

  /**
   * 获取挑战历史
   * @param days 天数
   */
  async getChallengeHistory(days: number = 30) {
    const response = await axios.get(`${API_BASE}/challenge/history`, {
      params: { days },
    });
    return response.data;
  }
}

export default new DailyChallengeService();
