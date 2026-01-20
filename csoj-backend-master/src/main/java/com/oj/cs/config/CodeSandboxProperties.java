package com.oj.cs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/** 代码沙箱配置属性 */
@Component
@Data
@ConfigurationProperties(prefix = "codesandbox.remote")
public class CodeSandboxProperties {

  /** 远程沙箱服务地址 */
  private String url = "http://localhost:8090/executeCodeSync";

  /** 鉴权请求头名称 */
  private String authHeader = "auth";

  /** 鉴权密钥 */
  private String authSecret;

  /** 请求超时时间（毫秒） */
  private Integer timeout = 60000;
}
