# CSOJ 代码沙箱详细技术文档

## 1. 项目概述

### 1.1 项目简介

CSOJ 代码沙箱是一个专为在线判题系统（Online Judge, OJ）设计的核心组件，旨在提供一个安全、隔离的环境来执行用户提交的各种编程语言代码。它能够编译（如果需要）、运行代码，并对代码的执行资源（如时间和内存）进行限制，同时捕获执行结果、输出以及可能发生的错误信息。该沙箱通过统一的 API 接口与外部系统（如 OJ 的主系统）集成，实现了代码的自动化评测。

### 1.2 主要功能

根据项目中的 `doc.md` 和源代码分析，主要功能包括：

- **多语言支持**：通过策略模式和工厂模式，支持 Java、Python、C++、C#、JavaScript、Go、Ruby、Swift 等多种编程语言的代码执行。
- **代码编译与执行**：提供代码的编译（针对编译型语言）和运行环境。
- **资源限制**：能够限制代码执行的时间（例如，通过超时机制）和内存（例如，通过 JVM 参数 `-Xmx`）。
- **输入输出处理**：支持为代码执行提供标准输入，并捕获标准输出和标准错误。
- **安全隔离**：
    - 文件系统隔离：将用户代码保存在临时的、唯一的目录中，并在执行后清理。
    - 权限控制：虽然具体实现细节未完全查看，但项目中存在 `DefaultSecurityManager`、`DenySecurityManager` 等类，暗示了对 Java 安全管理器的使用或自定义，以限制代码执行权限（如文件访问、网络连接等）。
    - 进程级超时：通过监控子进程执行时间，超时则销毁进程。
- **结果反馈**：返回详细的执行结果，包括输出列表、执行状态（成功、失败、编译错误、运行超时等）、以及判题信息（如执行时间）。
- **API 接口**：通过 `MainController` 提供 HTTP API 接口（`/executeCode`），接收代码执行请求并返回结果，包含基本的请求头认证机制。

### 1.3 应用场景

- 在线编程教育平台：用于学生提交代码作业并自动评分。
- 编程竞赛评判系统：作为核心评测机，自动判断参赛者提交代码的正确性。
- 技术面试编程题目评估：在线评估候选人的编程能力。
- 企业内部代码能力在线评测与培训。

## 2. 系统架构

CSOJ 代码沙箱采用模块化和分层设计，核心思想是基于策略模式和模板方法模式来支持多语言和统一执行流程。

### 2.1 架构图（概念性）

```mermaid
graph TD
    A[HTTP请求 /executeCode] --> B(MainController);
    B -- ExecuteCodeRequest --> C{CodeSandboxFactory};
    C -- language --> D[获取对应CodeSandboxStrategy];
    D -- 执行代码 --> E{LanguageSpecificSandbox (e.g., JavaNativeCodeSandbox)};
    E -- 使用 --> F(JavaCodeSandboxTemplate);
    F -- 1. 保存代码 --> G[临时代码文件];
    F -- 2. 编译代码 --> H[编译产物/错误信息];
    F -- 3. 运行代码 (ProcessUtils) --> I[执行结果/错误/超时];
    F -- 4. 收集结果 --> J[ExecuteCodeResponse];
    F -- 5. 清理文件 --> G;
    E -- 返回 --> J;
    B -- 返回 --> A;
```
*(这是一个基于现有代码推断的简化架构图，具体细节可能需要更深入的分析或设计文档)*

### 2.2 核心组件交互

