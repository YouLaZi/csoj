package com.oj.cs.service.impl;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.annotation.Resource;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.oj.cs.mapper.QuestionSubmitMapper;
import com.oj.cs.mapper.UserMapper;
import com.oj.cs.model.entity.QuestionSubmit;
import com.oj.cs.model.entity.User;
import com.oj.cs.model.enums.QuestionSubmitStatusEnum;
import com.oj.cs.service.WebSocketRankingService;
import com.oj.cs.websocket.RankingWebSocketHandler;

import lombok.extern.slf4j.Slf4j;

/** WebSocket 排名推送服务实现 */
@Service
@Slf4j
public class WebSocketRankingServiceImpl implements WebSocketRankingService {

  @Resource private RankingWebSocketHandler rankingWebSocketHandler;

  @Resource private QuestionSubmitMapper questionSubmitMapper;

  @Resource private UserMapper userMapper;

  /** 排名缓存，避免频繁计算 key: contestId, value: 上次计算的排名数据 */
  private final Map<Long, List<RankingWebSocketHandler.RankingItem>> rankingCache =
      new ConcurrentHashMap<>();

  /** 最后更新时间戳 */
  private final Map<Long, Long> lastUpdateTime = new ConcurrentHashMap<>();

  /** 推送间隔（毫秒），避免频繁推送 */
  private static final long PUSH_INTERVAL = 2000; // 2秒

  @Override
  @Async
  public void pushRankingUpdate(
      Long contestId, List<RankingWebSocketHandler.RankingItem> rankings) {
    try {
      RankingWebSocketHandler.RankingMessage message = new RankingWebSocketHandler.RankingMessage();
      message.setType("update");
      message.setContestId(contestId);
      message.setRankings(rankings);
      message.setTimestamp(System.currentTimeMillis());

      rankingWebSocketHandler.broadcastRankingUpdate(contestId, message);

      // 更新缓存
      rankingCache.put(contestId, rankings);
      lastUpdateTime.put(contestId, System.currentTimeMillis());

      log.debug("推送排名更新: contestId={}, 排名数={}", contestId, rankings.size());
    } catch (Exception e) {
      log.error("推送排名更新失败: contestId={}", contestId, e);
    }
  }

  @Override
  public Integer getOnlineViewerCount(Long contestId) {
    return rankingWebSocketHandler.getOnlineUserCount(contestId);
  }

  @Override
  @Async
  public void triggerRankingRefresh(Long contestId, Long questionId) {
    // 检查推送间隔，避免过于频繁
    Long lastTime = lastUpdateTime.get(contestId);
    long now = System.currentTimeMillis();
    if (lastTime != null && (now - lastTime) < PUSH_INTERVAL) {
      log.debug("推送间隔未到，跳过本次更新: contestId={}", contestId);
      return;
    }

    try {
      // 计算最新排名
      List<RankingWebSocketHandler.RankingItem> rankings = calculateRankings(contestId);

      if (!rankings.isEmpty()) {
        pushRankingUpdate(contestId, rankings);
      }
    } catch (Exception e) {
      log.error("触发排名刷新失败: contestId={}", contestId, e);
    }
  }

  /** 计算比赛排名 按照通过题数降序、总耗时升序排序 */
  private List<RankingWebSocketHandler.RankingItem> calculateRankings(Long contestId) {
    // 查询比赛的所有成功提交
    QueryWrapper<QuestionSubmit> query = new QueryWrapper<>();
    query
        .eq("contestId", contestId)
        .eq("status", QuestionSubmitStatusEnum.SUCCEED.getValue())
        .orderByDesc("createTime");

    List<QuestionSubmit> submits = questionSubmitMapper.selectList(query);

    if (submits.isEmpty()) {
      return new ArrayList<>();
    }

    // 按用户统计
    Map<Long, UserRankingData> userDataMap = new HashMap<>();

    for (QuestionSubmit submit : submits) {
      Long userId = submit.getUserId();
      UserRankingData data = userDataMap.computeIfAbsent(userId, k -> new UserRankingData(userId));

      // 记录该用户每道题的最早通过时间
      Long questionId = submit.getQuestionId();
      if (!data.questionPassTime.containsKey(questionId)) {
        data.questionPassTime.put(questionId, submit.getCreateTime().getTime());
      }
    }

    // 转换为排名项并排序
    List<RankingWebSocketHandler.RankingItem> rankings = new ArrayList<>();

    for (UserRankingData data : userDataMap.values()) {
      User user = userMapper.selectById(data.userId);
      if (user == null) {
        continue;
      }

      RankingWebSocketHandler.RankingItem item = new RankingWebSocketHandler.RankingItem();
      item.setUserId(data.userId);
      item.setUserName(user.getUserName());
      item.setSolvedCount(data.questionPassTime.size());

      // 计算总耗时（最后一题通过时间 - 第一题通过时间）
      if (data.questionPassTime.size() > 0) {
        List<Long> times = new ArrayList<>(data.questionPassTime.values());
        Collections.sort(times);
        long totalTime = times.get(times.size() - 1) - times.get(0);
        item.setTotalTime((int) (totalTime / 1000)); // 转换为秒
        item.setLastSubmitTime(times.get(times.size() - 1).toString());
      }

      rankings.add(item);
    }

    // 排序：通过题数降序，总耗时升序
    rankings.sort(
        (a, b) -> {
          if (!a.getSolvedCount().equals(b.getSolvedCount())) {
            return b.getSolvedCount() - a.getSolvedCount();
          }
          return a.getTotalTime().compareTo(b.getTotalTime());
        });

    // 添加排名
    AtomicInteger rank = new AtomicInteger(1);
    rankings.forEach(item -> item.setRank(rank.getAndIncrement()));

    return rankings;
  }

  /** 用户排名数据（内部类） */
  private static class UserRankingData {
    Long userId;
    Map<Long, Long> questionPassTime = new HashMap<>(); // 题目ID -> 通过时间

    UserRankingData(Long userId) {
      this.userId = userId;
    }
  }
}
