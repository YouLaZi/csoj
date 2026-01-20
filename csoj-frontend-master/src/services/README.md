# 聊天机器人后端支持说明

## 概述

本文档描述了解题助手（ChatBot）组件的后端支持实现。解题助手是一个浮动的聊天界面，可以帮助用户解决算法题目，提供代码分析、算法知识和解题思路等功能。

## 文件结构

- `ChatBotService.ts`: 聊天机器人服务，处理与后端API的交互
- `../types/chat.ts`: 聊天相关的类型定义
- `../mock/chatbot-api.js`: 模拟后端API（仅用于开发环境）

## 功能实现

### 1. 聊天功能

聊天功能通过`ChatBotService.sendChatMessage`方法实现，该方法将用户消息发送到后端，并接收机器人的回复。

```typescript
// 发送消息示例
const response = await ChatBotService.sendChatMessage("请分析这段代码", questionId);
```

### 2. 学习进度跟踪

学习进度通过`ChatBotService.getLearningProgress`方法获取，包括已解决题目数量、总题目数量、最近学习主题和推荐学习主题。

```typescript
// 获取学习进度示例
const progress = await ChatBotService.getLearningProgress();
```

### 3. 题目推荐

题目推荐通过`ChatBotService.getRecommendedProblems`方法获取，返回推荐给用户的题目列表。

```typescript
// 获取推荐题目示例
const recommendedProblems = await ChatBotService.getRecommendedProblems();
```

## 后端API接口

### 1. 发送聊天消息

- **URL**: `/api/chat/message`
- **方法**: POST
- **请求体**:
  ```json
  {
    "message": "用户消息内容",
    "questionId": 123 // 可选，当前题目ID
  }
  ```
- **响应**:
  ```json
  {
    "code": 0,
    "message": "success",
    "data": {
      "content": "回复内容",
      "contentType": "text", // 可选值: "text", "code", "math"
      "language": "java", // 当contentType为code时有效
      "codeAnalysis": { // 可选，代码分析结果
        "errors": ["错误1", "错误2"],
        "suggestions": ["建议1", "建议2"]
      }
    }
  }
  ```

### 2. 获取学习进度

- **URL**: `/api/user/learning-progress`
- **方法**: GET
- **响应**:
  ```json
  {
    "code": 0,
    "message": "success",
    "data": {
      "solvedProblems": 15,
      "totalProblems": 50,
      "recentTopics": ["排序算法", "动态规划", "二叉树"],
      "recommendedTopics": ["图算法", "贪心算法"]
    }
  }
  ```

### 3. 获取推荐题目

- **URL**: `/api/problems/recommended`
- **方法**: GET
- **响应**:
  ```json
  {
    "code": 0,
    "message": "success",
    "data": [
      {
        "id": 101,
        "title": "最大子数组和",
        "difficulty": "简单",
        "tags": ["数组", "动态规划"]
      },
      // 更多推荐题目...
    ]
  }
  ```

## 开发环境测试

在开发环境中，使用了MirageJS模拟后端API。模拟服务器配置在`src/mock/chatbot-api.js`中，并在`main.ts`中根据环境变量决定是否启用。

## 注意事项

1. 所有API请求都会自动附加用户认证信息（如果用户已登录）
2. 错误处理已在服务中实现，但在组件中仍需处理异常情况
3. 在生产环境中，需要实现真实的后端API接口