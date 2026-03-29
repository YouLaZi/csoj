package com.oj.cs.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oj.cs.model.entity.CompetitionRegistration;

/** 比赛报名 Mapper */
@Mapper
public interface CompetitionRegistrationMapper extends BaseMapper<CompetitionRegistration> {

  /** 根据比赛ID查询报名团队ID列表 */
  @Select(
      "SELECT team_id FROM competition_registration WHERE competition_id = #{competitionId} AND is_delete = 0")
  List<Long> selectTeamIdsByCompetitionId(@Param("competitionId") Long competitionId);

  /** 检查团队是否已报名 */
  @Select(
      "SELECT COUNT(*) FROM competition_registration WHERE competition_id = #{competitionId} AND team_id = #{teamId} AND is_delete = 0")
  int countByCompetitionIdAndTeamId(
      @Param("competitionId") Long competitionId, @Param("teamId") Long teamId);

  /** 根据比赛和团队查询报名记录 */
  @Select(
      "SELECT * FROM competition_registration WHERE competition_id = #{competitionId} AND team_id = #{teamId} AND is_delete = 0 LIMIT 1")
  CompetitionRegistration selectByCompetitionIdAndTeamId(
      @Param("competitionId") Long competitionId, @Param("teamId") Long teamId);
}
