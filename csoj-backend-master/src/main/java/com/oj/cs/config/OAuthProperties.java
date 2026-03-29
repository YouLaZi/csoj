package com.oj.cs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/** OAuth 配置属性 */
@Data
@Configuration
@ConfigurationProperties(prefix = "oauth")
public class OAuthProperties {

  /** 前端回调URL前缀 */
  private String frontendCallbackUrl = "http://localhost:8080/oauth/callback";

  /** State 令牌过期时间（秒） */
  private int stateExpiration = 300;

  /** GitHub 配置 */
  private GitHubConfig github = new GitHubConfig();

  /** Gitee 配置 */
  private GiteeConfig gitee = new GiteeConfig();

  /** QQ 配置 */
  private QQConfig qq = new QQConfig();

  /** 微信配置 */
  private WeChatConfig wechat = new WeChatConfig();

  @Data
  public static class GitHubConfig {
    private boolean enabled = false;
    private String clientId;
    private String clientSecret;
    private String scope = "user:email";
  }

  @Data
  public static class GiteeConfig {
    private boolean enabled = false;
    private String clientId;
    private String clientSecret;
    private String scope = "user_info";
  }

  @Data
  public static class QQConfig {
    private boolean enabled = false;
    private String clientId;
    private String clientSecret;
    private String scope = "get_user_info";
  }

  @Data
  public static class WeChatConfig {
    private boolean enabled = false;
    private String clientId;
    private String clientSecret;
  }

  /** 获取平台的回调URL */
  public String getCallbackUrl(String platform) {
    return frontendCallbackUrl + "/" + platform.toLowerCase();
  }
}
