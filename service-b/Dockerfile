FROM gradle:8.10-jdk17 AS builder
WORKDIR /app
COPY build.gradle.kts ./
COPY settings.gradle ./
COPY gradlew ./
COPY gradlew.bat ./
COPY gradle ./gradle
COPY common ./common
COPY service-b ./service-b

RUN chmod +x gradlew
RUN ./gradlew :service-b:dependencies --no-daemon
RUN ./gradlew :service-b:bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
RUN addgroup -g 1001 spring && adduser -u 1001 -G spring -D spring
WORKDIR /app
COPY --from=builder /app/service-b/build/libs/service-b-1.0.0.jar app.jar
RUN chown -R spring:spring /app
USER spring
EXPOSE 8081
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]