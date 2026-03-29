package com.oj.cs.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oj.cs.model.entity.LearningReport;

/** 瀛︿範鎶ュ憡 Mapper */
public interface LearningReportMapper extends BaseMapper<LearningReport> {

  /** 鏌ヨ鐢ㄦ埛鏈€鏂扮殑鎶ュ憡 */
  @Select(
      "SELECT * FROM learning_report WHERE user_id = #{userId} "
          + "ORDER BY create_time DESC LIMIT #{limit}")
  List<LearningReport> getRecentReports(@Param("userId") Long userId, @Param("limit") int limit);

  /** 鏌ヨ鐢ㄦ埛鎸囧畾绫诲瀷鐨勬姤鍛? */
  @Select(
      "SELECT * FROM learning_report WHERE user_id = #{userId} "
          + "AND report_type = #{reportType} ORDER BY create_time DESC LIMIT 1")
  LearningReport getLatestReportByType(
      @Param("userId") Long userId, @Param("reportType") String reportType);

  /** 鏌ヨ鎸囧畾鏃堕棿鑼冨洿鍐呯殑鎶ュ憡 */
  @Select(
      "SELECT * FROM learning_report WHERE user_id = #{userId} "
          + "AND start_date >= #{startDate} AND end_date <= #{endDate} "
          + "ORDER BY create_time DESC")
  List<LearningReport> getReportsByDateRange(
      @Param("userId") Long userId,
      @Param("startDate") java.util.Date startDate,
      @Param("endDate") java.util.Date endDate);
}
