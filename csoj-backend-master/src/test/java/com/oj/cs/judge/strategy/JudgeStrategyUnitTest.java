package com.oj.cs.judge.strategy;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.oj.cs.judge.codesandbox.model.JudgeInfo;
import com.oj.cs.model.dto.question.JudgeCase;
import com.oj.cs.model.entity.Question;
import com.oj.cs.model.enums.JudgeInfoMessageEnum;

/**
 * DefaultJudgeStrategy 单元测试
 *
 * <p>测试覆盖： - 输出数量检查 - 输出内容比较 - 内存限制检查 - 时间限制检查
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("默认判题策略单元测试")
public class JudgeStrategyUnitTest {

  @InjectMocks private DefaultJudgeStrategy defaultJudgeStrategy;

  private JudgeContext judgeContext;
  private Question testQuestion;

  @BeforeEach
  void setUp() {
    // 创建测试题目
    testQuestion = new Question();
    testQuestion.setId(1L);
    testQuestion.setTitle("两数之和");
    testQuestion.setContent("给定一个整数数组nums和一个整数目标值target...");
    testQuestion.setJudgeConfig("{\"timeLimit\":1000,\"memoryLimit\":256}");

    // 创建判题上下文
    judgeContext = new JudgeContext();
    JudgeInfo judgeInfo = new JudgeInfo();
    judgeInfo.setMessage("Accepted");
    judgeInfo.setMemory(100L);
    judgeInfo.setTime(200L);
    judgeContext.setJudgeInfo(judgeInfo);
  }

  // ==================== 输出数量检查测试 ====================

  @Test
  @DisplayName("判题 - 输出数量不足")
  void testDoJudge_OutputCountLess() {
    List<String> inputList = Arrays.asList("1 2", "3 4");
    List<String> outputList = Arrays.asList("3");

    judgeContext.setInputList(inputList);
    judgeContext.setOutputList(outputList);
    judgeContext.setJudgeCaseList(createJudgeCases(inputList, Arrays.asList("3 4", "3 6")));
    judgeContext.setQuestion(testQuestion);

    JudgeInfo result = defaultJudgeStrategy.doJudge(judgeContext);

    assertEquals(JudgeInfoMessageEnum.WRONG_ANSWER.getValue(), result.getMessage());
  }

  @Test
  @DisplayName("判题 - 输出数量为空")
  void testDoJudge_OutputEmpty() {
    List<String> inputList = Arrays.asList("1 2", "3 4");

    judgeContext.setInputList(inputList);
    judgeContext.setOutputList(Collections.emptyList());
    judgeContext.setJudgeCaseList(Collections.emptyList());
    judgeContext.setQuestion(testQuestion);

    JudgeInfo result = defaultJudgeStrategy.doJudge(judgeContext);

    assertEquals(JudgeInfoMessageEnum.WRONG_ANSWER.getValue(), result.getMessage());
  }

  @Test
  @DisplayName("判题 - 输出数量正确")
  void testDoJudge_OutputCountCorrect() {
    List<String> inputList = Arrays.asList("1 2", "3 4");
    List<String> outputList = Arrays.asList("3 4", "3 6");

    judgeContext.setInputList(inputList);
    judgeContext.setOutputList(outputList);
    judgeContext.setJudgeCaseList(createJudgeCases(inputList, outputList));
    judgeContext.setQuestion(testQuestion);

    JudgeInfo result = defaultJudgeStrategy.doJudge(judgeContext);

    assertNotNull(result);
  }

  // ==================== 输出内容比较测试 ====================

  @Test
  @DisplayName("判题 - 答案正确")
  void testDoJudge_CorrectAnswer() {
    List<String> inputList = Arrays.asList("1 2", "3 4");
    List<String> outputList = Arrays.asList("3 4", "3 6");

    judgeContext.setInputList(inputList);
    judgeContext.setOutputList(outputList);
    judgeContext.setJudgeCaseList(createJudgeCases(inputList, outputList));
    judgeContext.setQuestion(testQuestion);

    JudgeInfo result = defaultJudgeStrategy.doJudge(judgeContext);

    assertEquals(JudgeInfoMessageEnum.ACCEPTED.getValue(), result.getMessage());
  }

  @Test
  @DisplayName("判题 - 答案错误")
  void testDoJudge_WrongAnswer() {
    List<String> inputList = Arrays.asList("1 2", "3 4");
    List<String> expectedOutput = Arrays.asList("3 5", "3 6");
    // 实际输出与期望输出不同
    List<String> actualOutput = Arrays.asList("wrong", "answer");

    judgeContext.setInputList(inputList);
    judgeContext.setOutputList(actualOutput);
    judgeContext.setJudgeCaseList(createJudgeCases(inputList, expectedOutput));
    judgeContext.setQuestion(testQuestion);

    JudgeInfo result = defaultJudgeStrategy.doJudge(judgeContext);

    assertEquals(JudgeInfoMessageEnum.WRONG_ANSWER.getValue(), result.getMessage());
  }

  @Test
  @DisplayName("判题 - 空输出")
  void testDoJudge_EmptyOutput() {
    List<String> inputList = Arrays.asList("1 2", "3 4");
    List<String> expectedOutput = Arrays.asList("3 5", "3 6");
    // 实际输出为空
    List<String> actualOutput = Arrays.asList("", "");

    judgeContext.setInputList(inputList);
    judgeContext.setOutputList(actualOutput);
    judgeContext.setJudgeCaseList(createJudgeCases(inputList, expectedOutput));
    judgeContext.setQuestion(testQuestion);

    JudgeInfo result = defaultJudgeStrategy.doJudge(judgeContext);

    // 空字符串输出也算数量正确，但内容不匹配
    assertEquals(JudgeInfoMessageEnum.WRONG_ANSWER.getValue(), result.getMessage());
  }

  // ==================== 内存限制测试 ====================

  @Test
  @DisplayName("判题 - 内存超限")
  void testDoJudge_MemoryLimitExceeded() {
    List<String> inputList = Arrays.asList("1 2", "3 4");
    List<String> outputList = Arrays.asList("3 4", "3 6");

    judgeContext.setInputList(inputList);
    judgeContext.setOutputList(outputList);
    judgeContext.setJudgeCaseList(createJudgeCases(inputList, outputList));
    judgeContext.setQuestion(testQuestion);

    // 设置超出内存限制的模拟数据
    judgeContext.getJudgeInfo().setMemory(500L); // 超过256限制

    JudgeInfo result = defaultJudgeStrategy.doJudge(judgeContext);

    assertEquals(JudgeInfoMessageEnum.MEMORY_LIMIT_EXCEEDED.getValue(), result.getMessage());
  }

  @Test
  @DisplayName("判题 - 内存正常")
  void testDoJudge_MemoryNormal() {
    List<String> inputList = Arrays.asList("1 2", "3 4");
    List<String> outputList = Arrays.asList("3 4", "3 6");

    judgeContext.setInputList(inputList);
    judgeContext.setOutputList(outputList);
    judgeContext.setJudgeCaseList(createJudgeCases(inputList, outputList));
    judgeContext.setQuestion(testQuestion);

    // 设置正常的内存使用
    judgeContext.getJudgeInfo().setMemory(128L);

    JudgeInfo result = defaultJudgeStrategy.doJudge(judgeContext);

    assertEquals(JudgeInfoMessageEnum.ACCEPTED.getValue(), result.getMessage());
  }

  @Test
  @DisplayName("判题 - 内存边界值")
  void testDoJudge_MemoryBoundary() {
    List<String> inputList = Arrays.asList("1 2", "3 4");
    List<String> outputList = Arrays.asList("3 4", "3 6");

    judgeContext.setInputList(inputList);
    judgeContext.setOutputList(outputList);
    judgeContext.setJudgeCaseList(createJudgeCases(inputList, outputList));
    judgeContext.setQuestion(testQuestion);

    // 恰好等于限制值
    judgeContext.getJudgeInfo().setMemory(256L);

    JudgeInfo result = defaultJudgeStrategy.doJudge(judgeContext);

    assertEquals(JudgeInfoMessageEnum.ACCEPTED.getValue(), result.getMessage());
  }

  // ==================== 时间限制测试 ====================

  @Test
  @DisplayName("判题 - 时间超限")
  void testDoJudge_TimeLimitExceeded() {
    List<String> inputList = Arrays.asList("1 2", "3 4");
    List<String> outputList = Arrays.asList("3 4", "3 6");

    judgeContext.setInputList(inputList);
    judgeContext.setOutputList(outputList);
    judgeContext.setJudgeCaseList(createJudgeCases(inputList, outputList));
    judgeContext.setQuestion(testQuestion);

    // 设置超出时间限制的模拟数据
    judgeContext.getJudgeInfo().setTime(2000L); // 超过1000限制

    JudgeInfo result = defaultJudgeStrategy.doJudge(judgeContext);

    assertEquals(JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED.getValue(), result.getMessage());
  }

  @Test
  @DisplayName("判题 - 时间正常")
  void testDoJudge_TimeNormal() {
    List<String> inputList = Arrays.asList("1 2", "3 4");
    List<String> outputList = Arrays.asList("3 4", " 3 6");

    judgeContext.setInputList(inputList);
    judgeContext.setOutputList(outputList);
    judgeContext.setJudgeCaseList(createJudgeCases(inputList, outputList));
    judgeContext.setQuestion(testQuestion);

    // 设置正常的时间使用
    judgeContext.getJudgeInfo().setTime(500L);

    JudgeInfo result = defaultJudgeStrategy.doJudge(judgeContext);

    assertEquals(JudgeInfoMessageEnum.ACCEPTED.getValue(), result.getMessage());
  }

  @Test
  @DisplayName("判题 - 时间边界值")
  void testDoJudge_TimeBoundary() {
    List<String> inputList = Arrays.asList("1 2", "3 4");
    List<String> outputList = Arrays.asList("3 4", "3 6");

    judgeContext.setInputList(inputList);
    judgeContext.setOutputList(outputList);
    judgeContext.setJudgeCaseList(createJudgeCases(inputList, outputList));
    judgeContext.setQuestion(testQuestion);

    // 恰好等于限制值
    judgeContext.getJudgeInfo().setTime(1000L);

    JudgeInfo result = defaultJudgeStrategy.doJudge(judgeContext);

    assertEquals(JudgeInfoMessageEnum.ACCEPTED.getValue(), result.getMessage());
  }

  // ==================== 边界条件测试 ====================

  @Test
  @DisplayName("边界条件 - 空输入")
  void testBoundary_EmptyInput() {
    List<String> inputList = Arrays.asList("", "");
    List<String> outputList = Arrays.asList("", "");

    judgeContext.setInputList(inputList);
    judgeContext.setOutputList(outputList);
    judgeContext.setJudgeCaseList(createJudgeCases(inputList, outputList));
    judgeContext.setQuestion(testQuestion);

    assertNotNull(defaultJudgeStrategy.doJudge(judgeContext));
  }

  @Test
  @DisplayName("边界条件 - 大输入输出")
  void testBoundary_LargeInputOutput() {
    String largeInput = StringUtils.repeat("a", 10000);
    String largeOutput = StringUtils.repeat("b", 10000);

    judgeContext.setInputList(Arrays.asList(largeInput, largeInput));
    judgeContext.setOutputList(Arrays.asList(largeOutput, largeOutput));
    judgeContext.setJudgeCaseList(
        createJudgeCases(
            Arrays.asList(largeInput, largeInput), Arrays.asList(largeOutput, largeOutput)));
    judgeContext.setQuestion(testQuestion);

    assertNotNull(defaultJudgeStrategy.doJudge(judgeContext));
  }

  @Test
  @DisplayName("边界条件 - 特殊字符")
  void testBoundary_SpecialCharacters() {
    List<String> inputList = Arrays.asList("1\t2", "3\n4");
    List<String> outputList = Arrays.asList("3\t4", "3\n6");

    judgeContext.setInputList(inputList);
    judgeContext.setOutputList(outputList);
    judgeContext.setJudgeCaseList(createJudgeCases(inputList, outputList));
    judgeContext.setQuestion(testQuestion);

    assertNotNull(defaultJudgeStrategy.doJudge(judgeContext));
  }

  // ==================== 判题配置测试 ====================

  @Test
  @DisplayName("判题 - 无判题配置")
  void testJudgeConfig_NoConfig() {
    testQuestion.setJudgeConfig(null);

    judgeContext.setInputList(Arrays.asList("1 2", "3 4"));
    judgeContext.setOutputList(Arrays.asList("3 4", "3 6"));
    judgeContext.setJudgeCaseList(
        createJudgeCases(Arrays.asList("1 2", "3 4"), Arrays.asList("3 4", " 3 6")));
    judgeContext.setQuestion(testQuestion);
    judgeContext.setJudgeInfo(new JudgeInfo());

    // 无配置时应该能正常运行，只检查答案正确性
    JudgeInfo result = defaultJudgeStrategy.doJudge(judgeContext);

    assertNotNull(result);
  }

  @Test
  @DisplayName("判题 - 无限制配置")
  void testJudgeConfig_NoLimits() {
    // 设置足够大的限制值，实际使用远小于限制
    testQuestion.setJudgeConfig("{\"timeLimit\":10000,\"memoryLimit\":10000}");

    judgeContext.setInputList(Arrays.asList("1 2", "3 4"));
    judgeContext.setOutputList(Arrays.asList("3 4", "3 6"));
    judgeContext.setJudgeCaseList(
        createJudgeCases(Arrays.asList("1 2", "3 4"), Arrays.asList("3 4", "3 6")));
    judgeContext.setQuestion(testQuestion);
    JudgeInfo judgeInfo = new JudgeInfo();
    judgeInfo.setMemory(100L); // 远小于10000限制
    judgeInfo.setTime(100L); // 远小于10000限制
    judgeContext.setJudgeInfo(judgeInfo);

    JudgeInfo result = defaultJudgeStrategy.doJudge(judgeContext);

    assertEquals(JudgeInfoMessageEnum.ACCEPTED.getValue(), result.getMessage());
  }

  // ==================== 判题状态枚举测试 ====================

  // ==================== 辅助方法 ====================

  private List<JudgeCase> createJudgeCases(List<String> inputs, List<String> outputs) {
    List<JudgeCase> cases = new java.util.ArrayList<>();
    for (int i = 0; i < inputs.size(); i++) {
      JudgeCase judgeCase = new JudgeCase();
      judgeCase.setInput(inputs.get(i));
      judgeCase.setOutput(outputs.get(i));
      cases.add(judgeCase);
    }
    return cases;
  }
}
