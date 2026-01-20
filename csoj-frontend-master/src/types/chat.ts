/**
 * 聊天消息类型定义
 */
export interface ChatMessage {
  content: string;
  type: "user" | "bot";
  // 内容类型，支持代码和公式
  contentType?: "text" | "code" | "math";
  language?: string; // 代码语言
  codeAnalysis?: {
    errors?: string[];
    suggestions?: string[];
  };
}

/**
 * 学习进度类型定义
 */
export interface LearningProgress {
  solvedProblems: number;
  totalProblems: number;
  recentTopics: string[];
  recommendedTopics: string[];
}

/**
 * 题目信息类型定义
 */
export interface Question {
  id?: number;
  title?: string;
  content?: string;
  difficulty?: string;
  tags?: string[];
}
