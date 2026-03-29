package com.oj.cs.judge;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.oj.cs.common.ErrorCode;
import com.oj.cs.exception.BusinessException;
import com.oj.cs.judge.codesandbox.CodeSandbox;
import com.oj.cs.judge.codesandbox.CodeSandboxFactory;
import com.oj.cs.judge.codesandbox.CodeSandboxProxy;
import com.oj.cs.judge.codesandbox.model.ExecuteCodeRequest;
import com.oj.cs.judge.codesandbox.model.ExecuteCodeResponse;
import com.oj.cs.judge.codesandbox.model.JudgeInfo;
import com.oj.cs.judge.strategy.JudgeContext;
import com.oj.cs.model.dto.question.JudgeCase;
import com.oj.cs.model.entity.Question;
import com.oj.cs.model.entity.QuestionSubmit;
import com.oj.cs.model.enums.JudgeInfoMessageEnum;
import com.oj.cs.model.enums.QuestionSubmitStatusEnum;
import com.oj.cs.service.CompileCacheService;
import com.oj.cs.service.QuestionService;
import com.oj.cs.service.QuestionSubmitService;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JudgeServiceImpl implements JudgeService {

  @Resource private QuestionService questionService;

  @Resource private QuestionSubmitService questionSubmitService;

  @Resource private JudgeManager judgeManager;

  @Resource private CompileCacheService compileCacheService;

  @Value("${codesandbox.type:example}")
  private String type;

  @Value("${compile.cache.enabled:true}")
  private boolean cacheEnabled;

  @Override
  public QuestionSubmit doJudge(long questionSubmitId) {
    // 1）传入题目的提交 id，获取到对应的题目、提交信息（包含代码、编程语言等）
    QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
    if (questionSubmit == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交信息不存在");
    }
    Long questionId = questionSubmit.getQuestionId();
    Question question = questionService.getById(questionId);
    if (question == null) {
      throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
    }
    // 2）如果题目提交状态不为等待中，就不用重复执行了
    if (!questionSubmit.getStatus().equals(QuestionSubmitStatusEnum.WAITING.getValue())) {
      throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目正在判题中");
    }
    // 3）更改判题（题目提交）的状态为 “判题中”，防止重复执行
    QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
    questionSubmitUpdate.setId(questionSubmitId);
    questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
    boolean update = questionSubmitService.updateById(questionSubmitUpdate);
    if (!update) {
      throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
    }
    // 4）调用沙箱，获取到执行结果
    CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
    codeSandbox = new CodeSandboxProxy(codeSandbox);
    String language = questionSubmit.getLanguage();
    String code = questionSubmit.getCode();

    // 检查编译缓存（仅用于日志记录，实际缓存需要在沙箱层面实现）
    if (cacheEnabled) {
      String codeHash = compileCacheService.computeHash(code, language);
      log.debug("代码哈希: {}, 语言: {}", codeHash, language);
      // 注意: 完整的缓存集成需要沙箱支持返回编译产物
      // 当前仅记录缓存统计信息
      CompileCacheService.CacheStats stats = compileCacheService.getStats();
      log.debug("编译缓存统计: 条目数={}, 总大小={} bytes", stats.getTotalEntries(), stats.getTotalSize());
    }

    // 获取输入用例
    String judgeCaseStr = question.getJudgeCase();
    List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
    List<String> inputList =
        judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());
    ExecuteCodeRequest executeCodeRequest =
        ExecuteCodeRequest.builder().code(code).language(language).inputList(inputList).build();

    ExecuteCodeResponse executeCodeResponse;
    List<String> outputList;
    JudgeInfo judgeInfo;

    try {
      // 调用代码沙箱执行代码
      executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
      outputList = executeCodeResponse.getOutputList();

      // 检查代码沙箱执行状态
      Integer sandboxStatus = executeCodeResponse.getStatus();
      if (sandboxStatus == null || !sandboxStatus.equals(3)) { // 3 表示 SUCCEED
        // 代码沙箱执行失败，直接设置相应的判题结果
        judgeInfo = new JudgeInfo();
        judgeInfo.setMemory(0L);
        judgeInfo.setTime(0L);

        // 根据沙箱状态设置相应的判题信息
        switch (sandboxStatus != null ? sandboxStatus : 10) {
          case 4: // FAILED
            judgeInfo.setMessage(JudgeInfoMessageEnum.RUNTIME_ERROR.getValue());
            break;
          case 5: // TIME_LIMIT_EXCEEDED
            judgeInfo.setMessage(JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED.getValue());
            break;
          case 6: // MEMORY_LIMIT_EXCEEDED
            judgeInfo.setMessage(JudgeInfoMessageEnum.MEMORY_LIMIT_EXCEEDED.getValue());
            break;
          case 9: // COMPILE_ERROR
            judgeInfo.setMessage(JudgeInfoMessageEnum.COMPILE_ERROR.getValue());
            break;
          default: // SYSTEM_ERROR 或其他
            judgeInfo.setMessage(JudgeInfoMessageEnum.SYSTEM_ERROR.getValue());
            break;
        }
      } else {
        // 5）代码沙箱执行成功，根据沙箱的执行结果进行判题
        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setJudgeInfo(executeCodeResponse.getJudgeInfo());
        judgeContext.setInputList(inputList);
        judgeContext.setOutputList(outputList);
        judgeContext.setJudgeCaseList(judgeCaseList);
        judgeContext.setQuestion(question);
        judgeContext.setQuestionSubmit(questionSubmit);
        judgeInfo = judgeManager.doJudge(judgeContext);
      }

      // 6）修改数据库中的判题结果
      questionSubmitUpdate = new QuestionSubmit();
      questionSubmitUpdate.setId(questionSubmitId);
      // 根据判题结果设置状态
      if (JudgeInfoMessageEnum.ACCEPTED.getValue().equals(judgeInfo.getMessage())) {
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
      } else {
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.FAILED.getValue());
      }
      questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
      update = questionSubmitService.updateById(questionSubmitUpdate);
      if (!update) {
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
      }
    } catch (Exception e) {
      // 代码沙箱执行失败，设置判题失败状态
      judgeInfo = new JudgeInfo();
      judgeInfo.setMessage(JudgeInfoMessageEnum.SYSTEM_ERROR.getValue());
      judgeInfo.setMemory(0L);
      judgeInfo.setTime(0L);

      questionSubmitUpdate = new QuestionSubmit();
      questionSubmitUpdate.setId(questionSubmitId);
      questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.FAILED.getValue());
      questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
      boolean failedUpdate = questionSubmitService.updateById(questionSubmitUpdate);
      if (!failedUpdate) {
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
      }
      throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码执行失败: " + e.getMessage());
    }

    // 7）更新题目的提交数和通过数
    Question questionUpdate = new Question();
    questionUpdate.setId(questionId);
    // 无论判题结果如何，提交数都要增加
    questionUpdate.setSubmitNum(question.getSubmitNum() + 1);
    // 只有在判题通过时才增加通过数
    if (JudgeInfoMessageEnum.ACCEPTED.getValue().equals(judgeInfo.getMessage())) {
      questionUpdate.setAcceptedNum(question.getAcceptedNum() + 1);
    }
    boolean questionUpdateResult = questionService.updateById(questionUpdate);
    if (!questionUpdateResult) {
      throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目统计信息更新错误");
    }

    QuestionSubmit questionSubmitResult = questionSubmitService.getById(questionSubmitId);
    return questionSubmitResult;
  }
}
