package com.oj.cs.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oj.cs.model.entity.MistakeNotebook;

/** 閿欓鏈?Mapper */
public interface MistakeNotebookMapper extends BaseMapper<MistakeNotebook> {

  /** 鏌ヨ闇€瑕佸涔犳彁閱掔殑閿欓 */
  @Select(
      "SELECT * FROM mistake_notebook WHERE user_id = #{userId} "
          + "AND is_reviewed = 0 "
          + "AND remind_time <= #{currentTime} "
          + "AND is_delete = 0 "
          + "ORDER BY remind_time ASC")
  List<MistakeNotebook> getRemindList(
      @Param("userId") Long userId, @Param("currentTime") Date currentTime);

  /** 鏌ヨ鐢ㄦ埛鐨勯敊棰樼粺璁? */
  @Select(
      "SELECT mistake_type, COUNT(*) as count FROM mistake_notebook "
          + "WHERE user_id = #{userId} AND is_delete = 0 "
          + "GROUP BY mistake_type")
  List<Object> getMistakeStatistics(@Param("userId") Long userId);

  /** 鏌ヨ鐢ㄦ埛瀵规煇棰樼洰鐨勯敊棰樿褰? */
  @Select(
      "SELECT * FROM mistake_notebook WHERE user_id = #{userId} "
          + "AND question_id = #{questionId} AND is_delete = 0")
  List<MistakeNotebook> getByUserAndQuestion(
      @Param("userId") Long userId, @Param("questionId") Long questionId);
}
