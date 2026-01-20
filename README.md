# CSOJ 项目部署指南

## 快速启动

### 方式一：标准部署（单机版）

```bash
# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f backend

# 停止服务
docker-compose down
```

### 方式二：高可用部署（推荐）

```bash
# 启动高可用集群（包含主从复制、负载均衡、监控）
chmod +x scripts/start-ha.sh
./scripts/start-ha.sh
```

---

## 环境要求

| 组件 | 版本要求 |
|------|----------|
| Docker | 20.10+ |
| Docker Compose | 2.0+ |
| Java | 8+ |
| MySQL | 8.0+ |
| Redis | 7+ |
| 内存 | ≥ 4GB |
| 磁盘 | ≥ 20GB |

---

## 服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| 前端 | 80 | Nginx 静态文件 |
| 后端API | 8121, 8122 | Spring Boot (多实例) |
| 代码沙箱 | 8090 | Docker 判题服务 |
| MySQL | 3306, 3307 | 主库、从库 |
| Redis | 6379, 6380 | 主节点、从节点 |
| Redis Sentinel | 26379 | 哨兵 |
| RabbitMQ | 5672, 15672 | 消息队列、管理界面 |
| Prometheus | 9090 | 监控指标采集 |
| Grafana | 3000 | 监控可视化 |

---

## 默认账号

| 服务 | 用户名 | 密码 |
|------|--------|------|
| MySQL | root | 123456 |
| Grafana | admin | admin |
| RabbitMQ | guest | guest |

**⚠️ 生产环境请务必修改默认密码！**

---

## 配置文件

| 文件 | 说明 |
|------|------|
| `.env` | 环境变量配置 |
| `application.yml` | 应用配置 |
| `docker-compose.yml` | 标准部署配置 |
| `docker-compose-ha.yml` | 高可用部署配置 |

---

## 常见问题

### 1. 服务启动失败

```bash
# 查看详细日志
docker-compose logs -f [service_name]

# 重启特定服务
docker-compose restart [service_name]
```

### 2. 数据库连接失败

检查 `.env` 中的数据库配置：
```bash
MYSQL_HOST=mysql
MYSQL_PORT=3306
MYSQL_DATABASE=oj_db
MYSQL_USER=root
MYSQL_PASSWORD=123456
```

### 3. 判题服务异常

确保 Docker 守护进程运行正常，检查沙箱服务日志：
```bash
docker logs csoj-sandbox
```

---

## 数据库初始化

首次启动会自动执行以下 SQL 文件（位于 `sql/` 目录）：
- `init.sql` - 基础表结构
- `contest.sql` - 比赛相关表
- `code_similarity.sql` - 代码查重表

---

## 监控访问

启动监控服务：
```bash
docker-compose --profile monitoring up -d
```

访问地址：
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)

监控面板包含：
- 系统健康状态
- JVM 内存/GC
- HTTP 请求统计
- 判题队列状态
- 在线用户数
- 活跃比赛数

---

## 备份恢复

### 备份数据库
```bash
# 备份主库
docker exec csoj-mysql-master mysqldump -uroot -p123456 oj_db > backup_$(date +%Y%m%d).sql
```

### 恢复数据库
```bash
# 恢复到主库
docker exec -i csoj-mysql-master mysql -uroot -p123456 oj_db < backup_20260116.sql
```

---

## 更新部署

```bash
# 拉取最新代码
git pull

# 重新构建镜像
docker-compose build

# 重启服务
docker-compose up -d
```

---

## 技术支持

如有问题请查看：
- [学生使用手册](STUDENT_MANUAL.md)
- [教师管理手册](TEACHER_MANUAL.md)
- [高可用部署指南](HIGH_AVAILABILITY.md)
- [监控指南](MONITORING.md)
