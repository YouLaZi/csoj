package com.oj.cs.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oj.cs.model.entity.CompetitionMatch;

/** 比赛对阵 Mapper */public interface CompetitionMatchMapper extends BaseMapper<CompetitionMatch> {

  /** 根据比赛ID查询对阵列表 */
  @Select(
      "SELECT * FROM competition_match WHERE competition_id = #{competitionId} AND is_delete = 0 ORDER BY round, match_number")
  List<CompetitionMatch> selectByCompetitionId(@Param("competitionId") Long competitionId);

  /** 统计比赛的对阵数�?*/
  @Select(
      "SELECT COUNT(*) FROM competition_match WHERE competition_id = #{competitionId} AND is_delete = 0")
  int countByCompetitionId(@Param("competitionId") Long competitionId);

  /** 查询比赛的最大轮�?*/
  @Select(
      "SELECT COALESCE(MAX(round), 0) FROM competition_match WHERE competition_id = #{competitionId} AND is_delete = 0")
  int getMaxRound(@Param("competitionId") Long competitionId);
}
