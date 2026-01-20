package com.oj.cs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Knife4j 接口文档配置 (OpenAPI 3) API 文档访问地址：http://localhost:8121/api/doc.html
 * 官方文档：https://doc.xiaominfo.com/knife4j/documentation/get_start.html
 */
@Configuration
@Profile({"dev", "test"})
public class Knife4jConfig {

  private static final String SECURITY_SCHEME_NAME = "Authorization";

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        // API 信息
        .info(apiInfo())
        // 安全认证
        .components(
            new Components()
                .addSecuritySchemes(
                    SECURITY_SCHEME_NAME,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("Authorization")))
        // 全局安全认证
        .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
  }

  /** API 信息 */
  private Info apiInfo() {
    return new Info()
        .title("CSOJ 在线判题系统 API 文档")
        .description("Campus Online Judge - 面向高校教学场景的编程题库与判题系统")
        .version("v3.0")
        .termsOfService("http://localhost:8121/api/terms")
        .contact(
            new Contact()
                .name("CSOJ 开发团队")
                .url("http://localhost:8121/api")
                .email("support@csoj.edu"))
        .license(new License().name("MIT License").url("https://opensource.org/licenses/MIT"));
  }
}
