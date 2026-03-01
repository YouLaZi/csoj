package com.oj.cs.judge.codesandbox;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import com.oj.cs.judge.codesandbox.model.ExecuteCodeRequest;
import com.oj.cs.judge.codesandbox.model.ExecuteCodeResponse;
import com.oj.cs.model.enums.QuestionSubmitLanguageEnum;

@SpringBootTest
class CodeSandboxTest {

  @Value("${codesandbox.type:example}")
  private String type;

  @Autowired private CodeSandboxFactory codeSandboxFactory;

  @Test
  void executeCode() {
    CodeSandbox codeSandbox = codeSandboxFactory.getCodeSandbox(type);
    String code = "int main() { }";
    String language = QuestionSubmitLanguageEnum.JAVA.getValue();
    List<String> inputList = Arrays.asList("1 2", "3 4");
    ExecuteCodeRequest executeCodeRequest =
        ExecuteCodeRequest.builder().code(code).language(language).inputList(inputList).build();
    ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
    Assertions.assertNotNull(executeCodeResponse);
  }

  @Test
  void executeCodeByValue() {
    CodeSandbox codeSandbox = codeSandboxFactory.getCodeSandbox(type);
    String code = "int main() { }";
    String language = QuestionSubmitLanguageEnum.JAVA.getValue();
    List<String> inputList = Arrays.asList("1 2", "3 4");
    ExecuteCodeRequest executeCodeRequest =
        ExecuteCodeRequest.builder().code(code).language(language).inputList(inputList).build();
    ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
    Assertions.assertNotNull(executeCodeResponse);
  }

  @Test
  void executeCodeByProxy() {
    CodeSandbox codeSandbox = codeSandboxFactory.getCodeSandbox(type);
    codeSandbox = new CodeSandboxProxy(codeSandbox);
    String code =
        "public class Main {\n"
            + "    public static void main(String[] args) {\n"
            + "        int a = Integer.parseInt(args[0]);\n"
            + "        int b = Integer.parseInt(args[1]);\n"
            + "        System.out.println(\"结果:\" + (a + b));\n"
            + "    }\n"
            + "}";
    String language = QuestionSubmitLanguageEnum.JAVA.getValue();
    List<String> inputList = Arrays.asList("1 2", "3 4");
    ExecuteCodeRequest executeCodeRequest =
        ExecuteCodeRequest.builder().code(code).language(language).inputList(inputList).build();
    ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
    Assertions.assertNotNull(executeCodeResponse);
  }

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    while (scanner.hasNext()) {
      String type = scanner.next();
      CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
      String code = "int main() { }";
      String language = QuestionSubmitLanguageEnum.JAVA.getValue();
      List<String> inputList = Arrays.asList("1 2", "3 4");
      ExecuteCodeRequest executeCodeRequest =
          ExecuteCodeRequest.builder().code(code).language(language).inputList(inputList).build();
      ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
    }
  }
}
