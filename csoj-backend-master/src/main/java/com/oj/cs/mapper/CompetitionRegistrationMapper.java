package com.oj.cs.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oj.cs.model.entity.CompetitionRegistration;

/** 姣旇禌鎶ュ悕 Mapper */
public interface CompetitionRegistrationMapper extends BaseMapper<CompetitionRegistration> {

  /** 鏍规嵁姣旇禌ID鏌ヨ鎶ュ悕鍥㈤槦ID鍒楄〃 */
  @Select(
      "SELECT team_id FROM competition_registration WHERE competition_id = #{competitionId} AND is_delete = 0")
  List<Long> selectTeamIdsByCompetitionId(@Param("competitionId") Long competitionId);

  /** 妫€鏌ュ洟闃熸槸鍚﹀凡鎶ュ悕 */
  @Select(
      "SELECT COUNT(*) FROM competition_registration WHERE competition_id = #{competitionId} AND team_id = #{teamId} AND is_delete = 0")
  int countByCompetitionIdAndTeamId(
      @Param("competitionId") Long competitionId, @Param("teamId") Long teamId);

  /** 鏍规嵁姣旇禌鍜屽洟闃熸煡璇㈡姤鍚嶈褰? */
  @Select(
      "SELECT * FROM competition_registration WHERE competition_id = #{competitionId} AND team_id = #{teamId} AND is_delete = 0 LIMIT 1")
  CompetitionRegistration selectByCompetitionIdAndTeamId(
      @Param("competitionId") Long competitionId, @Param("teamId") Long teamId);
}
