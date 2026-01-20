package com.oj.cs.job.once;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.CommandLineRunner;

import com.oj.cs.esdao.PostEsDao;
import com.oj.cs.model.dto.post.PostEsDTO;
import com.oj.cs.model.entity.Post;
import com.oj.cs.service.PostService;

import lombok.extern.slf4j.Slf4j;

/** 全量同步帖子到 es */
// todo 取消注释开启任务
// @Component
@Slf4j
public class FullSyncPostToEs implements CommandLineRunner {

  @Resource private PostService postService;

  @Resource private PostEsDao postEsDao;

  @Override
  public void run(String... args) {
    List<Post> postList = postService.list();
    if (CollectionUtils.isEmpty(postList)) {
      return;
    }
    List<PostEsDTO> postEsDTOList =
        postList.stream().map(PostEsDTO::objToDto).collect(Collectors.toList());
    final int pageSize = 500;
    int total = postEsDTOList.size();
    log.info("FullSyncPostToEs start, total {}", total);
    for (int i = 0; i < total; i += pageSize) {
      int end = Math.min(i + pageSize, total);
      log.info("sync from {} to {}", i, end);
      postEsDao.saveAll(postEsDTOList.subList(i, end));
    }
    log.info("FullSyncPostToEs end, total {}", total);
  }
}
