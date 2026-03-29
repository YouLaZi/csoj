package com.oj.cs.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oj.cs.model.entity.Team;

/** 鍥㈤槦 Mapper */
public interface TeamMapper extends BaseMapper<Team> {

  /** 缁熻绉垎楂樹簬鎸囧畾鍊肩殑鍥㈤槦鏁伴噺 */
  @Select("SELECT COUNT(*) FROM team WHERE rating > #{rating} AND is_delete = 0")
  int countByRatingGreaterThan(@Param("rating") int rating);

  /** 缁熻鎸囧畾鐢ㄦ埛鐨勫洟闃熸暟閲? */
  @Select("SELECT COUNT(*) FROM team_member WHERE user_id = #{userId} AND is_delete = 0")
  int countByUserId(@Param("userId") Long userId);
}
