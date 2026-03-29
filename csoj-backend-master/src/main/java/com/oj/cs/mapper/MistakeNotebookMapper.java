package com.oj.cs.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oj.cs.model.entity.MistakeNotebook;

/** 错题�?Mapper */public interface MistakeNotebookMapper extends BaseMapper<MistakeNotebook> {

  /** 查询需要复习提醒的错题 */
  @Select(
      "SELECT * FROM mistake_notebook WHERE user_id = #{userId} "
          + "AND is_reviewed = 0 "
          + "AND remind_time <= #{currentTime} "
          + "AND is_delete = 0 "
          + "ORDER BY remind_time ASC")
  List<MistakeNotebook> getRemindList(
      @Param("userId") Long userId, @Param("currentTime") Date currentTime);

  /** 查询用户的错题统�?*/
  @Select(
      "SELECT mistake_type, COUNT(*) as count FROM mistake_notebook "
          + "WHERE user_id = #{userId} AND is_delete = 0 "
          + "GROUP BY mistake_type")
  List<Object> getMistakeStatistics(@Param("userId") Long userId);

  /** 查询用户对某题目的错题记�?*/
  @Select(
      "SELECT * FROM mistake_notebook WHERE user_id = #{userId} "
          + "AND question_id = #{questionId} AND is_delete = 0")
  List<MistakeNotebook> getByUserAndQuestion(
      @Param("userId") Long userId, @Param("questionId") Long questionId);
}
