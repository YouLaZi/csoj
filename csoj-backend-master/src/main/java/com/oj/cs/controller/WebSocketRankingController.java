package com.oj.cs.controller;

import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.Resource;

import org.springframework.web.bind.annotation.*;

import com.oj.cs.annotation.AuthCheck;
import com.oj.cs.common.BaseResponse;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.common.ResultUtils;
import com.oj.cs.constant.UserConstant;
import com.oj.cs.service.WebSocketRankingService;
import com.oj.cs.websocket.RankingWebSocketHandler;

import lombok.extern.slf4j.Slf4j;

/** WebSocket 排名推送管理接口 */
@RestController
@RequestMapping("/ws/ranking")
@Slf4j
public class WebSocketRankingController {

  @Resource private WebSocketRankingService webSocketRankingService;

  @Resource private RankingWebSocketHandler rankingWebSocketHandler;

  /**
   * 手动触发排名刷新 用于测试或管理员手动刷新排名
   *
   * @param contestId 比赛ID
   * @return 操作结果
   */
  @PostMapping("/refresh/{contestId}")
  @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
  public BaseResponse<Boolean> refreshRanking(@PathVariable Long contestId) {
    try {
      webSocketRankingService.triggerRankingRefresh(contestId, null);
      return ResultUtils.success(true);
    } catch (Exception e) {
      log.error("手动刷新排名失败: contestId={}", contestId, e);
      return ResultUtils.error(ErrorCode.OPERATION_ERROR, "刷新排名失败: " + e.getMessage());
    }
  }

  /**
   * 获取比赛在线观看人数
   *
   * @param contestId 比赛ID
   * @return 在线人数
   */
  @GetMapping("/online/{contestId}")
  public BaseResponse<Integer> getOnlineViewerCount(@PathVariable Long contestId) {
    Integer count = webSocketRankingService.getOnlineViewerCount(contestId);
    return ResultUtils.success(count);
  }

  /**
   * 获取 WebSocket 连接信息
   *
   * @return 连接信息
   */
  @GetMapping("/info")
  public BaseResponse<Map<String, Object>> getWebSocketInfo() {
    Map<String, Object> info = new HashMap<>();
    info.put("rankingEndpoint", "/ws/ranking?contestId={contestId}");
    info.put("contestEndpoint", "/ws/contest?contestId={contestId}");
    info.put("description", "WebSocket 实时推送接口");
    info.put(
        "rankingMessage",
        new HashMap<String, String>() {
          {
            put("type", "消息类型 (connected/update/complete)");
            put("contestId", "比赛ID");
            put("rankings", "排名数据数组");
            put("timestamp", "时间戳");
          }
        });
    info.put(
        "rankingItem",
        new HashMap<String, String>() {
          {
            put("rank", "排名");
            put("userId", "用户ID");
            put("userName", "用户名");
            put("solvedCount", "通过题数");
            put("totalTime", "总耗时(秒)");
            put("lastSubmitTime", "最后提交时间");
          }
        });
    return ResultUtils.success(info);
  }

  /**
   * 获取 WebSocket 连接示例
   *
   * @return 连接示例
   */
  @GetMapping("/example")
  public BaseResponse<Map<String, Object>> getConnectionExample() {
    Map<String, Object> example = new HashMap<>();

    // JavaScript 客户端示例
    example.put(
        "javascript",
        "// JavaScript 客户端连接示例\n"
            + "const ws = new WebSocket('ws://localhost:8121/api/ws/ranking?contestId=1');\n"
            + "\n"
            + "ws.onopen = function() {\n"
            + "    console.log('WebSocket 连接已建立');\n"
            + "};\n"
            + "\n"
            + "ws.onmessage = function(event) {\n"
            + "    const data = JSON.parse(event.data);\n"
            + "    console.log('收到排名更新:', data);\n"
            + "    \n"
            + "    if (data.type === 'update') {\n"
            + "        // 更新排名显示\n"
            + "        updateRankingDisplay(data.rankings);\n"
            + "    }\n"
            + "};\n"
            + "\n"
            + "ws.onerror = function(error) {\n"
            + "    console.error('WebSocket 错误:', error);\n"
            + "};\n"
            + "\n"
            + "ws.onclose = function() {\n"
            + "    console.log('WebSocket 连接已关闭');\n"
            + "};\n"
            + "\n"
            + "// 心跳保持\n"
            + "setInterval(() => {\n"
            + "    if (ws.readyState === WebSocket.OPEN) {\n"
            + "        ws.send('ping');\n"
            + "    }\n"
            + "}, 30000);");

    // 服务器端推送示例
    example.put(
        "serverPush",
        "// 服务器端推送示例 (Java)\n"
            + "// 当用户提交判题成功后，自动触发排名更新\n"
            + "webSocketRankingService.triggerRankingRefresh(contestId, questionId);\n"
            + "\n"
            + "// 或手动推送排名数据\n"
            + "List<RankingItem> rankings = calculateRankings(contestId);\n"
            + "webSocketRankingService.pushRankingUpdate(contestId, rankings);");

    // 消息格式示例
    Map<String, Object> messageExample = new HashMap<>();
    messageExample.put("type", "update");
    messageExample.put("contestId", 1L);
    messageExample.put("message", "排名已更新");
    messageExample.put("timestamp", System.currentTimeMillis());

    Map<String, Object> ranking1 = new HashMap<>();
    ranking1.put("rank", 1);
    ranking1.put("userId", 1001L);
    ranking1.put("userName", "张三");
    ranking1.put("solvedCount", 5);
    ranking1.put("totalTime", 1234);
    ranking1.put("lastSubmitTime", "1705411200000");

    Map<String, Object> ranking2 = new HashMap<>();
    ranking2.put("rank", 2);
    ranking2.put("userId", 1002L);
    ranking2.put("userName", "李四");
    ranking2.put("solvedCount", 4);
    ranking2.put("totalTime", 987);
    ranking2.put("lastSubmitTime", "1705410600000");

    messageExample.put("rankings", new Map[] {ranking1, ranking2});
    example.put("messageExample", messageExample);

    return ResultUtils.success(example);
  }

  /**
   * 获取 WebSocket 健康状态
   *
   * @return 健康状态
   */
  @GetMapping("/health")
  public BaseResponse<Map<String, Object>> getHealthStatus() {
    Map<String, Object> health = new HashMap<>();
    health.put("status", "UP");
    health.put("description", "WebSocket 服务正常运行");
    health.put(
        "endpoints",
        new HashMap<String, String>() {
          {
            put("/ws/ranking", "比赛排名推送");
            put("/ws/contest", "比赛状态推送");
          }
        });
    return ResultUtils.success(health);
  }
}
