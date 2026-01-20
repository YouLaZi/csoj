package com.oj.cs.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.mapper.ChatMessageMapper;
import com.oj.cs.mapper.LearningProgressMapper;
import com.oj.cs.mapper.QuestionMapper;
import com.oj.cs.mapper.QuestionSubmitMapper;
import com.oj.cs.model.dto.chat.ChatMessageRequest;
import com.oj.cs.model.dto.chat.ChatMessageResponse;
import com.oj.cs.model.entity.ChatMessage;
import com.oj.cs.model.entity.LearningProgress;
import com.oj.cs.model.entity.Question;
import com.oj.cs.model.entity.QuestionSubmit;
import com.oj.cs.model.entity.User;
import com.oj.cs.service.ChatBotService;
import com.oj.cs.service.UserService;

import lombok.extern.slf4j.Slf4j;

/** 聊天机器人服务实现类 */
@Service
@Slf4j
public class ChatBotServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage>
    implements ChatBotService {

  @Resource private UserService userService;

  @Resource private QuestionMapper questionMapper;

  @Resource private QuestionSubmitMapper questionSubmitMapper;

  @Resource private LearningProgressMapper learningProgressMapper;

  @Resource private RestTemplate restTemplate;

  @Value("${ai.service.url:https://openrouter.ai/api/v1/chat/completions}")
  private String aiServiceUrl;

  @Value(
      "${ai.service.api-key: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}")
  private String aiServiceApiKey;

  @Value("${ai.service.model:deepseek/deepseek-prover-v2:free}")
  private String aiServiceModel;

  @Override
  public ChatMessageResponse sendChatMessage(
      ChatMessageRequest chatMessageRequest, Long loginUserId) {
    // 参数校验
    if (chatMessageRequest == null || chatMessageRequest.getMessage() == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR, "消息不能为空");
    }

    // 获取用户信息
    User loginUser = userService.getById(loginUserId);
    if (loginUser == null) {
      throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
    }

    // 保存用户消息
    ChatMessage userMessage = new ChatMessage();
    userMessage.setUserId(loginUserId);
    userMessage.setQuestionId(chatMessageRequest.getQuestionId());
    userMessage.setContent(chatMessageRequest.getMessage());
    userMessage.setMessageType("user");
    userMessage.setContentType("text");
    userMessage.setCreateTime(new Date());
    userMessage.setUpdateTime(new Date());
    userMessage.setIsDelete(0);
    this.saveChatMessage(userMessage);

    // 调用AI服务处理用户问题
    ChatMessageResponse response = callAiService(chatMessageRequest, loginUser);

    // 保存机器人回复
    ChatMessage botMessage = new ChatMessage();
    botMessage.setUserId(loginUserId);
    botMessage.setQuestionId(chatMessageRequest.getQuestionId());
    botMessage.setContent(response.getContent());
    botMessage.setMessageType("bot");
    botMessage.setContentType(response.getContentType());
    botMessage.setLanguage(response.getLanguage());
    botMessage.setCreateTime(new Date());
    botMessage.setUpdateTime(new Date());
    botMessage.setIsDelete(0);
    this.saveChatMessage(botMessage);

    return response;
  }

  /**
   * 调用AI服务API
   *
   * @param chatMessageRequest 聊天消息请求
   * @param user 当前用户
   * @return AI响应结果
   */
  private ChatMessageResponse callAiService(ChatMessageRequest chatMessageRequest, User user) {
    try {
      // 准备请求头
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.set("Authorization", "Bearer " + aiServiceApiKey);
      headers.set("HTTP-Referer", "https://csoj.com");
      headers.set("X-Title", "CSOJ AI Assistant");

      // 准备消息内容
      StringBuilder messageContent = new StringBuilder(chatMessageRequest.getMessage());

      // 如果有关联题目，添加题目信息到消息内容
      if (chatMessageRequest.getQuestionId() != null) {
        Question question = questionMapper.selectById(chatMessageRequest.getQuestionId());
        if (question != null) {
          messageContent
              .append("\n\n题目信息：\n")
              .append("标题：")
              .append(question.getTitle())
              .append("\n")
              .append("内容：")
              .append(question.getContent())
              .append("\n")
              .append("难度：")
              .append(question.getDifficulty());
        }
      }

      // 准备请求体
      Map<String, Object> message = new HashMap<>();
      message.put("role", "user");
      message.put("content", messageContent.toString());

      List<Map<String, Object>> messages = new ArrayList<>();
      messages.add(message);

      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("model", aiServiceModel);
      requestBody.put("messages", messages);

      // 发送请求到AI服务
      HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
      ResponseEntity<Map<String, Object>> responseEntity =
          restTemplate.exchange(
              aiServiceUrl,
              HttpMethod.POST,
              requestEntity,
              new ParameterizedTypeReference<Map<String, Object>>() {});

      // 处理响应
      Map<String, Object> responseBody = responseEntity.getBody();
      if (responseBody == null) {
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI服务返回为空");
      }

      // 解析AI服务响应
      ChatMessageResponse response = new ChatMessageResponse();

      @SuppressWarnings("unchecked")
      List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
      if (choices != null && !choices.isEmpty()) {
        Map<String, Object> choice = choices.get(0);
        @SuppressWarnings("unchecked")
        Map<String, Object> message_response = (Map<String, Object>) choice.get("message");
        if (message_response != null) {
          String content = (String) message_response.get("content");
          response.setContent(content != null ? content : "AI服务暂时无法回应，请稍后再试");
          response.setContentType("text");
          response.setLanguage("");
        }
      } else {
        response.setContent("AI服务暂时无法回应，请稍后再试");
        response.setContentType("text");
        response.setLanguage("");
      }

      return response;

    } catch (Exception e) {
      log.error("调用AI服务异常", e);
      // 发生异常时返回默认响应
      ChatMessageResponse fallbackResponse = new ChatMessageResponse();
      fallbackResponse.setContent("抱歉，AI服务暂时不可用，请稍后再试。");
      fallbackResponse.setContentType("text");
      return fallbackResponse;
    }
  }

  @Override
  public Map<String, Object> getLearningProgress(Long userId) {
    // 参数校验
    if (userId == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    // 获取用户信息
    User user = userService.getById(userId);
    if (user == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
    }

    // 查询用户已解决的题目数量
    QueryWrapper<QuestionSubmit> submitQueryWrapper = new QueryWrapper<>();
    submitQueryWrapper.eq("userId", userId);
    submitQueryWrapper.eq("status", 2); // 假设状态2表示通过
    long solvedProblems = questionSubmitMapper.selectCount(submitQueryWrapper);

    // 查询总题目数量
    long totalProblems = questionMapper.selectCount(null);

    // 查询或创建学习进度记录
    QueryWrapper<LearningProgress> progressQueryWrapper = new QueryWrapper<>();
    progressQueryWrapper.eq("user_id", userId);
    progressQueryWrapper.eq("is_delete", 0);
    LearningProgress learningProgress = learningProgressMapper.selectOne(progressQueryWrapper);

    List<String> recentTopics;
    List<String> recommendedTopics;

    if (learningProgress == null) {
      // 如果不存在学习进度记录，创建新记录
      learningProgress = new LearningProgress();
      learningProgress.setUserId(userId);
      learningProgress.setSolvedProblems((int) solvedProblems);

      // 默认的最近学习主题和推荐主题
      recentTopics = Arrays.asList("排序算法", "动态规划", "二叉树");
      recommendedTopics = Arrays.asList("图算法", "贪心算法");

      // 将列表转换为JSON字符串存储
      try {
        ObjectMapper objectMapper = new ObjectMapper();
        learningProgress.setRecentTopics(objectMapper.writeValueAsString(recentTopics));
        learningProgress.setRecommendedTopics(objectMapper.writeValueAsString(recommendedTopics));
      } catch (Exception e) {
        log.error("JSON转换异常", e);
        learningProgress.setRecentTopics("[]");
        learningProgress.setRecommendedTopics("[]");
      }

      learningProgress.setCreateTime(new Date());
      learningProgress.setUpdateTime(new Date());
      learningProgress.setIsDelete(0);

      learningProgressMapper.insert(learningProgress);
    } else {
      // 更新已解决题目数量
      learningProgress.setSolvedProblems((int) solvedProblems);
      learningProgressMapper.updateById(learningProgress);

      // 从数据库中获取最近学习主题和推荐主题
      try {
        ObjectMapper objectMapper = new ObjectMapper();
        recentTopics =
            objectMapper.readValue(
                learningProgress.getRecentTopics(), new TypeReference<List<String>>() {});
        recommendedTopics =
            objectMapper.readValue(
                learningProgress.getRecommendedTopics(), new TypeReference<List<String>>() {});
      } catch (Exception e) {
        log.error("JSON解析异常", e);
        recentTopics = new ArrayList<>();
        recommendedTopics = new ArrayList<>();
      }
    }

    // 组装结果
    Map<String, Object> result = new HashMap<>();
    result.put("solvedProblems", solvedProblems);
    result.put("totalProblems", totalProblems);
    result.put("recentTopics", recentTopics);
    result.put("recommendedTopics", recommendedTopics);

    return result;
  }

  @Override
  public List<Map<String, Object>> getRecommendedProblems(Long userId) {
    // 参数校验
    if (userId == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }

    // 获取用户信息
    User user = userService.getById(userId);
    if (user == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
    }

    // 查询用户已解决的题目ID列表
    QueryWrapper<QuestionSubmit> submitQueryWrapper = new QueryWrapper<>();
    submitQueryWrapper.eq("userId", userId);
    submitQueryWrapper.eq("status", 2); // 假设状态2表示通过
    submitQueryWrapper.select("questionId");
    List<QuestionSubmit> submits = questionSubmitMapper.selectList(submitQueryWrapper);
    Set<Long> solvedQuestionIds =
        submits.stream().map(QuestionSubmit::getQuestionId).collect(Collectors.toSet());

    // 查询推荐题目（这里简单实现为未解决的题目）
    QueryWrapper<Question> questionQueryWrapper = new QueryWrapper<>();
    if (!solvedQuestionIds.isEmpty()) {
      questionQueryWrapper.notIn("id", solvedQuestionIds);
    }
    questionQueryWrapper.orderByAsc("difficulty"); // 按难度排序
    questionQueryWrapper.last("limit 10"); // 最多返回10道题
    List<Question> questions = questionMapper.selectList(questionQueryWrapper);

    // 转换为前端需要的格式
    return questions.stream()
        .map(
            question -> {
              Map<String, Object> map = new HashMap<>();
              map.put("id", question.getId());
              map.put("title", question.getTitle());
              map.put("difficulty", question.getDifficulty());
              // TODO: 获取题目标签
              List<String> tags = Arrays.asList("数组", "动态规划");
              map.put("tags", tags);
              return map;
            })
        .collect(Collectors.toList());
  }

  @Override
  public Long saveChatMessage(ChatMessage chatMessage) {
    if (chatMessage == null) {
      throw new BusinessException(ErrorCode.PARAMS_ERROR);
    }
    boolean result = this.save(chatMessage);
    if (!result) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "保存失败");
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
    return this.list(queryWrapper);
  }
}
