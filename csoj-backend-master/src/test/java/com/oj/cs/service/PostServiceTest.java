package com.oj.cs.service;

import jakarta.annotation.Resource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oj.cs.model.dto.post.PostQueryRequest;
import com.oj.cs.model.entity.Post;

/** 帖子服务测试 */
@SpringBootTest
class PostServiceTest {

  @Resource private PostService postService;

  @Test
  void searchFromEs() {
    PostQueryRequest postQueryRequest = new PostQueryRequest();
    postQueryRequest.setUserId(1L);
    Page<Post> postPage = postService.searchFromEs(postQueryRequest);
    Assertions.assertNotNull(postPage);
  }
}
