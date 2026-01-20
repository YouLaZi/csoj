package com.oj.cs.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/** 常见错误库服务 为新手提供常见错误的中文解释和解决方案 */
@Slf4j
@Service
public class CommonErrorLibraryService {

  /** 错误知识库 */
  private final Map<String, List<ErrorInfo>> errorLibrary = new ConcurrentHashMap<>();

  @PostConstruct
  public void init() {
    // 初始化Java常见错误
    initJavaErrors();

    // 初始化Python常见错误
    initPythonErrors();

    // 初始化C++常见错误
    initCppErrors();

    // 初始化C常见错误
    initCErrors();

    // 初始化C#常见错误
    initCSharpErrors();

    // 初始化JavaScript常见错误
    initJavaScriptErrors();

    // 初始化Go常见错误
    initGoErrors();

    // 初始化Ruby常见错误
    initRubyErrors();

    // 初始化Swift常见错误
    initSwiftErrors();

    log.info("常见错误库初始化完成，共加载 {} 种错误类型", errorLibrary.size());
  }

  /**
   * 根据错误消息获取错误解释
   *
   * @param errorMessage 错误消息
   * @param language 编程语言
   * @return 错误解释列表（可能匹配多个）
   */
  public List<ErrorInfo> getErrorExplanation(String errorMessage, String language) {
    if (errorMessage == null || errorMessage.isEmpty()) {
      return new ArrayList<>();
    }

    String key = language.toLowerCase();
    List<ErrorInfo> errors = errorLibrary.get(key);

    if (errors == null || errors.isEmpty()) {
      return new ArrayList<>();
    }

    // 匹配相关的错误
    List<ErrorInfo> matchedErrors = new ArrayList<>();
    String lowerErrorMessage = errorMessage.toLowerCase();

    for (ErrorInfo error : errors) {
      // 检查错误消息是否包含关键词
      if (matchesError(lowerErrorMessage, error)) {
        matchedErrors.add(error);
      }
    }

    return matchedErrors;
  }

  /** 匹配错误 */
  private boolean matchesError(String errorMessage, ErrorInfo error) {
    for (String keyword : error.getKeywords()) {
      if (errorMessage.contains(keyword.toLowerCase())) {
        return true;
      }
    }
    return false;
  }

  /** 初始化Java常见错误 */
  private void initJavaErrors() {
    List<ErrorInfo> javaErrors = new ArrayList<>();

    // 编译错误
    javaErrors.add(
        new ErrorInfo(
            "class, interface, or enum expected",
            ErrorType.COMPILE,
            "类定义有误",
            "通常是因为缺少或多余的大括号 {}，或者类名与文件名不匹配。",
            "检查所有大括号是否配对，确保类名与文件名一致（public类）。"));

    javaErrors.add(
        new ErrorInfo("';' expected", ErrorType.COMPILE, "缺少分号", "语句末尾缺少分号。", "在上一行代码末尾添加分号 ;"));

    javaErrors.add(
        new ErrorInfo(
            "cannot find symbol",
            ErrorType.COMPILE,
            "找不到符号",
            "使用了未定义的变量、方法或类。",
            "1. 检查变量名/方法名拼写是否正确\n2. 确认变量/方法已定义\n3. 检查导入的包是否正确"));

    javaErrors.add(
        new ErrorInfo(
            "incompatible types",
            ErrorType.COMPILE,
            "类型不兼容",
            "尝试将不兼容的类型进行赋值或转换。",
            "检查变量类型是否匹配，必要时进行类型转换。"));

    // 运行时错误
    javaErrors.add(
        new ErrorInfo(
            "NullPointerException",
            ErrorType.RUNTIME,
            "空指针异常",
            "尝试使用一个空（null）对象引用。",
            "在使用对象前先判断是否为null，或确保对象已正确初始化。"));

    javaErrors.add(
        new ErrorInfo(
            "ArrayIndexOutOfBoundsException",
            ErrorType.RUNTIME,
            "数组越界",
            "访问数组时使用的索引超出了数组范围。",
            "检查数组索引是否在0到array.length-1范围内。"));

    javaErrors.add(
        new ErrorInfo(
            "NumberFormatException",
            ErrorType.RUNTIME,
            "数字格式异常",
            "尝试将字符串转换为数字时，字符串格式不正确。",
            "确保字符串是有效的数字格式，或使用try-catch处理异常。"));

    errorLibrary.put("java", javaErrors);
  }

