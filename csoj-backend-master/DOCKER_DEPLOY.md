# CSOJ 后端 ARM64 Docker 部署指南

本指南帮助你在 ARM64 架构设备上快速部署 CSOJ 后端服务。

## 支持的平台

- **Apple Silicon Mac** (M1/M2/M3/M4)
- **Windows ARM64**
- **Linux ARM64** (AWS Graviton, 树莓派 4/5, etc.)

## 前置要求

1. **Docker Desktop** 4.0+ (已启用 ARM64 支持)
   - [macOS 下载](https://docs.docker.com/desktop/install/mac-install/)
   - [Windows 下载](https://docs.docker.com/desktop/install/windows-install/)
   - [Linux 安装](https://docs.docker.com/engine/install/)

2. **系统资源**
   - 最小: 4GB RAM, 20GB 磁盘
   - 推荐: 8GB+ RAM, 50GB+ 磁盘

## 快速开始

### macOS / Linux

```bash
# 1. 进入项目目录
cd csoj-backend-master

# 2. 添加执行权限
chmod +x deploy.sh

# 3. 一键部署
./deploy.sh deploy
```

### Windows PowerShell

```powershell
# 1. 进入项目目录
cd csoj-backend-master

# 2. 允许脚本执行（首次需要）
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser

# 3. 一键部署
.\deploy.ps1 deploy
```

## 部署命令

| 命令 | 说明 |
|------|------|
| `deploy` | 完整部署（检查环境、构建、启动） |
| `build` | 构建 Docker 镜像 |
| `start` | 启动所有服务 |
| `stop` | 停止所有服务 |
| `restart` | 重启所有服务 |
| `logs` | 查看应用日志 |
| `status` | 查看服务状态 |
| `monitoring` | 启动监控服务 |
| `clean` | 清理所有数据 |

## 手动部署步骤

### 1. 创建环境配置

```bash
# 复制环境配置模板
cp .env.example .env

# 编辑配置（可选）
vim .env
```

### 2. 构建并启动

```bash
# 构建镜像
docker compose build

# 启动所有服务
docker compose up -d

# 查看日志
docker compose logs -f csoj-backend
```

### 3. 验证部署

```bash
# 检查服务状态
docker compose ps

# 健康检查
curl http://localhost:8121/api/system/monitor/health
```

## 配置说明

### 环境变量 (.env)

```env
# MySQL 配置
MYSQL_ROOT_PASSWORD=csoj123456
MYSQL_PORT=3306

# Redis 配置
REDIS_PORT=6379

# Elasticsearch 配置
ES_PORT=9200

# 应用端口
APP_PORT=8121

# CORS 配置（生产环境请修改）
CORS_ALLOWED_ORIGINS=*
```

### 服务端口映射

| 服务 | 容器端口 | 主机端口 |
|------|----------|----------|
| CSOJ 后端 | 8121 | 8121 |
| MySQL | 3306 | 3306 |
| Redis | 6379 | 6379 |
| Elasticsearch | 9200 | 9200 |
| Prometheus | 9090 | 9090 (可选) |
| Grafana | 3000 | 3000 (可选) |

## 访问地址

启动成功后，可以访问以下地址：

- **API 文档 (Swagger)**: http://localhost:8121/api/doc.html
- **健康检查**: http://localhost:8121/api/system/monitor/health
- **Prometheus**: http://localhost:9090 (需启用监控)
- **Grafana**: http://localhost:3000 (需启用监控，admin/admin)

## 启用监控服务

```bash
# macOS/Linux
./deploy.sh monitoring

# Windows
.\deploy.ps1 monitoring

# 或手动启动
docker compose --profile monitoring up -d
```

## 常见问题

### 1. Elasticsearch 启动失败

**问题**: `max virtual memory areas vm.max_map_count [65530] is too low`

**解决方案**:

macOS (Docker Desktop):
```bash
docker run --rm --privileged alpine sysctl -w vm.max_map_count=262144
```

Linux:
```bash
sudo sysctl -w vm.max_map_count=262144
# 永久生效
echo "vm.max_map_count=262144" | sudo tee -a /etc/sysctl.conf
```

Windows (Docker Desktop):
- 在 Docker Desktop 设置中增加内存限制
- 或使用 WSL2 后端

### 2. 构建超时

**解决方案**: 使用国内 Maven 镜像（已配置阿里云镜像）

### 3. 内存不足

**解决方案**: 修改 `.env` 或 `docker-compose.yml` 中的内存限制

```yaml
# 减少 Elasticsearch 内存
environment:
  - "ES_JAVA_OPTS=-Xms256m -Xmx256m"
```

### 4. 端口冲突

**解决方案**: 修改 `.env` 中的端口映射

```env
APP_PORT=8122
MYSQL_PORT=3307
REDIS_PORT=6380
ES_PORT=9201
```

## 数据持久化

数据存储在 Docker volumes 中：

```bash
# 查看所有 volumes
docker volume ls | grep csoj

# 备份数据
docker run --rm -v csoj-mysql-data:/data -v $(pwd):/backup alpine tar czf /backup/mysql-backup.tar.gz /data

# 恢复数据
docker run --rm -v csoj-mysql-data:/data -v $(pwd):/backup alpine tar xzf /backup/mysql-backup.tar.gz -C /
```

## 生产环境建议

1. **修改默认密码**
   ```env
   MYSQL_ROOT_PASSWORD=your-strong-password
   ```

2. **配置 HTTPS**
   - 使用 Nginx 反向代理
   - 配置 SSL 证书

3. **限制 CORS**
   ```env
   CORS_ALLOWED_ORIGINS=https://your-domain.com
   ```

4. **启用防火墙**
   - 只暴露必要端口
   - 内部服务不要对外暴露

5. **定期备份**
   - 数据库备份
   - 配置文件备份

## 架构图

```
┌─────────────────────────────────────────────────────────┐
│                    Docker Network                        │
│                      (csoj-network)                      │
│  ┌─────────────────────────────────────────────────────┐│
│  │                                                     ││
│  │  ┌──────────────┐    ┌──────────────┐              ││
│  │  │   MySQL      │    │    Redis     │              ││
│  │  │   (3306)     │    │   (6379)     │              ││
│  │  └──────────────┘    └──────────────┘              ││
│  │         │                   │                       ││
│  │         └─────────┬─────────┘                       ││
│  │                   │                                 ││
│  │         ┌─────────▼─────────┐                       ││
│  │         │   CSOJ Backend    │                       ││
│  │         │     (8121)        │                       ││
│  │         └─────────┬─────────┘                       ││
│  │                   │                                 ││
│  │         ┌─────────▼─────────┐                       ││
│  │         │  Elasticsearch    │                       ││
│  │         │     (9200)        │                       ││
│  │         └───────────────────┘                       ││
│  │                                                     ││
│  └─────────────────────────────────────────────────────┘│
│                                                         │
│  ┌─────────────────────────────────────────────────────┐│
│  │  可选: 监控服务                                      ││
│  │  ┌──────────────┐    ┌──────────────┐              ││
│  │  │ Prometheus   │◄───│   Grafana    │              ││
│  │  │   (9090)     │    │   (3000)     │              ││
│  │  └──────────────┘    └──────────────┘              ││
│  └─────────────────────────────────────────────────────┘│
│                                                         │
└─────────────────────────────────────────────────────────┘
```

## 文件结构

```
csoj-backend-master/
├── Dockerfile.arm64      # ARM64 专用 Dockerfile
├── docker-compose.yml    # Docker Compose 配置
├── .dockerignore         # Docker 构建忽略文件
├── .env.example          # 环境变量模板
├── deploy.sh             # macOS/Linux 部署脚本
├── deploy.ps1            # Windows 部署脚本
├── sql/                  # 数据库初始化脚本
│   ├── create_table.sql
│   └── ...
└── src/main/resources/
    └── application-docker.yml  # Docker 环境配置
```

## 技术支持

如遇问题，请检查：

1. Docker Desktop 是否正常运行
2. 系统资源是否充足
3. 端口是否被占用
4. 查看日志: `docker compose logs -f`
