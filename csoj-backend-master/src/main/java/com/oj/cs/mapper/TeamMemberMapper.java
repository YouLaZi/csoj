package com.oj.cs.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oj.cs.model.entity.TeamMember;

/** 团队成员 Mapper */
@Mapper
public interface TeamMemberMapper extends BaseMapper<TeamMember> {

  /** 根据用户ID查询所属团队ID列表 */
  @Select("SELECT team_id FROM team_member WHERE user_id = #{userId} AND is_delete = 0")
  List<Long> selectTeamIdsByUserId(@Param("userId") Long userId);

  /** 根据团队ID查询成员ID列表 */
  @Select("SELECT user_id FROM team_member WHERE team_id = #{teamId} AND is_delete = 0")
  List<Long> selectUserIdsByTeamId(@Param("teamId") Long teamId);

  /** 统计团队成员数量 */
  @Select("SELECT COUNT(*) FROM team_member WHERE team_id = #{teamId} AND is_delete = 0")
  int countByTeamId(@Param("teamId") Long teamId);

  /** 统计用户加入的团队数量 */
  @Select("SELECT COUNT(*) FROM team_member WHERE user_id = #{userId} AND is_delete = 0")
  int countByUserId(@Param("userId") Long userId);
}
