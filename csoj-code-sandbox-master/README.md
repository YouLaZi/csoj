# CSOJ 代码沙箱技术文档

## 1. 项目简介

CSOJ 代码沙箱是一个用于在线判题系统（Online Judge, OJ）的核心组件，它提供了一个安全的环境来执行用户提交的各种编程语言代码，并返回执行结果。本项目旨在支持多种编程语言，并提供可扩展的架构以便未来轻松添加对新语言的支持。

## 2. 核心架构

项目主要采用了以下设计模式：

*   **策略模式 (Strategy Pattern)**：针对不同的编程语言，定义了不同的执行策略。每种语言的编译、运行和结果处理逻辑被封装在各自的策略类中（例如 `JavaLanguageStrategy`, `PythonLanguageStrategy` 等）。这使得添加新的语言支持变得简单，只需实现新的策略接口即可。
*   **工厂模式 (Factory Pattern)**：通过 `CodeSandboxFactory` 类来创建和管理不同语言的执行策略实例。客户端代码通过语言名称向工厂请求相应的策略对象，而无需关心具体的策略实现类。

这种设计提高了代码的灵活性和可维护性。

## 3. 关键组件说明

*   `CodeSandboxStrategy` (接口)：定义了代码沙箱执行策略的统一接口，所有特定语言的策略类都实现此接口。其核心方法是 `executeCode(ExecuteCodeRequest executeCodeRequest)`，用于接收代码执行请求并返回执行结果。
*   `LanguageStrategy` 实现类 (例如 `JavaLanguageStrategy`, `PythonLanguageStrategy`, `CppLanguageStrategy` 等)：这些类是 `CodeSandboxStrategy` 接口的具体实现，分别负责处理特定编程语言的代码执行逻辑。它们通常会依赖于更底层的原生代码沙箱实现（例如 `JavaNativeCodeSandbox`, `PythonNativeCodeSandbox`）。
*   `CodeSandboxFactory`：这是一个工厂类，负责根据请求的语言类型，返回相应的 `CodeSandboxStrategy` 实例。它内部维护一个 `Map`，将语言名称映射到对应的策略对象。在应用启动时，通过 `@PostConstruct` 注解的 `init()` 方法，将所有已注册的语言策略加载到 `Map` 中。
*   `ExecuteCodeRequest`：封装了执行代码所需的请求信息，例如源代码、输入参数、语言类型等。
*   `ExecuteCodeResponse`：封装了代码执行后的结果信息，例如输出、错误信息、执行状态、执行时间、消耗内存等。

## 4. 已支持的编程语言

根据 `CodeSandboxFactory.java` 的配置，目前项目支持以下编程语言：

*   Java
*   Python
*   C++ (cpp)
*   C# (csharp)
*   JavaScript (javascript)
*   Go
*   Ruby
*   Swift

## 5. 如何扩展以支持新语言

要为本项目添加对新编程语言的支持，请遵循以下步骤：

1.  **创建原生代码沙箱实现 (可选但推荐)**：
    *   如果需要与操作系统底层交互或执行外部命令来编译和运行代码，建议为新语言创建一个原生的代码沙箱实现类（例如 `NewLanguageNativeCodeSandbox.java`）。这个类将负责具体的代码编译、执行、资源限制等操作。

2.  **创建语言策略类**：
    *   在 `com.cs.ojcodesandbox.strategy` 包下创建一个新的 Java 类，实现 `CodeSandboxStrategy` 接口（例如 `NewLanguageStrategy.java`）。
    *   在该类中，实现 `executeCode` 方法。此方法通常会调用步骤 1 中创建的原生代码沙箱（如果适用），或者直接在此实现代码执行逻辑。
    *   使用 `@Component("newlanguage")` 注解标记该类，其中 `"newlanguage"` 是新语言的小写标识符。

    ```java
    package com.cs.ojcodesandbox.strategy;

    import com.cs.ojcodesandbox.CodeSandboxStrategy;
    import com.cs.ojcodesandbox.NewLanguageNativeCodeSandbox; // 假设存在
    import com.cs.ojcodesandbox.model.ExecuteCodeRequest;
    import com.cs.ojcodesandbox.model.ExecuteCodeResponse;
    import org.springframework.stereotype.Component;
    import javax.annotation.Resource; // 如果使用原生沙箱

    @Component("newlanguage") // Bean name，例如 "kotlin"
    public class NewLanguageStrategy implements CodeSandboxStrategy {

        // @Resource
        // private NewLanguageNativeCodeSandbox newLanguageNativeCodeSandbox; // 如果有原生沙箱

        @Override
        public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
            // 实现新语言的代码执行逻辑
            // 例如: return newLanguageNativeCodeSandbox.executeCode(executeCodeRequest);
            System.out.println("Executing " + executeCodeRequest.getLanguage() + " code...");
            // 模拟执行
            ExecuteCodeResponse response = new ExecuteCodeResponse();
            response.setOutput("Output for new language");
            response.setStatus("Success");
            return response;
        }
    }
    ```

