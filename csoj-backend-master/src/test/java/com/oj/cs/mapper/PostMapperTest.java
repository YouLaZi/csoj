package com.oj.cs.mapper;

import java.util.Date;
import java.util.List;

import jakarta.annotation.Resource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.oj.cs.model.entity.Post;

/** 帖子数据库操作测试 */
@SpringBootTest
class PostMapperTest {

  @Resource private PostMapper postMapper;

  @Test
  void listPostWithDelete() {
    List<Post> postList = postMapper.listPostWithDelete(new Date());
    Assertions.assertNotNull(postList);
  }
}
