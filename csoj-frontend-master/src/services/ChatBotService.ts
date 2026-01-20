import axios from "axios";
import { useUserStore } from "@/store/useUserStore";
import type { ChatMessage } from "@/types/chat";

// API基础URL
const API_BASE_URL = "/api";

// 学习进度接口响应类型
interface LearningProgressResponse {
  code: number;
  data: {
    solvedProblems: number;
    totalProblems: number;
    recentTopics: string[];
    recommendedTopics: string[];
  };
  message: string;
}

// 聊天消息响应类型
interface ChatResponse {
  code: number;
  data: {
    content: string;
    contentType: "text" | "code" | "math";
    language?: string;
    codeAnalysis?: {
      errors?: string[];
      suggestions?: string[];
    };
  };
  message: string;
}

/**
 * 聊天机器人服务
 * 处理与后端API的交互
 */
class ChatBotService {
  /**
   * 获取用户学习进度
   * @returns 学习进度数据
   */
  async getLearningProgress() {
    try {
      const userStore = useUserStore();
      const token = userStore.token;

      if (!token) {
        return {
          solvedProblems: 0,
          totalProblems: 0,
          recentTopics: [],
          recommendedTopics: [],
        };
      }

      const response = await axios.get<LearningProgressResponse>(
        `${API_BASE_URL}/user/learning-progress`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (response.data.code === 0) {
        return response.data.data;
      } else {
        console.error("获取学习进度失败:", response.data.message);
        // 返回默认数据
        return {
          solvedProblems: 0,
          totalProblems: 0,
          recentTopics: [],
          recommendedTopics: [],
        };
      }
    } catch (error) {
      console.error("获取学习进度异常:", error);
      // 返回默认数据
      return {
        solvedProblems: 0,
        totalProblems: 0,
        recentTopics: [],
        recommendedTopics: [],
      };
    }
  }

  /**
   * 发送聊天消息到后端
   * @param message 用户消息
   * @param questionId 当前题目ID
   * @returns 机器人回复
   */
  async sendChatMessage(message: string, questionId?: number) {
    try {
      const userStore = useUserStore();
      const token = userStore.token;

      const headers: Record<string, string> = {
        "Content-Type": "application/json",
      };

      if (token) {
        headers["Authorization"] = `Bearer ${token}`;
      }

      const response = await axios.post<ChatResponse>(
        `${API_BASE_URL}/chat/message`,
        {
          message,
          questionId,
        },
        { headers }
      );

      if (response.data.code === 0) {
        return response.data.data;
      } else {
        console.error("发送消息失败:", response.data.message);
        throw new Error(response.data.message);
      }
    } catch (error) {
      console.error("发送消息异常:", error);
      throw error;
    }
  }

  /**
   * 获取题目推荐
   * @returns 推荐题目列表
   */
  async getRecommendedProblems() {
    try {
      const userStore = useUserStore();
      const token = userStore.token;

      if (!token) {
        return [];
      }

      const response = await axios.get(`${API_BASE_URL}/problems/recommended`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (response.data.code === 0) {
        return response.data.data;
      } else {
        console.error("获取推荐题目失败:", response.data.message);
        return [];
      }
    } catch (error) {
      console.error("获取推荐题目异常:", error);
      return [];
    }
  }

  /**
   * 获取聊天历史记录
   * @param questionId 当前题目ID (可选)
   * @returns 聊天历史消息列表
   */
  async getChatHistory(questionId?: number): Promise<ChatMessage[]> {
    try {
      const userStore = useUserStore();
      const token = userStore.token;

      // 检查用户是否已登录
      if (!token) {
        console.error("获取聊天历史失败: 用户未登录");
        return [];
      }

      const headers: Record<string, string> = {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      };

      const params: Record<string, any> = {};
      if (questionId !== undefined) {
        params.questionId = questionId;
      }

      const response = await axios.get<{
        code: number;
        data: ChatMessage[];
        message: string;
      }>(`${API_BASE_URL}/chat/history`, {
        headers,
        params,
      });

      if (response.data.code === 0 && Array.isArray(response.data.data)) {
        return response.data.data;
      } else {
        console.error("获取聊天历史失败:", response.data.message);
        return [];
      }
    } catch (error: unknown) {
      const errorMessage = error instanceof Error ? error.message : "未知错误";
      console.error("获取聊天历史异常:", errorMessage);
      return [];
    }
  }
}

export default new ChatBotService();