1.  **`MainController`**：作为系统的入口，接收 HTTP POST 请求到 `/executeCode` 端点。它负责基本的请求认证，然后将 `ExecuteCodeRequest`（包含代码、语言、输入列表）传递给 `CodeSandboxFactory`。
2.  **`CodeSandboxFactory`**：根据请求中指定的 `language`，从其内部维护的策略映射（`strategyMap`）中获取对应的 `CodeSandboxStrategy` 实例。这个工厂在应用启动时（`@PostConstruct`）初始化，注册所有支持的语言策略。
3.  **`CodeSandboxStrategy`**：这是一个接口，定义了执行代码的统一方法 `executeCode(ExecuteCodeRequest)`。具体的语言沙箱实现（如 `JavaNativeCodeSandbox`, `PythonNativeCodeSandbox` 等）会实现此接口。
4.  **`JavaCodeSandboxTemplate`**：这是一个抽象类，实现了 `CodeSandbox` 接口（间接通过具体的子类如 `JavaNativeCodeSandbox`）。它定义了 Java 代码（以及类似编译型语言）执行的通用流程（模板方法）：
    *   `saveCodeToFile()`: 将源代码保存到临时文件。
    *   `compileFile()`: 编译源代码文件（抽象方法，由子类实现特定语言的编译逻辑）。
    *   `runFile()`: 运行编译后的代码，处理输入并收集输出（抽象方法，由子类实现特定语言的运行逻辑）。
    *   `getOutputResponse()`: 整理执行结果和判题信息到 `ExecuteCodeResponse`。
    *   `deleteFile()`: 清理临时文件和目录。
5.  **`JavaNativeCodeSandbox`** (及其他语言的 Native 实现)：继承自 `JavaCodeSandboxTemplate`（或直接实现 `CodeSandboxStrategy`）。它们提供了特定语言的编译和运行命令，例如 `javac` 和 `java` 命令的构造和执行。它们利用 `ProcessUtils` 来执行外部进程并获取结果。
6.  **`ProcessUtils`**：工具类，用于执行操作系统进程（如编译和运行命令），捕获进程的输出流、错误流、退出码，并处理执行超时。
7.  **数据模型**：
    *   `ExecuteCodeRequest`: 封装了执行代码所需的输入数据（代码字符串、编程语言、输入用例列表）。
    *   `ExecuteCodeResponse`: 封装了代码执行后的结果（输出列表、状态码、错误信息、判题信息）。
    *   `ExecuteMessage`: 封装了单次编译或执行操作的消息（标准输出、错误输出、退出码、执行时间）。
    *   `JudgeInfo`: 封装了判题相关信息（如执行时间、内存消耗等，当前内存未实现）。
    *   `ExecuteCodeStatusEnum`: 枚举类，定义了代码执行的各种状态（如成功、失败、编译错误、系统错误等）。

## 3. 核心组件详解

### 3.1 接口与抽象类

-   **`CodeSandbox` (interface)**
    -   路径: `com.cs.ojcodesandbox.CodeSandbox`
    -   描述: 定义了代码沙箱的核心行为，即执行代码。只有一个方法 `ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest)`。
-   **`CodeSandboxStrategy` (interface)**
    -   路径: `com.cs.ojcodesandbox.CodeSandboxStrategy`
    -   描述: 策略接口，用于实现不同编程语言的执行逻辑。同样定义了 `ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest)` 方法。这是工厂模式返回的具体策略类型。
-   **`JavaCodeSandboxTemplate` (abstract class)**
    -   路径: `com.cs.ojcodesandbox.JavaCodeSandboxTemplate`
    -   描述: 实现了 `CodeSandbox` 接口。采用模板方法设计模式，为 Java（及可扩展至其他编译型语言）代码的执行定义了一个骨架流程。它处理了文件保存、结果收集、文件清理等通用步骤，并将特定于语言的编译 (`compileFile`) 和运行 (`runFile`) 步骤声明为抽象方法，由子类实现。
        -   `GLOBAL_CODE_DIR_NAME = "tmpCode"`: 存储临时代码的根目录名。
        -   `MAIN_FILE_BASENAME = "Main"`: 默认的主文件名（不含扩展名）。
        -   `TIME_OUT = 5000L`: 默认的执行超时时间（毫秒）。
        -   `saveCodeToFile(String code, String language)`: 将代码字符串写入临时文件，文件名基于 `MAIN_FILE_BASENAME` 和 `getFileExtension(language)`。
        -   `getFileExtension(String language)`: 根据语言获取文件扩展名，默认为 `.java`，其他语言需要子类覆盖或抛出异常。
        -   `getOutputResponse(List<ExecuteMessage> executeMessageList)`: 将多次运行（针对多个输入用例）的 `ExecuteMessage` 聚合成一个 `ExecuteCodeResponse`。
        -   `deleteFile(File userCodeFile)`: 删除用户代码所在的整个临时目录。

