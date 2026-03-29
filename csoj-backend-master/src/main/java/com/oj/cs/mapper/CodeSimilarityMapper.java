package com.oj.cs.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oj.cs.model.entity.CodeSimilarity;

/** 代码相似度记录 Mapper */
public interface CodeSimilarityMapper extends BaseMapper<CodeSimilarity> {

  /** 查询某个题目的高度相似记录 */
  @Select(
      "SELECT * FROM code_similarity WHERE question_id = #{questionId} "
          + "AND similarity_score >= #{threshold} ORDER BY similarity_score DESC")
  List<CodeSimilarity> getHighSimilarityByQuestion(
      @Param("questionId") Long questionId, @Param("threshold") Integer threshold);

  /** 查询用户涉及的所有相似记录 */
  @Select(
      "SELECT * FROM code_similarity WHERE user_id1 = #{userId} OR user_id2 = #{userId} "
          + "ORDER BY create_time DESC")
  List<CodeSimilarity> getByUserId(@Param("userId") Long userId);

  /** 查询两次提交之间的相似记录 */
  @Select(
      "SELECT * FROM code_similarity WHERE "
          + "((submit_id1 = #{submitId1} AND submit_id2 = #{submitId2}) OR "
          + "(submit_id1 = #{submitId2} AND submit_id2 = #{submitId1}))")
  List<CodeSimilarity> getBySubmitIds(
      @Param("submitId1") Long submitId1, @Param("submitId2") Long submitId2);
}
