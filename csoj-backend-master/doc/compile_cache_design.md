# 编译结果缓存设计方案

> 版本：1.0
> 日期：2026-03-02
> 状态：设计阶段

---

## 1. 背景与目标

### 1.1 问题分析
当前每次代码提交都需要重新编译，即使代码完全相同。对于以下场景造成资源浪费：
- 用户重复提交相同代码
- 不同用户提交相同的模板代码
- 测试用例执行时重复编译

### 1.2 目标
- 减少编译时间 50%+（对于缓存命中的代码）
- 降低服务器 CPU 负载
- 保持系统安全性

---

## 2. 系统设计

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                      代码提交流程                              │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   用户代码 ──► 计算Hash ──► 查询缓存 ──► 命中?              │
│                                   │                         │
│                          ┌────────┴────────┐                │
│                          ▼                 ▼                │
│                       [命中]            [未命中]            │
│                          │                 │                │
│                          ▼                 ▼                │
│                    获取编译结果         编译代码             │
│                          │                 │                │
│                          │                 ▼                │
│                          │           存入缓存               │
│                          │                 │                │
│                          └────────┬────────┘                │
│                                   ▼                         │
│                              执行代码                        │
│                                   │                         │
│                                   ▼                         │
│                              返回结果                        │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 缓存键设计

```java
/**
 * 缓存键 = 语言 + 代码Hash
 *
 * 示例: "java:a1b2c3d4e5f6..."
 */
public class CacheKey {
    private String language;      // 编程语言
    private String codeHash;      // SHA-256 哈希值
    private String compilerVersion; // 编译器版本（可选）
}
```

**Hash 计算方式**：
```java
public String computeHash(String code, String language) {
    String normalizedCode = normalizeCode(code);
    String input = language + ":" + normalizedCode;
    return DigestUtils.sha256Hex(input);
}

// 标准化代码（移除空白差异）
private String normalizeCode(String code) {
    return code.trim()
               .replaceAll("\\s+", " ")
               .replaceAll("//.*", "")  // 移除单行注释
               .replaceAll("/\\*.*?\\*/", "");  // 移除多行注释
}
```

### 2.3 缓存存储设计

#### 方案 A：本地文件系统（推荐用于单机部署）

```
/tmp/compile-cache/
├── java/
│   ├── a1b2c3d4.class
│   └── e5f6g7h8.class
├── python/
│   └── i9j0k1l2.pyc
└── cpp/
    └── m3n4o5p6.out
```

#### 方案 B：Redis（推荐用于集群部署）

```redis
# 键格式
compile:cache:{language}:{hash}

# 值结构
{
    "bytecode": "base64_encoded_bytecode",
    "compileTime": 1234567890,
    "expireAt": 1234598890,
    "size": 1024
}

# 过期时间：24小时
```

### 2.4 核心类设计

```java
/**
 * 编译缓存服务
 */
@Service
public class CompileCacheService {

    @Value("${compile.cache.enabled:true}")
    private boolean cacheEnabled;

    @Value("${compile.cache.max-size:1024}")  // MB
    private long maxCacheSize;

    @Value("${compile.cache.expire-hours:24}")
    private int expireHours;

    @Resource
    private CompileCacheStorage cacheStorage;

    /**
     * 获取编译结果
     */
    public Optional<CompileResult> getCompileResult(String code, String language) {
        if (!cacheEnabled) {
            return Optional.empty();
        }

        String cacheKey = computeCacheKey(code, language);
        return cacheStorage.get(cacheKey);
    }

    /**
     * 存储编译结果
     */
    public void putCompileResult(String code, String language, CompileResult result) {
        if (!cacheEnabled) {
            return;
        }

        // 安全检查：不缓存过大的编译结果
        if (result.getSize() > 10 * 1024 * 1024) { // 10MB
            return;
        }

        String cacheKey = computeCacheKey(code, language);
        cacheStorage.put(cacheKey, result, expireHours);
    }

    /**
     * 清理过期缓存
     */
    @Scheduled(cron = "0 0 * * * ?")  // 每小时执行
    public void cleanupExpiredCache() {
        cacheStorage.cleanupExpired();
    }
}

/**
 * 编译结果
 */
@Data
public class CompileResult {
    private String cacheKey;
    private byte[] bytecode;       // 编译后的字节码/二进制
    private long compileTime;      // 编译耗时
    private long createTime;       // 创建时间
    private long size;             // 文件大小
    private String checksum;       // 完整性校验
}

/**
 * 缓存存储接口
 */
public interface CompileCacheStorage {
    Optional<CompileResult> get(String cacheKey);
    void put(String cacheKey, CompileResult result, int expireHours);
    void delete(String cacheKey);
    void cleanupExpired();
    CacheStats getStats();
}
```

