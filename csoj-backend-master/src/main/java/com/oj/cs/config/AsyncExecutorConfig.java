package com.oj.cs.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.extern.slf4j.Slf4j;

/** 异步任务线程池配置 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncExecutorConfig {

  /** 判题线程池配置 */
  @Value("${async.judge.core-pool-size:5}")
  private int judgeCorePoolSize;

  @Value("${async.judge.max-pool-size:10}")
  private int judgeMaxPoolSize;

  @Value("${async.judge.queue-capacity:100}")
  private int judgeQueueCapacity;

  @Value("${async.judge.thread-name-prefix:judge-}")
  private String judgeThreadNamePrefix;

  /** 判题专用线程池 */
  @Bean("judgeExecutor")
  public Executor judgeExecutor() {
    log.info(
        "初始化判题线程池，核心线程数: {}, 最大线程数: {}, 队列容量: {}",
        judgeCorePoolSize,
        judgeMaxPoolSize,
        judgeQueueCapacity);

    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

    // 核心线程数：始终保持活跃的线程数
    executor.setCorePoolSize(judgeCorePoolSize);

    // 最大线程数：线程池允许的最大线程数
    executor.setMaxPoolSize(judgeMaxPoolSize);

    // 队列容量：等待执行的任务队列大小
    executor.setQueueCapacity(judgeQueueCapacity);

    // 线程名称前缀
    executor.setThreadNamePrefix(judgeThreadNamePrefix);

    // 拒绝策略：由调用线程处理该任务
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

    // 线程空闲时间：超过核心线程数的线程在空闲这个时间后会被回收
    executor.setKeepAliveSeconds(60);

    // 允许核心线程超时
    executor.setAllowCoreThreadTimeOut(true);

    // 等待所有任务完成后再关闭线程池
    executor.setWaitForTasksToCompleteOnShutdown(true);

    // 等待时间：线程池关闭时最多等待任务完成的时间
    executor.setAwaitTerminationSeconds(60);

    executor.initialize();

    return executor;
  }

  /** 通用异步任务线程池 */
  @Bean("commonAsyncExecutor")
  public Executor commonAsyncExecutor() {
    log.info("初始化通用异步线程池");

    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(3);
    executor.setMaxPoolSize(5);
    executor.setQueueCapacity(50);
    executor.setThreadNamePrefix("async-");
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    executor.setKeepAliveSeconds(60);
    executor.setAllowCoreThreadTimeOut(true);
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(30);

    executor.initialize();

    return executor;
  }
}
