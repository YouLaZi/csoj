package com.oj.cs.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oj.cs.model.entity.License;

/** 许可�?Mapper */public interface LicenseMapper extends BaseMapper<License> {

  /** 根据许可证密钥查�?*/
  @Select("SELECT * FROM license WHERE license_key = #{licenseKey}")
  License getByLicenseKey(@Param("licenseKey") String licenseKey);

  /** 查询所有需要检查的许可证（已激活且未过期） */
  @Select("SELECT * FROM license WHERE is_active = 1 AND end_date > NOW()")
  java.util.List<License> getActiveLicenses();
}
