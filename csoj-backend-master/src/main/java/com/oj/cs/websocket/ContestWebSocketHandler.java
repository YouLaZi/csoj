package com.oj.cs.websocket;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/** 比赛状态 WebSocket 处理器 支持推送比赛状态变化、倒计时、公告等 */
@Component
@Slf4j
public class ContestWebSocketHandler extends TextWebSocketHandler {

  /** 存储比赛ID对应的 WebSocket 会话列表 */
  private final Map<Long, Map<String, WebSocketSession>> contestSessions =
      new ConcurrentHashMap<>();

  /** 存储会话ID到比赛ID的映射 */
  private final Map<String, Long> sessionContestMap = new ConcurrentHashMap<>();

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    String query = session.getUri().getQuery();
    Long contestId = extractContestId(query);

    if (contestId != null) {
      contestSessions
          .computeIfAbsent(contestId, k -> new ConcurrentHashMap<>())
          .put(session.getId(), session);
      sessionContestMap.put(session.getId(), contestId);

      log.info("比赛状态 WebSocket 连接建立: contestId={}, sessionId={}", contestId, session.getId());

      sendConnectMessage(session, contestId);
    } else {
      log.warn("WebSocket 连接缺少 contestId 参数: sessionId={}", session.getId());
      session.close();
    }
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    String payload = message.getPayload();

    if ("ping".equals(payload)) {
      session.sendMessage(new TextMessage("pong"));
    }
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    Long contestId = sessionContestMap.remove(session.getId());
    if (contestId != null) {
      Map<String, WebSocketSession> sessions = contestSessions.get(contestId);
      if (sessions != null) {
        sessions.remove(session.getId());
        if (sessions.isEmpty()) {
          contestSessions.remove(contestId);
        }
      }
      log.info("比赛状态 WebSocket 连接关闭: contestId={}, sessionId={}", contestId, session.getId());
    }
  }

  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
    log.error("比赛状态 WebSocket 传输错误: sessionId={}", session.getId(), exception);
    if (session.isOpen()) {
      session.close();
    }
  }

  /** 推送比赛状态更新 */
  public void broadcastContestUpdate(Long contestId, ContestMessage message) {
    Map<String, WebSocketSession> sessions = contestSessions.get(contestId);
    if (sessions == null || sessions.isEmpty()) {
      return;
    }

    message.setContestId(contestId);
    message.setTimestamp(System.currentTimeMillis());

    String messageJson;
    try {
      messageJson = objectMapper.writeValueAsString(message);
    } catch (Exception e) {
      log.error("序列化比赛消息失败: contestId={}", contestId, e);
      return;
    }

    TextMessage textMessage = new TextMessage(messageJson);

    for (Map.Entry<String, WebSocketSession> entry : sessions.entrySet()) {
      WebSocketSession session = entry.getValue();
      if (session.isOpen()) {
        try {
          session.sendMessage(textMessage);
        } catch (IOException e) {
          log.error("发送比赛状态消息失败: sessionId={}", session.getId(), e);
        }
      }
    }
  }

  /** 推送比赛倒计时 */
  public void broadcastCountdown(Long contestId, Integer remainingSeconds) {
    ContestMessage message = new ContestMessage();
    message.setType("countdown");
    message.setRemainingSeconds(remainingSeconds);

    broadcastContestUpdate(contestId, message);
  }

  /** 推送比赛公告 */
  public void broadcastAnnouncement(Long contestId, String announcement) {
    ContestMessage message = new ContestMessage();
    message.setType("announcement");
    message.setContent(announcement);

    broadcastContestUpdate(contestId, message);
  }

  /** 推送比赛状态变化 */
  public void broadcastStatusChange(Long contestId, String status) {
    ContestMessage message = new ContestMessage();
    message.setType("status_change");
    message.setStatus(status);

    broadcastContestUpdate(contestId, message);
  }

  /** 推送新题目解锁 */
  public void broadcastQuestionUnlock(Long contestId, Long questionId, String questionTitle) {
    ContestMessage message = new ContestMessage();
    message.setType("question_unlock");
    message.setQuestionId(questionId);
    message.setQuestionTitle(questionTitle);

    broadcastContestUpdate(contestId, message);
  }

  private Long extractContestId(String query) {
    if (query == null || query.isEmpty()) {
      return null;
    }
    for (String param : query.split("&")) {
      String[] pair = param.split("=");
      if (pair.length == 2 && "contestId".equals(pair[0])) {
        try {
          return Long.parseLong(pair[1]);
        } catch (NumberFormatException e) {
          return null;
        }
      }
    }
    return null;
  }

  private void sendConnectMessage(WebSocketSession session, Long contestId) throws IOException {
    ContestMessage message = new ContestMessage();
    message.setType("connected");
    message.setMessage("比赛状态推送连接已建立");
    message.setOnlineCount(getOnlineUserCount(contestId));

    String messageJson = objectMapper.writeValueAsString(message);
    session.sendMessage(new TextMessage(messageJson));
  }

  private int getOnlineUserCount(Long contestId) {
    Map<String, WebSocketSession> sessions = contestSessions.get(contestId);
    return sessions == null ? 0 : sessions.size();
  }

  /** 比赛消息 */
  public static class ContestMessage {
    private String type;
    private Long contestId;
    private String message;
    private String content;
    private String status;
    private Integer remainingSeconds;
    private Long questionId;
    private String questionTitle;
    private Integer onlineCount;
    private Long timestamp;

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public Long getContestId() {
      return contestId;
    }

    public void setContestId(Long contestId) {
      this.contestId = contestId;
    }

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }

    public String getContent() {
      return content;
    }

    public void setContent(String content) {
      this.content = content;
    }

    public String getStatus() {
      return status;
    }

    public void setStatus(String status) {
      this.status = status;
    }

    public Integer getRemainingSeconds() {
      return remainingSeconds;
    }

    public void setRemainingSeconds(Integer remainingSeconds) {
      this.remainingSeconds = remainingSeconds;
    }

    public Long getQuestionId() {
      return questionId;
    }

    public void setQuestionId(Long questionId) {
      this.questionId = questionId;
    }

    public String getQuestionTitle() {
      return questionTitle;
    }

    public void setQuestionTitle(String questionTitle) {
      this.questionTitle = questionTitle;
    }

    public Integer getOnlineCount() {
      return onlineCount;
    }

    public void setOnlineCount(Integer onlineCount) {
      this.onlineCount = onlineCount;
    }

    public Long getTimestamp() {
      return timestamp;
    }

    public void setTimestamp(Long timestamp) {
      this.timestamp = timestamp;
    }
  }
}