  /** 初始化Python常见错误 */
  private void initPythonErrors() {
    List<ErrorInfo> pythonErrors = new ArrayList<>();

    pythonErrors.add(
        new ErrorInfo(
            "IndentationError",
            ErrorType.COMPILE,
            "缩进错误",
            "Python对缩进要求严格，缩进不正确会导致此错误。",
            "检查代码缩进是否一致（建议使用4个空格）。"));

    pythonErrors.add(
        new ErrorInfo(
            "NameError", ErrorType.RUNTIME, "变量未定义", "使用了未定义的变量。", "检查变量名拼写，确保变量已先定义后使用。"));

    pythonErrors.add(
        new ErrorInfo(
            "IndexError",
            ErrorType.RUNTIME,
            "索引越界",
            "访问列表/元组时使用的索引超出范围。",
            "检查索引是否在0到len(list)-1范围内。"));

    pythonErrors.add(
        new ErrorInfo(
            "KeyError",
            ErrorType.RUNTIME,
            "字典键不存在",
            "访问字典时使用了不存在的键。",
            "使用dict.get(key, default)方法避免错误，或先检查键是否存在。"));

    pythonErrors.add(
        new ErrorInfo(
            "TypeError",
            ErrorType.RUNTIME,
            "类型错误",
            "操作或函数应用于错误的数据类型。",
            "检查数据类型是否匹配，使用type()查看变量类型。"));

    errorLibrary.put("python", pythonErrors);
  }

  /** 初始化C++常见错误 */
  private void initCppErrors() {
    List<ErrorInfo> cppErrors = new ArrayList<>();

    cppErrors.add(
        new ErrorInfo(
            "expected", ErrorType.COMPILE, "语法错误", "代码语法不正确，通常缺少分号或括号。", "检查上一行代码是否缺少分号或其他语法错误。"));

    cppErrors.add(
        new ErrorInfo(
            "undefined reference to",
            ErrorType.LINK,
            "未定义引用",
            "链接时找不到函数或变量的定义。",
            "确保函数已实现，检查链接的库文件是否完整。"));

    cppErrors.add(
        new ErrorInfo(
            "segmentation fault",
            ErrorType.RUNTIME,
            "段错误",
            "程序访问了不允许访问的内存区域。",
            "检查指针是否正确初始化，数组是否越界，空指针是否判断。"));

    errorLibrary.put("cpp", cppErrors);
    errorLibrary.put("c", cppErrors);
  }

  /** 初始化C常见错误 */
  private void initCErrors() {
    List<ErrorInfo> cErrors = new ArrayList<>();

    cErrors.add(
        new ErrorInfo(
            "expected.*before",
            ErrorType.COMPILE,
            "语法错误",
            "代码语法不正确，通常缺少分号、括号或大括号。",
            "检查缺少的符号（;{}）是否补全。"));

    cErrors.add(
        new ErrorInfo(
            "conflicting types",
            ErrorType.COMPILE,
            "类型冲突",
            "函数或变量的声明与定义类型不一致。",
            "检查头文件中的声明与源文件中的定义是否一致。"));

    cErrors.add(
        new ErrorInfo(
            "segmentation fault",
            ErrorType.RUNTIME,
            "段错误",
            "程序访问了不允许访问的内存区域。",
            "检查指针是否正确初始化，数组是否越界，空指针是否判断。"));

    errorLibrary.put("c", cErrors);
  }

