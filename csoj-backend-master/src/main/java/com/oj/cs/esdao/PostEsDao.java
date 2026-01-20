package com.oj.cs.esdao;

import java.util.List;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.oj.cs.model.dto.post.PostEsDTO;

/** 帖子 ES 操作 */
public interface PostEsDao extends ElasticsearchRepository<PostEsDTO, Long> {

  List<PostEsDTO> findByUserId(Long userId);
}
