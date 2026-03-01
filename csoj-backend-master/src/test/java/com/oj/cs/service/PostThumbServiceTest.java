package com.oj.cs.service;

import jakarta.annotation.Resource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.oj.cs.model.entity.User;

/** 帖子点赞服务测试 */
@SpringBootTest
class PostThumbServiceTest {

  @Resource private PostThumbService postThumbService;

  private static final User loginUser = new User();

  @BeforeAll
  static void setUp() {
    loginUser.setId(1L);
  }

  @Test
  void doPostThumb() {
    // 测试点赞/取消点赞功能，-1表示取消点赞，1表示点赞成功，0表示失败
    int i = postThumbService.doPostThumb(1L, loginUser);
    Assertions.assertTrue(i >= -1 && i <= 1);
  }
}
