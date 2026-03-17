# ============================================================
# Stage 1: Build - dùng Maven để build JAR
# ============================================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper và pom.xml trước để cache dependencies
COPY mvnw mvnw.cmd ./
COPY .mvn .mvn
COPY pom.xml ./

# Download dependencies (cache layer riêng để tái sử dụng khi chỉ thay đổi source)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code và build
COPY src ./src
RUN ./mvnw package -DskipTests -B

# ============================================================
# Stage 2: Runtime - image gọn nhẹ, chỉ chứa JRE + JAR
# ============================================================
FROM eclipse-temurin:21-jre-alpine AS runtime

# Tạo user không phải root để chạy app an toàn hơn
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy JAR từ build stage
COPY --from=builder /app/target/*.jar app.jar

RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]