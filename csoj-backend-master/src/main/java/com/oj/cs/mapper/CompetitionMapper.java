package com.oj.cs.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oj.cs.model.entity.Competition;

/** 绔炶禌 Mapper */
public interface CompetitionMapper extends BaseMapper<Competition> {

  /** 缁熻姣旇禌鎶ュ悕闃熶紞鏁伴噺 */
  @Select(
      "SELECT COUNT(*) FROM competition_registration WHERE competition_id = #{competitionId} AND is_delete = 0")
  int countRegistrationsByCompetitionId(@Param("competitionId") Long competitionId);

  /** 鏇存柊姣旇禌鐘舵€? */
  @Update("UPDATE competition SET status = #{status} WHERE id = #{competitionId}")
  int updateStatus(@Param("competitionId") Long competitionId, @Param("status") Integer status);

  /** 缁熻杩涜涓殑姣旇禌鏁伴噺 */
  @Select("SELECT COUNT(*) FROM competition WHERE status = 2 AND is_delete = 0")
  int countOngoingCompetitions();
}