### 2.5 本地文件存储实现

```java
/**
 * 本地文件系统缓存存储
 */
@Component
@ConditionalOnProperty(name = "compile.cache.type", havingValue = "local", matchIfMissing = true)
public class LocalCompileCacheStorage implements CompileCacheStorage {

    private final String cacheDir = System.getProperty("java.io.tmpdir") + "/compile-cache";
    private final Map<String, CacheEntry> index = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        File dir = new File(cacheDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        loadIndex();
    }

    @Override
    public Optional<CompileResult> get(String cacheKey) {
        CacheEntry entry = index.get(cacheKey);
        if (entry == null || entry.isExpired()) {
            return Optional.empty();
        }

        try {
            File file = new File(entry.getFilePath());
            if (!file.exists()) {
                index.remove(cacheKey);
                return Optional.empty();
            }

            CompileResult result = new CompileResult();
            result.setCacheKey(cacheKey);
            result.setBytecode(Files.readAllBytes(file.toPath()));
            result.setCreateTime(entry.getCreateTime());
            result.setSize(file.length());

            return Optional.of(result);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    @Override
    public void put(String cacheKey, CompileResult result, int expireHours) {
        String fileName = cacheKey + "." + getExtension(result);
        String filePath = cacheDir + "/" + result.getLanguage() + "/" + fileName;

        try {
            // 创建语言目录
            File langDir = new File(cacheDir + "/" + result.getLanguage());
            if (!langDir.exists()) {
                langDir.mkdirs();
            }

            // 写入文件
            Files.write(Paths.get(filePath), result.getBytecode());

            // 更新索引
            CacheEntry entry = new CacheEntry();
            entry.setCacheKey(cacheKey);
            entry.setFilePath(filePath);
            entry.setCreateTime(System.currentTimeMillis());
            entry.setExpireTime(System.currentTimeMillis() + expireHours * 3600 * 1000L);
            entry.setSize(result.getBytecode().length);

            index.put(cacheKey, entry);

        } catch (IOException e) {
            throw new RuntimeException("Failed to cache compile result", e);
        }
    }

    @Override
    public void cleanupExpired() {
        long now = System.currentTimeMillis();
        index.entrySet().removeIf(entry -> {
            if (entry.getValue().isExpired()) {
                new File(entry.getValue().getFilePath()).delete();
                return true;
            }
            return false;
        });
    }
}
```

### 2.6 策略模式集成

修改各语言策略，集成缓存：

```java
@Component("java")
public class JavaLanguageStrategy implements CodeSandboxStrategy {

    @Resource
    private CompileCacheService compileCacheService;

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest request) {
        String code = request.getCode();
        String language = request.getLanguage();

        // 1. 尝试从缓存获取编译结果
        Optional<CompileResult> cached = compileCacheService.getCompileResult(code, language);

        CompileResult compileResult;
        if (cached.isPresent()) {
            // 缓存命中，直接使用
            compileResult = cached.get();
            log.info("Compile cache hit for key: {}", compileResult.getCacheKey());
        } else {
            // 缓存未命中，执行编译
            compileResult = compileCode(code);
            // 存入缓存
            compileCacheService.putCompileResult(code, language, compileResult);
            log.info("Compile cache miss, compiled and cached");
        }

        // 2. 执行代码
        return executeWithCompiledCode(compileResult, request.getInputList());
    }
}
```