  /** 初始化C#常见错误 */
  private void initCSharpErrors() {
    List<ErrorInfo> csharpErrors = new ArrayList<>();

    csharpErrors.add(
        new ErrorInfo(
            "CS0246|cannot find type",
            ErrorType.COMPILE,
            "找不到类型或命名空间",
            "使用了未定义的类或命名空间。",
            "1. 检查类名拼写\n2. 确保已添加必要的 using 引用\n3. 检查引用的程序集是否正确"));

    csharpErrors.add(
        new ErrorInfo(
            "CS0165|use of unassigned local variable",
            ErrorType.COMPILE,
            "使用了未赋值的局部变量",
            "使用了未初始化的局部变量。",
            "在使用变量前先赋值。"));

    csharpErrors.add(
        new ErrorInfo(
            "NullReferenceException",
            ErrorType.RUNTIME,
            "空引用异常",
            "尝试使用空（null）对象引用。",
            "在使用对象前先判断是否为null。"));

    csharpErrors.add(
        new ErrorInfo(
            "IndexOutOfRangeException",
            ErrorType.RUNTIME,
            "索引越界",
            "访问数组时使用的索引超出了数组范围。",
            "检查数组索引是否在0到array.Length-1范围内。"));

    errorLibrary.put("csharp", csharpErrors);
  }

  /** 初始化JavaScript常见错误 */
  private void initJavaScriptErrors() {
    List<ErrorInfo> jsErrors = new ArrayList<>();

    jsErrors.add(
        new ErrorInfo(
            "SyntaxError|Unexpected token",
            ErrorType.COMPILE,
            "语法错误",
            "代码语法不正确。",
            "检查是否缺少括号、大括号、引号等。"));

    jsErrors.add(
        new ErrorInfo(
            "ReferenceError|is not defined",
            ErrorType.RUNTIME,
            "引用错误",
            "使用了未定义的变量或函数。",
            "检查变量名拼写，确保变量已先定义后使用。"));

    jsErrors.add(
        new ErrorInfo(
            "TypeError|is not a function",
            ErrorType.RUNTIME,
            "类型错误",
            "尝试调用不是函数的对象。",
            "检查对象是否真的是函数，或函数名是否拼写正确。"));

    jsErrors.add(
        new ErrorInfo(
            "TypeError|Cannot read property|undefined",
            ErrorType.RUNTIME,
            "属性读取错误",
            "尝试读取undefined或null对象的属性。",
            "在使用对象属性前先判断对象是否存在。"));

    jsErrors.add(
        new ErrorInfo(
            "RangeError|Maximum call stack size exceeded",
            ErrorType.RUNTIME,
            "栈溢出",
            "递归调用没有正确的终止条件。",
            "检查递归函数的终止条件是否正确。"));

    errorLibrary.put("javascript", jsErrors);
  }

  /** 初始化Go常见错误 */
  private void initGoErrors() {
    List<ErrorInfo> goErrors = new ArrayList<>();

    goErrors.add(
        new ErrorInfo(
            "syntax error|unexpected",
            ErrorType.COMPILE,
            "语法错误",
            "代码语法不正确。",
            "检查缺少的符号（如大括号、分号）是否补全。"));

    goErrors.add(
        new ErrorInfo(
            "undefined.*package|cannot find package",
            ErrorType.COMPILE,
            "包未定义",
            "导入了不存在的包。",
            "检查包名拼写，确保包已安装（使用go get）。"));

    goErrors.add(
        new ErrorInfo(
            "panic|runtime error", ErrorType.RUNTIME, "运行时错误", "程序运行时发生严重错误。", "检查数组越界、空指针访问等问题。"));

    goErrors.add(
        new ErrorInfo(
            "invalid memory address|nil pointer",
            ErrorType.RUNTIME,
            "空指针引用",
            "尝试访问空指针。",
            "在使用指针前先判断是否为nil。"));

    goErrors.add(
        new ErrorInfo(
            "index out of range",
            ErrorType.RUNTIME,
            "索引越界",
            "访问切片或数组时索引超出范围。",
            "检查索引是否在0到len-1范围内。"));

    errorLibrary.put("go", goErrors);
  }

