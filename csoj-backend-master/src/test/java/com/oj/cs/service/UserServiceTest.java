package com.oj.cs.service;

import jakarta.annotation.Resource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/** 用户服务测试 */
@SpringBootTest
public class UserServiceTest {

  @Resource private UserService userService;

  @Test
  void userRegister() {
    String userAccount = "admin";
    String userName = "admin";
    String userPassword = "123456";
    String checkPassword = "123456";
    try {
      long result = userService.userRegister(userAccount, userName, userPassword, checkPassword);
      Assertions.assertEquals(-1, result);
      userAccount = "admin";
      result = userService.userRegister(userAccount, userName, userPassword, checkPassword);
      Assertions.assertEquals(-1, result);
    } catch (Exception e) {

    }
  }
}
