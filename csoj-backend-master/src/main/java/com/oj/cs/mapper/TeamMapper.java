package com.oj.cs.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oj.cs.model.entity.Team;

/** 团队 Mapper */public interface TeamMapper extends BaseMapper<Team> {

  /** 统计积分高于指定值的团队数量 */
  @Select("SELECT COUNT(*) FROM team WHERE rating > #{rating} AND is_delete = 0")
  int countByRatingGreaterThan(@Param("rating") int rating);

  /** 统计指定用户的团队数�?*/
  @Select("SELECT COUNT(*) FROM team_member WHERE user_id = #{userId} AND is_delete = 0")
  int countByUserId(@Param("userId") Long userId);
}
