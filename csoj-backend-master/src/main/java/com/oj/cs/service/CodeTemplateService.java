package com.oj.cs.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/** 代码模板服务 为新手提供各语言的标准输入输出模板 */
@Slf4j
@Service
public class CodeTemplateService {

  /**
   * 获取指定语言的代码模板
   *
   * @param language 编程语言
   * @return 代码模板字符串
   */
  public String getTemplate(String language) {
    if (language == null || language.isEmpty()) {
      return getDefaultTemplate();
    }

    CodeTemplate template = CodeTemplate.fromLanguage(language);
    if (template != null) {
      return template.getCode();
    }

    log.warn("未找到语言 {} 的模板，使用默认模板", language);
    return getDefaultTemplate();
  }

  /** 获取默认模板（Java） */
  private String getDefaultTemplate() {
    return CodeTemplate.JAVA.getCode();
  }

  /** 获取所有支持的语言列表 */
  public String[] getSupportedLanguages() {
    return CodeTemplate.getSupportedLanguages();
  }

  /** 检查语言是否支持 */
  public boolean isSupported(String language) {
    return CodeTemplate.fromLanguage(language) != null;
  }

  /** 代码模板枚举 */
  private enum CodeTemplate {
    JAVA(
        "java",
        "// Java标准输入输出模板\n"
            + "import java.util.Scanner;\n"
            + "\n"
            + "public class Main {\n"
            + "    public static void main(String[] args) {\n"
            + "        Scanner scanner = new Scanner(System.in);\n"
            + "        \n"
            + "        // 读取输入\n"
            + "        while (scanner.hasNext()) {\n"
            + "            String line = scanner.nextLine();\n"
            + "            // 处理输入并输出结果\n"
            + "            System.out.println(process(line));\n"
            + "        }\n"
            + "        \n"
            + "        scanner.close();\n"
            + "    }\n"
            + "    \n"
            + "    private static String process(String input) {\n"
            + "        // 在这里编写你的处理逻辑\n"
            + "        return input;\n"
            + "    }\n"
            + "}\n"),

    PYTHON(
        "python",
        "# Python标准输入输出模板\n"
            + "import sys\n"
            + "\n"
            + "def process(line):\n"
            + "    # 在这里编写你的处理逻辑\n"
            + "    return line\n"
            + "\n"
            + "def main():\n"
            + "    # 读取所有输入\n"
            + "    for line in sys.stdin:\n"
            + "        line = line.strip()\n"
            + "        if line:\n"
            + "            # 处理并输出结果\n"
            + "            print(process(line))\n"
            + "\n"
            + "if __name__ == \"__main__\":\n"
            + "    main()\n"),

    CPP(
        "cpp",
        "// C++标准输入输出模板\n"
            + "#include <iostream>\n"
            + "#include <string>\n"
            + "using namespace std;\n"
            + "\n"
            + "string process(string input) {\n"
            + "    // 在这里编写你的处理逻辑\n"
            + "    return input;\n"
            + "}\n"
            + "\n"
            + "int main() {\n"
            + "    string line;\n"
            + "    // 逐行读取输入\n"
            + "    while (getline(cin, line)) {\n"
            + "        // 处理并输出结果\n"
            + "        cout << process(line) << endl;\n"
            + "    }\n"
            + "    return 0;\n"
            + "}\n"),

    C(
        "c",
        "// C标准输入输出模板\n"
            + "#include <stdio.h>\n"
            + "#include <string.h>\n"
            + "\n"
            + "#define MAX_LEN 1000\n"
            + "\n"
            + "void process(char* input) {\n"
            + "    // 在这里编写你的处理逻辑\n"
            + "    printf(\"%s\\n\", input);\n"
            + "}\n"
            + "\n"
            + "int main() {\n"
            + "    char line[MAX_LEN];\n"
            + "    // 逐行读取输入\n"
            + "    while (fgets(line, MAX_LEN, stdin) != NULL) {\n"
            + "        // 移除换行符\n"
            + "        line[strcspn(line, \"\\n\")] = 0;\n"
            + "        // 处理并输出结果\n"
            + "        process(line);\n"
            + "    }\n"
            + "    return 0;\n"
            + "}\n"),

    JAVASCRIPT(
        "javascript",
        "// JavaScript(Node.js)标准输入输出模板\n"
            + "const readline = require('readline');\n"
            + "\n"
            + "const rl = readline.createInterface({\n"
            + "    input: process.stdin,\n"
            + "    output: process.stdout\n"
            + "});\n"
            + "\n"
            + "function process(input) {\n"
            + "    // 在这里编写你的处理逻辑\n"
            + "    return input;\n"
            + "}\n"
            + "\n"
            + "rl.on('line', (line) => {\n"
            + "    // 处理并输出结果\n"
            + "    console.log(process(line.trim()));\n"
            + "});\n"),

    GO(
        "go",
        "// Go标准输入输出模板\n"
            + "package main\n"
            + "\n"
            + "import (\n"
            + "    \"bufio\"\n"
            + "    \"fmt\"\n"
            + "    \"os\"\n"
            + ")\n"
            + "\n"
            + "func process(input string) string {\n"
            + "    // 在这里编写你的处理逻辑\n"
            + "    return input\n"
            + "}\n"
            + "\n"
            + "func main() {\n"
            + "    scanner := bufio.NewScanner(os.Stdin)\n"
            + "    for scanner.Scan() {\n"
            + "        line := scanner.Text()\n"
            + "        // 处理并输出结果\n"
            + "        fmt.Println(process(line))\n"
            + "    }\n"
            + "}\n"),

    CSHARP(
        "csharp",
        "// C#标准输入输出模板\n"
            + "using System;\n"
            + "\n"
            + "class Program\n"
            + "{\n"
            + "    static string Process(string input)\n"
            + "    {\n"
            + "        // 在这里编写你的处理逻辑\n"
            + "        return input;\n"
            + "    }\n"
            + "    \n"
            + "    static void Main()\n"
            + "    {\n"
            + "        string line;\n"
            + "        while ((line = Console.ReadLine()) != null)\n"
            + "        {\n"
            + "            // 处理并输出结果\n"
            + "            Console.WriteLine(Process(line));\n"
            + "        }\n"
            + "    }\n"
            + "}\n");

    private final String language;
    private final String code;

    CodeTemplate(String language, String code) {
      this.language = language;
      this.code = code;
    }

    public String getLanguage() {
      return language;
    }

    public String getCode() {
      return code;
    }

    private static final Map<String, CodeTemplate> TEMPLATE_MAP = new HashMap<>();

    static {
      for (CodeTemplate template : values()) {
        TEMPLATE_MAP.put(template.language, template);
      }
    }

    public static CodeTemplate fromLanguage(String language) {
      return TEMPLATE_MAP.get(language.toLowerCase());
    }

    public static String[] getSupportedLanguages() {
      return TEMPLATE_MAP.keySet().toArray(new String[0]);
    }
  }
}