### 3.2 工厂类

-   **`CodeSandboxFactory` (class)**
    -   路径: `com.cs.ojcodesandbox.CodeSandboxFactory`
    -   描述: 负责根据编程语言类型创建和提供相应的 `CodeSandboxStrategy` 实例。使用 `@Component` 注解，由 Spring 管理。
    -   `strategyMap`: 一个 `Map<String, CodeSandboxStrategy>`，在 `@PostConstruct` 初始化方法 `init()` 中填充，键是语言名称（小写，如 "java", "python"），值是对应的策略实现类实例（通过 `@Autowired` 注入）。
    -   `getStrategy(String language)`: 根据传入的语言名称（不区分大小写）查找并返回对应的策略实例。如果找不到，则抛出 `IllegalArgumentException`。

### 3.3 具体沙箱实现 (以 Java 为例)

-   **`JavaNativeCodeSandbox` (class)**
    -   路径: `com.cs.ojcodesandbox.JavaNativeCodeSandbox`
    -   描述: 继承自 `JavaCodeSandboxTemplate`，提供了 Java 语言原生执行的具体实现。使用 `@Component` 注解。
    -   `executeCode(ExecuteCodeRequest executeCodeRequest)`: 直接调用父类 `JavaCodeSandboxTemplate` 的同名方法来执行标准流程。
    -   `compileFile(File userCodeFile, String language)`: 实现 Java 代码的编译。构造 `javac -encoding utf-8 <userCodeFile.getAbsolutePath()>` 命令，并使用 `Runtime.getRuntime().exec()` 执行。通过 `ProcessUtils.runProcessAndGetMessage()` 获取编译结果。如果编译进程的退出码非0，则视为编译错误并抛出异常。
    -   `runFile(File userCodeFile, List<String> inputList, String language)`: 实现 Java 代码的运行。对每个输入用例 `inputArgs`，构造 `java -Xmx256m -Dfile.encoding=UTF-8 -cp <userCodeParentPath> Main <inputArgs>` 命令执行。其中 `-Xmx256m` 限制了最大堆内存。同样使用 `ProcessUtils` 执行，并启动一个新线程来监控超时 (`TIME_OUT`)，若超时则销毁运行进程。
    -   `getFileExtension(String language)`: 覆盖父类方法，确保只处理 "java" 语言，返回 ".java"。

### 3.4 其他语言策略

在 `com.cs.ojcodesandbox.strategy` 包下，存在多种语言的策略类，如：

-   `PythonLanguageStrategy.java`
-   `CppLanguageStrategy.java`
-   `CSharpLanguageStrategy.java`
-   `JavaScriptLanguageStrategy.java`
-   `GoLanguageStrategy.java`
-   `RubyLanguageStrategy.java`
-   `SwiftLanguageStrategy.java`

这些类预计会实现 `CodeSandboxStrategy` 接口，并提供对应语言的代码保存、编译（如果需要）、运行逻辑。它们可能直接实现所有步骤，或者也可能复用/扩展类似 `JavaCodeSandboxTemplate` 的模板（如果适用）。

### 3.5 数据模型

位于 `com.cs.ojcodesandbox.model` 包及其子包下：

-   **`ExecuteCodeRequest.java`**: `@Data @Builder @NoArgsConstructor @AllArgsConstructor`
    -   `inputList (List<String>)`: 输入参数列表，每个字符串代表一组输入。
    -   `code (String)`: 用户提交的源代码字符串。
    -   `language (String)`: 编程语言标识（如 "java", "python"）。
-   **`ExecuteCodeResponse.java`**: `@Data @Builder @NoArgsConstructor @AllArgsConstructor`
    -   `outputList (List<String>)`: 代码对每个输入用例的输出列表。
    -   `message (String)`: 接口信息，通常用于存放错误信息或执行摘要。
    -   `status (Integer)`: 执行状态码，对应 `ExecuteCodeStatusEnum` 中的值。
    -   `judgeInfo (JudgeInfo)`: 判题信息，如时间和内存消耗。
