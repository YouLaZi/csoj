# 多阶段构建 - 构建阶段
FROM maven:3.8.6-eclipse-temurin-17 AS builder

WORKDIR /app

# 复制 pom.xml 并下载依赖（利用 Docker 缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码并构建
COPY src ./src
RUN mvn clean package -DskipTests -B

# 运行阶段
FROM eclipse-temurin:17-jre

# 安装必要的工具
RUN apt-get update && apt-get install -y --no-install-recommends curl && \
    ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# 从构建阶段复制 jar 包
COPY --from=builder /app/target/csoj-backend-0.0.1-SNAPSHOT.jar app.jar

# 创建日志和上传目录
RUN mkdir -p /app/logs /app/uploads

# 暴露端口
EXPOSE 8121

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD curl -f http://localhost:8121/api/system/monitor/health || exit 1

# JVM 参数优化
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
