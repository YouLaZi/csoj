import { useUserStore } from "@/store/useUserStore";
import type { ChatMessage, ChatSession, LearningProgress } from "@/types/chat";

const API_BASE = "/api";

/**
 * Generate a unique ID
 */
function generateId(): string {
  return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
}

/**
 * ChatBot Service - Handles all chat-related API interactions
 */
class ChatBotService {
  private abortController: AbortController | null = null;

  /**
   * Send a message and get streaming response via SSE
   */
  async sendMessageStream(
    message: string,
    questionId?: number,
    onChunk: (chunk: string) => void,
    onComplete: (fullResponse: string) => void,
    onError: (error: Error) => void
  ): Promise<void> {
    // Cancel any existing request
    this.cancelStreaming();

    const userStore = useUserStore();
    const token = userStore.token;

    // Create abort controller for this request
    this.abortController = new AbortController();

    try {
      const response = await fetch(`${API_BASE}/chat/stream`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        body: JSON.stringify({ message, questionId }),
        signal: this.abortController.signal,
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const reader = response.body?.getReader();
      if (!reader) {
        throw new Error("No response body");
      }

      const decoder = new TextDecoder();
      let fullResponse = "";

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        const chunk = decoder.decode(value, { stream: true });
        const lines = chunk.split("\n");

        for (const line of lines) {
          if (line.startsWith("data: ")) {
            const data = line.slice(6);
            if (data === "[DONE]") {
              onComplete(fullResponse);
              return;
            }
            try {
              const parsed = JSON.parse(data);
              if (parsed.content) {
                fullResponse += parsed.content;
                onChunk(parsed.content);
              }
            } catch {
              // If not JSON, treat as plain text
              if (data && data !== "[DONE]") {
                fullResponse += data;
                onChunk(data);
              }
            }
          }
        }
      }

      onComplete(fullResponse);
    } catch (error) {
      if ((error as Error).name === "AbortError") {
        console.log("Request aborted");
      } else {
        onError(error as Error);
      }
    } finally {
      this.abortController = null;
    }
  }

  /**
   * Cancel ongoing streaming request
   */
  cancelStreaming(): void {
    if (this.abortController) {
      this.abortController.abort();
      this.abortController = null;
    }
  }

  /**
   * Send a message without streaming (fallback)
   */
  async sendMessage(message: string, questionId?: number): Promise<string> {
    const userStore = useUserStore();
    const token = userStore.token;

    const response = await fetch(`${API_BASE}/chat/message`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: JSON.stringify({ message, questionId }),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const result = await response.json();
    if (result.code === 0 && result.data) {
      return result.data.content || "";
    }
    throw new Error(result.message || "Failed to get response");
  }

  /**
   * Get chat history
   */
  async getChatHistory(questionId?: number): Promise<ChatMessage[]> {
    const userStore = useUserStore();
    const token = userStore.token;

    if (!token) return [];

    try {
      const params = new URLSearchParams();
      if (questionId) params.append("questionId", String(questionId));

      const response = await fetch(
        `${API_BASE}/chat/history?${params.toString()}`,
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );

      if (!response.ok) return [];

      const result = await response.json();
      if (result.code === 0 && Array.isArray(result.data)) {
        return result.data.map((msg: any) => ({
          id: String(msg.id || generateId()),
          role: msg.messageType === "user" ? "user" : "assistant",
          content: msg.content,
          contentType: msg.contentType || "text",
          language: msg.language,
          timestamp: new Date(msg.createTime).getTime(),
        }));
      }
      return [];
    } catch (error) {
      console.error("Failed to load chat history:", error);
      return [];
    }
  }

  /**
   * Get learning progress
   */
  async getLearningProgress(): Promise<LearningProgress> {
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

    try {
      const response = await fetch(`${API_BASE}/user/learning-progress`, {
        headers: { Authorization: `Bearer ${token}` },
      });

      if (!response.ok) {
        return {
          solvedProblems: 0,
          totalProblems: 0,
          recentTopics: [],
          recommendedTopics: [],
        };
      }

      const result = await response.json();
      if (result.code === 0 && result.data) {
        return result.data;
      }
      return {
        solvedProblems: 0,
        totalProblems: 0,
        recentTopics: [],
        recommendedTopics: [],
      };
    } catch (error) {
      console.error("Failed to load learning progress:", error);
      return {
        solvedProblems: 0,
        totalProblems: 0,
        recentTopics: [],
        recommendedTopics: [],
      };
    }
  }

  /**
   * Clear chat history for current session
   */
  async clearHistory(questionId?: number): Promise<boolean> {
    const userStore = useUserStore();
    const token = userStore.token;

    if (!token) return false;

    try {
      const params = new URLSearchParams();
      if (questionId) params.append("questionId", String(questionId));

      const response = await fetch(`${API_BASE}/chat/clear?${params.toString()}`, {
        method: "DELETE",
        headers: { Authorization: `Bearer ${token}` },
      });

      return response.ok;
    } catch (error) {
      console.error("Failed to clear chat history:", error);
      return false;
    }
  }
}

export const chatBotService = new ChatBotService();
export default chatBotService;