---

## 3. 安全设计

### 3.1 安全措施

| 风险 | 措施 |
|------|------|
| 缓存投毒 | 使用 SHA-256 哈希确保缓存键唯一 |
| 恶意代码缓存 | 编译时执行安全检查，不缓存恶意代码 |
| 缓存大小膨胀 | 限制单个缓存大小（10MB），总量限制（1GB） |
| 缓存文件泄露 | 缓存文件存储在隔离目录，权限控制 |

### 3.2 缓存失效场景

```java
/**
 * 缓存失效条件
 */
public boolean shouldInvalidateCache(String code, CompileResult cached) {
    // 1. 检测到恶意代码模式
    if (containsMaliciousPattern(code)) {
        return true;
    }

    // 2. 编译器版本更新
    if (compilerVersionChanged(cached)) {
        return true;
    }

    // 3. 缓存文件损坏
    if (!validateChecksum(cached)) {
        return true;
    }

    return false;
}
```

---

## 4. 配置项

```yaml
# application.yml
compile:
  cache:
    enabled: true
    type: local          # local 或 redis
    max-size: 1024       # 最大缓存大小（MB）
    expire-hours: 24     # 缓存过期时间（小时）
    max-entry-size: 10   # 单个缓存最大大小（MB）
```

---

## 5. 监控指标

```java
/**
 * 缓存统计
 */
@Data
public class CacheStats {
    private long totalRequests;      // 总请求数
    private long cacheHits;          // 缓存命中数
    private long cacheMisses;        // 缓存未命中数
    private double hitRate;          // 命中率
    private long totalSize;          // 总缓存大小
    private long entryCount;         // 缓存条目数
    private long evictions;          // 驱逐次数
}

/**
 * 监控端点
 */
@RestController
@RequestMapping("/api/cache")
public class CacheMonitorController {

    @GetMapping("/stats")
    public CacheStats getCacheStats() {
        return compileCacheService.getStats();
    }

    @PostMapping("/clear")
    public void clearCache() {
        compileCacheService.clearAll();
    }
}
```

---

## 6. 实施计划

### Phase 1：基础实现（1-2天）
- [ ] 实现 `CompileCacheService` 核心类
- [ ] 实现 `LocalCompileCacheStorage`
- [ ] 集成到 JavaLanguageStrategy

### Phase 2：多语言支持（2-3天）
- [ ] 集成到 PythonLanguageStrategy
- [ ] 集成到 CppLanguageStrategy
- [ ] 集成到其他语言策略

### Phase 3：Redis 支持（可选）
- [ ] 实现 `RedisCompileCacheStorage`
- [ ] 添加配置切换支持

### Phase 4：监控与优化（1天）
- [ ] 添加监控端点
- [ ] 性能测试
- [ ] 文档完善

---

## 7. 预期效果

| 指标 | 优化前 | 优化后（预估）|
|------|--------|--------------|
| 编译时间 | 2-5s | 0.1-0.5s（缓存命中）|
| CPU 使用率 | 100% | 20-30%（缓存命中时）|
| 内存使用 | 正常 | +100MB（缓存）|

---

## 8. 风险与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 缓存一致性 | 中 | 使用编译器版本作为缓存键的一部分 |
| 存储空间 | 低 | 定期清理，LRU 驱逐 |
| 安全绕过 | 高 | 多层安全检查，缓存隔离 |

---

## 9. 后续优化

1. **预热缓存**：预编译常见模板代码
2. **分布式缓存**：多节点共享缓存
3. **智能预测**：基于历史数据预测热门代码
4. **增量编译**：支持增量编译缓存