-   **`ExecuteMessage.java`**: (未查看具体字段，但根据 `JavaCodeSandboxTemplate` 中的使用推断)
    -   `message (String)`: 单次执行的标准输出。
    -   `errorMessage (String)`: 单次执行的标准错误输出。
    -   `exitValue (Integer)`: 进程退出码。
    -   `time (Long)`: 执行时间（毫秒）。
-   **`JudgeInfo.java`**: (未查看具体字段，但根据 `JavaCodeSandboxTemplate` 中的使用推断)
    -   `time (Long)`: 最大执行时间（毫秒）。
    -   `memory (Long)`: 内存消耗（当前版本未实现）。
-   **`enums/ExecuteCodeStatusEnum.java`**: (未查看具体字段，但根据名称推断)
    -   定义了如 `SUCCEED`, `FAILED`, `COMPILE_ERROR`, `RUNTIME_ERROR`, `TIME_LIMIT_EXCEEDED`, `MEMORY_LIMIT_EXCEEDED`, `SYSTEM_ERROR` 等执行状态。

### 3.6 控制器

-   **`MainController.java`**
    -   路径: `com.cs.ojcodesandbox.controller.MainController`
    -   描述: Spring MVC 控制器，处理外部 HTTP 请求。
    -   `AUTH_REQUEST_HEADER = "auth"`
    -   `AUTH_REQUEST_SECRET = "secretKey"`: 用于简单的请求头认证。实际生产环境应使用更安全的认证机制。
    -   `@GetMapping("/health")`: 提供健康检查端点，返回 "ok"。
    -   `@PostMapping("/executeCode")`: 核心接口，接收 `ExecuteCodeRequest` JSON 对象。它首先进行请求头认证，然后通过注入的 `CodeSandboxFactory` 获取对应语言的 `CodeSandboxStrategy`，并调用其 `executeCode` 方法执行代码，最后返回 `ExecuteCodeResponse`。

### 3.7 工具类

-   **`ProcessUtils.java`**
    -   路径: `com.cs.ojcodesandbox.utils.ProcessUtils`
    -   描述: (未查看具体实现) 封装了执行外部命令（进程）的逻辑。预期功能包括：
        -   启动进程。
        -   读取进程的标准输出流和标准错误流。
        -   等待进程结束并获取退出码。
        -   处理进程执行超时（可能通过 `Process.waitFor(long, TimeUnit)` 或单独的监控线程实现）。
        -   返回一个包含输出、错误、退出码和执行时间的 `ExecuteMessage` 对象。

### 3.8 安全管理

位于 `com.cs.ojcodesandbox.security` 包下：

-   `DefaultSecurityManager.java`
-   `DenySecurityManager.java`
-   `MySecurityManager.java`
-   `TestSecurityManager.java`

这些类的存在表明项目考虑了使用 Java Security Manager 来限制代码执行权限，例如禁止文件系统任意读写、禁止网络访问、禁止执行危险操作等。`DenySecurityManager` 可能是一个严格限制权限的实现。具体策略需要查看这些类的实现细节。

## 4. 代码执行流程

以 Java 代码提交流程为例：

