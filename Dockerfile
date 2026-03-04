# ============================================================
# Stage 1: Build - dùng Maven để build JAR
# ============================================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# SỬA TẠI ĐÂY: Trỏ đường dẫn vào thư mục SWD392
COPY SWD392/mvnw SWD392/mvnw.cmd ./
COPY SWD392/.mvn .mvn
COPY SWD392/pom.xml ./

# Download dependencies
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# SỬA TẠI ĐÂY: Copy source code từ thư mục SWD392
COPY SWD392/src ./src
RUN ./mvnw package -DskipTests -B

# ============================================================
# Stage 2: Runtime - image gọn nhẹ, chỉ chứa JRE + JAR
# ============================================================
FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy JAR từ build stage (Giữ nguyên vì file JAR đã được build vào /app/target trong container)
COPY --from=builder /app/target/*.jar app.jar

RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]