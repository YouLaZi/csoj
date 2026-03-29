/**
 * ChatBot Type Definitions
 */

export type MessageRole = "user" | "assistant";

export type ContentType = "text" | "code";

export interface ChatMessage {
  id: string;
  role: MessageRole;
  content: string;
  contentType?: ContentType;
  language?: string;
  timestamp: number;
  isStreaming?: boolean;
}

export interface ChatSession {
  id: string;
  questionId?: number;
  messages: ChatMessage[];
  createdAt: number;
  updatedAt: number;
}

export interface SendMessageOptions {
  message: string;
  questionId?: number;
  onChunk?: (chunk: string) => void;
  onComplete?: (fullResponse: string) => void;
  onError?: (error: Error) => void;
}

export interface LearningProgress {
  solvedProblems: number;
  totalProblems: number;
  recentTopics: string[];
  recommendedTopics: string[];
}

export interface Question {
  id: number;
  title: string;
  content?: string;
  difficulty?: string;
  tags?: string[];
}

export interface AIConfig {
  model: string;
  maxTokens: number;
  temperature: number;
}
