package com.oj.cs.config;

import jakarta.annotation.Resource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import com.oj.cs.websocket.ContestWebSocketHandler;
import com.oj.cs.websocket.RankingWebSocketHandler;

/** WebSocket 配置 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

  @Resource private RankingWebSocketHandler rankingWebSocketHandler;

  @Resource private ContestWebSocketHandler contestWebSocketHandler;

  /** 注册 WebSocket 处理器 */
  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    // 比赛排名推送端点
    registry.addHandler(rankingWebSocketHandler, "/ws/ranking").setAllowedOrigins("*");

    // 比赛状态推送端点
    registry.addHandler(contestWebSocketHandler, "/ws/contest").setAllowedOrigins("*");
  }

  /** 支持 ServerEndpoint 注解方式（仅在 Servlet 容器环境中加载） */
  @Bean
  @ConditionalOnWebApplication
  public ServerEndpointExporter serverEndpointExporter() {
    return new ServerEndpointExporter();
  }
}
