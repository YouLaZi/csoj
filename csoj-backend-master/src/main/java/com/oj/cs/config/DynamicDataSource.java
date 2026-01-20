package com.oj.cs.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/** 动态数据源 根据上下文中的 DataSourceType 决定使用主库还是从库 */
public class DynamicDataSource extends AbstractRoutingDataSource {

  @Override
  protected Object determineCurrentLookupKey() {
    return DataSourceContextHolder.getDataSourceType();
  }
}
