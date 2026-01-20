package com.oj.cs.judge.codesandbox;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.oj.cs.judge.codesandbox.impl.ExampleCodeSandbox;
import com.oj.cs.judge.codesandbox.impl.RemoteCodeSandbox;
import com.oj.cs.judge.codesandbox.impl.ThirdPartyCodeSandbox;
import com.oj.cs.model.enums.CodeSandboxTypeEnum;

import lombok.extern.slf4j.Slf4j;

/** 代码沙箱工厂（使用枚举 + Spring依赖注入） */
@Slf4j
@Component
public class CodeSandboxFactory {

  /** Spring自动注入所有CodeSandbox类型的Bean key: Bean名称，value: 对应的沙箱实例 */
  private final Map<String, CodeSandbox> codeSandboxMap;

  @Autowired
  public CodeSandboxFactory(Map<String, CodeSandbox> codeSandboxMap) {
    this.codeSandboxMap = codeSandboxMap;
    log.info("代码沙箱工厂初始化，注册沙箱: {}", codeSandboxMap.keySet());
  }

  /**
   * 根据类型获取代码沙箱实例
   *
   * @param type 沙箱类型字符串
   * @return 代码沙箱实例
   */
  public CodeSandbox getCodeSandbox(String type) {
    CodeSandboxTypeEnum typeEnum = CodeSandboxTypeEnum.fromValue(type);
    return getCodeSandbox(typeEnum);
  }

  /**
   * 根据枚举获取代码沙箱实例
   *
   * @param typeEnum 沙箱类型枚举
   * @return 代码沙箱实例
   */
  public CodeSandbox getCodeSandbox(CodeSandboxTypeEnum typeEnum) {
    if (typeEnum == null) {
      return getDefaultCodeSandbox();
    }

    // 根据枚举值获取对应的Bean名称
    String beanName = getBeanNameByType(typeEnum);
    CodeSandbox codeSandbox = codeSandboxMap.get(beanName);

    if (codeSandbox == null) {
      log.warn("未找到沙箱类型 {} 对应的实例，使用默认沙箱", typeEnum);
      return getDefaultCodeSandbox();
    }

    return codeSandbox;
  }

  /**
   * 获取默认的代码沙箱
   *
   * @return 默认代码沙箱实例
   */
  private CodeSandbox getDefaultCodeSandbox() {
    // 优先使用远程沙箱
    CodeSandbox codeSandbox = codeSandboxMap.get("remoteCodeSandbox");
    if (codeSandbox != null) {
      return codeSandbox;
    }
    // 其次使用示例沙箱
    codeSandbox = codeSandboxMap.get("exampleCodeSandbox");
    if (codeSandbox != null) {
      return codeSandbox;
    }
    throw new IllegalStateException("没有可用的代码沙箱实例");
  }

  /**
   * 根据枚举类型获取对应的Bean名称
   *
   * @param typeEnum 沙箱类型枚举
   * @return Bean名称
   */
  private String getBeanNameByType(CodeSandboxTypeEnum typeEnum) {
    switch (typeEnum) {
      case EXAMPLE:
        return "exampleCodeSandbox";
      case REMOTE:
        return "remoteCodeSandbox";
      case THIRD_PARTY:
        return "thirdPartyCodeSandbox";
      default:
        return "remoteCodeSandbox";
    }
  }

  /**
   * 静态方法：创建代码沙箱实例（向后兼容）
   *
   * @param type 沙箱类型字符串
   * @return 代码沙箱实例
   * @deprecated 建议使用注入的CodeSandboxFactory实例调用getCodeSandbox()
   */
  @Deprecated
  public static CodeSandbox newInstance(String type) {
    CodeSandboxTypeEnum typeEnum = CodeSandboxTypeEnum.fromValue(type);
    switch (typeEnum) {
      case EXAMPLE:
        return new ExampleCodeSandbox();
      case REMOTE:
        return new RemoteCodeSandbox();
      case THIRD_PARTY:
        return new ThirdPartyCodeSandbox();
      default:
        return new ExampleCodeSandbox();
    }
  }
}
