/**
 * 聊天机器人API模拟数据
 * 用于在后端API完成前进行前端测试
 */

import { createServer } from "miragejs";

/**
 * 配置模拟服务器
 */
export function setupMockServer() {
  return createServer({
    routes() {
      this.namespace = "api";

      // 处理根路径 /api/ 的请求
      this.get("/", () => {
        return { message: "MirageJS mock server is running." };
      });

      // 获取用户登录状态
      this.get("/user/get/login", (schema, request) => {
        // 检查认证头
        const authHeader = request.requestHeaders.Authorization;
        if (!authHeader || !authHeader.startsWith("Bearer ")) {
          return {
            code: 401,
            message: "未授权访问",
            data: null,
          };
        }

        // 返回模拟数据
        return {
          code: 0,
          message: "success",
          data: {
            isLoggedIn: true,
            username: "testUser",
            role: "student",
          },
        };
      });

      // 获取学习进度
      this.get("/user/learning-progress", (schema, request) => {
        // 检查认证头
        const authHeader = request.requestHeaders.Authorization;
        if (!authHeader || !authHeader.startsWith("Bearer ")) {
          return {
            code: 401,
            message: "未授权访问",
            data: null,
          };
        }

        // 返回模拟数据
        return {
          code: 0,
          message: "success",
          data: {
            solvedProblems: 15,
            totalProblems: 50,
            recentTopics: ["排序算法", "动态规划", "二叉树", "图算法"],
            recommendedTopics: ["贪心算法", "分治法", "回溯算法"],
          },
        };
      });

      // 发送聊天消息
      this.post("/chat/message", (schema, request) => {
        const body = JSON.parse(request.requestBody);
        const { message, questionId } = body;

        // 检查是否包含代码
        const codeRegex = /```([\w]*)[\s\S]*?```/;
        const isCodeAnalysis = codeRegex.test(message);

        // 根据不同类型的消息返回不同的响应
        if (isCodeAnalysis) {
          return {
            code: 0,
            message: "success",
            data: {
              content: "我分析了你的代码，发现了以下问题：",
              contentType: "text",
              codeAnalysis: {
                errors: [
                  "没有处理边界情况",
                  "可能存在整数溢出问题",
                  "算法时间复杂度较高",
                ],
                suggestions: [
                  "添加边界条件检查",
                  "考虑使用long类型避免整数溢出",
                  "可以使用更高效的算法降低时间复杂度",
                ],
              },
            },
          };
        } else if (message.includes("算法知识")) {
          return {
            code: 0,
            message: "success",
            data: {
              content:
                "这道题可能涉及以下算法知识：\n1. 动态规划\n2. 贪心算法\n3. 图的遍历",
              contentType: "text",
            },
          };
        } else if (message.includes("解题思路")) {
          return {
            code: 0,
            message: "success",
            data: {
              content:
                "解决这个问题的思路如下：\n1. 首先分析问题特点\n2. 考虑使用动态规划方法\n3. 定义状态和转移方程\n4. 处理边界情况",
              contentType: "text",
            },
          };
        } else {
          // 默认返回代码示例
          return {
            code: 0,
            message: "success",
            data: {
              content: "这是一个解决该问题的示例代码：",
              contentType: "text",
              nextMessage: {
                content:
                  "public class Solution {\n    public int solve(int[] nums) {\n        if (nums == null || nums.length == 0) {\n            return 0;\n        }\n        \n        int n = nums.length;\n        int[] dp = new int[n];\n        dp[0] = nums[0];\n        \n        for (int i = 1; i < n; i++) {\n            dp[i] = Math.max(dp[i-1] + nums[i], nums[i]);\n        }\n        \n        int max = dp[0];\n        for (int i = 1; i < n; i++) {\n            max = Math.max(max, dp[i]);\n        }\n        \n        return max;\n    }\n}",
                contentType: "code",
                language: "java",
              },
            },
          };
        }
      });

      // 获取推荐题目
      this.get("/problems/recommended", (schema, request) => {
        // 检查认证头
        const authHeader = request.requestHeaders.Authorization;
        if (!authHeader || !authHeader.startsWith("Bearer ")) {
          return {
            code: 401,
            message: "未授权访问",
            data: null,
          };
        }

        // 返回模拟数据
        return {
          code: 0,
          message: "success",
          data: [
            {
              id: 101,
              title: "最大子数组和",
              difficulty: "简单",
              tags: ["数组", "动态规划"],
            },
            {
              id: 102,
              title: "合并两个有序链表",
              difficulty: "简单",
              tags: ["链表", "递归"],
            },
            {
              id: 103,
              title: "二叉树的层序遍历",
              difficulty: "中等",
              tags: ["树", "广度优先搜索"],
            },
          ],
        };
      });
    },
  });
}
