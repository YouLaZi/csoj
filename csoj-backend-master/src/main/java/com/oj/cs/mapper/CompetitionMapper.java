package com.oj.cs.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oj.cs.model.entity.Competition;

/** 竞赛 Mapper */
@Mapper
public interface CompetitionMapper extends BaseMapper<Competition> {

  /** 统计比赛报名队伍数量 */
  @Select(
      "SELECT COUNT(*) FROM competition_registration WHERE competition_id = #{competitionId} AND is_delete = 0")
  int countRegistrationsByCompetitionId(@Param("competitionId") Long competitionId);

  /** 更新比赛状态 */
  @Update("UPDATE competition SET status = #{status} WHERE id = #{competitionId}")
  int updateStatus(@Param("competitionId") Long competitionId, @Param("status") Integer status);

  /** 统计进行中的比赛数量 */
  @Select("SELECT COUNT(*) FROM competition WHERE status = 2 AND is_delete = 0")
  int countOngoingCompetitions();
}
