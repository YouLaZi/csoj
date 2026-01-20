# ChatBot 后端实现说明

## 概述

本文档描述了解题助手（ChatBot）组件的后端实现。解题助手是一个浮动的聊天界面，可以帮助用户解决算法题目，提供代码分析、算法知识和解题思路等功能。

## 文件结构

- `controller/ChatBotController.java`: 聊天机器人控制器，处理前端API请求
- `service/ChatBotService.java`: 聊天机器人服务接口
- `service/impl/ChatBotServiceImpl.java`: 聊天机器人服务实现类
- `model/entity/ChatMessage.java`: 聊天消息实体类
- `model/dto/chat/ChatMessageRequest.java`: 聊天消息请求DTO
- `model/dto/chat/ChatMessageResponse.java`: 聊天消息响应DTO
- `mapper/ChatMessageMapper.java`: 聊天消息数据库操作接口
- `sql/chat_message.sql`: 聊天消息表SQL脚本

## API接口

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

### 4. 获取聊天历史

- **URL**: `/api/chat/history`
- **方法**: GET
- **参数**: `questionId`（可选，问题ID）
- **响应**:
  ```json
  {
    "code": 0,
    "message": "success",
    "data": [
      {
        "id": 1,
        "userId": 10001,
        "questionId": 123,
        "content": "这道题如何解决？",
        "messageType": "user",
        "contentType": "text",
        "createTime": "2023-05-01 10:00:00"
      },
      {
        "id": 2,
        "userId": 10001,
        "questionId": 123,
        "content": "这道题可以使用动态规划解决...",
        "messageType": "bot",
        "contentType": "text",
        "createTime": "2023-05-01 10:00:05"
      }
    ]
  }
  ```

## 数据库设计

### 聊天消息表 (chat_message)

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint | 主键 |
| user_id | bigint | 用户ID |
| question_id | bigint | 问题ID（可选） |
| content | text | 消息内容 |
| message_type | varchar(10) | 消息类型（user-用户消息，bot-机器人回复） |
| content_type | varchar(10) | 内容类型（text-文本，code-代码，math-数学公式） |
| language | varchar(20) | 编程语言（当contentType为code时有效） |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |
| is_delete | tinyint | 是否删除（0-未删除，1-已删除） |

## 扩展与集成

### 集成AI服务

当前实现中，AI回复是模拟生成的。在实际生产环境中，需要集成真实的AI服务来处理用户问题。可以通过以下方式扩展：

1. 在`ChatBotServiceImpl`中的`sendChatMessage`方法中，替换模拟回复的代码，调用外部AI服务API
2. 可以集成OpenAI、百度文心一言等大语言模型服务
3. 针对编程问题，可以构建专门的知识库和训练数据

### 学习进度分析

当前实现中，学习进度和推荐题目是基于简单逻辑生成的。可以通过以下方式增强：

1. 实现更复杂的学习进度分析算法，基于用户解题历史和题目难度
2. 使用协同过滤或内容推荐算法，为用户推荐更合适的题目
3. 分析用户的解题模式和错误类型，提供针对性的学习建议

## 部署说明

1. 执行`sql/chat_message.sql`脚本创建数据库表
2. 在应用配置中添加必要的AI服务API密钥和配置
3. 编译并部署应用

## 注意事项

1. 所有API请求都需要用户认证，未登录用户无法访问
2. 聊天历史会按时间顺序保存，可以按问题ID筛选
3. 在生产环境中，需要实现真实的AI服务集成，当前实现仅为演示