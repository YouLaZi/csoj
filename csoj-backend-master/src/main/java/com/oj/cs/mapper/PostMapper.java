package com.oj.cs.mapper;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oj.cs.model.entity.Post;

/** 帖子数据库操作 */
public interface PostMapper extends BaseMapper<Post> {

  /** 查询帖子列表（包括已被删除的数据） */
  List<Post> listPostWithDelete(Date minUpdateTime);
}