1.  **请求接收**: 外部系统（如 OJ 前端或后端）向代码沙箱的 `/executeCode` API (POST) 发送请求。请求体是一个 JSON 对象，包含 `code` (Java 源代码), `language` ("java"), 和 `inputList` (一个或多个测试用例的输入字符串)。请求头中需要包含正确的 `auth` 密钥。
2.  **认证与分发**: `MainController` 验证 `auth` 请求头。验证通过后，调用 `CodeSandboxFactory.getStrategy("java")` 获取 `JavaNativeCodeSandbox` 实例（或其他配置的 Java 策略）。
3.  **执行代码 (通过 `JavaNativeCodeSandbox` -> `JavaCodeSandboxTemplate`)**:
    a.  **保存代码**: `saveCodeToFile()` 方法被调用。系统在 `tmpCode` 目录下创建一个唯一的子目录（基于 UUID），并将用户代码保存为 `Main.java` 文件。
    b.  **编译代码**: `JavaNativeCodeSandbox` 的 `compileFile()` 方法被调用。它执行 `javac -encoding utf-8 <path_to_Main.java>` 命令。`ProcessUtils` 捕获编译过程的输出、错误和退出码。如果编译失败（退出码非0），则记录错误信息，后续流程可能中断或标记为编译错误。
    c.  **运行代码**: 如果编译成功，`JavaNativeCodeSandbox` 的 `runFile()` 方法被调用。对于 `inputList` 中的每一个输入字符串：
        i.  构造运行命令: `java -Xmx256m -Dfile.encoding=UTF-8 -cp <temp_dir_path> Main <current_input>`。
        ii. `ProcessUtils` 执行该命令。同时，一个独立的线程启动，如果在 `TIME_OUT` (默认5秒) 内进程未结束，该线程会尝试 `process.destroy()` 来终止它。
        iii. `ProcessUtils` 收集运行的标准输出、标准错误、退出码和实际执行时间，封装成 `ExecuteMessage` 对象。
    d.  **收集结果**: 所有输入用例执行完毕后，`JavaCodeSandboxTemplate` 的 `getOutputResponse()` 方法被调用。它遍历所有 `ExecuteMessage` 对象：
        -   汇总所有标准输出到 `outputList`。
        -   检查是否有错误信息或非正常退出码，设置 `ExecuteCodeResponse` 的 `status` 和 `message`。
        -   记录所有测试用例中的最大执行时间到 `JudgeInfo`。
    e.  **文件清理**: `deleteFile()` 方法被调用，删除之前创建的包含 `Main.java` 和 `Main.class` 的临时目录。
4.  **响应返回**: `MainController` 将 `ExecuteCodeResponse` 对象序列化为 JSON 并返回给调用方。

## 5. 安全机制

CSOJ 代码沙箱采用多种机制来确保执行用户代码的安全性：

1.  **文件系统隔离**: 用户代码在独立的、临时的、随机命名的目录中创建、编译和执行。执行完毕后，这些临时文件和目录会被删除。这防止了不同用户代码之间的干扰以及对系统关键文件的意外访问。
2.  **资源限制**:
    *   **时间限制**: 通过 `ProcessUtils`（或其内部机制，如 `JavaNativeCodeSandbox` 中的超时监控线程）限制每个测试用例的执行时间。超时则强行终止进程。
    *   **内存限制**: 对于 Java 代码，通过 JVM 启动参数 `-Xmx256m` (示例值) 来限制最大堆内存。其他语言可能需要不同的机制（如使用 `ulimit` 命令配合原生执行，或 Docker 容器的资源限制）。
3.  **进程隔离**: 用户代码作为独立的操作系统进程运行，与代码沙箱主服务进程分离。这提供了基本的隔离。
4.  **Java 安全管理器 (Security Manager)**: 项目中 `security` 包下的 `DefaultSecurityManager`, `DenySecurityManager` 等类表明可能使用了 Java Security Manager 来限制 Java 代码在 JVM 内的权限。例如，可以禁止：
    *   读写文件系统上除了允许路径之外的任何文件。
    *   建立网络连接。
    *   执行本地命令 (`Runtime.exec()`)。
    *   访问或修改系统属性。
    *   加载本地库。
    具体策略取决于这些 `SecurityManager` 的实现和配置方式。
5.  **API 认证**: `MainController` 实现了一个简单的基于请求头的认证机制 (`AUTH_REQUEST_HEADER` 和 `AUTH_REQUEST_SECRET`)，防止未经授权的访问。这是一种基础防护，生产环境建议采用更健壮的认证授权方案（如 OAuth2, JWT）。
6.  **输入校验**: (虽然未直接在代码中看到明确的输入校验逻辑，但这是推荐的安全实践) 对用户提交的代码大小、输入数据长度等进行限制，防止恶意构造的超长输入消耗过多资源。
7.  **黑名单/白名单机制**: (推测) `DenySecurityManager` 可能实现了一种黑名单机制，禁止某些危险操作。更完善的系统可能会有更细致的权限配置。

## 6. 支持的编程语言

根据 `CodeSandboxFactory` 的初始化逻辑，目前显式支持以下语言（通过对应的策略类实现）：

