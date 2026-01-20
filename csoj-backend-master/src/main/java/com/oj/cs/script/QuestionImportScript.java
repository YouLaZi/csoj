package com.oj.cs.script;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oj.cs.model.dto.question.QuestionAddRequest;
import com.oj.cs.model.dto.question.QuestionBatchAddRequest;

/** 题目批量导入脚本 */
public class QuestionImportScript {

  public static void main(String[] args) {
    try {
      // 读取JSON文件
      ObjectMapper mapper = new ObjectMapper();
      QuestionBatchAddRequest batchRequest =
          mapper.readValue(
              new File("src/main/resources/question_batch_import.json"),
              QuestionBatchAddRequest.class);

      List<QuestionAddRequest> questions = batchRequest.getQuestionList();
      System.out.println("成功读取 " + questions.size() + " 道题目");

      // TODO: 调用QuestionController的批量添加接口
      // 这里需要启动Spring容器才能注入Controller，或者直接使用HTTP客户端调用接口

    } catch (IOException e) {
      System.err.println("读取题目文件失败：" + e.getMessage());
      e.printStackTrace();
    }
  }
}
