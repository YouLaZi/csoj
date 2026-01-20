package com.oj.cs.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

/** 代码模板服务测试 */
@ExtendWith(MockitoExtension.class)
class CodeTemplateServiceTest {

  @InjectMocks private CodeTemplateService codeTemplateService;

  @Test
  void testGetJavaTemplate() {
    String template = codeTemplateService.getTemplate("java");

    assertNotNull(template);
    assertTrue(template.contains("public class Main"));
    assertTrue(template.contains("Scanner"));
    assertTrue(template.contains("public static void main"));
  }

  @Test
  void testGetPythonTemplate() {
    String template = codeTemplateService.getTemplate("python");

    assertNotNull(template);
    assertTrue(template.contains("import sys"));
    assertTrue(template.contains("def main()"));
    assertTrue(template.contains("if __name__"));
  }

  @Test
  void testGetCppTemplate() {
    String template = codeTemplateService.getTemplate("cpp");

    assertNotNull(template);
    assertTrue(template.contains("#include"));
    assertTrue(template.contains("using namespace std"));
    assertTrue(template.contains("int main()"));
  }

  @Test
  void testGetUnsupportedLanguage() {
    String template = codeTemplateService.getTemplate("rust");

    // 不支持的语言应返回默认模板（Java）
    assertNotNull(template);
    assertTrue(template.contains("public class Main"));
  }

  @Test
  void testGetNullLanguage() {
    String template = codeTemplateService.getTemplate(null);

    // null 语言应返回默认模板（Java）
    assertNotNull(template);
    assertTrue(template.contains("public class Main"));
  }

  @Test
  void testGetEmptyLanguage() {
    String template = codeTemplateService.getTemplate("");

    // 空语言应返回默认模板（Java）
    assertNotNull(template);
    assertTrue(template.contains("public class Main"));
  }

  @Test
  void testIsSupported() {
    assertTrue(codeTemplateService.isSupported("java"));
    assertTrue(codeTemplateService.isSupported("python"));
    assertTrue(codeTemplateService.isSupported("cpp"));
    assertFalse(codeTemplateService.isSupported("rust"));
  }

  @Test
  void testGetSupportedLanguages() {
    String[] languages = codeTemplateService.getSupportedLanguages();

    assertNotNull(languages);
    assertTrue(languages.length > 0);
    assertTrue(java.util.Arrays.asList(languages).contains("java"));
  }
}
