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

/** 比赛排名 WebSocket 处理器 支持实时推送排名变化 */
@Component
@Slf4j
public class RankingWebSocketHandler extends TextWebSocketHandler {

  /** 存储比赛ID对应的 WebSocket 会话列表 key: contestId, value: sessionId -> WebSocketSession */
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
      // 将会话加入对应比赛
      contestSessions
          .computeIfAbsent(contestId, k -> new ConcurrentHashMap<>())
          .put(session.getId(), session);
      sessionContestMap.put(session.getId(), contestId);

      log.info("WebSocket 连接建立: contestId={}, sessionId={}", contestId, session.getId());

      // 发送连接成功消息
      sendConnectMessage(session, contestId);
    } else {
      log.warn("WebSocket 连接缺少 contestId 参数: sessionId={}", session.getId());
      session.close();
    }
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    String payload = message.getPayload();
    log.debug("收到 WebSocket 消息: sessionId={}, message={}", session.getId(), payload);

    // 处理客户端消息（如心跳检测）
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
      log.info(
          "WebSocket 连接关闭: contestId={}, sessionId={}, status={}",
          contestId,
          session.getId(),
          status);
    }
  }

  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
    log.error("WebSocket 传输错误: sessionId={}", session.getId(), exception);
    if (session.isOpen()) {
      session.close();
    }
  }

  /**
   * 向指定比赛的所有订阅者推送排名更新
   *
   * @param contestId 比赛ID
   * @param rankingData 排名数据
   */
  public void broadcastRankingUpdate(Long contestId, RankingMessage rankingData) {
    Map<String, WebSocketSession> sessions = contestSessions.get(contestId);
    if (sessions == null || sessions.isEmpty()) {
      log.debug("比赛 {} 没有活跃的 WebSocket 订阅者", contestId);
      return;
    }

    String messageJson;
    try {
      messageJson = objectMapper.writeValueAsString(rankingData);
    } catch (Exception e) {
      log.error("序列化排名数据失败: contestId={}", contestId, e);
      return;
    }

    TextMessage message = new TextMessage(messageJson);
    int successCount = 0;
    int failCount = 0;

    for (Map.Entry<String, WebSocketSession> entry : sessions.entrySet()) {
      WebSocketSession session = entry.getValue();
      if (session.isOpen()) {
        try {
          session.sendMessage(message);
          successCount++;
        } catch (IOException e) {
          log.error("发送 WebSocket 消息失败: sessionId={}", session.getId(), e);
          failCount++;
        }
      }
    }

    log.debug("广播排名更新: contestId={}, 成功={}, 失败={}", contestId, successCount, failCount);
  }

  /**
   * 获取指定比赛的在线用户数
   *
   * @param contestId 比赛ID
   * @return 在线用户数
   */
  public int getOnlineUserCount(Long contestId) {
    Map<String, WebSocketSession> sessions = contestSessions.get(contestId);
    return sessions == null ? 0 : sessions.size();
  }

  /** 从查询字符串中提取比赛ID */
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
          log.warn("无效的 contestId 参数: {}", pair[1]);
          return null;
        }
      }
    }
    return null;
  }

  /** 发送连接成功消息 */
  private void sendConnectMessage(WebSocketSession session, Long contestId) throws IOException {
    RankingMessage message = new RankingMessage();
    message.setType("connected");
    message.setContestId(contestId);
    message.setMessage("排名推送连接已建立");
    message.setOnlineCount(getOnlineUserCount(contestId));

    String messageJson = objectMapper.writeValueAsString(message);
    session.sendMessage(new TextMessage(messageJson));
  }

  /** 排名消息 */
  public static class RankingMessage {
    /** 消息类型 (connected/update/complete) */
    private String type;

    /** 比赛ID */
    private Long contestId;

    /** 消息内容 */
    private String message;

    /** 在线用户数 */
    private Integer onlineCount;

    /** 排名数据 */
    private java.util.List<RankingItem> rankings;

    /** 更新时间戳 */
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

    public Integer getOnlineCount() {
      return onlineCount;
    }

    public void setOnlineCount(Integer onlineCount) {
      this.onlineCount = onlineCount;
    }

    public java.util.List<RankingItem> getRankings() {
      return rankings;
    }

    public void setRankings(java.util.List<RankingItem> rankings) {
      this.rankings = rankings;
    }

    public Long getTimestamp() {
      return timestamp;
    }

    public void setTimestamp(Long timestamp) {
      this.timestamp = timestamp;
    }
  }

  /** 排名项 */
  public static class RankingItem {
    /** 排名 */
    private Integer rank;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String userName;

    /** 通过题数 */
    private Integer solvedCount;

    /** 总耗时 */
    private Integer totalTime;

    /** 最近提交时间 */
    private String lastSubmitTime;

    public Integer getRank() {
      return rank;
    }

    public void setRank(Integer rank) {
      this.rank = rank;
    }

    public Long getUserId() {
      return userId;
    }

    public void setUserId(Long userId) {
      this.userId = userId;
    }

    public String getUserName() {
      return userName;
    }

    public void setUserName(String userName) {
      this.userName = userName;
    }

    public Integer getSolvedCount() {
      return solvedCount;
    }

    public void setSolvedCount(Integer solvedCount) {
      this.solvedCount = solvedCount;
    }

    public Integer getTotalTime() {
      return totalTime;
    }

    public void setTotalTime(Integer totalTime) {
      this.totalTime = totalTime;
    }

    public String getLastSubmitTime() {
      return lastSubmitTime;
    }

    public void setLastSubmitTime(String lastSubmitTime) {
      this.lastSubmitTime = lastSubmitTime;
    }
  }
}