-   Java (`javaLanguageStrategy` -> `JavaNativeCodeSandbox`)
-   Python (`pythonLanguageStrategy` -> `PythonNativeCodeSandbox` 或类似实现)
-   C++ (`cppLanguageStrategy`)
-   C# (`cSharpLanguageStrategy`)
-   JavaScript (`javaScriptLanguageStrategy`)
-   Go (`goLanguageStrategy`)
-   Ruby (`rubyLanguageStrategy`)
-   Swift (`swiftLanguageStrategy`)

每种语言的具体执行方式（编译命令、运行命令、文件扩展名等）由其对应的 `CodeSandboxStrategy` 实现类定义。

## 7. 扩展性与优化

### 7.1 扩展新语言

要支持一种新的编程语言，例如 "Kotlin"，需要以下步骤：

1.  **创建策略实现**: 创建一个新的类，比如 `KotlinLanguageStrategy.java`，实现 `CodeSandboxStrategy` 接口 (或者如果适用，继承某个模板类如 `JavaCodeSandboxTemplate`)。
    *   实现 `executeCode` 方法，或者如果继承模板，则实现 `compileFile` (如果 Kotlin 是编译型) 和 `runFile` 方法，包括正确的编译命令、运行命令、文件扩展名处理等。
    *   确保处理好输入输出、错误捕获和资源限制。
2.  **注册策略**: 在 `CodeSandboxFactory` 中：
    *   通过 `@Autowired` 注入新的 `KotlinLanguageStrategy` 实例。
    *   在 `init()` 方法中，将其添加到 `strategyMap`：`strategyMap.put("kotlin", kotlinLanguageStrategy);`

### 7.2 优化方向

-   **Docker化沙箱**: 当前的 `JavaNativeCodeSandbox` 是在宿主机上直接执行命令。为了更好的隔离性、安全性和环境一致性，可以考虑使用 Docker 容器作为沙箱环境。每个代码执行请求都在一个独立的、临时的 Docker 容器中运行。项目中有 `JavaDockerCodeSandbox.java` 和 `JavaDockerCodeSandboxOld.java`，以及 `docker/DockerDemo.java`，表明已经有这方面的尝试或实现。
    -   优点：更强的隔离（文件系统、网络、进程空间），易于管理依赖和环境，更精确的资源控制（CPU, 内存）。
    -   挑战：Docker API 调用开销，镜像管理，冷启动延迟。
-   **更精细的资源监控**: 当前内存监控未实现。可以使用如 `ps`、`top` (Linux) 或 `tasklist` (Windows) 等系统命令，或者集成专门的库来监控进程的实际内存峰值。
-   **异步执行与队列**: 对于高并发场景，可以将代码执行请求放入消息队列（如 RabbitMQ, Kafka），由独立的 worker 服务消费并执行。这可以提高系统的吞吐量和响应性。
-   **安全增强**: 
    *   完善 Java Security Manager 策略，或采用更现代的沙箱技术（如 seccomp-bpf for Linux native processes, gVisor）。
    *   对用户代码进行静态分析，检测潜在的恶意行为。
    *   更强的 API 认证和授权机制。
-   **配置化**: 将超时时间、内存限制、支持的语言列表、编译/运行命令模板等参数外部化到配置文件中，而不是硬编码在代码里，方便调整和维护。
-   **日志与监控**: 完善日志记录，集成监控系统（如 Prometheus, Grafana）来跟踪沙箱服务的性能指标和健康状况。

## 8. 总结

CSOJ 代码沙箱通过策略模式、模板方法模式等设计原则，构建了一个可扩展、支持多种编程语言的代码执行环境。它提供了基本的安全隔离和资源限制功能，是构建在线判题系统等应用的关键后端服务。通过进一步引入容器化技术（如 Docker）和完善安全机制，可以使其更加健壮和安全。

---
*本文档基于对 `csoj-code-sandbox-master` 项目部分源代码和 `doc.md` 的分析编写。某些细节（如未查看的类具体实现、完整的错误处理流程等）可能需要进一步查阅代码或原始设计文档来确认。*