# Docker 网络连接问题修复指南

## 问题描述

您遇到的错误是由于无法连接到 Docker Hub (registry-1.docker.io) 导致的镜像拉取失败：

```
com.github.dockerjava.api.exception.InternalServerErrorException: Status 500
failed to resolve reference "docker.io/library/openjdk:8-alpine"
dialing registry-1.docker.io:443: connectex: A connection attempt failed
```

## 解决方案

### 方案一：配置国内镜像源（推荐）

#### 自动配置（推荐）

1. **运行自动配置脚本**：
   ```bash
   # 以管理员身份运行
   .\setup-docker-mirrors.bat
   ```

2. **按照脚本提示操作**：
   - 关闭 Docker Desktop
   - 等待脚本完成配置
   - 重启 Docker Desktop

#### 手动配置

1. **关闭 Docker Desktop**

2. **找到 Docker 配置文件位置**：
   - Windows: `%USERPROFILE%\.docker\daemon.json`
   - 如果文件不存在，请创建它

3. **编辑配置文件**，添加以下内容：
   ```json
   {
     "registry-mirrors": [
       "https://docker.mirrors.ustc.edu.cn",
       "https://hub-mirror.c.163.com",
       "https://mirror.baidubce.com",
       "https://dockerproxy.com",
       "https://mirror.ccs.tencentyun.com"
     ]
   }
   ```

4. **重启 Docker Desktop**

### 方案二：预先拉取镜像

如果镜像源配置后仍有问题，可以尝试手动拉取：

```bash
# 拉取 Java 镜像
docker pull openjdk:8-jdk-alpine

# 拉取 C# 镜像
docker pull mcr.microsoft.com/dotnet/sdk:latest

# 拉取其他语言镜像
docker pull python:3.9-alpine
docker pull node:16-alpine
```

## 验证修复效果

### 方法一：使用测试脚本

```powershell
# 运行测试脚本
.\test-docker-fix.ps1
```

### 方法二：手动验证

1. **检查镜像源配置**：
   ```bash
   docker info | findstr "Registry Mirrors"
   ```

2. **测试镜像拉取**：
   ```bash
   docker pull openjdk:8-jdk-alpine
   ```

3. **运行代码沙箱测试**：
   ```bash
   mvn test -Dtest=CsojCodeSandboxApplicationTests#testExecuteCodeByJava
   ```

## 常见问题解决

### 问题1：配置后仍然无法拉取镜像

**解决方案**：
- 确认 Docker Desktop 已完全重启
- 检查网络连接
- 尝试使用不同的镜像源

### 问题2：某些镜像仍然拉取失败

**解决方案**：
- 某些镜像可能不在国内镜像源中
- 尝试使用 VPN 或代理
- 寻找替代镜像

### 问题3：Docker Desktop 启动失败

**解决方案**：
- 检查配置文件 JSON 格式是否正确
- 恢复备份的配置文件
- 重新安装 Docker Desktop

## 代码修复说明

我已经对 `JavaLanguageStrategy.java` 进行了以下改进：

1. **添加镜像预拉取逻辑**：在执行代码前主动拉取 Docker 镜像
2. **完善异常处理**：当镜像拉取失败时返回明确错误信息
3. **资源管理优化**：确保临时文件能够正确清理

这些改进使得系统在网络问题时能够：
- 提供清晰的错误信息
- 避免系统崩溃
- 正确清理资源

## 联系支持

如果按照以上步骤仍无法解决问题，请提供：
- Docker 版本信息：`docker version`
- 系统信息：`systeminfo`
- 错误日志的完整内容

---

**注意**：配置镜像源后，首次拉取镜像可能仍需要一些时间，请耐心等待。