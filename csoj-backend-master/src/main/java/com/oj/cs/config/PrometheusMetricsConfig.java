package com.oj.cs.config;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/** Prometheus 自定义指标配置 用于监控系统关键业务指标 */
@Component
public class PrometheusMetricsConfig {

  private final MeterRegistry meterRegistry;

  // 计数器
  private final Counter questionSubmitCounter;
  private final Counter questionSubmitSuccessCounter;
  private final Counter questionSubmitFailureCounter;
  private final Counter userLoginCounter;
  private final Counter userRegisterCounter;
  private final Counter contestJoinCounter;

  // 计时器
  private final Timer judgeExecutionTimer;
  private final Timer questionSubmitTimer;

  // 仪表（Gauge）- 用于表示可增减的值
  private final AtomicLong judgeQueueSize = new AtomicLong(0);
  private final AtomicLong activeContestCount = new AtomicLong(0);
  private final AtomicLong onlineUserCount = new AtomicLong(0);

  public PrometheusMetricsConfig(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;

    // 初始化计数器 - 题目提交相关
    this.questionSubmitCounter =
        Counter.builder("question_submit_total")
            .description("Total number of question submissions")
            .tag("type", "total")
            .register(meterRegistry);

    this.questionSubmitSuccessCounter =
        Counter.builder("question_submit_total")
            .description("Total number of successful question submissions")
            .tag("type", "success")
            .register(meterRegistry);

    this.questionSubmitFailureCounter =
        Counter.builder("question_submit_total")
            .description("Total number of failed question submissions")
            .tag("type", "failure")
            .register(meterRegistry);

    // 初始化计数器 - 用户相关
    this.userLoginCounter =
        Counter.builder("user_login_total")
            .description("Total number of user logins")
            .register(meterRegistry);

    this.userRegisterCounter =
        Counter.builder("user_register_total")
            .description("Total number of user registrations")
            .register(meterRegistry);

    // 初始化计数器 - 比赛相关
    this.contestJoinCounter =
        Counter.builder("contest_join_total")
            .description("Total number of contest joins")
            .register(meterRegistry);

    // 初始化计时器 - 判题执行时间
    this.judgeExecutionTimer =
        Timer.builder("judge_execution_duration_seconds")
            .description("Time taken for code execution judging")
            .tag("operation", "judge")
            .register(meterRegistry);

    // 初始化计时器 - 题目提交时间
    this.questionSubmitTimer =
        Timer.builder("question_submit_duration_seconds")
            .description("Time taken for question submission processing")
            .tag("operation", "submit")
            .register(meterRegistry);

    // 注册仪表（Gauge）
    Gauge.builder("judge_queue_size", judgeQueueSize, AtomicLong::get)
        .description("Current size of the judge queue")
        .register(meterRegistry);

    Gauge.builder("contest_active_count", activeContestCount, AtomicLong::get)
        .description("Number of currently active contests")
        .register(meterRegistry);

    Gauge.builder("user_online_count", onlineUserCount, AtomicLong::get)
        .description("Number of currently online users")
        .register(meterRegistry);
  }

  // ========== 计数器方法 ==========

  public void incrementQuestionSubmit() {
    questionSubmitCounter.increment();
  }

  public void incrementQuestionSubmitSuccess() {
    questionSubmitSuccessCounter.increment();
  }

  public void incrementQuestionSubmitFailure() {
    questionSubmitFailureCounter.increment();
  }

  public void incrementUserLogin() {
    userLoginCounter.increment();
  }

  public void incrementUserRegister() {
    userRegisterCounter.increment();
  }

  public void incrementContestJoin() {
    contestJoinCounter.increment();
  }

  // ========== 计时器方法 ==========

  public Timer.Sample startJudgeTimer() {
    return Timer.start(meterRegistry);
  }

  public void recordJudgeTime(Timer.Sample sample) {
    sample.stop(judgeExecutionTimer);
  }

  public Timer.Sample startSubmitTimer() {
    return Timer.start(meterRegistry);
  }

  public void recordSubmitTime(Timer.Sample sample) {
    sample.stop(questionSubmitTimer);
  }

  // ========== 仪表（Gauge）方法 ==========

  public void setJudgeQueueSize(long size) {
    judgeQueueSize.set(size);
  }

  public void setActiveContestCount(long count) {
    activeContestCount.set(count);
  }

  public void setOnlineUserCount(long count) {
    onlineUserCount.set(count);
  }

  public long getJudgeQueueSize() {
    return judgeQueueSize.get();
  }

  public long getActiveContestCount() {
    return activeContestCount.get();
  }

  public long getOnlineUserCount() {
    return onlineUserCount.get();
  }
}
