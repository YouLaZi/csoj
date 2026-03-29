package com.oj.cs.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oj.cs.model.entity.License;

/** 璁稿彲璇?Mapper */
public interface LicenseMapper extends BaseMapper<License> {

  /** 鏍规嵁璁稿彲璇佸瘑閽ユ煡璇? */
  @Select("SELECT * FROM license WHERE license_key = #{licenseKey}")
  License getByLicenseKey(@Param("licenseKey") String licenseKey);

  /** 鏌ヨ鎵€鏈夐渶瑕佹鏌ョ殑璁稿彲璇侊紙宸叉縺娲讳笖鏈繃鏈燂級 */
  @Select("SELECT * FROM license WHERE is_active = 1 AND end_date > NOW()")
  java.util.List<License> getActiveLicenses();
}
