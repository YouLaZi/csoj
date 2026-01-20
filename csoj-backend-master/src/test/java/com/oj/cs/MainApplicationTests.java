package com.oj.cs;

import jakarta.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.oj.cs.config.WxOpenConfig;

/** 主类测试 */
@SpringBootTest
class MainApplicationTests {

  @Resource private WxOpenConfig wxOpenConfig;

  @Test
  void contextLoads() {
    System.out.println(wxOpenConfig);
  }
}
