package com.oj.cs.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oj.cs.model.entity.TeamMember;

/** 鍥㈤槦鎴愬憳 Mapper */
public interface TeamMemberMapper extends BaseMapper<TeamMember> {

  /** 鏍规嵁鐢ㄦ埛ID鏌ヨ鎵€灞炲洟闃烮D鍒楄〃 */
  @Select("SELECT team_id FROM team_member WHERE user_id = #{userId} AND is_delete = 0")
  List<Long> selectTeamIdsByUserId(@Param("userId") Long userId);

  /** 鏍规嵁鍥㈤槦ID鏌ヨ鎴愬憳ID鍒楄〃 */
  @Select("SELECT user_id FROM team_member WHERE team_id = #{teamId} AND is_delete = 0")
  List<Long> selectUserIdsByTeamId(@Param("teamId") Long teamId);

  /** 缁熻鍥㈤槦鎴愬憳鏁伴噺 */
  @Select("SELECT COUNT(*) FROM team_member WHERE team_id = #{teamId} AND is_delete = 0")
  int countByTeamId(@Param("teamId") Long teamId);

  /** 缁熻鐢ㄦ埛鍔犲叆鐨勫洟闃熸暟閲? */
  @Select("SELECT COUNT(*) FROM team_member WHERE user_id = #{userId} AND is_delete = 0")
  int countByUserId(@Param("userId") Long userId);
}
