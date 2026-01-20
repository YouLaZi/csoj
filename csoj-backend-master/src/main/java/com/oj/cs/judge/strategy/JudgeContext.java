package com.oj.cs.judge.strategy;

import java.util.List;

import com.oj.cs.judge.codesandbox.model.JudgeInfo;
import com.oj.cs.model.dto.question.JudgeCase;
import com.oj.cs.model.entity.Question;
import com.oj.cs.model.entity.QuestionSubmit;

import lombok.Data;

/** 上下文（用于定义在策略中传递的参数） */
@Data
public class JudgeContext {

  private JudgeInfo judgeInfo;

  private List<String> inputList;

  private List<String> outputList;

  private List<JudgeCase> judgeCaseList;

  private Question question;

  private QuestionSubmit questionSubmit;
}
