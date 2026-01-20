package com.oj.cs.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oj.cs.model.dto.tag.TagQueryRequest;
import com.oj.cs.model.entity.Tag;

/** 标签服务 */
public interface TagService {

  /**
   * 获取标签列表
   *
   * @return 标签列表
   */
  List<Tag> getTagList();

  /**
   * 分页获取标签列表
   *
   * @param tagQueryRequest 查询请求
   * @return 标签分页列表
   */
  Page<Tag> getTagListByPage(TagQueryRequest tagQueryRequest);

  /**
   * 创建标签
   *
   * @param tag 标签信息
   * @param loginUserId 当前登录用户ID
   * @return 创建的标签ID
   */
  long createTag(Tag tag, Long loginUserId);

  /**
   * 更新标签
   *
   * @param tag 标签信息
   * @param loginUserId 当前登录用户ID
   * @return 是否成功
   */
  boolean updateTag(Tag tag, Long loginUserId);

  /**
   * 删除标签
   *
   * @param id 标签ID
   * @param loginUserId 当前登录用户ID
   * @return 是否成功
   */
  boolean deleteTag(long id, Long loginUserId);
}