3.  **注册新语言策略到工厂**：
    *   打开 `com.cs.ojcodesandbox.CodeSandboxFactory.java` 文件。
    *   在类中注入新的语言策略 Bean：

        ```java
        @Autowired
        private NewLanguageStrategy newLanguageStrategy;
        ```

    *   在 `init()` 方法中，将新的语言策略添加到 `strategyMap`：

        ```java
        @PostConstruct
        public void init() {
            // ... 其他语言
            strategyMap.put("newlanguage", newLanguageStrategy);
            // ...
        }
        ```

完成以上步骤后，代码沙箱就能够处理新的编程语言请求了。

## 6. 项目结构概览

```
. (项目根目录)
├── pom.xml                   # Maven 项目配置文件
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/cs/ojcodesandbox/
│   │   │       ├── CodeSandbox.java              # (可能存在的) 通用沙箱接口或抽象类
│   │   │       ├── CodeSandboxFactory.java       # 核心工厂类
│   │   │       ├── CodeSandboxStrategy.java      # 策略接口
│   │   │       ├── DockerCodeSandbox.java        # (示例) Docker实现的沙箱
│   │   │       ├── JavaDockerCodeSandbox.java    # (示例) Java的Docker沙箱
│   │   │       ├── JavaNativeCodeSandbox.java    # Java原生沙箱实现
│   │   │       ├── PythonNativeCodeSandbox.java  # Python原生沙箱实现 (待创建)
│   │   │       ├── controller/                 # (如果作为服务) API控制器
│   │   │       │   └── CodeSandboxController.java
│   │   │       ├── model/                      # 数据模型
│   │   │       │   ├── ExecuteCodeRequest.java
│   │   │       │   ├── ExecuteCodeResponse.java
│   │   │       │   └── JudgeInfo.java
│   │   │       ├── strategy/                   # 语言策略实现
│   │   │       │   ├── CppLanguageStrategy.java
│   │   │       │   ├── CSharpLanguageStrategy.java
│   │   │       │   ├── GoLanguageStrategy.java
│   │   │       │   ├── JavaLanguageStrategy.java
│   │   │       │   ├── JavaScriptLanguageStrategy.java
│   │   │       │   ├── PythonLanguageStrategy.java
│   │   │       │   ├── RubyLanguageStrategy.java
│   │   │       │   └── SwiftLanguageStrategy.java
│   │   │       └── utils/                      # 工具类
│   │   │           └── ProcessUtils.java
│   │   └── resources/
│   │       ├── application.yml             # Spring Boot 配置文件
│   │       └── security/                   # 安全相关配置 (如 Seccomp)
│   └── test/                             # 测试代码
└── README.md                         # 本技术文档
```

**注意**: 上述项目结构是根据典型代码沙箱项目推断的，实际结构可能略有不同。请参考您项目中的实际文件和目录。

## 7. 部署与运行 (示例)

本项目是一个标准的 Spring Boot 项目，可以使用 Maven 进行构建和运行。

1.  **构建项目**:
    ```bash
    mvn clean package
    ```

2.  **运行项目**:
    ```bash
    java -jar target/oj-code-sandbox-*.jar
    ```

   或者通过 IDE直接运行 `OjCodeSandboxApplication.java` (如果存在主应用类)。

   项目启动后，通常会监听一个端口（例如 8090，具体请查看 `application.yml`），可以通过 HTTP 请求与之交互。

## 8. 注意事项

*   **安全性**：代码沙箱的核心在于安全。执行不受信任的代码时，必须严格限制其权限，防止恶意代码对系统造成破坏。这通常涉及到文件访问限制、网络访问限制、系统调用限制（如使用 Seccomp）、资源使用限制（CPU时间、内存）等。
*   **资源隔离**：不同用户的代码执行应该相互隔离，避免资源竞争或信息泄露。
*   **依赖管理**：对于需要特定库或环境的语言，需要考虑如何管理这些依赖。

希望这份文档对您有所帮助！