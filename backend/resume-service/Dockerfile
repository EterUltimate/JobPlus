FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# 复制pom.xml文件并下载依赖
COPY pom.xml .
COPY common/pom.xml common/
COPY auth-service/pom.xml auth-service/
COPY user-service/pom.xml user-service/
COPY job-service/pom.xml job-service/
COPY resume-service/pom.xml resume-service/
COPY gateway/pom.xml gateway/

# 复制源代码
COPY common/src common/src
COPY resume-service/src resume-service/src

# 构建应用
RUN mvn clean package -pl resume-service -am -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 复制构建好的jar包
COPY --from=builder /app/resume-service/target/*.jar app.jar

# 暴露端口
EXPOSE 8084

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8084/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]
