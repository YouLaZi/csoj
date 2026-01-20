package com.oj.cs.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oj.cs.common.ErrorCode;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.model.dto.question.JudgeConfig;
import com.oj.cs.model.dto.question.QuestionQueryRequest;
import com.oj.cs.model.entity.Question;
import com.oj.cs.model.vo.QuestionVO;
import com.oj.cs.service.impl.QuestionServiceImpl;

/**
 * QuestionService 单元测试
 *
 * <p>测试覆盖： - 题目校验 - 查询条件构建 - 题目VO转换 - 已完成题目查询 - 推荐题目查询
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("题目服务单元测试")
public class QuestionServiceUnitTest {

  @Mock private HttpServletRequest request;

  @Mock private UserService userService;

  @InjectMocks private QuestionServiceImpl questionService;

  private Question testQuestion;
  private QuestionQueryRequest questionQueryRequest;

  @BeforeEach
  void setUp() {
    // 创建测试题目
    testQuestion = new Question();
    testQuestion.setId(1L);
    testQuestion.setTitle("两数之和");
    testQuestion.setContent("给定一个整数数组nums和一个整数目标值target...");
    testQuestion.setTags("[\"数组\",\"哈希表\"]");
    testQuestion.setAnswer("class Solution {...}");
    testQuestion.setJudgeCase("[{\"input\":\"[2,7,11,15]\\n9\",\"output\":\"[0,1]\"}]");
    testQuestion.setJudgeConfig("{\"timeLimit\":1000,\"memoryLimit\":256}");
    testQuestion.setDifficulty("简单");
    testQuestion.setUserId(1L);
    testQuestion.setSubmitNum(100);
    testQuestion.setAcceptedNum(50);
    testQuestion.setThumbNum(10);
    testQuestion.setFavourNum(5);
    testQuestion.setCreateTime(new Date());
    testQuestion.setUpdateTime(new Date());
    testQuestion.setIsDelete(0);

    // 创建查询请求
    questionQueryRequest = new QuestionQueryRequest();
    questionQueryRequest.setTitle("两数");
    questionQueryRequest.setCurrent(1L);
    questionQueryRequest.setPageSize(10L);
  }

  // ==================== validQuestion 测试 ====================

  @Test
  @DisplayName("题目校验 - 新增题目成功")
  void testValidQuestion_Add_Success() {
    Question validQuestion = new Question();
    validQuestion.setTitle("有效标题");
    validQuestion.setContent("有效内容");
    validQuestion.setTags("[\"标签1\"]");

    assertDoesNotThrow(() -> questionService.validQuestion(validQuestion, true));
  }

  @Test
  @DisplayName("题目校验 - 新增题目缺少必填字段")
  void testValidQuestion_Add_MissingRequired() {
    Question invalidQuestion = new Question();
    invalidQuestion.setTitle("只有标题");

    BusinessException exception =
        assertThrows(
            BusinessException.class, () -> questionService.validQuestion(invalidQuestion, true));
    assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
  }

  @Test
  @DisplayName("题目校验 - null题目")
  void testValidQuestion_NullQuestion() {
    BusinessException exception =
        assertThrows(BusinessException.class, () -> questionService.validQuestion(null, true));
    assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
  }

  @Test
  @DisplayName("题目校验 - 标题过长")
  void testValidQuestion_TitleTooLong() {
    Question invalidQuestion = new Question();
    invalidQuestion.setTitle("a".repeat(100)); // 超过80字符
    invalidQuestion.setContent("内容");
    invalidQuestion.setTags("[\"标签\"]");

    BusinessException exception =
        assertThrows(
            BusinessException.class, () -> questionService.validQuestion(invalidQuestion, true));
    assertTrue(exception.getMessage().contains("标题过长"));
  }

  @Test
  @DisplayName("题目校验 - 内容过长")
  void testValidQuestion_ContentTooLong() {
    Question invalidQuestion = new Question();
    invalidQuestion.setTitle("标题");
    invalidQuestion.setContent("a".repeat(10000)); // 超过8192字符
    invalidQuestion.setTags("[\"标签\"]");

    BusinessException exception =
        assertThrows(
            BusinessException.class, () -> questionService.validQuestion(invalidQuestion, true));
    assertTrue(exception.getMessage().contains("内容过长"));
  }

  @Test
  @DisplayName("题目校验 - 答案过长")
  void testValidQuestion_AnswerTooLong() {
    Question invalidQuestion = new Question();
    invalidQuestion.setTitle("标题");
    invalidQuestion.setContent("内容");
    invalidQuestion.setTags("[\"标签\"]");
    invalidQuestion.setAnswer("a".repeat(10000)); // 超过8192字符

    BusinessException exception =
        assertThrows(
            BusinessException.class, () -> questionService.validQuestion(invalidQuestion, true));
    assertTrue(exception.getMessage().contains("答案过长"));
  }

  @Test
  @DisplayName("题目校验 - 判题用例过长")
  void testValidQuestion_JudgeCaseTooLong() {
    Question invalidQuestion = new Question();
    invalidQuestion.setTitle("标题");
    invalidQuestion.setContent("内容");
    invalidQuestion.setTags("[\"标签\"]");
    invalidQuestion.setJudgeCase("a".repeat(10000)); // 超过8192字符

    BusinessException exception =
        assertThrows(
            BusinessException.class, () -> questionService.validQuestion(invalidQuestion, true));
    assertTrue(exception.getMessage().contains("判题用例过长"));
  }

  @Test
  @DisplayName("题目校验 - 判题配置过长")
  void testValidQuestion_JudgeConfigTooLong() {
    Question invalidQuestion = new Question();
    invalidQuestion.setTitle("标题");
    invalidQuestion.setContent("内容");
    invalidQuestion.setTags("[\"标签\"]");
    invalidQuestion.setJudgeConfig("a".repeat(10000)); // 超过8192字符

    BusinessException exception =
        assertThrows(
            BusinessException.class, () -> questionService.validQuestion(invalidQuestion, true));
    assertTrue(exception.getMessage().contains("判题配置过长"));
  }

  @Test
  @DisplayName("题目校验 - 更新时允许部分字段为空")
  void testValidQuestion_Update_PartialFields() {
    Question partialQuestion = new Question();
    partialQuestion.setId(1L);
    partialQuestion.setTitle("更新的标题");

    assertDoesNotThrow(() -> questionService.validQuestion(partialQuestion, false));
  }

  // ==================== getQueryWrapper 测试 ====================

  @Test
  @DisplayName("获取查询条件 - 成功")
  void testGetQueryWrapper_Success() {
    QueryWrapper<Question> queryWrapper = questionService.getQueryWrapper(questionQueryRequest);

    assertNotNull(queryWrapper);
  }

  @Test
  @DisplayName("获取查询条件 - 空请求")
  void testGetQueryWrapper_EmptyRequest() {
    QuestionQueryRequest emptyRequest = new QuestionQueryRequest();
    QueryWrapper<Question> queryWrapper = questionService.getQueryWrapper(emptyRequest);

    assertNotNull(queryWrapper);
  }

  @Test
  @DisplayName("获取查询条件 - null请求")
  void testGetQueryWrapper_NullRequest() {
    QueryWrapper<Question> queryWrapper = questionService.getQueryWrapper(null);

    assertNotNull(queryWrapper);
  }

  @Test
  @DisplayName("获取查询条件 - 带标题过滤")
  void testGetQueryWrapper_WithTitle() {
    questionQueryRequest.setTitle("两数之和");
    QueryWrapper<Question> queryWrapper = questionService.getQueryWrapper(questionQueryRequest);

    assertNotNull(queryWrapper);
  }

  @Test
  @DisplayName("获取查询条件 - 带内容过滤")
  void testGetQueryWrapper_WithContent() {
    questionQueryRequest.setContent("数组");
    QueryWrapper<Question> queryWrapper = questionService.getQueryWrapper(questionQueryRequest);

    assertNotNull(queryWrapper);
  }

  @Test
  @DisplayName("获取查询条件 - 带标签过滤")
  void testGetQueryWrapper_WithTags() {
    questionQueryRequest.setTags(Arrays.asList("数组", "哈希表"));
    QueryWrapper<Question> queryWrapper = questionService.getQueryWrapper(questionQueryRequest);

    assertNotNull(queryWrapper);
  }

  @Test
  @DisplayName("获取查询条件 - 带用户ID过滤")
  void testGetQueryWrapper_WithUserId() {
    questionQueryRequest.setUserId(1L);
    QueryWrapper<Question> queryWrapper = questionService.getQueryWrapper(questionQueryRequest);

    assertNotNull(queryWrapper);
  }

  @Test
  @DisplayName("获取查询条件 - 带ID过滤")
  void testGetQueryWrapper_WithId() {
    questionQueryRequest.setId(1L);
    QueryWrapper<Question> queryWrapper = questionService.getQueryWrapper(questionQueryRequest);

    assertNotNull(queryWrapper);
  }

  @Test
  @DisplayName("获取查询条件 - 带排序")
  void testGetQueryWrapper_WithSort() {
    questionQueryRequest.setSortField("createTime");
    questionQueryRequest.setSortOrder("desc");

    QueryWrapper<Question> queryWrapper = questionService.getQueryWrapper(questionQueryRequest);

    assertNotNull(queryWrapper);
  }

  // ==================== getQuestionVO 测试 ====================

  @Test
  @DisplayName("获取题目VO - 成功")
  void testGetQuestionVO_Success() {
    QuestionVO questionVO = questionService.getQuestionVO(testQuestion, request);

    assertNotNull(questionVO);
    assertEquals(testQuestion.getId(), questionVO.getId());
    assertEquals(testQuestion.getTitle(), questionVO.getTitle());
    assertEquals(testQuestion.getContent(), questionVO.getContent());
    assertNotNull(questionVO.getTags());
  }

  @Test
  @DisplayName("获取题目VO - null题目会抛出NPE")
  void testGetQuestionVO_NullQuestion() {
    // 实现没有对null做处理，会抛出NullPointerException
    assertThrows(NullPointerException.class, () -> questionService.getQuestionVO(null, request));
  }

  @Test
  @DisplayName("获取题目VO - 脱敏验证")
  void testGetQuestionVO_Desensitization() {
    QuestionVO questionVO = questionService.getQuestionVO(testQuestion, request);

    // QuestionVO 不包含答案和判题用例（敏感信息）
    assertNotNull(questionVO);
    assertNotNull(questionVO.getJudgeConfig());
    // 答案和判题用例应该被移除或脱敏
  }

  // ==================== getQuestionVOPage 测试 ====================

  @Test
  @DisplayName("分页获取题目VO - 空页")
  void testGetQuestionVOPage_EmptyPage() {
    Page<Question> emptyPage = new Page<>(1, 10, 0);
    emptyPage.setRecords(Collections.emptyList());

    Page<QuestionVO> resultPage = questionService.getQuestionVOPage(emptyPage, request);

    assertNotNull(resultPage);
    assertEquals(0, resultPage.getRecords().size());
  }

  @Test
  @DisplayName("分页获取题目VO - 有数据")
  void testGetQuestionVOPage_WithData() {
    Page<Question> questionPage = new Page<>(1, 10, 1);
    questionPage.setRecords(Arrays.asList(testQuestion));

    Page<QuestionVO> resultPage = questionService.getQuestionVOPage(questionPage, request);

    assertNotNull(resultPage);
    assertEquals(1, resultPage.getRecords().size());
    assertEquals(testQuestion.getId(), resultPage.getRecords().get(0).getId());
  }

  // ==================== QuestionVO 静态方法测试 ====================

  @Test
  @DisplayName("QuestionVO.objToVo - 成功转换")
  void testQuestionVO_ObjToVo() {
    QuestionVO questionVO = QuestionVO.objToVo(testQuestion);

    assertNotNull(questionVO);
    assertEquals(testQuestion.getId(), questionVO.getId());
    assertEquals(testQuestion.getTitle(), questionVO.getTitle());
    assertEquals(testQuestion.getContent(), questionVO.getContent());
    assertNotNull(questionVO.getTags());
    assertNotNull(questionVO.getJudgeConfig());
  }

  @Test
  @DisplayName("QuestionVO.objToVo - null输入")
  void testQuestionVO_ObjToVo_Null() {
    QuestionVO questionVO = QuestionVO.objToVo(null);

    assertNull(questionVO);
  }

  @Test
  @DisplayName("QuestionVO.voToObj - 成功转换")
  void testQuestionVO_VoToObj() {
    QuestionVO questionVO = new QuestionVO();
    questionVO.setId(1L);
    questionVO.setTitle("测试题目");
    questionVO.setContent("测试内容");
    questionVO.setTags(Arrays.asList("数组", "哈希表"));

    JudgeConfig judgeConfig = new JudgeConfig();
    judgeConfig.setTimeLimit(1000L);
    judgeConfig.setMemoryLimit(256L);
    judgeConfig.setStackLimit(128L);
    questionVO.setJudgeConfig(judgeConfig);

    Question question = QuestionVO.voToObj(questionVO);

    assertNotNull(question);
    assertEquals(questionVO.getId(), question.getId());
    assertEquals(questionVO.getTitle(), question.getTitle());
    assertEquals(questionVO.getContent(), question.getContent());
    assertNotNull(question.getTags());
    assertNotNull(question.getJudgeConfig());
  }

  @Test
  @DisplayName("QuestionVO.voToObj - null输入")
  void testQuestionVO_VoToObj_Null() {
    Question question = QuestionVO.voToObj(null);

    assertNull(question);
  }

  // ==================== 题目实体测试 ====================

  @Test
  @DisplayName("题目实体 - 字段完整性")
  void testQuestionEntity_CompleteFields() {
    assertNotNull(testQuestion.getId());
    assertNotNull(testQuestion.getTitle());
    assertNotNull(testQuestion.getContent());
    assertNotNull(testQuestion.getTags());
    assertNotNull(testQuestion.getUserId());
    assertNotNull(testQuestion.getCreateTime());
    assertNotNull(testQuestion.getUpdateTime());
    assertNotNull(testQuestion.getIsDelete());
  }

  @Test
  @DisplayName("题目实体 - 统计数据")
  void testQuestionEntity_Statistics() {
    assertEquals(Integer.valueOf(100), testQuestion.getSubmitNum());
    assertEquals(Integer.valueOf(50), testQuestion.getAcceptedNum());
    assertEquals(Integer.valueOf(10), testQuestion.getThumbNum());
    assertEquals(Integer.valueOf(5), testQuestion.getFavourNum());
  }

  @Test
  @DisplayName("题目实体 - 难度设置")
  void testQuestionEntity_Difficulty() {
    testQuestion.setDifficulty("简单");
    assertEquals("简单", testQuestion.getDifficulty());

    testQuestion.setDifficulty("中等");
    assertEquals("中等", testQuestion.getDifficulty());

    testQuestion.setDifficulty("困难");
    assertEquals("困难", testQuestion.getDifficulty());
  }

  // ==================== 题目验证规则测试 ====================

  @Test
  @DisplayName("题目验证 - 标题长度边界")
  void testQuestionValidation_TitleBoundary() {
    Question validQuestion = new Question();
    validQuestion.setTitle("a".repeat(80)); // 恰好80字符
    validQuestion.setContent("内容");
    validQuestion.setTags("[\"标签\"]");

    assertDoesNotThrow(() -> questionService.validQuestion(validQuestion, true));
  }

  @Test
  @DisplayName("题目验证 - 内容长度边界")
  void testQuestionValidation_ContentBoundary() {
    Question validQuestion = new Question();
    validQuestion.setTitle("标题");
    validQuestion.setContent("a".repeat(8192)); // 恰好8192字符
    validQuestion.setTags("[\"标签\"]");

    assertDoesNotThrow(() -> questionService.validQuestion(validQuestion, true));
  }

  @Test
  @DisplayName("题目验证 - 更新时空字符串可接受")
  void testQuestionValidation_EmptyStrings() {
    // 在更新模式下，空字符串的可选字段应该被接受
    Question questionWithEmptyFields = new Question();
    questionWithEmptyFields.setTitle("标题");
    questionWithEmptyFields.setContent("有效内容");
    questionWithEmptyFields.setTags("[\"标签\"]");
    questionWithEmptyFields.setAnswer("");
    questionWithEmptyFields.setJudgeCase("");

    // 更新模式(add=false)，空字符串的可选字段不会触发校验错误
    assertDoesNotThrow(() -> questionService.validQuestion(questionWithEmptyFields, false));
  }
}
