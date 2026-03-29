package com.oj.cs.service.impl;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.mapper.ChatMessageMapper;
import com.oj.cs.mapper.QuestionMapper;
import com.oj.cs.model.dto.chat.ChatMessageRequest;
import com.oj.cs.model.dto.chat.ChatMessageResponse;
import com.oj.cs.model.entity.ChatMessage;
import com.oj.cs.model.entity.Question;
import com.oj.cs.model.entity.User;
import com.oj.cs.service.ChatBotService;
import com.oj.cs.service.UserService;

import lombok.extern.slf4j.Slf4j;

/**
 * ChatBot Service Implementation
 * Supports streaming responses via SSE
 */
@Service
@Slf4j
public class ChatBotServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage>
    implements ChatBotService {

    @Resource
    private UserService userService;

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private RestClient restClient;

    @Value("${ai.service.url:https://openrouter.ai/api/v1/chat/completions}")
    private String aiServiceUrl;

    @Value("${ai.service.api-key:}")
    private String aiServiceApiKey;

    @Value("${ai.service.model:deepseek/deepseek-chat}")
    private String aiServiceModel;

    @Value("${ai.service.max-tokens:2048}")
    private int maxTokens;

    @Value("${ai.service.temperature:0.7}")
    private double temperature;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Session storage for conversation context (in production, use Redis)
    private final ConcurrentHashMap<String, List<Map<String, String>>> sessionContexts = new ConcurrentHashMap<>();

    private static final int MAX_CONTEXT_MESSAGES = 10;
    private static final long SSE_TIMEOUT = 60_000L; // 60 seconds

    @Override
    public SseEmitter streamChatMessage(ChatMessageRequest request, Long userId) {
        // Validate parameters
        if (request == null || request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Message cannot be empty");
        }

        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // Create SSE emitter
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        // Get or create session ID
        String sessionId = getSessionId(userId, request.getQuestionId());

        // Save user message
        ChatMessage userMessage = new ChatMessage();
        userMessage.setUserId(userId);
        userMessage.setQuestionId(request.getQuestionId());
        userMessage.setContent(request.getMessage());
        userMessage.setMessageType("user");
        userMessage.setContentType("text");
        userMessage.setCreateTime(new Date());
        userMessage.setUpdateTime(new Date());
        userMessage.setIsDelete(0);
        this.save(userMessage);

        // Build context with conversation history
        List<Map<String, String>> context = sessionContexts.computeIfAbsent(sessionId, k -> new ArrayList<>());

        // Add current message to context
        context.add(Map.of("role", "user", "content", request.getMessage()));

        // Trim context if too long
        if (context.size() > MAX_CONTEXT_MESSAGES) {
            context = new ArrayList<>(context.subList(context.size() - MAX_CONTEXT_MESSAGES, context.size()));
            sessionContexts.put(sessionId, context);
        }

        // Build system prompt
        String systemPrompt = buildSystemPrompt(request.getQuestionId());

        // Prepare messages for API
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.addAll(context);

        // Start async streaming
        new Thread(() -> {
            try {
                StringBuilder fullResponse = new StringBuilder();

                // Call AI API with streaming
                streamFromAI(messages, (chunk) -> {
                    try {
                        fullResponse.append(chunk);
                        emitter.send(SseEmitter.event()
                                .data(objectMapper.writeValueAsString(Map.of("content", chunk)))
                                .build());
                    } catch (IOException e) {
                        log.error("Error sending SSE event", e);
                        emitter.completeWithError(e);
                    }
                });

                // Save bot message
                ChatMessage botMessage = new ChatMessage();
                botMessage.setUserId(userId);
                botMessage.setQuestionId(request.getQuestionId());
                botMessage.setContent(fullResponse.toString());
                botMessage.setMessageType("bot");
                botMessage.setContentType("text");
                botMessage.setCreateTime(new Date());
                botMessage.setUpdateTime(new Date());
                botMessage.setIsDelete(0);
                this.save(botMessage);

                // Add response to context
                context.add(Map.of("role", "assistant", "content", fullResponse.toString()));
                sessionContexts.put(sessionId, context);

                // Send completion signal
                emitter.send(SseEmitter.event().data("[DONE]").build());
                emitter.complete();

            } catch (Exception e) {
                log.error("Error during streaming", e);
                try {
                    emitter.send(SseEmitter.event()
                            .data(objectMapper.writeValueAsString(Map.of("error", e.getMessage())))
                            .build());
                } catch (IOException ignored) {
                }
                emitter.completeWithError(e);
            }
        }).start();

        emitter.onCompletion(() -> log.debug("SSE completed for user {}", userId));
        emitter.onTimeout(() -> {
            log.warn("SSE timeout for user {}", userId);
            emitter.complete();
        });
        emitter.onError(e -> log.error("SSE error for user {}", userId, e));

        return emitter;
    }

    /**
     * Stream response from AI API
     */
    private void streamFromAI(List<Map<String, String>> messages, Consumer<String> onChunk) {
        if (aiServiceApiKey == null || aiServiceApiKey.isEmpty()) {
            // Fallback to mock response if no API key
            streamMockResponse(messages, onChunk);
            return;
        }

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", aiServiceModel);
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("temperature", temperature);
            requestBody.put("stream", true);

            // For now, use non-streaming and chunk the response
            // In production, implement proper SSE streaming from the AI provider
            String response = restClient.post()
                    .uri(aiServiceUrl)
                    .header("Authorization", "Bearer " + aiServiceApiKey)
                    .header("Content-Type", "application/json")
                    .header("HTTP-Referer", "https://csoj.com")
                    .header("X-Title", "CSOJ AI Assistant")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            // Parse response
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");

            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> choice = choices.get(0);
                Map<String, Object> message = (Map<String, Object>) choice.get("message");
                if (message != null) {
                    String content = (String) message.get("content");
                    if (content != null) {
                        // Simulate streaming by chunking the response
                        int chunkSize = 10;
                        for (int i = 0; i < content.length(); i += chunkSize) {
                            int end = Math.min(i + chunkSize, content.length());
                            onChunk.accept(content.substring(i, end));
                            Thread.sleep(30); // Small delay for effect
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error calling AI service", e);
            // Fallback to mock response
            streamMockResponse(messages, onChunk);
        }
    }

    /**
     * Mock response for testing/fallback
     */
    private void streamMockResponse(List<Map<String, String>> messages, Consumer<String> onChunk) {
        String lastUserMessage = messages.get(messages.size() - 1).get("content");
        String response = "I understand you're asking about: \"" + lastUserMessage + "\". " +
                "I'm currently operating in demo mode. Please configure the AI API key for full functionality. " +
                "In the meantime, I can help you with algorithm explanations, code analysis, and problem-solving strategies.";

        // Simulate streaming
        int chunkSize = 8;
        for (int i = 0; i < response.length(); i += chunkSize) {
            int end = Math.min(i + chunkSize, response.length());
            onChunk.accept(response.substring(i, end));
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Build system prompt with optional question context
     */
    private String buildSystemPrompt(Long questionId) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a helpful AI assistant for an Online Judge (OJ) platform. ");
        prompt.append("You help users understand algorithms, debug code, and solve programming problems. ");
        prompt.append("Be concise, clear, and encouraging. ");
        prompt.append("Use markdown formatting for code blocks and emphasis.\n\n");

        if (questionId != null) {
            Question question = questionMapper.selectById(questionId);
            if (question != null) {
                prompt.append("Current problem context:\n");
                prompt.append("- Title: ").append(question.getTitle()).append("\n");
                prompt.append("- Difficulty: ").append(question.getDifficulty()).append("\n");
                if (question.getContent() != null) {
                    // Truncate long content
                    String content = question.getContent();
                    if (content.length() > 500) {
                        content = content.substring(0, 500) + "...";
                    }
                    prompt.append("- Description: ").append(content).append("\n");
                }
            }
        }

        return prompt.toString();
    }

    /**
     * Generate session ID from user and question
     */
    private String getSessionId(Long userId, Long questionId) {
        if (questionId != null) {
            return userId + ":" + questionId;
        }
        return userId + ":global";
    }

    @Override
    public ChatMessageResponse sendChatMessage(ChatMessageRequest request, Long userId) {
        // Non-streaming fallback
        if (request == null || request.getMessage() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Message cannot be empty");
        }

        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // Save user message
        ChatMessage userMessage = new ChatMessage();
        userMessage.setUserId(userId);
        userMessage.setQuestionId(request.getQuestionId());
        userMessage.setContent(request.getMessage());
        userMessage.setMessageType("user");
        userMessage.setContentType("text");
        userMessage.setCreateTime(new Date());
        userMessage.setUpdateTime(new Date());
        userMessage.setIsDelete(0);
        this.save(userMessage);

        // Get response from AI (simplified non-streaming version)
        String sessionId = getSessionId(userId, request.getQuestionId());
        List<Map<String, String>> context = sessionContexts.computeIfAbsent(sessionId, k -> new ArrayList<>());
        context.add(Map.of("role", "user", "content", request.getMessage()));

        String systemPrompt = buildSystemPrompt(request.getQuestionId());
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.addAll(context);

        StringBuilder responseBuilder = new StringBuilder();
        streamFromAI(messages, responseBuilder::append);

        String responseContent = responseBuilder.toString();

        // Save bot message
        ChatMessage botMessage = new ChatMessage();
        botMessage.setUserId(userId);
        botMessage.setQuestionId(request.getQuestionId());
        botMessage.setContent(responseContent);
        botMessage.setMessageType("bot");
        botMessage.setContentType("text");
        botMessage.setCreateTime(new Date());
        botMessage.setUpdateTime(new Date());
        botMessage.setIsDelete(0);
        this.save(botMessage);

        // Update context
        context.add(Map.of("role", "assistant", "content", responseContent));
        sessionContexts.put(sessionId, context);

        ChatMessageResponse response = new ChatMessageResponse();
        response.setContent(responseContent);
        response.setContentType("text");
        return response;
    }

    @Override
    public Map<String, Object> getLearningProgress(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "User not found");
        }

        // Return default progress data
        Map<String, Object> result = new HashMap<>();
        result.put("solvedProblems", 0);
        result.put("totalProblems", questionMapper.selectCount(null));
        result.put("recentTopics", Arrays.asList("Arrays", "Dynamic Programming", "Binary Trees"));
        result.put("recommendedTopics", Arrays.asList("Graph Algorithms", "Greedy Algorithms"));

        return result;
    }

    @Override
    public List<Map<String, Object>> getRecommendedProblems(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // Return empty list for now
        return new ArrayList<>();
    }

    @Override
    public Long saveChatMessage(ChatMessage chatMessage) {
        if (chatMessage == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = this.save(chatMessage);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Failed to save");
        }
        return chatMessage.getId();
    }

    @Override
    public List<ChatMessage> getChatHistory(Long userId, Long questionId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        if (questionId != null) {
            queryWrapper.eq("question_id", questionId);
        }
        queryWrapper.eq("is_delete", 0);
        queryWrapper.orderByAsc("create_time");
        queryWrapper.last("LIMIT 50");
        return this.list(queryWrapper);
    }

    @Override
    public void clearChatHistory(Long userId, Long questionId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        if (questionId != null) {
            queryWrapper.eq("question_id", questionId);
        }

        ChatMessage update = new ChatMessage();
        update.setIsDelete(1);
        update.setUpdateTime(new Date());
        this.update(update, queryWrapper);

        // Clear session context
        String sessionId = getSessionId(userId, questionId);
        sessionContexts.remove(sessionId);
    }
}
