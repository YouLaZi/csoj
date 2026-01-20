package com.oj.cs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/** RestTemplate配置类 用于创建RestTemplate Bean，以便在服务中进行HTTP请求 */
@Configuration
public class RestTemplateConfig {

  /**
   * 创建RestTemplate Bean
   *
   * @return RestTemplate实例
   */
  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
}
