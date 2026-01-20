import { useUserStore } from "@/store/useUserStore";
import axios from "axios";

// API基础URL
const API_BASE_URL = "/api";

/**
 * 公告类型定义
 */
export interface Announcement {
  id: number;
  title: string;
  content: string;
  createTime: Date;
  isNew?: boolean;
}

/**
 * 系统公告服务
 * 处理系统公告相关功能
 */
class AnnouncementService {
  /**
   * 获取系统公告列表
   * @returns 公告列表
   */
  async getAnnouncements() {
    try {
      const userStore = useUserStore();
      const token = userStore.token;

      const headers: Record<string, string> = {
        "Content-Type": "application/json",
      };

      if (token) {
        headers["Authorization"] = `Bearer ${token}`;
      }

      const response = await axios.get(`${API_BASE_URL}/announcement/list`, {
        headers,
      });

      if (response.data.code === 0) {
        // 处理公告数据，标记新公告
        const announcementData = response.data.data;
        const now = new Date();
        const threeDaysAgo = new Date(now.getTime() - 3 * 24 * 60 * 60 * 1000);

        // 检查是否是分页对象（包含records字段）
        if (announcementData && announcementData.records) {
          const announcements = announcementData.records;
          return {
            code: 0,
            message: "success",
            data: announcements.map((item: Announcement) => ({
              ...item,
              isNew: new Date(item.createTime) > threeDaysAgo,
            })),
            total: announcementData.total,
            current: announcementData.current,
            size: announcementData.size,
          };
        } else if (Array.isArray(announcementData)) {
          // 兼容直接返回数组的情况
          return {
            code: 0,
            message: "success",
            data: announcementData.map((item: Announcement) => ({
              ...item,
              isNew: new Date(item.createTime) > threeDaysAgo,
            })),
          };
        } else {
          console.error(
            "获取公告数据格式错误：不是数组或分页对象",
            announcementData
          );
          // 返回空数组，避免后续处理错误
          return {
            code: -1,
            message: "获取公告数据格式错误",
            data: [],
          };
        }
      } else {
        return response.data;
      }
    } catch (error: unknown) {
      console.error("获取公告失败:", error);
      return {
        code: -1,
        message: error instanceof Error ? error.message : "获取公告失败",
        data: [],
      };
    }
  }

  /**
   * 获取未读公告数量
   * @returns 未读公告数量
   */
  async getUnreadAnnouncementCount() {
    try {
      const userStore = useUserStore();
      const token = userStore.token;

      if (!token) {
        return {
          code: -1,
          message: "用户未登录",
          data: 0,
        };
      }

      const response = await axios.get(
        `${API_BASE_URL}/announcements/unread/count`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      return response.data;
    } catch (error: unknown) {
      console.error("获取未读公告数量失败:", error);
      return {
        code: -1,
        message:
          error instanceof Error ? error.message : "获取未读公告数量失败",
        data: 0,
      };
    }
  }

  /**
   * 标记公告为已读
   * @param announcementId 公告ID
   * @returns 操作结果
   */
  async markAnnouncementAsRead(announcementId: number) {
    try {
      const userStore = useUserStore();
      const token = userStore.token;

      if (!token) {
        return {
          code: -1,
          message: "用户未登录",
          data: false,
        };
      }

      const response = await axios.post(
        `${API_BASE_URL}/announcements/read/${announcementId}`,
        {},
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      return response.data;
    } catch (error: unknown) {
      console.error("标记公告已读失败:", error);
      return {
        code: -1,
        message: error instanceof Error ? error.message : "标记公告已读失败",
        data: false,
      };
    }
  }
}

export default new AnnouncementService();
