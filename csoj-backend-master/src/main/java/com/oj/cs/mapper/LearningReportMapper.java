package com.oj.cs.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oj.cs.model.entity.LearningReport;

/** 学习报告 Mapper */
@Mapper
public interface LearningReportMapper extends BaseMapper<LearningReport> {

  /** 查询用户最新的报告 */
  @Select(
      "SELECT * FROM learning_report WHERE user_id = #{userId} "
          + "ORDER BY create_time DESC LIMIT #{limit}")
  List<LearningReport> getRecentReports(@Param("userId") Long userId, @Param("limit") int limit);

  /** 查询用户指定类型的报告 */
  @Select(
      "SELECT * FROM learning_report WHERE user_id = #{userId} "
          + "AND report_type = #{reportType} ORDER BY create_time DESC LIMIT 1")
  LearningReport getLatestReportByType(
      @Param("userId") Long userId, @Param("reportType") String reportType);

  /** 查询指定时间范围内的报告 */
  @Select(
      "SELECT * FROM learning_report WHERE user_id = #{userId} "
          + "AND start_date >= #{startDate} AND end_date <= #{endDate} "
          + "ORDER BY create_time DESC")
  List<LearningReport> getReportsByDateRange(
      @Param("userId") Long userId,
      @Param("startDate") java.util.Date startDate,
      @Param("endDate") java.util.Date endDate);
}