  /** 初始化Ruby常见错误 */
  private void initRubyErrors() {
    List<ErrorInfo> rubyErrors = new ArrayList<>();

    rubyErrors.add(
        new ErrorInfo(
            "syntax error|unexpected",
            ErrorType.COMPILE,
            "语法错误",
            "代码语法不正确。",
            "检查是否缺少关键字end，或缺少逗号、括号等。"));

    rubyErrors.add(
        new ErrorInfo(
            "undefined.*method|undefined local variable",
            ErrorType.RUNTIME,
            "方法或变量未定义",
            "使用了未定义的方法或变量。",
            "检查方法名拼写，确保方法已定义。"));

    rubyErrors.add(
        new ErrorInfo(
            "NoMethodError|undefined method",
            ErrorType.RUNTIME,
            "方法未定义",
            "调用了对象上不存在的方法。",
            "检查对象类型是否正确，方法名是否拼写正确。"));

    rubyErrors.add(
        new ErrorInfo(
            "NameError|undefined local variable",
            ErrorType.RUNTIME,
            "变量未定义",
            "使用了未定义的局部变量。",
            "检查变量名拼写，确保变量已先赋值。"));

    rubyErrors.add(
        new ErrorInfo(
            "TypeError|no implicit conversion",
            ErrorType.RUNTIME,
            "类型转换错误",
            "尝试进行不支持的类型转换。",
            "检查数据类型，使用to_s、to_i等方法进行显式转换。"));

    errorLibrary.put("ruby", rubyErrors);
  }

  /** 初始化Swift常见错误 */
  private void initSwiftErrors() {
    List<ErrorInfo> swiftErrors = new ArrayList<>();

    swiftErrors.add(
        new ErrorInfo(
            "syntax error|expected",
            ErrorType.COMPILE,
            "语法错误",
            "代码语法不正确。",
            "检查缺少的符号（如括号、大括号）是否补全。"));

    swiftErrors.add(
        new ErrorInfo(
            "use of unresolved identifier",
            ErrorType.COMPILE,
            "未定义的标识符",
            "使用了未定义的变量、函数或类型。",
            "检查名称拼写，确保已正确导入模块。"));

    swiftErrors.add(
        new ErrorInfo(
            "Unexpectedly found nil|fatal error",
            ErrorType.RUNTIME,
            "空值解包错误",
            "尝试解包一个nil的可选值。",
            "使用if let或guard let进行安全的可选值解包。"));

    swiftErrors.add(
        new ErrorInfo(
            "Index out of range",
            ErrorType.RUNTIME,
            "索引越界",
            "访问数组时索引超出范围。",
            "检查索引是否在0到array.count-1范围内。"));

    swiftErrors.add(
        new ErrorInfo(
            "Type.*has no member|Value of type.*has no member",
            ErrorType.COMPILE,
            "类型成员不存在",
            "访问了类型上不存在的属性或方法。",
            "检查类型是否正确，确认该类型是否有该成员。"));

    errorLibrary.put("swift", swiftErrors);
  }

  /** 错误类型枚举 */
  public enum ErrorType {
    /** 编译错误 */
    COMPILE,
    /** 链接错误 */
    LINK,
    /** 运行时错误 */
    RUNTIME
  }

  /** 错误信息类 */
  @Data
  public static class ErrorInfo {
    /** 关键词列表（用于匹配） */
    private final List<String> keywords;

    /** 错误类型 */
    private final ErrorType errorType;

    /** 错误标题（中文） */
    private final String title;

    /** 错误原因（中文） */
    private final String reason;

    /** 解决方案（中文） */
    private final String solution;

    public ErrorInfo(
        String keyword, ErrorType errorType, String title, String reason, String solution) {
      this.keywords = java.util.Arrays.asList(keyword.split("[,|]"));
      this.errorType = errorType;
      this.title = title;
      this.reason = reason;
      this.solution = solution;
    }
  }
}
