# Docker 环境配置指南

## 问题描述

在 Windows 环境下使用 Docker Desktop v28.1.1 时，代码沙箱模块可能遇到以下连接问题：
- `java.io.EOFException`
- `java.nio.channels.ClosedChannelException`
- Named Pipe 连接不稳定

## 解决方案

### 方案一：启用 Docker Desktop TCP 连接（推荐）

1. **打开 Docker Desktop 设置**
   - 右键点击系统托盘中的 Docker 图标
   - 选择 "Settings" 或"设置"

2. **启用 TCP 连接**
   - 进入 "General" 或"常规" 选项卡
   - 勾选 "Expose daemon on tcp://localhost:2375 without TLS"
   - 点击 "Apply & Restart" 应用并重启

3. **验证 TCP 连接**
   ```bash
   # 在命令行中测试连接
   curl http://localhost:2375/version
   ```

### 方案二：使用环境变量配置

如果方案一不可用，可以通过设置环境变量来指定 Docker 连接：

```bash
# Windows PowerShell
$env:DOCKER_HOST="tcp://localhost:2375"

# Windows CMD
set DOCKER_HOST=tcp://localhost:2375

# 或者使用 Named Pipe（默认）
set DOCKER_HOST=npipe:////./pipe/docker_engine
```

### 方案三：Docker Desktop 高级配置

1. **编辑 Docker Desktop 配置文件**
   - 位置：`%USERPROFILE%\.docker\daemon.json`
   - 如果文件不存在，请创建它

2. **添加以下配置**
   ```json
   {
     "hosts": ["tcp://0.0.0.0:2375", "npipe://"],
     "insecure-registries": [],
     "debug": false,
     "experimental": false
   }
   ```

3. **重启 Docker Desktop**

## 代码修改说明

本次修改在 `DockerService.java` 中实现了以下改进：

1. **智能连接检测**：优先尝试 TCP 连接，如果不可用则回退到 Named Pipe
2. **连接状态日志**：添加详细的连接状态日志，便于问题排查
3. **超时控制**：为 TCP 连接检测添加 3 秒超时，避免长时间等待

## 测试验证

配置完成后，可以通过以下方式验证：

1. **运行测试用例**
   ```bash
   mvn test -Dtest=CsojCodeSandboxApplicationTests
   ```

2. **查看日志输出**
   - 成功的 TCP 连接会显示：`使用Docker TCP连接: tcp://localhost:2375`
   - 回退的 Named Pipe 连接会显示：`使用Docker Named Pipe连接: npipe:////./pipe/docker_engine`

## 故障排除

### 常见问题

1. **TCP 连接被拒绝**
   - 确保 Docker Desktop 已启用 TCP 连接
   - 检查防火墙设置是否阻止了 2375 端口

2. **Named Pipe 连接失败**
   - 确保 Docker Desktop 正在运行
   - 尝试重启 Docker Desktop
   - 检查 Windows 用户权限

3. **权限问题**
   - 确保当前用户在 "docker-users" 组中
   - 以管理员身份运行应用程序

### 日志分析

查看应用程序日志中的以下关键信息：
- Docker 连接类型选择
- 连接建立成功/失败信息
- 具体的异常堆栈信息

## 安全注意事项

⚠️ **重要提醒**：启用 TCP 连接（特别是不使用 TLS）会带来安全风险。建议：

1. 仅在开发环境中使用
2. 生产环境应使用 TLS 加密的连接
3. 配置防火墙规则限制访问
4. 定期检查和更新 Docker 版本

## 版本兼容性

- **Docker Desktop**: v28.1.1 及以上版本
- **docker-java**: 3.5.0
- **Java**: 1.8+
- **Spring Boot**: 2.7.14

## 参考资料

- [Docker Desktop for Windows 官方文档](https://docs.docker.com/desktop/windows/)
- [docker-java 项目文档](https://github.com/docker-java/docker-java)
- [Docker Engine API 文档](https://docs.docker.com/engine/api/)