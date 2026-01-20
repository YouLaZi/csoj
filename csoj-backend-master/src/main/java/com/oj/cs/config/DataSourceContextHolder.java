package com.oj.cs.config;

/** 数据源上下文持有者 使用 ThreadLocal 存储当前线程的数据源类型 */
public class DataSourceContextHolder {

  private static final ThreadLocal<DataSourceType> contextHolder = new ThreadLocal<>();

  /** 设置数据源类型 */
  public static void setDataSourceType(DataSourceType dataSourceType) {
    contextHolder.set(dataSourceType);
  }

  /** 获取数据源类型 */
  public static DataSourceType getDataSourceType() {
    return contextHolder.get();
  }

  /** 清除数据源类型 */
  public static void clearDataSourceType() {
    contextHolder.remove();
  }

  /** 数据源类型枚举 */
  public enum DataSourceType {
    /** 主库（写） */
    MASTER,

    /** 从库（读） */
    SLAVE
  }
}
