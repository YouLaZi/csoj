package com.oj.cs.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oj.cs.model.entity.CompetitionMatch;

/** 姣旇禌瀵归樀 Mapper */
public interface CompetitionMatchMapper extends BaseMapper<CompetitionMatch> {

  /** 鏍规嵁姣旇禌ID鏌ヨ瀵归樀鍒楄〃 */
  @Select(
      "SELECT * FROM competition_match WHERE competition_id = #{competitionId} AND is_delete = 0 ORDER BY round, match_number")
  List<CompetitionMatch> selectByCompetitionId(@Param("competitionId") Long competitionId);

  /** 缁熻姣旇禌鐨勫闃垫暟閲? */
  @Select(
      "SELECT COUNT(*) FROM competition_match WHERE competition_id = #{competitionId} AND is_delete = 0")
  int countByCompetitionId(@Param("competitionId") Long competitionId);

  /** 鏌ヨ姣旇禌鐨勬渶澶ц疆娆? */
  @Select(
      "SELECT COALESCE(MAX(round), 0) FROM competition_match WHERE competition_id = #{competitionId} AND is_delete = 0")
  int getMaxRound(@Param("competitionId") Long competitionId);
}
