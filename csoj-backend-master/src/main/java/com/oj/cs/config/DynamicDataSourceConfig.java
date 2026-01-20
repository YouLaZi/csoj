package com.oj.cs.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.zaxxer.hikari.HikariDataSource;

/** 动态数据源配置 - 读写分离 仅在配置了主从数据库地址时启用 */
@Configuration
@ConditionalOnProperty(prefix = "spring.datasource", name = "slave-url")
@MapperScan(basePackages = "com.oj.cs.mapper")
public class DynamicDataSourceConfig {

  @Value("${spring.datasource.master.url:jdbc:mysql://localhost:3306/oj_db}")
  private String masterUrl;

  @Value("${spring.datasource.master.username:root}")
  private String masterUsername;

  @Value("${spring.datasource.master.password:}")
  private String masterPassword;

  @Value("${spring.datasource.slave.url:}")
  private String slaveUrl;

  @Value("${spring.datasource.slave.username:${spring.datasource.master.username}}")
  private String slaveUsername;

  @Value("${spring.datasource.slave.password:${spring.datasource.master.password}}")
  private String slavePassword;

  @Value("${spring.datasource.driver-class-name:com.mysql.cj.jdbc.Driver}")
  private String driverClassName;

  /** 主数据源（写） */
  @Bean
  @Primary
  public DataSource masterDataSource() {
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setJdbcUrl(masterUrl);
    dataSource.setUsername(masterUsername);
    dataSource.setPassword(masterPassword);
    dataSource.setDriverClassName(driverClassName);
    dataSource.setPoolName("MasterHikariPool");

    // 连接池配置
    dataSource.setMaximumPoolSize(20);
    dataSource.setMinimumIdle(5);
    dataSource.setConnectionTimeout(30000);
    dataSource.setIdleTimeout(600000);
    dataSource.setMaxLifetime(1800000);

    return dataSource;
  }

  /** 从数据源（读） */
  @Bean
  public DataSource slaveDataSource() {
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setJdbcUrl(slaveUrl);
    dataSource.setUsername(slaveUsername);
    dataSource.setPassword(slavePassword);
    dataSource.setDriverClassName(driverClassName);
    dataSource.setPoolName("SlaveHikariPool");

    // 连接池配置（读库可以有更多连接）
    dataSource.setMaximumPoolSize(30);
    dataSource.setMinimumIdle(5);
    dataSource.setConnectionTimeout(30000);
    dataSource.setIdleTimeout(600000);
    dataSource.setMaxLifetime(1800000);

    return dataSource;
  }

  /** 动态数据源 */
  @Bean
  @Primary
  public DynamicDataSource dynamicDataSource() {
    DynamicDataSource dynamicDataSource = new DynamicDataSource();

    // 设置默认数据源（主库）
    dynamicDataSource.setDefaultTargetDataSource(masterDataSource());

    // 配置多数据源
    Map<Object, Object> targetDataSources = new HashMap<>();
    targetDataSources.put("master", masterDataSource());
    targetDataSources.put("slave", slaveDataSource());
    dynamicDataSource.setTargetDataSources(targetDataSources);

    return dynamicDataSource;
  }

  /** SqlSessionFactory 配置 */
  @Bean
  public SqlSessionFactory sqlSessionFactory() throws Exception {
    MybatisSqlSessionFactoryBean sqlSessionFactory = new MybatisSqlSessionFactoryBean();
    sqlSessionFactory.setDataSource(dynamicDataSource());

    // MyBatis-Plus 配置
    com.baomidou.mybatisplus.core.MybatisConfiguration configuration =
        new com.baomidou.mybatisplus.core.MybatisConfiguration();
    configuration.setMapUnderscoreToCamelCase(false);
    configuration.setLogImpl(org.apache.ibatis.logging.stdout.StdOutImpl.class);

    // 全局配置
    com.baomidou.mybatisplus.core.config.GlobalConfig globalConfig =
        new com.baomidou.mybatisplus.core.config.GlobalConfig();
    com.baomidou.mybatisplus.core.config.GlobalConfig.DbConfig dbConfig =
        new com.baomidou.mybatisplus.core.config.GlobalConfig.DbConfig();
    dbConfig.setLogicDeleteField("isDelete");
    dbConfig.setLogicDeleteValue("1");
    dbConfig.setLogicNotDeleteValue("0");
    globalConfig.setDbConfig(dbConfig);
    // MyBatis-Plus 3.5.x 不再需要 setGlobalConfig，全局配置通过 Spring Bean 自动注入
    // configuration.setGlobalConfig(globalConfig);
    sqlSessionFactory.setGlobalConfig(globalConfig);

    sqlSessionFactory.setConfiguration(configuration);

    // Mapper 扫描
    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    sqlSessionFactory.setMapperLocations(resolver.getResources("classpath:mapper/**/*.xml"));

    return sqlSessionFactory.getObject();
  }
}
