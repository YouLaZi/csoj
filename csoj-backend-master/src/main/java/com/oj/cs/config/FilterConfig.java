package com.oj.cs.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.oj.cs.filter.CsrfTokenFilter;

/**
 * 过滤器配置类 注册Servlet过滤器
 *
 * <p>注意：JwtInterceptor 和 RateLimitInterceptor 是 HandlerInterceptor， 它们在 WebMvcConfig.java 中通过
 * InterceptorRegistry 注册
 */
@Configuration
public class FilterConfig {

  /** 创建 CsrfTokenFilter Bean */
  @Bean
  public CsrfTokenFilter csrfTokenFilter() {
    return new CsrfTokenFilter();
  }

  /** 注册 CSRF Token 过滤器 */
  @Bean
  public FilterRegistrationBean<CsrfTokenFilter> csrfTokenFilterRegistration(
      CsrfTokenFilter csrfTokenFilter) {
    FilterRegistrationBean<CsrfTokenFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(csrfTokenFilter);
    registrationBean.addUrlPatterns("/*");
    registrationBean.setOrder(1); // 高优先级
    return registrationBean;
  }
}
