# ============================================================
# Stage 1: Build - dùng Maven để build JAR
# ============================================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper & POM trước để cache dependency layer
COPY mvnw mvnw.cmd ./
COPY .mvn .mvn
COPY pom.xml ./

# Download dependencies (cache layer riêng, chỉ re-run khi pom.xml thay đổi)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code rồi build
COPY src ./src
RUN ./mvnw package -DskipTests -B

# ============================================================
# Stage 2: Runtime - image gọn nhẹ, chỉ chứa JRE + JAR
# ============================================================
FROM eclipse-temurin:21-jre-alpine AS runtime

# Tạo user non-root để chạy app (best practice bảo mật)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy JAR từ build stage
COPY --from=builder /app/target/*.jar app.jar

# Đổi owner cho user non-root
RUN chown appuser:appgroup app.jar

USER appuser

# Port mặc định của Spring Boot
EXPOSE 8080

# Chạy app với các JVM options tối ưu cho container
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
