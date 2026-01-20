package com.oj.cs.judge;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.oj.cs.judge.codesandbox.model.JudgeInfo;
import com.oj.cs.judge.strategy.JudgeContext;
import com.oj.cs.judge.strategy.JudgeStrategy;
import com.oj.cs.model.entity.QuestionSubmit;

import lombok.extern.slf4j.Slf4j;

/** 判题管理（使用策略模式 + 工厂模式） */
@Slf4j
@Service
public class JudgeManager {

  /** 判题策略映射表（使用ConcurrentHashMap保证线程安全） key: 语言名称（小写），value: 对应的判题策略 */
  private final Map<String, JudgeStrategy> judgeStrategyMap = new ConcurrentHashMap<>();

  /** 默认判题策略 */
  private final Map<String, JudgeStrategy> defaultStrategyMap = new ConcurrentHashMap<>();

  /**
   * 构造函数注入所有判题策略
   *
   * @param judgeStrategies Spring会自动注入所有JudgeStrategy类型的Bean
   */
  public JudgeManager(Map<String, JudgeStrategy> judgeStrategies) {
    // 将所有策略Bean按Bean名称存入Map
    // Bean命名规则：javaLanguageJudgeStrategy -> java
    //                 defaultJudgeStrategy -> default
    for (Map.Entry<String, JudgeStrategy> entry : judgeStrategies.entrySet()) {
      String beanName = entry.getKey();
      JudgeStrategy strategy = entry.getValue();

      // 提取语言标识（从BeanName中提取，如 "javaLanguageJudgeStrategy" -> "java"）
      String language = extractLanguageFromBeanName(beanName);
      if (language != null) {
        judgeStrategyMap.put(language, strategy);
        log.info("注册判题策略: {} -> {}", language, strategy.getClass().getSimpleName());
      }

      // 注册默认策略
      if (beanName.contains("default")) {
        defaultStrategyMap.put("default", strategy);
        log.info("注册默认判题策略: {}", strategy.getClass().getSimpleName());
      }
    }
  }

  /**
   * 从Bean名称中提取语言标识
   *
   * @param beanName Bean名称（如 "javaLanguageJudgeStrategy"）
   * @return 语言标识（如 "java"），如果无法提取则返回null
   */
  private String extractLanguageFromBeanName(String beanName) {
    if (beanName == null || beanName.isEmpty()) {
      return null;
    }
    // 移除"LanguageJudgeStrategy"或"JudgeStrategy"后缀
    String lowerName = beanName.toLowerCase();
    if (lowerName.contains("language")) {
      return lowerName.split("language")[0];
    }
    return null;
  }

  /**
   * 执行判题
   *
   * @param judgeContext 判题上下文
   * @return 判题结果
   */
  public JudgeInfo doJudge(JudgeContext judgeContext) {
    QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
    String language = questionSubmit.getLanguage();

    // 获取对应的判题策略
    JudgeStrategy judgeStrategy = getJudgeStrategy(language);
    log.info("使用判题策略: {}，语言: {}", judgeStrategy.getClass().getSimpleName(), language);

    return judgeStrategy.doJudge(judgeContext);
  }

  /**
   * 根据语言获取判题策略
   *
   * @param language 编程语言
   * @return 判题策略
   */
  private JudgeStrategy getJudgeStrategy(String language) {
    if (language == null || language.isEmpty()) {
      return getDefaultStrategy();
    }

    // 标准化语言名称（转小写）
    String normalizedLanguage = language.toLowerCase().trim();

    // 获取对应语言的策略
    JudgeStrategy strategy = judgeStrategyMap.get(normalizedLanguage);
    if (strategy != null) {
      return strategy;
    }

    // 未找到匹配的策略，使用默认策略
    log.warn("未找到语言 {} 的判题策略，使用默认策略", language);
    return getDefaultStrategy();
  }

  /**
   * 获取默认判题策略
   *
   * @return 默认判题策略
   */
  private JudgeStrategy getDefaultStrategy() {
    JudgeStrategy defaultStrategy = defaultStrategyMap.get("default");
    if (defaultStrategy == null) {
      throw new IllegalStateException("默认判题策略未配置");
    }
    return defaultStrategy;
  }

  /**
   * 获取当前支持的语言列表
   *
   * @return 支持的语言列表
   */
  public java.util.Set<String> getSupportedLanguages() {
    return new java.util.HashSet<>(judgeStrategyMap.keySet());
  }
}
