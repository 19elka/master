FROM gradle:8.10-jdk17 AS builder
WORKDIR /app
COPY build.gradle.kts ./
COPY settings.gradle ./
COPY gradlew ./
COPY gradlew.bat ./
COPY gradle ./gradle
COPY common ./common
COPY service-book ./service-book

RUN chmod +x gradlew
RUN ./gradlew :service-book:dependencies --no-daemon
RUN ./gradlew :service-book:bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
RUN addgroup -g 1001 spring && adduser -u 1001 -G spring -D spring
WORKDIR /app
COPY --from=builder /app/service-book/build/libs/*.jar app.jar
RUN chown -R spring:spring /app
USER spring
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]